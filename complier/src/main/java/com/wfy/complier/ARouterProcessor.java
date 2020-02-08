package com.wfy.complier;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import com.wfy.annotation.ARouter;
import com.wfy.annotation.model.RouterBean;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;


/**
 * 每个模块都会调用一次，会在指定模块下生成java文件，那么我们如何使用这些文件？
 * <p>
 * <p>
 * <p>
 * JavaPoet：
 * MethodSpec：代表一个构造方法或方法声明
 * TypeSpec： 代表一个类或接口或者枚举
 * FieldSpec：代表一个成员变量或一个字段
 * ParameterSpec：用来创建参数
 * JavaFile：包含一个顶级类的Java文件
 * AnnotationSpec：用来创建注解
 * ClassName:用来包装一个类
 * TypeName：类型，如在添加返回值类型使用TypeName.VOID
 * <p>
 * $S:字符串，如：$S,"JavaPoet" ->将$S赋值为"JavaPoet"
 * $L: 字面量，如："int value = $L",10
 * $N: 变量，如，user.$N,"name"
 * $T: 类或接口，如：$T,MainActivity
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes({Constants.ARouter_annotation_types})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedOptions({Constants.MODEL_NAME})
public class ARouterProcessor extends AbstractProcessor {

    private Elements elementUtils;
    private Types typeUtils;
    private Messager messager;
    private Filer filer;
    private String modelName;
    private String packageNameForAPT;


    //需要注意并发引发的错误
    // 临时map存储，用来存放路由组Group对应的详细Path类对象，生成路由路径类文件时遍历
    // key:组名"app", value:"app"组的路由路径"ARouter$$Path$$app.class"
    private Map<String, List<RouterBean>> tempPathMap = new ConcurrentHashMap<>();

    // 临时map存储，用来存放路由Group信息，生成路由组类文件时遍历
    // key:组名"app", value:类名"ARouter$$Path$$app.class"
    private Map<String, String> tempGroupMap = new HashMap<>();


    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtils = processingEnvironment.getElementUtils();
        typeUtils = processingEnvironment.getTypeUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();

        Map<String, String> options = processingEnvironment.getOptions();
        if (!EmptyUtils.isEmpty(options)) {
            modelName = options.get(Constants.MODEL_NAME);
            packageNameForAPT = Constants.APT_PACKAGE_NAME;

            messager.printMessage(Diagnostic.Kind.NOTE, "modelName >>>" + modelName);
            messager.printMessage(Diagnostic.Kind.NOTE, "packageNameForAPT >>>" + packageNameForAPT);
        }


        if (EmptyUtils.isEmpty(modelName) || EmptyUtils.isEmpty(packageNameForAPT)) {
            throw new RuntimeException("modelName 或 packageNameForAPT 为空");
        }
    }

    /**
     * 相当于main函数，开始处理注解
     * 注解处理器的核心方法，处理具体的注解，生成Java文件
     *
     * @param set              使用了支持处理注解的节点集合（类 上面写了注解）
     * @param roundEnvironment 当前或是之前的运行环境,可以通过该对象查找找到的注解。
     * @return true 表示后续处理器不会再处理（已经处理完成）
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set.isEmpty()) return true;

        // 获取被ARouter注解的类set集合
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(ARouter.class);
        if (!EmptyUtils.isEmpty(elements)) {
            try {
                parseElement(elements);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private void parseElement(Set<? extends Element> elements) throws IOException {
        // 通过ElementUtils，获取Activity类型
        TypeElement typeElement = elementUtils.getTypeElement(Constants.ACTIVITY);
        //显示类的信息
        TypeMirror activityTypeMirror = typeElement.asType();

        for (Element element : elements) {
            //获取每个元素的类信息
            TypeMirror typeMirror = element.asType();
            // 遍历元素的类信息 >>>com.wfy.order.Order_MainActivity
            messager.printMessage(Diagnostic.Kind.NOTE, "遍历元素的类信息 >>>" + typeMirror.toString());

            ARouter aRouter = element.getAnnotation(ARouter.class);

            RouterBean routerBean = new RouterBean.Builder()
                    .setGroup(aRouter.group())
                    .setPath(aRouter.path())
                    .setElement(element)
                    .build();

            //高级判断，@ARouter直接仅仅只能作用在类山，并且是规定的Activity
            if (typeUtils.isSubtype(typeMirror, activityTypeMirror)) {
                routerBean.setType(RouterBean.Type.ACTIVITY);
            } else {
                throw new RuntimeException("@ARouter注解目前仅限用于Activity类之上");
            }
            //赋值临时的Map存储以上信息，用来遍历时生成代码
            valueOfPathMap(routerBean);
        }

        // ARouterLoadPath 和 ARouterLoadGroup 类型，用来生成类文件时，实现接口
        TypeElement groupTypeElement = elementUtils.getTypeElement(Constants.AROUTE_GROUP);
        TypeElement pathTypeElement = elementUtils.getTypeElement(Constants.AROUTE_PATH);

        //1、生成路由的详细Path类，如：ARouter$$Path$$app
        createPathFile(pathTypeElement);
        //2、生成路由组Group文件，（没有Path类，取不到），如：ARouter$$Group$$app
        createGroupFile(groupTypeElement, pathTypeElement);
    }


    private void createPathFile(TypeElement pathTypeElement) throws IOException {
        if (EmptyUtils.isEmpty(tempPathMap)) return;
        //方法的返回值： Map<String, RouterBean>
        TypeName methodReturns = ParameterizedTypeName.get(ClassName.get(Map.class),
                ClassName.get(String.class), ClassName.get(RouterBean.class));


        //遍历分组，每一个分组创建一个路劲文件ARouter$$Path$$order
        Set<Map.Entry<String, List<RouterBean>>> entries = tempPathMap.entrySet();
        messager.printMessage(Diagnostic.Kind.NOTE, "" + entries);
        for (Map.Entry<String, List<RouterBean>> entry : entries) {
            // public Map<String, RouterBean> loadPath() {
            MethodSpec.Builder builder = MethodSpec.methodBuilder(Constants.PATH_METHOD_NAME)
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(methodReturns);

            // Map<String, RouterBean> map = new HashMap<>();
            builder.addStatement("$T<$T,$T> $N = new $T<>()",
                    ClassName.get(Map.class),
                    ClassName.get(String.class),
                    ClassName.get(RouterBean.class),
                    Constants.PATH_PARAMETER_NAME,
                    HashMap.class);//试一下


            List<RouterBean> routerBeans = entry.getValue();

            /*
             map.put("/order/Order_MainActivity",
                RouterBean.create(RouterBean.Type.ACTIVITY, Order_MainActivity.class,
                        "/order/Order_MainActivity", "order"));

             */
            for (RouterBean bean : routerBeans) {
                builder.addStatement("$N.put($S,$T.create($T.$L,$T.class,$S,$S))",
                        Constants.PATH_PARAMETER_NAME,
                        bean.getPath(),
                        ClassName.get(RouterBean.class),
                        ClassName.get(RouterBean.Type.class),
                        bean.getType(),//枚举值
                        ClassName.get((TypeElement) bean.getElement()),
                        bean.getPath(),
                        bean.getGroup());
            }

            // return map;
            builder.addStatement("return $N", Constants.PATH_PARAMETER_NAME);

            //ARouter$$Path$$order
            String finalClassName = Constants.PATH_FILE_NAME + entry.getKey();

            JavaFile.builder(packageNameForAPT,
                    TypeSpec.classBuilder(finalClassName)
                            .addSuperinterface(ClassName.get(pathTypeElement))
                            .addModifiers(Modifier.PUBLIC)
                            .addMethod(builder.build())
                            .build()
            ).build().writeTo(filer);


            tempGroupMap.put(entry.getKey(), finalClassName);
        }
    }


    //ARouter$$Group$$order
    private void createGroupFile(TypeElement groupTypeElement, TypeElement pathTypeElement) throws IOException {
        // 判断是否有需要生成的类文件
        if (EmptyUtils.isEmpty(tempGroupMap) || EmptyUtils.isEmpty(tempPathMap)) return;


        //返回值Map<String, Class<? extends ARouterLoadPath>>
        TypeName retuens = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                //Class<? extends ARouterLoadPath>
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(pathTypeElement)))
        );

        //public Map<String, Class<? extends ARouterLoadPath>> loadGroup()
        MethodSpec.Builder builder = MethodSpec.methodBuilder(Constants.GROUP_METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(retuens);

        /*
          Map<String, Class<? extends ARouterLoadPath>> map = new HashMap<>();
        map.put("order",ARouter$$Path$$order.class);
        return map;
         */

        builder.addStatement("$T<$T,$T> $N = new $T<>()",
                ClassName.get(Map.class),
                ClassName.get(String.class),
                //Class<? extends ARouterLoadPath>
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(pathTypeElement))),
                Constants.GROUP_PARAMETER_NAME,
                HashMap.class
        );

        Set<Map.Entry<String, String>> entries = tempGroupMap.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            // map.put("order",ARouter$$Path$$order.class);
            builder.addStatement("$N.put($S,$T.class)",
                    Constants.GROUP_PARAMETER_NAME,
                    entry.getKey(),
                    ClassName.get(packageNameForAPT, entry.getValue())
            );
        }

        // return map;
        builder.addStatement("return $N", Constants.GROUP_PARAMETER_NAME);

        String finalClassName = Constants.GROUP_FILE_NAME + modelName;
        messager.printMessage(Diagnostic.Kind.NOTE, "APT生成组类文件 >>>" + finalClassName);

        JavaFile.builder(packageNameForAPT,
                TypeSpec.classBuilder(finalClassName)
                        .addSuperinterface(ClassName.get(groupTypeElement))
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(builder.build())
                        .build()
        ).build().writeTo(filer);

    }

    private void valueOfPathMap(RouterBean routerBean) {
        if (checkRouterPathRule(routerBean)) {
            messager.printMessage(Diagnostic.Kind.NOTE, "RouterBean >>>" + routerBean);

            List<RouterBean> routerBeans = tempPathMap.get(routerBean.getGroup());

            messager.printMessage(Diagnostic.Kind.NOTE, "routerBeans >>>" + routerBeans);

            if (EmptyUtils.isEmpty(routerBeans)) {
                routerBeans = new CopyOnWriteArrayList<>();
                routerBeans.add(routerBean);
                tempPathMap.put(routerBean.getGroup(), routerBeans);
            } else {
                for (RouterBean bean : routerBeans) {
                    if (!routerBean.getPath().equalsIgnoreCase(bean.getPath())) {
                        routerBeans.add(routerBean);
                    }
                }
            }
        } else {
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解未按规范配置，如：/app/MainActivity");
        }
        messager.printMessage(Diagnostic.Kind.NOTE, "tempPathMap >>>" + tempPathMap);
    }

    /**
     * 检查Path的规则:
     * 1、@ARouter注解中的path值，必须要以 / 开头；
     * 2、@ARouter注解未按规范配置，如：/app/MainActivity；
     * 3、@ARouter注解中的group值必须和子模块名一致！
     *
     * @param routerBean
     * @return
     */
    private boolean checkRouterPathRule(RouterBean routerBean) {
        String group = routerBean.getGroup();
        String path = routerBean.getPath();

        // @ARouter注解中的path值，必须要以 / 开头（模仿阿里Arouter规范）
        if (EmptyUtils.isEmpty(path) || !path.startsWith("/")) {
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解中的path值，必须要以 / 开头");
            return false;
        }

        // 比如开发者代码为：path = "/MainActivity"，最后一个 / 符号必然在字符串第1位
        if (path.lastIndexOf("/") == 0) {
            // 架构师定义规范，让开发者遵循
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解未按规范配置，如：/app/MainActivity");
            return false;
        }

        // 从第一个 / 到第二个 / 中间截取，如：/app/MainActivity 截取出 app 作为group
        String finalGroup = path.substring(1, path.indexOf("/", 1));

        // @ARouter注解中的group有赋值情况
        if (!EmptyUtils.isEmpty(group)) {
            // 架构定义规范，让开发者遵循, 定义的组必须和模块名一致
            if (!group.equals(modelName)) {
                messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解中的group值必须和子模块名一致！");
                return false;
            }
            //定义的组必须和模块名一致
            routerBean.setGroup(group);
        } else {
            routerBean.setGroup(finalGroup);
        }

        return true;
    }


    /**
     * JavaPoet实现
     *
     * @param set
     * @param roundEnvironment
     */
    private void javaPoet(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(ARouter.class);
        for (Element element : elements) {
            //类节点上一个节点
            String packageName = elementUtils.getPackageOf(element).getQualifiedName().toString();
            //简单类名
            String className = element.getSimpleName().toString();
            messager.printMessage(Diagnostic.Kind.NOTE, modelName + "模块被注解的类有:" + className);
            messager.printMessage(Diagnostic.Kind.NOTE, "packageName:" + packageName);

            //最终生成的类文件名
            String finalClassName = modelName + "$$" + className + "$$ARouter";

            ARouter aRouter = element.getAnnotation(ARouter.class);

            MethodSpec methodSpec = MethodSpec.methodBuilder("findTargetClass")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(Class.class)
                    .addParameter(String.class, "path")
                    .addStatement("return path.equals($S) ? $T.class : null",
                            aRouter.path(),
                            ClassName.get((TypeElement) element))
                    .build();

            TypeSpec typeSpec = TypeSpec.classBuilder(finalClassName)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addMethod(methodSpec)
                    .build();


            JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();
            try {
                javaFile.writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


    /**
     * 原始方式
     *
     * @param set
     * @param roundEnvironment
     * @return
     */
    private boolean origin(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        // 获取被ARouter注解的类set集合
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(ARouter.class);

        for (Element element : elements) {
            String packageName = elementUtils.getPackageOf(element).getQualifiedName().toString();
            String className = element.getSimpleName().toString();
            messager.printMessage(Diagnostic.Kind.NOTE, modelName + "模块被注解的类有:" + className);
            String finalClassName = className + "$$ARouter";


            try {
                // 创建一个新的源文件（Class），并返回一个对象以允许写入它
                JavaFileObject sourceFile =
                        filer.createSourceFile(packageName + "." + finalClassName);
                // 定义Writer对象，开启写入
                Writer writer = sourceFile.openWriter();
                // 设置包名
                writer.write("package " + packageName + ";\n");

                writer.write("public class " + finalClassName + " {\n");

                writer.write("public static Class<?> findTargetClass(String path) {\n");

                // 获取类之上@ARouter注解的path值
                ARouter aRouter = element.getAnnotation(ARouter.class);

                writer.write("if (path.equals(\"" + aRouter.path() + "\")) {\n");

                writer.write("return " + className + ".class;\n}\n");

                writer.write("return null;\n");

                writer.write("}\n}");

                // 最后结束别忘了
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
