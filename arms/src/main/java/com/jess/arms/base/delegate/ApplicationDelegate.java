package com.jess.arms.base.delegate;

import android.app.Application;

import com.jess.arms.common.utils.HandlerUtil;
import com.jess.arms.di.component.AppComponent;
import com.jess.arms.di.component.DaggerAppComponent;
import com.jess.arms.di.module.AppModule;
import com.jess.arms.di.module.ClientModule;
import com.jess.arms.di.module.GlobalConfigModule;
import com.jess.arms.di.module.ImageModule;
import com.jess.arms.integration.ActivityLifecycle;
import com.jess.arms.integration.AppManager;
import com.jess.arms.integration.ConfigModule;
import com.jess.arms.integration.IRepositoryManager;
import com.jess.arms.integration.ManifestParser;
import com.jess.arms.netstate.NetworkStateReceiver;
import com.zhy.autolayout.config.AutoLayoutConifg;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * @author: lujianzhao
 * @date: 25/04/2017 10:14
 * @Description:
 */
public class ApplicationDelegate {
    private Application mApplication;
    private AppComponent mAppComponent;
    private AppModule mAppModule;
    private GlobalConfigModule mGlobalConfigModule;

    @Inject
    protected ActivityLifecycle mActivityLifecycle;

    private final List<ConfigModule> mModules;

    private List<Lifecycle> mLifecycles = new ArrayList<>();

    public ApplicationDelegate(Application application) {
        this.mApplication = application;

        AutoLayoutConifg.getInstance().useDeviceSize().init(application);
        this.mModules = new ManifestParser(mApplication).parse();
        for (ConfigModule module : mModules) {
            module.injectAppLifecycle(mApplication, mLifecycles);
        }
    }


    public void onCreate() {
        mAppModule = new AppModule(mApplication);
        mGlobalConfigModule = getGlobeConfigModule(mApplication,mModules);

        mAppComponent = DaggerAppComponent
                .builder()
                .appModule(mAppModule)////提供application
                .clientModule(new ClientModule())//用于提供okhttp和retrofit的单例
                .imageModule(new ImageModule())//图片加载框架默认使用glide
                .globalConfigModule(mGlobalConfigModule)//全局配置
                .build();
        mAppComponent.inject(this);

        mApplication.registerActivityLifecycleCallbacks(mActivityLifecycle);

        //注册网络状态广播
        NetworkStateReceiver.registerNetworkStateReceiver(mApplication);

        for (ConfigModule module : mModules) {
            module.registerComponents(mApplication, mAppComponent.getRepositoryManager());
        }


        for (Lifecycle lifecycle : mLifecycles) {
            lifecycle.onCreate(mApplication);
        }
    }


    public void onTerminate() {
        if (mLifecycles != null) {
            for (Lifecycle lifecycle : mLifecycles) {
                lifecycle.onTerminate(mApplication);
            }
            mLifecycles.clear();
            mLifecycles = null;
        }

        HandlerUtil.removeCallbacksAndMessages();

        //注销网络状态广播
        NetworkStateReceiver.unRegisterNetworkStateReceiver(mApplication);

        if (mGlobalConfigModule != null) {
            mGlobalConfigModule.release();
            mGlobalConfigModule = null;
        }


        if (mAppModule != null) {
            mAppModule.release();
            mAppModule = null;
        }

        if (mActivityLifecycle != null) {
            mApplication.unregisterActivityLifecycleCallbacks(mActivityLifecycle);
            mActivityLifecycle.release();
            mActivityLifecycle = null;
        }


        if (mAppComponent != null) {
            AppManager appManager = mAppComponent.getAppManager();
            if (appManager != null) {
                appManager.release();
            }
            IRepositoryManager repositoryManager = mAppComponent.getRepositoryManager();
            if (repositoryManager != null) {
                repositoryManager.release();
            }
            mAppComponent = null;
        }

        mApplication = null;
    }


    /**
     * 将app的全局配置信息封装进module(使用Dagger注入到需要配置信息的地方)
     * 需要在AndroidManifest中声明{@link ConfigModule}的实现类,和Glide的配置方式相似
     *
     * @return
     */
    private GlobalConfigModule getGlobeConfigModule(Application context, List<ConfigModule> modules) {

        GlobalConfigModule.Builder builder = GlobalConfigModule
                .builder()
                .baseurl("https://api.github.com");//为了防止用户没有通过GlobeConfigModule配置baseurl,而导致报错,所以提前配置个默认baseurl

        for (ConfigModule module : modules) {
            module.applyOptions(context, builder);
        }

        return builder.build();
    }


    /**
     * 将AppComponent返回出去,供其它地方使用, AppComponent接口中声明的方法返回的实例,在getAppComponent()拿到对象后都可以直接使用
     *
     * @return
     */
    public AppComponent getAppComponent() {
        return mAppComponent;
    }


    public interface Lifecycle {
        void onCreate(Application application);

        void onTerminate(Application application);
    }
}
