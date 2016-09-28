package org.bbs.android.log.androidliblog;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.EditText;

import org.bbs.android.log.Log;
import org.bbs.android.log.Logcat_AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        initLogger();

        Log.i(TAG, "onCreate.");

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
        try {
            Logger l = Logger.getAnonymousLogger();
            Log.d(TAG, "isExternalStorageWritable:" + isExternalStorageWritable());
            Log.d(TAG, "isExternalStorageReadable:" + isExternalStorageReadable());

            File sdcardLogDir = new File(Environment.getExternalStorageDirectory(),
                    getApplication().getPackageName() + "/log");
            sdcardLogDir = new File("/sdcard/log");
            boolean mk = sdcardLogDir.mkdirs();
//            Log.d(TAG, "mk:" + mk);

            File logDir = new File(getExternalFilesDir(null), "log%g");
            logDir = new File(sdcardLogDir, "log%g");
            Log.d(TAG, "logDir:" + logDir.getParent());
            FileHandler h = new FileHandler(logDir.getPath() + ".txt",
                    1 * 1024 * 1024,
                    5);            h.setFormatter(new Log.SimpleFormatter());
            l.addHandler(h);
            l.setLevel(Level.ALL);
            Log.setLogger(l);
        } catch (IOException e) {
            e.printStackTrace();
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
