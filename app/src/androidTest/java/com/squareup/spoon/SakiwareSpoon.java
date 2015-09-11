package com.squareup.spoon;

import android.content.Context;
import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.UiDevice;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import static android.content.Context.MODE_WORLD_READABLE;
import static android.os.Environment.getExternalStorageDirectory;
import static com.squareup.spoon.Chmod.chmodPlusRWX;

/**
 * Created by akkuma on 2015/09/10.
 */
public class SakiwareSpoon {

    static final String SPOON_SCREENSHOTS = "spoon-screenshots";
    static final String NAME_SEPARATOR = "_";
    static final String TEST_CASE_CLASS_JUNIT_3 = "android.test.InstrumentationTestCase";
    static final String TEST_CASE_METHOD_JUNIT_3 = "runMethod";
    static final String TEST_CASE_CLASS_JUNIT_4 = "org.junit.runners.model.FrameworkMethod$1";
    static final String TEST_CASE_METHOD_JUNIT_4 = "runReflectiveCall";
    private static final String EXTENSION = ".png";
    private static final String TAG = "Spoon";
    private static final Object LOCK = new Object();
    private static final Pattern TAG_VALIDATION = Pattern.compile("[a-zA-Z0-9_-]+");

    /** Whether or not the screenshot output directory needs cleared. */
    private static boolean outputNeedsClear = true;

    public static File screenshot(UiDevice uiDevice, String tag) {
        StackTraceElement testClass = findTestClassTraceElement(Thread.currentThread().getStackTrace());
        String className = testClass.getClassName().replaceAll("[^A-Za-z0-9._-]", "_");
        String methodName = testClass.getMethodName();
        return screenshot(uiDevice, tag, className, methodName);
    }

    public static File screenshot(UiDevice uiDevice, String tag, String testClassName,
                                  String testMethodName) {
        if (!TAG_VALIDATION.matcher(tag).matches()) {
            throw new IllegalArgumentException("Tag must match " + TAG_VALIDATION.pattern() + ".");
        }
        try {
            File screenshotDirectory =
                    obtainScreenshotDirectory(InstrumentationRegistry.getTargetContext(), testClassName,
                            testMethodName);
            String screenshotName = System.currentTimeMillis() + NAME_SEPARATOR + tag + EXTENSION;
            File screenshotFile = new File(screenshotDirectory, screenshotName);
            takeScreenshot(screenshotFile, uiDevice);
            Log.d(TAG, "Captured screenshot '" + tag + "'.");
            return screenshotFile;
        } catch (Exception e) {
            throw new RuntimeException("Unable to capture screenshot.", e);
        }
    }


    private static void takeScreenshot(File file, UiDevice uiDevice) throws IOException {
        uiDevice.takeScreenshot(file);
    }

    private static File obtainScreenshotDirectory(Context context, String testClassName,
                                                  String testMethodName) throws IllegalAccessException {
        File screenshotsDir;
        if (Build.VERSION.SDK_INT >= 21) {
            // Use external storage.
            screenshotsDir = new File(getExternalStorageDirectory(), "app_" + SPOON_SCREENSHOTS);
        } else {
            // Use internal storage.
            screenshotsDir = context.getDir(SPOON_SCREENSHOTS, MODE_WORLD_READABLE);
        }

        synchronized (LOCK) {
            if (outputNeedsClear) {
                deletePath(screenshotsDir, false);
                outputNeedsClear = false;
            }
        }

        File dirClass = new File(screenshotsDir, testClassName);
        File dirMethod = new File(dirClass, testMethodName);
        createDir(dirMethod);
        return dirMethod;
    }

    static StackTraceElement findTestClassTraceElement(StackTraceElement[] trace) {
        for (int i = trace.length - 1; i >= 0; i--) {
            StackTraceElement element = trace[i];

            if (TEST_CASE_CLASS_JUNIT_3.equals(element.getClassName()) //
                    && TEST_CASE_METHOD_JUNIT_3.equals(element.getMethodName())) {
                return trace[i - 3];
            }

            if (TEST_CASE_CLASS_JUNIT_4.equals(element.getClassName()) //
                    && TEST_CASE_METHOD_JUNIT_4.equals(element.getMethodName())) {
                return trace[i - 3];
            }
        }

        throw new IllegalArgumentException("Could not find test class!");
    }


    private static void createDir(File dir) throws IllegalAccessException {
        File parent = dir.getParentFile();
        if (!parent.exists()) {
            createDir(parent);
        }
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalAccessException("Unable to create output dir: " + dir.getAbsolutePath());
        }
        chmodPlusRWX(dir);
    }

    private static void deletePath(File path, boolean inclusive) {
        if (path.isDirectory()) {
            File[] children = path.listFiles();
            if (children != null) {
                for (File child : children) {
                    deletePath(child, true);
                }
            }
        }
        if (inclusive) {
            path.delete();
        }
    }

    private SakiwareSpoon() {
        // No instances.
    }
}
