package common;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.apkfuns.logutils.LogLevel;
import com.apkfuns.logutils.LogUtils;
import com.jess.arms.base.delegate.IApplicationDelegate;
import com.jess.arms.base.delegate.ApplicationDelegate;
import com.jess.arms.di.module.GlobalConfigModule;
import com.jess.arms.http.IGlobalHttpHandler;
import com.jess.arms.http.RequestInterceptor;
import com.jess.arms.integration.ConfigModule;
import com.jess.arms.integration.IRepositoryManager;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.zxy.recovery.callback.RecoveryCallback;
import com.zxy.recovery.core.Recovery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import me.jessyan.mvparms.demo.BuildConfig;
import me.jessyan.mvparms.demo.R;
import me.jessyan.mvparms.demo.mvp.model.api.Api;
import me.jessyan.mvparms.demo.mvp.model.api.cache.CommonCache;
import me.jessyan.mvparms.demo.mvp.model.api.service.CommonService;
import me.jessyan.mvparms.demo.mvp.model.api.service.UserService;
import me.jessyan.mvparms.demo.mvp.ui.activity.UserActivity;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * app的全局配置信息在此配置,需要将此实现类声明到AndroidManifest中
 * Created by jess on 12/04/2017 17:25
 * Contact with jess.yan.effort@gmail.com
 */


public class GlobalConfiguration implements ConfigModule {

    @Override
    public void applyOptions(Context context, GlobalConfigModule.Builder builder) {
        builder.baseurl(Api.APP_DOMAIN)
                .globalHttpHandler(new IGlobalHttpHandler() {// 这里可以提供一个全局处理Http请求和响应结果的处理类,
                    // 这里可以比客户端提前一步拿到服务器返回的结果,可以做一些操作,比如token超时,重新获取
                    @Override
                    public Response onHttpResultResponse(String httpResult, Interceptor.Chain chain, Response response) {
                        /* 这里可以先客户端一步拿到每一次http请求的结果,可以解析成json,做一些操作,如检测到token过期后
                           重新请求token,并重新执行请求 */
                        try {
                            if (!TextUtils.isEmpty(httpResult) && RequestInterceptor.isJson(response.body())) {
                                JSONArray array = new JSONArray(httpResult);
                                JSONObject object = (JSONObject) array.get(0);
                                String login = object.getString("login");
                                String avatar_url = object.getString("avatar_url");
                                LogUtils.w("Result ------> " + login + "    ||   Avatar_url------> " + avatar_url);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            return response;
                        }


                     /* 这里如果发现token过期,可以先请求最新的token,然后在拿新的token放入request里去重新请求
                        注意在这个回调之前已经调用过proceed,所以这里必须自己去建立网络请求,如使用okhttp使用新的request去请求
                        create a new request and modify it accordingly using the new token
                        Request newRequest = chain.request().newBuilder().header("token", newToken)
                                             .build();

                        retry the request

                        response.body().close();
                        如果使用okhttp将新的请求,请求成功后,将返回的response  return出去即可
                        如果不需要返回新的结果,则直接把response参数返回出去 */

                        return response;
                    }

                    // 这里可以在请求服务器之前可以拿到request,做一些操作比如给request统一添加token或者header以及参数加密等操作
                    @Override
                    public Request onHttpRequestBefore(Interceptor.Chain chain, Request request) {
                        /* 如果需要再请求服务器之前做一些操作,则重新返回一个做过操作的的requeat如增加header,不做操作则直接返回request参数
                           return chain.request().newBuilder().header("token", tokenId)
                                  .build(); */
                        return request;
                    }
                });
    }

    @Override
    public void registerComponents(Context context, IRepositoryManager repositoryManager) {
        repositoryManager.injectRetrofitService(CommonService.class, UserService.class);
        repositoryManager.injectCacheService(CommonCache.class);
    }

    @Override
    public void injectAppLifecycle(Context context, List<ApplicationDelegate.Lifecycle> lifecycles) {
        // AppDelegate.Lifecycle 的所有方法都会在基类Application对应的生命周期中被调用,所以在对应的方法中可以扩展一些自己需要的逻辑
        lifecycles.add(new ApplicationDelegate.Lifecycle() {

            @Override
            public void onCreate(Application application) {
                LogUtils.getLogConfig().configAllowLog(BuildConfig.LOG_DEBUG).configTagPrefix("AndroidBase").configShowBorders(true).configFormatTag("%d{HH:mm:ss:SSS} %t %c{-5}").configLevel(LogLevel.TYPE_VERBOSE);

                //leakCanary内存泄露检查
                installLeakCanary(application);

                initReference(application);

               }

            private void installLeakCanary(Application application) {
                RefWatcher refWatcher;
                if (LeakCanary.isInAnalyzerProcess(application)) {
                    return;
                }
                if (BuildConfig.USE_CANARY) {
                    enabledStrictMode();
                    refWatcher = LeakCanary.install(application);
                } else {
                    refWatcher = RefWatcher.DISABLED;
                }
                ((IApplicationDelegate) application).getAppComponent().extras().put(RefWatcher.class.getName(), refWatcher);
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

            }
        });
    }

    @Override
    public void injectActivityLifecycle(Context context, List<Application.ActivityLifecycleCallbacks> lifecycles) {
        lifecycles.add(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(final Activity activity, Bundle savedInstanceState) {




                //这里全局给Activity设置toolbar和title,你想象力有多丰富,这里就有多强大,以前放到BaseActivity的操作都可以放到这里
                if (activity.findViewById(R.id.toolbar) != null) {
                    if (activity instanceof AppCompatActivity) {
                        ((AppCompatActivity) activity).setSupportActionBar((Toolbar) activity.findViewById(R.id.toolbar));
                        ((AppCompatActivity) activity).getSupportActionBar().setDisplayShowTitleEnabled(false);
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            activity.setActionBar((android.widget.Toolbar) activity.findViewById(R.id.toolbar));
                            activity.getActionBar().setDisplayShowTitleEnabled(false);
                        }
                    }
                }
                if (activity.findViewById(R.id.toolbar_title) != null) {
                    ((TextView) activity.findViewById(R.id.toolbar_title)).setText(activity.getTitle());
                }
                if (activity.findViewById(R.id.toolbar_back) != null) {
                    activity.findViewById(R.id.toolbar_back).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            activity.onBackPressed();
                        }
                    });
                }
            }


            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

    @Override
    public void injectFragmentLifecycle(Context context, List<FragmentManager.FragmentLifecycleCallbacks> lifecycles) {
        lifecycles.add(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentDestroyed(FragmentManager fm, Fragment f) {
                ((RefWatcher)((IApplicationDelegate) f.getActivity().getApplication()).getAppComponent().extras().get(RefWatcher.class.getName())).watch(this);
            }
        });
    }
}
