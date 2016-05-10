package org.bbs.android.log;

import org.bbs.android.log.LogcatProcess.CycleArray;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
@RunWith(JUnit4)
public class LogcatProcessTest implements LogcatProcess.OnLogListener {

    private LogcatProcess mProcess;

    @Before
    public void setup(){
        mProcess = new LogcatProcess(100);
    }

    @Test
    public void test_cycleArray(){
        CycleArray a = new CycleArray(50);
        a.addLog("a");
        assertTrue(1 == a.getCount());
        a.addLog("a");
        assertTrue(2 == a.getCount());

        for (int i = 0 ; i < 500 ; i ++){
            a.addLog("index:" + i);
        }
        assertTrue(50 == a.getCount());
        assertEquals("index:499", a.getAt(a.getCursor()));
    }
}