package info.akkuma.spoonwithuiautomator;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;

import com.squareup.spoon.SakiwareSpoon;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by akkuma on 2015/09/10.
 */
@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 18)
public class MainActivityUiTest {
    private static final String APP_PACKAGE = "info.akkuma.spoonwithuiautomator";
    private static final int TIMEOUT = 5000;
    private UiDevice mDevice;

    @Before
    public void startApp() {
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        mDevice.pressHome();

        String launcherPackage = getLauncherPackageName();
        mDevice.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), TIMEOUT);

        Context context = InstrumentationRegistry.getContext();
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(APP_PACKAGE);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);

        mDevice.wait(Until.hasObject(By.pkg(APP_PACKAGE).depth(0)), TIMEOUT);
    }

    @Test
    public void testInput() {

        UiObject2 emailForm = mDevice.findObject(
                By.res("info.akkuma.spoonwithuiautomator:id/email"));
        emailForm.setText("hoge@example.jp");

        UiObject2 passwordForm = mDevice.findObject(
                By.res("info.akkuma.spoonwithuiautomator:id/password"));
        passwordForm.setText("dragon");

        SakiwareSpoon.screenshot(mDevice, "Input");
    }

    @Test
    public void testLogin() {
        UiObject2 emailForm = mDevice.findObject(
                By.res("info.akkuma.spoonwithuiautomator:id/email"));
        emailForm.setText("hoge@example.jp");

        UiObject2 passwordForm = mDevice.findObject(
                By.res("info.akkuma.spoonwithuiautomator:id/password"));
        passwordForm.setText("dragon");

        UiObject2 submitButton = mDevice.findObject(
                By.res("info.akkuma.spoonwithuiautomator:id/submit"));
        submitButton.click();

        SakiwareSpoon.screenshot(mDevice, "Login");

        mDevice.wait(Until.hasObject(By.text("ホーム")), TIMEOUT);

        UiObject2 loginSuccessText = mDevice.findObject(By.text("ログイン完了"));

        Assert.assertNotNull(loginSuccessText);

        SakiwareSpoon.screenshot(mDevice, "Home");
    }

    private String getLauncherPackageName() {
        // Create launcher Intent
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);

        // Use PackageManager to get the launcher package name
        PackageManager pm = InstrumentationRegistry.getContext().getPackageManager();
        ResolveInfo resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return resolveInfo.activityInfo.packageName;
    }
}
