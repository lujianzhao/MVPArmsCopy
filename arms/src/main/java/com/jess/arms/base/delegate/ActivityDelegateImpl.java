package com.jess.arms.base.delegate;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.view.Window;

import org.simple.eventbus.EventBus;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by jess on 26/04/2017 20:23
 * Contact with jess.yan.effort@gmail.com
 */

public class ActivityDelegateImpl implements IActivityDelegate {
    private Activity mActivity;
    private Unbinder mUnbinder;

    public ActivityDelegateImpl(Activity activity) {
        this.mActivity = activity;
    }


    public void onCreate(Bundle savedInstanceState) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //新版本的转场动画
            mActivity.getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        }

        if (((IActivity)mActivity).useEventBus()) {
            //如果要使用eventbus请将此方法返回true
            EventBus.getDefault().register(mActivity);//注册到事件主线
        }
        ((IActivity)mActivity).setupActivityComponent(((IApplicationDelegate) mActivity.getApplication()).getAppComponent());//依赖注入

        ((IActivity)mActivity).onBeforeSetContentView();

        mActivity.setContentView(((IActivity)mActivity).getContentViewId());
        //绑定到butterknife
        mUnbinder = ButterKnife.bind(mActivity);
        ((IActivity)mActivity).initView(savedInstanceState);
        ((IActivity)mActivity).initData(savedInstanceState);
    }

    public void onStart() {

    }

    public void onResume() {

    }

    public void onPause() {

    }

    public void onStop() {

    }

    public void onSaveInstanceState(Bundle outState) {

    }


    public void onDestroy() {
        if (mUnbinder != Unbinder.EMPTY) {
            mUnbinder.unbind();
        }
        this.mUnbinder = null;

        if (((IActivity)mActivity).useEventBus()) {
            //如果要使用eventbus请将此方法返回true
            EventBus.getDefault().unregister(mActivity);
        }
        this.mActivity = null;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    protected ActivityDelegateImpl(Parcel in) {
        this.mActivity = in.readParcelable(Activity.class.getClassLoader());
        this.mUnbinder = in.readParcelable(Unbinder.class.getClassLoader());
    }

    public static final Creator<ActivityDelegateImpl> CREATOR = new Creator<ActivityDelegateImpl>() {
        @Override
        public ActivityDelegateImpl createFromParcel(Parcel source) {
            return new ActivityDelegateImpl(source);
        }

        @Override
        public ActivityDelegateImpl[] newArray(int size) {
            return new ActivityDelegateImpl[size];
        }
    };
}
