package com.jess.arms.di.component;

import android.app.Application;

import com.google.gson.Gson;
import com.jess.arms.base.delegate.ApplicationDelegate;
import com.jess.arms.di.module.AppModule;
import com.jess.arms.di.module.ClientModule;
import com.jess.arms.di.module.GlobalConfigModule;
import com.jess.arms.integration.AppManager;
import com.jess.arms.integration.IRepositoryManager;
import com.jess.arms.widget.imageloader.ImageLoader;

import java.io.File;
import java.util.Map;

import javax.inject.Singleton;

import dagger.Component;
import okhttp3.OkHttpClient;

/**
 * Created by jess on 8/4/16.
 */
@Singleton
@Component(modules = {AppModule.class, ClientModule.class, GlobalConfigModule.class})
public interface AppComponent {
    Application getApplication();

    //用于管理网络请求层,以及数据缓存层
    IRepositoryManager getRepositoryManager();

    OkHttpClient getOkHttpClient();

    //图片管理器,用于加载图片的管理类,默认使用glide,使用策略模式,可替换框架
    ImageLoader getImageLoader();

    //gson
    Gson getGson();

    //缓存文件根目录(RxCache和Glide的的缓存都已经作为子文件夹在这个目录里),应该将所有缓存放到这个根目录里,便于管理和清理,可在GlobeConfigModule里配置
    File getCacheFile();

    //用于管理所有activity
    AppManager getAppManager();

    //用来存取一些整个App公用的数据,切勿大量存放大容量数据
    Map<String, Object> extras();

    void inject(ApplicationDelegate delegate);
}
