
package org.bbs.android.log;

import android.Manifest.permission;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * android logcat application output log viewer.
 * 
 * <pre>
 * need permission android.permission.READ_LOGS
 *
 * 
 * @TODO how to see system log???
 * 
 * @author bangbang.song@gmail.com
 * 
 * @see {@link permission#READ_LOGS}
 */
public class Logcat_AppCompatActivity extends AppCompatActivity
{
    private static final String TAG = Logcat_AppCompatActivity.class.getSimpleName();

    public static void start(Context context){
    	Intent logcat = new Intent(context, Logcat_AppCompatActivity.class);
    	context.startActivity(logcat);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(getResources().getIdentifier("android_comm_lib_logcat", "layout", getPackageName()));

        Fragment logcat = new LogcatFragment();
        setArgs(logcat, getIntent());
        
        getSupportFragmentManager().beginTransaction()
                .add(getResources().getIdentifier("logcat_container", "id", getPackageName()), logcat)
                .commit();

    }

	private void setArgs(Fragment fragment, Intent intent) {
        if (null == intent) {
            return;
        }

        String mLogSaveDir = intent.getStringExtra(LogcatFragment.EXTRA_LOG_SAVE_DIR);

        int mLogLimit = intent.getIntExtra(LogcatFragment.EXTRA_LOG_LIMIT, LogcatFragment.DEFAULT_LOG_LIMIT);
        
        Parcelable p = intent.getParcelableExtra(LogcatFragment.EXTRA_FILTER_SPEC);
        if (null == p) {
            Log.w(TAG, "no filter in intent, ignore.");
        }
        
        LogcatFragment.FilterSpec spec = (LogcatFragment.FilterSpec) p;
        
        Bundle args = new Bundle();
        args.putString(LogcatFragment.EXTRA_LOG_SAVE_DIR, mLogSaveDir);
        args.putInt(LogcatFragment.EXTRA_LOG_LIMIT, mLogLimit);
        if (null != p ) {
        	args.putParcelable(LogcatFragment.EXTRA_FILTER_SPEC, spec);
        }
        
        fragment.setArguments(args);
    }
}
