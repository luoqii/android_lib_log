
package org.bbs.android.log;

import android.Manifest.permission;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.bbs.android.log.LogcatProcess.CycleArray;
import org.bbs.android.log.LogcatProcess.OnLogListener;
import org.bbs.android.log.LogcatProcess.FilterSpec;
import org.bbs.android.log.LogcatProcess.LevelSpec;
import org.bbs.android.log.LogcatProcess.Filter;
import org.bbs.android.log.LogcatProcess.MergeFilter;
import org.bbs.android.log.LogcatProcess.Formator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;

/**
 * android logcat application output log viewer.
 *
 * <pre>
 * need permission android.permission.READ_LOGS
 * need permission android.permission.WRITE_EXTERNAL_STORAGE if you do not give a
 * log dir in {@link #EXTRA_LOG_SAVE_DIR}, or the dir you give live on SDCard.
 *
 *
 * @TODO how to see system log???
 *
 * @author bysong@tudou.com
 *
 * @see {@link permission#READ_LOGS}
 */
public class LogcatFragment extends Fragment
{
    private static final String TAG = LogcatFragment.class.getSimpleName();

    /**
     * type: {@link FilterSpec}
     */
    public static final String EXTRA_FILTER_SPEC = LogcatProcess.EXTRA_FILTER_SPEC;
    /**
     * type: {@link String}
     */
    public static final String EXTRA_LOG_SAVE_DIR = LogcatProcess.EXTRA_LOG_SAVE_DIR;
    /**
     * how many logs should output. (by line)
     *
     * type: {@link Integer}
     */
    public static final String EXTRA_LOG_LIMIT = LogcatProcess.EXTRA_LOG_LIMIT;

    static final int DEFAULT_LOG_LIMIT = 1000;

    private Spinner mFilter;
    private FilterSpec[] mFilterSpecs;
    private ArrayAdapter<FilterSpec> mSpecAdapter;

    private View mFilterBar;

    private EditText mEditText;
    protected String mFilterText;

    protected FilterSpec mTemplateSpec;

    protected FilterSpec mRealTimeFilterSpec;
    protected LevelSpec mRealFilterLevel;
    private Spinner mRealTimeFilterLevel;

    private ListView mLogs;
    private FilterLogAdapter mAdapter;

    private static final String REAL_TIME_FILTER = "pref_real_time_filter";
    // use static for speed up 2nd startup.
    private static LogcatProcess sLogcat;
    private Handler mH;
    private String mLogSaveDir;
    private int mLogLimit;
    private Parcelable mFilterS;

    public LogcatFragment(){
        mLogLimit = DEFAULT_LOG_LIMIT;
        mLogSaveDir = Environment.getExternalStorageDirectory().toString();

        init();
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);

        mLogSaveDir = args.getString(EXTRA_LOG_SAVE_DIR);
        if (TextUtils.isEmpty(mLogSaveDir)) {
            mLogSaveDir = Environment.getExternalStorageDirectory().getPath();
            Log.v(TAG, "use default log dir:" + mLogSaveDir);
        }

        mLogLimit = args.getInt(EXTRA_LOG_LIMIT, DEFAULT_LOG_LIMIT);

        mFilterS = args.getParcelable(EXTRA_FILTER_SPEC);

        init();
    }

    private void init() {
        mFilterSpecs = new FilterSpec[] {
                new FilterSpec.ALL(), new FilterSpec.V(),
                new FilterSpec.I(), new FilterSpec.W(),
                new FilterSpec.D(), new FilterSpec.E()
        };
        if (null != mFilterS) {
            FilterSpec spec = (FilterSpec) mFilterS;
            final int COUNT = mFilterSpecs.length + 1;
            FilterSpec[] specs = new FilterSpec[COUNT];
            specs[0] = spec;
            System.arraycopy(mFilterSpecs, 0, specs, 1, mFilterSpecs.length);
            mFilterSpecs = specs;
        }

        if (null == sLogcat) {
            sLogcat = new LogcatProcess(mLogLimit);
        }
        sLogcat.setOnLogListerner(new OnLogListener() {

            @Override
            public void onLog(String log) {
                mH.sendMessage(mH.obtainMessage(0, log));
            }
        });

        mH = new Handler(new Callback() {

            @Override
            public boolean handleMessage(Message msg) {
                mAdapter.add((String) msg.obj);
                return true;
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(
                getResources().getIdentifier("android_comm_lib_logcat_fragment", "layout", getPackageName()), null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTemplateSpec = new FilterSpec.ALL();
        mRealTimeFilterSpec = new FilterSpec.ALL();
        mRealFilterLevel = new LevelSpec.ALL();

        mFilterBar = view.findViewById(
                getResources().getIdentifier("android_comm_lib_logcat_contralbar", "id", getPackageName()));
        mFilterBar.setVisibility(View.GONE);
        mFilter = ((Spinner) view.findViewById(getResources().getIdentifier("filter", "id", getPackageName())));
        mSpecAdapter = new ArrayAdapter<FilterSpec>(getActivity(), android.R.layout.simple_spinner_item,
                mFilterSpecs);
        mSpecAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mFilter.setAdapter(mSpecAdapter);
        mFilter.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mTemplateSpec = mSpecAdapter.getItem(position);
                updateFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mEditText = ((EditText) view.findViewById(getResources().getIdentifier("real_time_filter", "id", getPackageName())));
        mEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                mRealTimeFilterSpec.mTag = null;
                mRealTimeFilterSpec.mMsg = null;
                mFilterText = text;
                if (text.contains(":")){
                    String[] split = text.split(":");
                    if (split.length > 1 && "tag".equalsIgnoreCase(split[0].toLowerCase())) {
                        mRealTimeFilterSpec.mTag = split[1];
                        mRealTimeFilterSpec.mMsg = null;
                        mFilterText = "";
                    }
                }
                updateFilter();
            }
        });
        mRealTimeFilterLevel = ((Spinner) view.findViewById(getResources().getIdentifier("real_time_level", "id", getPackageName())));
        LevelSpec[] levelSpecs = new LevelSpec[]{new LevelSpec.ALL(), new LevelSpec.E(),
                new LevelSpec.D(), new LevelSpec.W(), new LevelSpec.I(), new LevelSpec.V()};
        ArrayAdapter<LevelSpec> adapter = new ArrayAdapter<LevelSpec>(getActivity(), android.R.layout.simple_spinner_item, levelSpecs);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mRealTimeFilterLevel.setAdapter(adapter);
        mRealTimeFilterLevel.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mRealFilterLevel = (LevelSpec) parent.getAdapter().getItem(position);
                updateFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mLogs = ((ListView) view.findViewById(getResources().getIdentifier("logs", "id", getPackageName())));
        mAdapter = new FilterLogAdapter(getActivity(), getResources().getIdentifier("android_comm_lib_log_info_item", "layout", getPackageName()), mLogLimit);
        mAdapter.setFilter(new Filter(new FilterSpec.ALL()));
        mLogs.setAdapter(mAdapter);
        mLogs.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
                view.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
            }
        });
        mLogs.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
    }

    private String getPackageName() {
        return getActivity().getPackageName();
    }

    protected void updateFilter() {
        Filter filter = null;

        if (mFilterText != null && mFilterText.length() > 0) {
            mRealTimeFilterSpec.mMsg = mFilterText;
        }
        mRealTimeFilterSpec.mLevelReg = mRealFilterLevel.mLevelReg;
        filter = new MergeFilter(new Filter(mTemplateSpec), new Filter(mRealTimeFilterSpec));

        mAdapter.setFilter(filter);
        mLogs.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflator) {
        inflator.inflate(getResources().getIdentifier("android_comm_lib_logcat", "menu", getPackageName()), menu);

//        return true;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            menu.findItem(getResources().getIdentifier("android_comm_lib_save", "id", getPackageName())).setEnabled(false);
        }

//        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == getResources().getIdentifier("android_comm_lib_share", "id", getPackageName())) {
            performShare();
            return true;
        } else if (id == getResources().getIdentifier("android_comm_lib_save", "id", getPackageName())) {
            performSaveLog();
            return true;
        } else if (id == getResources().getIdentifier("android_comm_lib_filter", "id", getPackageName())) {
            item.setChecked(!item.isChecked());
            boolean showFilter = item.isChecked();
            showFilter(showFilter);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showFilter(boolean showFilter) {
        mFilterBar.setVisibility(showFilter? View.VISIBLE : View.GONE);
    }

    private void performSaveLog() {
        String name = new Date(System.currentTimeMillis()).toGMTString() + ".log.txt";
        name = name.replace(" ", "_");
        name = name.replace(":", "_");

        File logFile = new File(mLogSaveDir + "/" + name);
        boolean saved = false;
        if (logFile.getParentFile().exists() || logFile.getParentFile().mkdirs())  {
            try {

                if (logFile.createNewFile()) {
                    FileWriter writer = new FileWriter(logFile);
                    writer.write(collectLog());

                    writer.flush();
                    writer.close();
                    saved = true;
                }
            } catch (IOException e) {
                Log.d(TAG, "IOException", e);
            }
        }

        String message = "";
        if (saved) {
            message = "log has saved at " + logFile.getPath();
        } else {
            message = "save log error at " + logFile.getPath();
        }
        Log.d(TAG, message);
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    private String collectLog() {
        StringBuilder builder = new StringBuilder();
        final int count = mAdapter.getCount();
        for (int index = 0 ; index < count; index++ ) {
            builder.append(mAdapter.getItem(index));
            builder.append("\n");
        }

        return builder.toString();
    }

    private void performShare() {

        String target = "libproject";
        try {
            target = getPackageName();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_SUBJECT, "log for " + target);
        share.putExtra(Intent.EXTRA_TEXT, collectLog());

        startActivity(Intent.createChooser(share, "send log by"));
    }


    @Override
    public void onResume() {
        super.onResume();

        saveFilter();

        sLogcat.pushLog(true);
    }

    private void saveFilter() {
        String realtimeFilter = getSharedPreferences("logcat", 0).getString(REAL_TIME_FILTER, "");
        mEditText.setText(realtimeFilter);
    }

    @Override
    public void onPause() {
        super.onPause();

        restoreFilter();

        sLogcat.pushLog(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sLogcat.unsetOnLogListener();
    }

    private void restoreFilter() {
        String realtimeFilter = mEditText.getText().toString();
        getSharedPreferences("logcat", 0)
                .edit()
                .putString(REAL_TIME_FILTER, realtimeFilter)
                .commit();
    }

    private SharedPreferences getSharedPreferences(String name, int mode) {
        return getActivity().getSharedPreferences(name, mode);
    }

    class FilterLogAdapter extends LimitedLogAdapter {
        private Filter mFilter;
        private boolean mFilterable;
        private CycleArray mFilterArray;
        private Formator mFormator;

        public FilterLogAdapter(Context context, int textViewResourceId, int limited) {
            super(context, textViewResourceId, limited);

            mFilterArray = new CycleArray(limited);
            mFormator = new Formator();
        }

        public void setFilter(Filter filter) {
            mFilter = filter;

            mFilterable = mFilter != null;
            if (mFilterable) {
                mFilterArray.clear();

                int count = super.getCount();
                String log = null;
                for (int i = 0; i < count; i++) {
                    log = super.getItem(i);
                    if (mFilter.isLoggable(log)) {
                        mFilterArray.addLog(mFormator.format(log));
                    }
                }
            }

            notifyDataSetChanged();
        }

        public void add(String log) {
            super.add(log);

            if (mFilterable) {
                if (mFilter.isLoggable(log)) {
                    mFilterArray.addLog(mFormator.format(log));
                }
            }

            notifyDataSetChanged();
        }

        @Override
        public String getItem(int position) {
            if (mFilterable) {
                return mFilterArray.getAt(position).replace("\\/", "/");
            } else {
                return super.getItem(position).replace("\\/", "/");
            }
        }

        @Override
        public int getCount() {
            if (!mFilterable) {
                return super.getCount();
            } else {
                return mFilterArray.getCount();
            }
        }

    }

    class LimitedLogAdapter extends BaseAdapter {

        private CycleArray mArray;
        private int mRes;
        private Context mContext;

        private int mColorDefault = getResources().getColor(getResources().getIdentifier("android_comm_lib_white", "color", getPackageName()));
        private int mColorWarring = getResources().getColor(getResources().getIdentifier("android_comm_lib_yellow", "color", getPackageName()));
        private int mColorError = getResources().getColor(getResources().getIdentifier("android_comm_lib_red", "color", getPackageName()));
        private int mColorInfo = getResources().getColor(getResources().getIdentifier("android_comm_lib_green", "color", getPackageName()));

        public LimitedLogAdapter(Context context, int textViewResourceId, int limited) {
            super();

            mContext = context;
            mRes = textViewResourceId;
            mArray = new CycleArray(limited);
        }

        public void add(String log) {
            mArray.addLog(log);

            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mArray.getCount();
        }

        @Override
        public String getItem(int position) {
            return mArray.getAt(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = null;
            if (null != convertView) {
                v = convertView;
            } else {
                v = LayoutInflater.from(mContext).inflate(mRes, null);
            }

//            TextView text = ((TextView) v.findViewById(R.id.log_item));
            TextView text = ((TextView) v.findViewById(getResources().getIdentifier("log_item", "id", getPackageName())));
            String log = getItem(position);

            // TODO re-factor with Formator
            if (log.matches("(?i)[\\d:.]*[D].*")) {
                text.setTextColor(mColorDefault);
            } else if (log.matches("(?i)[\\d:.]*I.*")) {
                text.setTextColor(mColorInfo);
            } else if (log.matches("(?i)[\\d:.]*W.*")) {
                text.setTextColor(mColorWarring);
            } else if (log.matches("(?i)[\\d:.]*E.*")) {
                text.setTextColor(mColorError);
            }

            text.setText(log);

            return v;
        }
    }


}
