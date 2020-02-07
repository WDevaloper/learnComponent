package com.wfy.arouter_api;

import androidx.collection.LruCache;

import com.wfy.arouter_api.core.ParameterInject;

public class ParameterManager {

    private volatile static ParameterManager instance;

    // Lru缓存，key:类名, value:参数Parameter加载接口
    private LruCache<String, ParameterInject> cache;
    // APT生成的获取参数源文件，后缀名
    private static final String FILE_SUFFIX_NAME = "$$Parameter";

    private ParameterManager() {
        // 初始化，并赋值缓存中条目的最大值
        cache = new LruCache<>(163);
    }

    public static ParameterManager getInstance() {
        if (instance == null) {
            synchronized (ParameterManager.class) {
                if (instance == null) {
                    instance = new ParameterManager();
                }
            }
        }
        return instance;
    }


    public void inject(Object target) {
        String className = target.getClass().getName();
        // 查找缓存集合中是否有对应activity的value
        ParameterInject iParameter = cache.get(className);
        try {
            // 找不到，加载类后放入缓存集合
            if (iParameter == null) {
                // 注意：这里的className是全类名：com.xxx.xxx.Activity
                Class<?> clazz = Class.forName(className + FILE_SUFFIX_NAME);
                iParameter = (ParameterInject) clazz.newInstance();
                cache.put(className, iParameter);
            }
            // 通过传入参数给生成的源文件中所有属性赋值
            iParameter.inject(target);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
