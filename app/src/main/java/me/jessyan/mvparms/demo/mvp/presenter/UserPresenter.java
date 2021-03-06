package me.jessyan.mvparms.demo.mvp.presenter;

import android.app.Activity;

import com.apkfuns.logutils.LogUtils;
import com.jess.arms.base.DefaultAdapter;
import com.jess.arms.common.utils.ImageUtils;
import com.jess.arms.di.component.AppComponent;
import com.jess.arms.di.scope.ActivityScope;
import com.jess.arms.mvp.PresenterImp;
import com.jess.arms.rx.RetryWithDelay;
import com.jess.arms.rx.RxUtils;
import com.jess.arms.rx.SimpleObserver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import me.jessyan.mvparms.demo.mvp.contract.UserContract;
import me.jessyan.mvparms.demo.mvp.model.entity.User;
import me.jessyan.mvparms.demo.mvp.ui.adapter.UserAdapter;

/**
 * Created by jess on 9/4/16 10:59
 * Contact with jess.yan.effort@gmail.com
 */
@ActivityScope
public class UserPresenter extends PresenterImp<UserContract.Model, UserContract.View> implements UserContract.Presenter {

    private List<User> mUsers = new ArrayList<>();
    private DefaultAdapter mAdapter;
    private int lastUserId = 1;
    private boolean isFirst = true;


    @Inject
    public UserPresenter(UserContract.Model model, UserContract.View rootView, AppComponent appComponent) {
        super(model, rootView, appComponent);
    }

    @Override
    public void requestUsers(final boolean pullToRefresh) {
        if (mAdapter == null) {
            mAdapter = new UserAdapter(mUsers);
            mRootView.setAdapter(mAdapter);//设置Adapter
        }

        //请求外部存储权限用于适配android6.0的权限管理机制
//        PermissionUtil.externalStorage(mRootView.getRxPermissions()).compose(RxUtils.<Boolean>bindToLifecycle(mRootView)).subscribe(new SimpleObserver<Boolean>() {
//            @Override
//            public void onNext(Boolean value) {
//
//            }
//
//            @Override
//            public void onError(Throwable e) {
//
//            }
//
//            @Override
//            public void onComplete() {
//
//            }
//        });


        if (pullToRefresh)
            lastUserId = 1;//上拉刷新默认只请求第一页

        //关于RxCache缓存库的使用请参考 http://www.jianshu.com/p/b58ef6b0624b

        boolean isEvictCache = pullToRefresh;//是否驱逐缓存,为ture即不使用缓存,每次上拉刷新即需要最新数据,则不使用缓存

        if (pullToRefresh && isFirst) {//默认在第一次上拉刷新时使用缓存
            isFirst = false;
            isEvictCache = false;
        }

        mModel.getUsers(lastUserId, isEvictCache).subscribeOn(Schedulers.io()).retryWhen(new RetryWithDelay(3, 2))//遇到错误时重试,第一个参数为重试几次,第二个参数为重试的间隔

                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        if (pullToRefresh)
                            mRootView.showLoading();//显示上拉刷新的进度条
                        else
                            mRootView.startLoadMore();//显示下拉加载更多的进度条
                    }
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).doAfterTerminate(new Action() {
            @Override
            public void run() throws Exception {
                if (pullToRefresh)
                    mRootView.hideLoading();//隐藏上拉刷新的进度条
                else
                    mRootView.endLoadMore();//隐藏下拉加载更多的进度条
            }
        }).compose(RxUtils.<List<User>>bindToLifecycle(mRootView))//使用RXlifecycle,使subscription和activity一起销毁
                .subscribe(new SimpleObserver<List<User>>() {
                    @Override
                    public void onNext(List<User> users) {
                        lastUserId = users.get(users.size() - 1).getId();//记录最后一个id,用于下一次请求
                        if (pullToRefresh)
                            mUsers.clear();//如果是上拉刷新则清空列表
                        for (User user : users) {
                            mUsers.add(user);
                        }
                        mAdapter.notifyDataSetChanged();//通知更新数据
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

//        TelephoneUtil.getInstance().getDeviceId((Activity)mRootView)
//                .compose(RxUtils.<String>bindToLifecycle(mRootView))
//                .subscribe(new SimpleObserver<String>() {
//                    @Override
//                    public void onNext(String s) {
//                        LogUtils.d("设备ID："+s);
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//
//                    }
//
//                    @Override
//                    public void onComplete() {
//
//                    }
//                });

        ImageUtils.downLoadImageWithGlide((Activity) mRootView,"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1488950363453&di=a6f3bd7d1b2461d2b6a8c1bb7fa9aeb7&imgtype=0&src=http%3A%2F%2Fc.hiphotos.baidu.com%2Fimage%2Fpic%2Fitem%2Fe7cd7b899e510fb3bde5709ddb33c895d1430c3f.jpg")
                .compose(RxUtils.<File>bindToLifecycle(mRootView))
                .subscribe(new SimpleObserver<File>() {
                    @Override
                    public void onNext(File file) {
                        LogUtils.d("保存的文件路径："+file.getAbsolutePath());
                    }

                    @Override
                    public void onError(Throwable e) {
                        LogUtils.e(e);
                    }

                    @Override
                    public void onComplete() {
                        LogUtils.d("下载完成");
                    }
                });

    }

    @Override
    public void onDestroy() {
        this.mAdapter = null;
        this.mUsers = null;
        super.onDestroy();
    }
}
