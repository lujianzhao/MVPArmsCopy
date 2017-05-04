package com.jess.arms.base;

import android.support.multidex.MultiDexApplication;

import com.jess.arms.base.delegate.ApplicationDelegate;
import com.jess.arms.di.component.AppComponent;


/**
 * 本项目由
 * mvp
 * +dagger2
 * +retrofit
 * +rxjava
 * +androideventbus
 * +butterknife组成
 */
public abstract class BaseApplication extends MultiDexApplication implements App{

    private ApplicationDelegate mApplicationDelegate;

    @Override
    public void onCreate() {
        super.onCreate();
        this.mApplicationDelegate = new ApplicationDelegate(this);
        this.mApplicationDelegate.onCreate();
    }

    /**
     * 程序终止的时候执行
     */
    @Override
    public void onTerminate() {
        this.mApplicationDelegate.onTerminate();
        super.onTerminate();
    }


    /**
     * 将AppComponent返回出去,供其它地方使用, AppComponent接口中声明的方法返回的实例,在getAppComponent()拿到对象后都可以直接使用
     *
     * @return
     */
    @Override
    public AppComponent getAppComponent() {
        return mApplicationDelegate.getAppComponent();
    }

}
