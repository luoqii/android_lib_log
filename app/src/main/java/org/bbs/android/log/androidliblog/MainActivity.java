package org.bbs.android.log.androidliblog;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.LoginFilter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.EditText;

import org.bbs.android.log.Log;
import org.bbs.android.log.LogUtil;
import org.bbs.android.log.Logcat_AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = RESULT_FIRST_USER + 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        Log.i(TAG, "onCreate.");

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            initLogger();
            logIt();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // we assume user grant permission
        initLogger();
        logIt();
    }

    private void logIt() {
        Log.setLevel(Log.VERBOSE);
        log();
        Log.setLevel(Log.DEBUG);
        log();
        Log.setLevel(Log.INFO);
        log();
        Log.setLevel(Log.WARN);
        log();
        Log.setLevel(Log.ERROR);
        log();
        Log.setLevel(Log.ASSERT);
        log();


        Log.enableLog(false);
        log();
        Log.enableLog(true);
        Log.setLevel(Log.VERBOSE);
        log();
    }

    private void initLogger() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "isExternalStorageWritable:" + isExternalStorageWritable());
            Log.d(TAG, "isExternalStorageReadable:" + isExternalStorageReadable());

            LogUtil.init();
        } else {
            // for release show waring & errors
            Log.setLevel(Log.WARN);
        }
    }

    void log(){
        Log.v(TAG, "Log.v");
        Log.d(TAG, "Log.d");
        Log.i(TAG, "Log.i");
        Log.w(TAG, "Log.w");
        Log.e(TAG, "Log.e");
    }


    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Log.d(TAG, "onOptionsItemSelected. id:" + id);

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_logcat){
//            Logcat_FragmentActivity.start(this);
            Logcat_AppCompatActivity.start(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements View.OnClickListener {

        private EditText mMethod;
        private EditText mMessage;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            Log.i(TAG, "onCreateView. view:" + rootView);
            return rootView;
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            view.findViewById(R.id.log).setOnClickListener(this);
            mMessage = (EditText)view.findViewById(R.id.message);
            mMethod = (EditText)view.findViewById(R.id.method);
        }

        @Override
        public void onClick(View v) {
            String method = mMethod.getText().toString().toLowerCase();
            log(method, mMessage.getText().toString());
        }

        private void log(String method, String message) {
            if ("d".equals(method)){
                Log.d(TAG, message);
            } else
            if ("e".equals(method)){
                Log.e(TAG, message);
            } else
            if ("w".equals(method)){
                Log.w(TAG, message);
            } else
            if ("i".equals(method)){
                Log.i(TAG, message);
            } else
            if ("v".equals(method)){
                Log.v(TAG, message);
            } else {
                Log.w(TAG, "no method. available: [dewiv]");
            }
        }
    }
}
