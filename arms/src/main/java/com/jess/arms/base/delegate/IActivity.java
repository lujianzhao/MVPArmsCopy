package com.jess.arms.base.delegate;


import android.support.annotation.LayoutRes;

import com.jess.arms.di.component.AppComponent;

/**
 * Created by jess on 26/04/2017 21:42
 * Contact with jess.yan.effort@gmail.com
 */

public interface IActivity {

    @LayoutRes
    int getContentViewId();

    void onBeforeSetContentView();

    /**
     * 提供AppComponent(提供所有的单例对象)给实现类，进行Component依赖
     * @param appComponent
     */
    void setupActivityComponent(AppComponent appComponent);

    boolean useEventBus();

    void initView();

    void initData();

    /**
     * 这个Activity是否会使用Fragment,框架会根据这个属性判断是否注册{@link android.support.v4.app.FragmentManager.FragmentLifecycleCallbacks}
     * 如果返回false,那意味着这个Activity不需要绑定Fragment,那你再在这个Activity中绑定继承于 {@link com.jess.arms.base.BaseFragment} 的Fragment将不起任何作用
     * @return
     */
    boolean useFragment();
}
