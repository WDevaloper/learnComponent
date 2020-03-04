package com.wfy.arouter_api;


import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.collection.LruCache;

import com.wfy.annotation.model.RouterBean;
import com.wfy.arouter_api.core.ARouterLoadGroup;
import com.wfy.arouter_api.core.ARouterLoadPath;

/**
 * 路由加载管理器：
 * 由于我们生成的路由组类是有规则的，可以通过ARouter$$Group$$组名得到对应的组，然后通过组去加载组对应的Path类
 */
public class RouterManager {
    // 路由组名
    private String group;
    // 路由详细路径
    private String path;
    private static RouterManager instance;
    // Lru缓存，key:组名, value:路由组Group加载接口
    private LruCache<String, ARouterLoadGroup> groupCache;
    // Lru缓存，key:类名, value:路由组Group对应的详细Path加载接口
    private LruCache<String, ARouterLoadPath> pathCache;
    // APT生成的路由组Group源文件前缀名
    private static final String GROUP_FILE_PREFIX_NAME = ".ARouter$$Group$$";
    private static final String APT_PACKAGE_NAME = "com.wfy.arouter";

    // 单例方式，全局唯一
    public static RouterManager getInstance() {
        if (instance == null) {
            synchronized (RouterManager.class) {
                if (instance == null) {
                    instance = new RouterManager();
                }
            }
        }
        return instance;
    }

    private RouterManager() {
        // 初始化，并赋值缓存中条目的最大值（最多128组）
        groupCache = new LruCache<>(128);
        // 每组最多128条路径值
        pathCache = new LruCache<>(128);
    }

    public RouterManager build(String path) {
        // @ARouter注解中的path值，必须要以 / 开头（模仿阿里Arouter规范）
        if (TextUtils.isEmpty(path) || !path.startsWith("/")) {
            throw new IllegalArgumentException("未按规范配置，如：/app/MainActivity");
        }

        group = subFromPath2Group(path);

        // 检查后再赋值
        this.path = path;

        return this;
    }

    private String subFromPath2Group(String path) {
        // 比如开发者代码为：path = "/MainActivity"，最后一个 / 符号必然在字符串第1位
        if (path.lastIndexOf("/") == 0) {
            // 架构师定义规范，让开发者遵循
            throw new IllegalArgumentException("@ARouter注解未按规范配置，如：/app/MainActivity");
        }

        // 从第一个 / 到第二个 / 中间截取，如：/app/MainActivity 截取出 app 作为group
        String finalGroup = path.substring(1, path.indexOf("/", 1));

        if (TextUtils.isEmpty(finalGroup)) {
            // 架构师定义规范，让开发者遵循
            throw new IllegalArgumentException("@ARouter注解未按规范配置，如：/app/MainActivity");
        }

        // 最终组名：app
        return finalGroup;
    }

    /**
     * 开始跳转
     *
     * @param context 上下文
     * @param code    这里的code，可能是requestCode，也可能是resultCode。取决于isResult
     * @return 普通跳转可以忽略，用于跨模块CALL接口
     */
    public Object navigation(@NonNull Context context, int code) {
        // 精华：阿里的路由path随意写，导致无法找到随意拼接APT生成的源文件，如：ARouter$$Group$$abc
        // 找不到，就加载私有目录下apk中的所有dex并遍历，获得所有包名为xxx的类。并开启了线程池工作
        // 这里的优化是：代码规范写法，准确定位ARouter$$Group$$app

        //ARouter$$Group$$
        // group = app
        String groupClassName = APT_PACKAGE_NAME + ".apt" + GROUP_FILE_PREFIX_NAME + group;
        Log.e("tag", "groupClassName -> " + groupClassName);

        try {
            //首先通过组名获取缓存中的组ARouterLoadGroup，通过ARouterLoadGroup，就可以拿到ARouterLoadPath
            ARouterLoadGroup groupLoad = groupCache.get(group);
            if (groupLoad == null) {
                //通过拼接groupClassName反射创建ARouterLoadGroup
                Class<?> clazz = Class.forName(groupClassName);
                groupLoad = (ARouterLoadGroup) clazz.newInstance();
                //加入缓存
                groupCache.put(group, groupLoad);
            }


            // 获取路由路径类ARouter$$Path$$app
            if (groupLoad.loadGroup().isEmpty()) {
                throw new RuntimeException("路由加载失败");
            }

            //在通过path，如：如：/app/MainActivity，从缓存中获取ARouterLoadPath，
            // 然后在通过ARouterLoadPath获取RouterBean
            ARouterLoadPath pathLoad = pathCache.get(path);
            if (pathLoad == null) {
                Class<? extends ARouterLoadPath> clazz = groupLoad.loadGroup().get(group);
                if (clazz != null) pathLoad = clazz.newInstance();
                //加入缓存
                if (pathLoad != null) pathCache.put(path, pathLoad);
            }

            if (pathLoad != null) {
                // tempMap赋值
                pathLoad.loadPath();
                if (pathLoad.loadPath().isEmpty()) {
                    throw new RuntimeException("路由路径加载失败");
                }

                RouterBean routerBean = pathLoad.loadPath().get(path);
                if (routerBean != null) {
                    Intent intent = new Intent(context, routerBean.getClzz());
                    context.startActivity(intent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 注入属性
     *
     * @param target
     */
    public static void  inject(Object target) {
        ParameterManager.getInstance().inject(target);
    }

}
