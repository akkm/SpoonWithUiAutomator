package info.akkuma.spoonwithuiautomator;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by akkuma on 2015/09/10.
 */
@RunWith(AndroidJUnit4.class)
public class LineBot {



    private static final String APP_PACKAGE = "jp.naver.line.android";
    private static final int TIMEOUT = 5000;
    private static final int SUPER_TIMEOUT = 50000;
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


    private String getLauncherPackageName() {
        // Create launcher Intent
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);

        // Use PackageManager to get the launcher package name
        PackageManager pm = InstrumentationRegistry.getContext().getPackageManager();
        ResolveInfo resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return resolveInfo.activityInfo.packageName;
    }

    @Test
    public void execute() throws InterruptedException {

        mDevice.wait(Until.hasObject(By.desc("トークタブ")),SUPER_TIMEOUT);
        mDevice.findObject(By.desc("トークタブ")).click();

        mDevice.wait(Until.hasObject(By.text("トーク")), TIMEOUT);
        mDevice.wait(Until.hasObject(By.text("ほげほげ")), TIMEOUT);
        mDevice.findObject(By.text("ほげほげ")).click();
        mDevice.wait(Until.hasObject(By.res("jp.naver.line.android:id/chatlog")), TIMEOUT);

        while(true) {
            UiObject2 chatlog = mDevice.findObject(By.res("jp.naver.line.android:id/chatlog"));
            int talkLength = chatlog.getChildCount();

            UiObject2 lastRowText = chatlog.getChildren().get(talkLength-1).findObject(By.res("jp.naver.line.android:id/chathistory_row_recv_msg"));

            if (lastRowText == null) {
                lastRowText = chatlog.getChildren().get(talkLength-1).findObject(By.res("jp.naver.line.android:id/chathistory_row_send_msg"));
            }

            if (lastRowText == null) {
                Thread.sleep(1000);
                continue;
            }

            String lastTalk = lastRowText.getText();

            Thread.sleep(1000);

            chatlog = mDevice.findObject(By.res("jp.naver.line.android:id/chatlog"));

            talkLength = chatlog.getChildCount();

            lastRowText = chatlog.getChildren().get(talkLength-1).findObject(By.res("jp.naver.line.android:id/chathistory_row_recv_msg"));

            if (lastRowText == null) {
                lastRowText = chatlog.getChildren().get(talkLength-1).findObject(By.res("jp.naver.line.android:id/chathistory_row_send_msg"));
            }

            String nextTalk = lastRowText.getText();

            if (nextTalk.equals("おわり")) break;

            if (lastTalk.equals(nextTalk)) continue;

            mDevice.findObject(By.res("jp.naver.line.android:id/chathistory_message_edit")).setText(getReplyMessage());
            mDevice.findObject(By.res("jp.naver.line.android:id/chathistory_send_button")).click();

            Thread.sleep(1000);
        }

    }

    private int strCount = -1;

    private String getReplyMessage() {

        strCount++;
        if (strCount == 0) {
            return "こんにちは〜";
        }

        if (strCount == 1) {
            return "本日は聞いてくれてありがとうございました！";
        }

        if (strCount == 2) {
            return "さようなら〜";
        }

        return "unknown";
    }

}
