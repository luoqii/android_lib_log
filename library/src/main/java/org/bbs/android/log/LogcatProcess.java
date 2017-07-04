package org.bbs.android.log;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Created by bysong on 16-5-10.
 */
public class LogcatProcess {
    private static final String TAG = LogcatProcess.class.getSimpleName();

    /**
     * type: {@link FilterSpec}
     */
    public static final String EXTRA_FILTER_SPEC = "Logcat.EXTRA_FILTER_SPEC";
    /**
     * type: {@link String}
     */
    public static final String EXTRA_LOG_SAVE_DIR ="Logcat.EXTRA_LOG_SAVE_DIR";
    /**
     * how many logs should output. (by line)
     *
     * type: {@link Integer}
     */
    public static final String EXTRA_LOG_LIMIT ="Logcat.EXTRA_LOG_LIMIT";

    private static final String[] CMD = {
            // DATE TIME PID TID LEVEL TAG MESSAGE
            "/system/bin/logcat", "-v", "threadtime"
    };

    //                    DATE      TIME       PID      TID      LEVEL     TAG      MESSAGE
    private static final String REG = "(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(.*$)";
    private static final Pattern PATTERN = Pattern.compile(REG);

    private OnLogListener mListener;
    private boolean mPush;
    private CacheLog mCachelog;
    private Thread mWorkerThread;
    private boolean mShouldQuit;

    LogcatProcess(int capacity) {
        mCachelog = new CacheLog(capacity);

        mWorkerThread = new Thread("logcat thread") {
            public void run() {
                final String[] command = CMD;
                Process process = null;
                try {
                    process = new ProcessBuilder(command).start();
                    BufferedReader r = new BufferedReader(new InputStreamReader(
                            process.getInputStream()));
                    String line = null;
                    while (!mShouldQuit && (line = r.readLine()) != null) {
                        mCachelog.addLog(line);
                    }
                } catch (Exception e) {
                    // ignore this, it's safe.
                    Log.e(TAG, "KO. command: " + command, e);
                } finally {
                    if (null != process) {
                        process.destroy();
                        Log.d(TAG, "destroy logcat process");
                    }
                }
            }

            ;
        };

        mWorkerThread.start();
    }

    void close() {
        mShouldQuit = true;
        if (null != mWorkerThread) {
            mWorkerThread.interrupt();
            mWorkerThread = null;
        }
    }

    void pushLog(boolean push) {
        mPush = push;

        mCachelog.pushLog();
    }

    void doLog(String log) {
        if (null != mListener) {
            mListener.onLog(log);
        }
    }

    void setOnLogListerner(OnLogListener listener) {
        mListener = listener;
    }

    void unsetOnLogListener() {
        mListener = null;
    }

    class CacheLog {
        private int mcapacity;
        private int mCursor;
        private String[] mLogs;

        CacheLog(int capacity) {
            mcapacity = capacity;
            mCursor = 0;
            mLogs = new String[capacity];
        }

        void addLog(String log) {
            if (mPush) {
                doLog(log);
            }
            mLogs[mCursor] = log;
            mCursor = (mCursor + 1) % mcapacity;
        }

        void pushLog() {
            for (int i = mCursor; i < mcapacity; i++) {
                if (mLogs[i] != null) {
                    doLog(mLogs[i]);
                }
            }

            for (int i = 0; i < mCursor; i++) {
                if (mLogs[i] != null) {
                    doLog(mLogs[i]);
                }
            }
        }
    }

    public interface OnLogListener {
        void onLog(String log);
    }

    public static class CycleArray {
        private String[] mArray;
        private int mLimit;
        private int mCursor;
        private int mCount;

        CycleArray(int limit) {
            mCursor = 0;
            mLimit = limit;
            mArray = new String[limit];
        }

        void clear() {
            mCursor = 0;
            mCount = 0;
            mArray = new String[mLimit];
        }

        void addLog(String log) {
            mArray[mCursor] = log;
            mCursor = (mCursor + 1) % mLimit;
        }

        String getAt(int index) {
            String log = null;

            int count = 0;
            boolean find = false;
            for (int i = mCursor; i < mLimit; i++) {
                if (mArray[i] != null) {
                    if (index == count) {
                        log = mArray[i];
                        find = true;
                        break;
                    }
                    count++;
                }
            }
            if (!find) {
                for (int i = 0; i < mCursor; i++) {
                    if (mArray[i] != null) {
                        if (index == count) {
                            log = mArray[i];
                            find = true;
                            break;
                        }
                        count++;
                    }
                }
            }

            return log;
        }

        int getCursor(){
            return mCursor;
        }

        int getCount() {
            if (mCount == mLimit) {
                return mCount;
            }

            int count = 0;

            for (int i = mCursor; i < mLimit; i++) {
                if (mArray[i] != null) {
                    count++;
                }
            }
            for (int i = 0; i < mCursor; i++) {
                if (mArray[i] != null) {
                    count++;
                }
            }

            if (count == mLimit) {
                mCount = count;
            }

            return count;
        }
    }

    public static class FilterSpec implements Parcelable {
        public String mId;
        public String mLabel;
        public String mDesc;

        public String mTag;
        public String mMsg;
        public int mPid = -1;
        public String mPackage;
        public String mLevelReg;

        public static final Parcelable.Creator<FilterSpec> CREATOR = new Parcelable.Creator<FilterSpec>() {
            public FilterSpec createFromParcel(Parcel in) {
                return new FilterSpec(in);
            }

            public FilterSpec[] newArray(int size) {
                return new FilterSpec[size];
            }
        };

        public FilterSpec() {

        }

        public FilterSpec(Parcel in) {
            mId = in.readString();
            mLabel = in.readString();
            mDesc = in.readString();

            mTag = in.readString();
            mMsg = in.readString();
            mPid = in.readInt();
            mPackage = in.readString();
            mLevelReg = in.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(mId);
            dest.writeString(mLabel);
            dest.writeString(mDesc);

            dest.writeString(mTag);
            dest.writeString(mMsg);
            dest.writeInt(mPid);
            dest.writeString(mPackage);
            dest.writeString(mLevelReg);
        }

        @Override
        public String toString() {
            return mLabel;
        }

        static class ALL extends FilterSpec {
            public ALL() {
                mId = "com.tudou.android.Logcat.FILTER_ALL";
                mLabel = "All Message";
            }
        }

        static class E extends FilterSpec {
            public E() {
                mId = "com.tudou.android.Logcat.FILTER_E";
                mLabel = "Error Message";

                mLevelReg = "[E]";
            }
        }

        static class D extends FilterSpec {
            public D() {
                mId = "com.tudou.android.Logcat.FILTER_D";
                mLabel = "Debug Message";

                mLevelReg = "[ED]";
            }
        }

        static class W extends FilterSpec {
            public W() {
                mId = "com.tudou.android.Logcat.FILTER_W";
                mLabel = "Warning Message";

                mLevelReg = "[EDW]";
            }
        }

        static class I extends FilterSpec {
            public I() {
                mLabel = "Infor Message";

                mLevelReg = "[EDWI]";
            }
        }

        static class V extends FilterSpec {
            public V() {
                mId = "com.tudou.android.Logcat.FILTER_V";
                mLabel = "Verbose Message";

                mLevelReg = "[EDWIV]";
            }
        }
    }


    public static class Filter {
        private FilterSpec mSpec;

        Matcher mMatcher;
        String mDate;
        String mTime;
        String mPid;
        String mTid;
        String mLevel;
        String mTag;
        String mMsg;

        public Filter() {
        }

        Filter(FilterSpec spec) {
            mSpec = spec;
        }

        public boolean isLoggable(String log) {
            boolean filter = true;
            try {
                mMatcher = PATTERN.matcher(log);
                if (mMatcher.matches()) {
                    mDate = mMatcher.group(1);
                    mTime = mMatcher.group(2);
                    mPid = mMatcher.group(3);
                    mTid = mMatcher.group(4);
                    mLevel = mMatcher.group(5);
                    mTag = mMatcher.group(6);
                    mMsg = mMatcher.group(7);

                    if (mSpec.mPid > 0) {
                        filter &= (mPid.contains(mSpec.mPid + ""));
                    }
                    // null means match.
                    if (null != (mSpec.mLevelReg)) {
                        filter &= mLevel.matches(mSpec.mLevelReg);
                    }
                    if (null != (mSpec.mTag)) {
                        filter &= (mTag.toLowerCase().contains(mSpec.mTag.toLowerCase()));
                    }
                    if (null != (mSpec.mMsg)) {
                        filter &= (mMsg.contains(mSpec.mMsg) || mMsg.matches("(?i).*" + mSpec.mMsg + ".*"));
                    }
                }
            } catch (PatternSyntaxException e) {
                Log.e(TAG, "PatternSyntaxException", e);
                filter = false;
            }

            return filter;
        }
    }

    public static class MergeFilter extends Filter {
        Filter mBaseFilter;
        private Filter mMergeFilter;

        MergeFilter(Filter baseFilter, Filter mergerFilter) {
            super();
            mBaseFilter = baseFilter;
            mMergeFilter = mergerFilter;
        }

        @Override
        public boolean isLoggable(String log) {
            return mBaseFilter.isLoggable(log) && mMergeFilter.isLoggable(log);
        }
    }

    public static class LevelSpec {
        public String mId;
        public String mLabel;
        public String mLevelReg;

        @Override
        public String toString() {
            return mLabel;
        }

        public static class ALL extends LevelSpec {

            public ALL() {
                mId = "com.tudou.android.logcat.LEVLE_ALL";
                mLabel = "ALL";
                mLevelReg = "[EDWIV]";
            }
        }

        public static class E extends LevelSpec {

            public E() {
                mId = "com.tudou.android.logcat.LEVLE_E";
                mLabel = "ERR";
                mLevelReg = "[E]";
            }
        }

        public static class D extends LevelSpec {

            public D() {
                mId = "com.tudou.android.logcat.LEVLE_D";
                mLabel = "DEBUG";
                mLevelReg = "[ED]";
            }
        }

        public static class W extends LevelSpec {

            public W() {
                mId = "com.tudou.android.logcat.LEVLE_W";
                mLabel = "WARNING";
                mLevelReg = "[EDW]";
            }
        }

        public static class I extends LevelSpec {

            public I() {
                mId = "com.tudou.android.logcat.LEVLE_D";
                mLabel = "INFO";
                mLevelReg = "[EDWI]";
            }
        }

        public static class V extends LevelSpec {

            public V() {
                mId = "com.tudou.android.logcat.LEVLE_V";
                mLabel = "VERBOSE";
                mLevelReg = "[EDWIV]";
            }
        }
    }

    public static class Formator {
        Matcher mMatcher;

        public String format(String log) {
            String l = null;
            mMatcher = PATTERN.matcher(log);
            if (mMatcher.matches()) {
                l = mMatcher.group(2) + ":" + mMatcher.group(5) + "/" + mMatcher.group(6) + mMatcher.group(7);
            }

            return l;
        }
    }
}
