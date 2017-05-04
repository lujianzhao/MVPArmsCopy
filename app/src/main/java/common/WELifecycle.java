package common;

import android.app.Application;
import android.os.StrictMode;

import com.apkfuns.logutils.LogLevel;
import com.apkfuns.logutils.LogUtils;
import com.jess.arms.base.delegate.ApplicationDelegate;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.zxy.recovery.callback.RecoveryCallback;
import com.zxy.recovery.core.Recovery;

import me.jessyan.mvparms.demo.BuildConfig;
import me.jessyan.mvparms.demo.mvp.ui.activity.UserActivity;

/**
 * @author: lujianzhao
 * @date: 25/04/2017 11:13
 * @Description:  ApplicationDelegate.Lifecycle 的所有方法都会在BaseApplication对应的生命周期中被调用,所以在对应的方法中可以扩展一些自己需要的逻辑
 */
class WELifecycle implements ApplicationDelegate.Lifecycle {
    private RefWatcher mRefWatcher;//leakCanary观察器

    @Override
    public void onCreate(Application application) {
        LogUtils.getLogConfig().configAllowLog(BuildConfig.LOG_DEBUG).configTagPrefix("AndroidBase").configShowBorders(true).configFormatTag("%d{HH:mm:ss:SSS} %t %c{-5}").configLevel(LogLevel.TYPE_VERBOSE);

        //leakCanary内存泄露检查
        installLeakCanary(application);

        initReference(application);
    }

    private void installLeakCanary(Application application) {
        if (LeakCanary.isInAnalyzerProcess(application)) {
            return;
        }
        if (BuildConfig.USE_CANARY) {
            enabledStrictMode();
            this.mRefWatcher = LeakCanary.install(application);
        } else {
            this.mRefWatcher = RefWatcher.DISABLED;
        }
    }

    private void enabledStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder() //
                .detectAll() //
                .penaltyLog() //
                .penaltyDeath() //
                .build());
    }

    private void initReference(Application application) {
        Recovery.getInstance()
                .debug(BuildConfig.LOG_DEBUG)
                .recoverInBackground(false)
                .recoverStack(true)
                .mainPage(UserActivity.class)
                .recoverEnabled(true)
                .callback(new RecoveryCallback() {
                    @Override
                    public void stackTrace(String s) {

                    }

                    @Override
                    public void cause(String s) {

                    }

                    @Override
                    public void exception(String s, String s1, String s2, int i) {

                    }

                    @Override
                    public void throwable(Throwable throwable) {
                        LogUtils.e(throwable);
                    }
                })
                .silent(!BuildConfig.LOG_DEBUG, Recovery.SilentMode.RECOVER_ACTIVITY_STACK)
                //忽略某些Activity的错误
//                .skip(TestActivity.class)
                .init(application);
    }

    @Override
    public void onTerminate(Application application) {
        this.mRefWatcher = null;
    }
}
