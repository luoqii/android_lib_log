package org.bbs.android.log;

import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by qiiluo on 2/8/17.
 */

public class LogUtil {
    /**
     * @see {@link FileHandler}
     *
     * @param pattern
     * @param limit
     * @param count
     */
    public static void initWithFileHander(String pattern, int limit, int count){
        Logger l = Logger.getAnonymousLogger();
        FileHandler h = null;
        try {
            h = new FileHandler(pattern, limit, count);
        } catch (IOException e) {
            e.printStackTrace();
        }
        h.setFormatter(new Log.SimpleFormatter());
        l.addHandler(h);
        l.setLevel(Level.ALL);
        Log.setLogger(l);
    }

    public static void init(){
        File sdcardLogDir = new File("/sdcard/log");
        sdcardLogDir.mkdirs();

        File logDir = new File(sdcardLogDir, "log%g");
        initWithFileHander(logDir.getPath() + ".txt",
                1 * 1024 * 1024,
                5);

    }
}
