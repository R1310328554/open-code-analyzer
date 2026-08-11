// Android 仪器化测试：在真机/模拟器上校验应用包名
package com.baidu.paddle.lite.demo.ocr;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
// 示例仪器化测试类
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    // 获取被测应用 Context 并断言包名
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.baidu.paddle.lite.demo", appContext.getPackageName());
    }
}
