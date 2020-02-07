package com.wfy.complier;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.wfy.annotation.Parameter;

import java.io.IOException;
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
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;


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
@SupportedAnnotationTypes({Constants.PARAMETER_ANNOTATION_TYPES})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ParameterProcessor extends AbstractProcessor {

    private Elements elementUtils;
    private Types typeUtils;
    private Messager messager;
    private Filer filer;
    // 通过ElementUtils，获取Activity类型,显示类的信息
    private TypeMirror activityTypeMirror;
    private TypeMirror androidXFragmentTypeMirror;
    private TypeMirror appFragmentTypeMirror;
    private TypeMirror parcelableTypeMirror;


    //临时map存储，用来存放@Parameter注解的属性集合，生成类文件时遍历
    //key : 类节点  value：被@Parameter注解的属性集合
    private Map<TypeElement, List<Element>> tempPrameterMap = new ConcurrentHashMap<>();


    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtils = processingEnvironment.getElementUtils();
        typeUtils = processingEnvironment.getTypeUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
        activityTypeMirror = elementUtils.getTypeElement(Constants.ACTIVITY).asType();
        appFragmentTypeMirror = elementUtils.getTypeElement(Constants.APP_FRAGMENT).asType();
        androidXFragmentTypeMirror = elementUtils.getTypeElement(Constants.ANDROIDX_FRAGMENT).asType();
        parcelableTypeMirror = elementUtils.getTypeElement(Constants.PARCELABLE).asType();
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

        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Parameter.class);
        if (!EmptyUtils.isEmpty(elements)) {
            valueOfParameter(elements);
            createParameterFile();
        }

        return true;
    }

    private void createParameterFile() {
        if (tempPrameterMap.isEmpty()) return;

        TypeElement typeElement = elementUtils.getTypeElement(Constants.PARAMETER_LOAD);

        ParameterSpec parameterSpec = ParameterSpec.builder(TypeName.OBJECT, Constants.PARAMETER_NAMR).build();


        for (Map.Entry<TypeElement, List<Element>> entry : tempPrameterMap.entrySet()) {
            //activity,Fragment or other
            TypeElement otherTypeElement = entry.getKey();
            ClassName otherClassName = ClassName.get(otherTypeElement);
            TypeMirror otherType = otherTypeElement.asType();


            MethodSpec.Builder builder = MethodSpec.methodBuilder(Constants.PARAMETER_METHOD_NAME)
                    .addParameter(parameterSpec)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(TypeName.VOID)
                    //MainActivity t = (MainActivity) target;
                    .addStatement("$T t = ($T)$N", otherClassName,
                            otherClassName, Constants.PARAMETER_NAMR);

            //该类下的所有被@Parameter注解的属性
            List<Element> elements = entry.getValue();

            for (Element element : elements) {
                //被@Parameter注解类信息
                TypeMirror typeMirror = element.asType();
                int type = typeMirror.getKind().ordinal();
                // 被@Parameter注解的属性名
                String filedName = element.getSimpleName().toString();
                // @Parameter注解获取属性名
                String annotationValue = element.getAnnotation(Parameter.class).name();
                annotationValue = EmptyUtils.isEmpty(annotationValue) ? filedName : annotationValue;

                //如：t.age = target.getIntent().getStringExtra("age", 1);
                String finalValue = "t" + "." + annotationValue;

                String methodContent;
                if (typeUtils.isSubtype(otherType, activityTypeMirror)) {//activity
                    methodContent = finalValue + " =  t.getIntent().";
                    if (type == TypeKind.INT.ordinal()) {
                        methodContent += "getIntExtra($S," + finalValue + ")";
                    } else if (type == TypeKind.BOOLEAN.ordinal()) {
                        methodContent += "getBooleanExtra($S," + finalValue + ")";
                    } else {
                        if (typeMirror.toString().equalsIgnoreCase(Constants.STRING)) {
                            methodContent += "getStringExtra($S)";
                        } else if (typeUtils.isSubtype(element.asType(),
                                parcelableTypeMirror)) {//Parcelable
                            methodContent += "getParcelableExtra($S)";
                        }
                    }
                    builder.addStatement(methodContent, annotationValue);
                } else if (typeUtils.isSubtype(otherType, appFragmentTypeMirror) ||
                        typeUtils.isSubtype(otherType, androidXFragmentTypeMirror)) {//fragment
                    methodContent = finalValue + " =  t.getArguments().";
                    if (type == TypeKind.INT.ordinal()) {
                        methodContent += "getInt($S," + finalValue + ")";
                    } else if (type == TypeKind.BOOLEAN.ordinal()) {
                        methodContent += "getBoolean($S," + finalValue + ")";
                    } else {
                        if (typeMirror.toString().equalsIgnoreCase(Constants.STRING)) {
                            methodContent += "getString($S," + finalValue + ")";
                        }
                    }
                    builder.addStatement(methodContent, annotationValue);
                }
            }


            String finalClassName = otherClassName.simpleName() + Constants.PARAMETER_FILE_NAME;
            messager.printMessage(Diagnostic.Kind.NOTE, otherClassName.packageName() + "生成parameter注解的类名 >>> " + finalClassName);


            TypeSpec.Builder typeSpec =
                    TypeSpec.classBuilder(finalClassName)
                            .addModifiers(Modifier.PUBLIC)
                            .addSuperinterface(ClassName.get(typeElement))
                            .addMethod(builder.build());

            try {
                JavaFile.builder(otherClassName.packageName(), typeSpec.build())
                        .build().writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void valueOfParameter(Set<? extends Element> elements) {
        for (Element element : elements) {
            TypeElement typeElement = (TypeElement) element.getEnclosingElement();
            if (tempPrameterMap.containsKey(typeElement)) {
                tempPrameterMap.get(typeElement).add(element);
            } else {
                CopyOnWriteArrayList<Element> files = new CopyOnWriteArrayList<>();
                files.add(element);
                tempPrameterMap.put(typeElement, files);
            }
        }
    }
}
