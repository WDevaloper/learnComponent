package com.wfy.complier;

public class Constants {
    public final static String ARouter_annotation_types = "com.wfy.annotation.ARouter";
    public final static String PARAMETER_ANNOTATION_TYPES = "com.wfy.annotation.Parameter";

    public final static String MODEL_NAME = "MODEL_NAME";
    public final static String APT_PACKAGE_NAME = "packageNameForAPT";

    // String全类名
    public static final String STRING = "java.lang.String";
    // Activity全类名
    public static final String ACTIVITY = "android.app.Activity";
    public static final String PARCELABLE = "android.os.Parcelable";
    public static final String ANDROIDX_FRAGMENT  = "androidx.fragment.app.Fragment";
    public static final String APP_FRAGMENT  = "android.app.Fragment";

    // 包名前缀封装
    public static final String BASE_PACKAGE = "com.wfy.arouter_api";
    // 路由组Group加载接口
    public static final String AROUTE_GROUP = BASE_PACKAGE + ".core.ARouterLoadGroup";
    // 路由组Group对应的详细Path加载接口
    public static final String AROUTE_PATH = BASE_PACKAGE + ".core.ARouterLoadPath";

    // 获取参数，加载接口
    public static final String PARAMETER_LOAD = BASE_PACKAGE + ".core.ParameterInject";

    // 路由组Group，参数名
    public static final String GROUP_PARAMETER_NAME = "groupMap";
    // 路由组Group，方法名
    public static final String GROUP_METHOD_NAME = "loadGroup";
    // 路由组Group对应的详细Path，参数名
    public static final String PATH_PARAMETER_NAME = "pathMap";
    // 路由组Group对应的详细Path，方法名
    public static final String PATH_METHOD_NAME = "loadPath";

    // APT生成的路由组Group类文件名
    public static final String GROUP_FILE_NAME = "ARouter$$Group$$";
    // APT生成的路由组Group对应的详细Path类文件名
    public static final String PATH_FILE_NAME = "ARouter$$Path$$";
    // APT生成的获取参数类文件名
    public static final String PARAMETER_FILE_NAME = "$$Parameter";

    // 获取参数，方法名
    public static final String PARAMETER_NAMR = "target";
    // 获取参数，参数名
    public static final String PARAMETER_METHOD_NAME = "inject";



}
