dpackage org.bbs.android.log.androidliblog;

import android.app.Activity;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;

import org.bbs.android.log.Logcat_FragmentActivity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by bysong on 16-5-6.
 */
@RunWith(AndroidJUnit4.class)
public class Logcat_FragmentActivity_Test extends ActivityInstrumentationTestCase2<Logcat_FragmentActivity> {
    private Logcat_FragmentActivity mActivity;

    public Logcat_FragmentActivity_Test() {
        super(Logcat_FragmentActivity.class);
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        // Injecting the Instrumentation instance is required
        // for your test to run with AndroidJUnitRunner.
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        mActivity = getActivity();
    }

    @Test
    public void testStart(){
        assertNotNull(mActivity);
    }
}
