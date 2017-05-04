package com.jess.arms.base.delegate;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;

import org.simple.eventbus.EventBus;

/**
 * Created by jess on 29/04/2017 16:12
 * Contact with jess.yan.effort@gmail.com
 */

public class FragmentDelegateImpl implements IFragmentDelegate {
    private FragmentManager mFragmentManager;
    private Fragment mFragment;





    public FragmentDelegateImpl(FragmentManager fragmentManager, Fragment fragment) {
        this.mFragmentManager = fragmentManager;
        this.mFragment = fragment;
    }

    @Override
    public void onAttach(Context context) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

    }

    @Override
    public void onCreateView(View view, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityCreate(Bundle savedInstanceState) {
        if (((IFragment)mFragment).useEventBus())//如果要使用eventbus请将此方法返回true
            EventBus.getDefault().register(mFragment);//注册到事件主线
        ((IFragment)mFragment).setupFragmentComponent(((IApplicationDelegate) mFragment.getActivity().getApplication()).getAppComponent());
        ((IFragment)mFragment).initData();
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onDestroyView() {

    }

    @Override
    public void onDestroy() {
        if (((IFragment)mFragment).useEventBus())//如果要使用eventbus请将此方法返回true
            EventBus.getDefault().unregister(mFragment);//注册到事件主线

        this.mFragmentManager = null;
        this.mFragment = null;
    }

    @Override
    public void onDetach() {

    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    protected FragmentDelegateImpl(Parcel in) {
        this.mFragmentManager = in.readParcelable(FragmentManager.class.getClassLoader());
        this.mFragment = in.readParcelable(Fragment.class.getClassLoader());
    }

    public static final Creator<FragmentDelegateImpl> CREATOR = new Creator<FragmentDelegateImpl>() {
        @Override
        public FragmentDelegateImpl createFromParcel(Parcel source) {
            return new FragmentDelegateImpl(source);
        }

        @Override
        public FragmentDelegateImpl[] newArray(int size) {
            return new FragmentDelegateImpl[size];
        }
    };
}
