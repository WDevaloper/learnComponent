package com.wfy.complier;

import javax.annotation.processing.Messager;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementScanner6;
import javax.tools.Diagnostic;


//访问Java文件元素
public class SimpleElementVisitorImpl extends ElementScanner6<Void, Void> {

    Messager messager;

    public SimpleElementVisitorImpl(Messager messager) {
        this.messager = messager;
    }

    //访问一个包程序元素
    @Override
    public Void visitPackage(PackageElement packageElement, Void aVoid) {
        messager.printMessage(Diagnostic.Kind.NOTE, "packageElement>>>> " + packageElement);
        return aVoid;
    }
    //访问一个类或接口程序元素。
    @Override
    public Void visitType(TypeElement typeElement, Void aVoid) {
        messager.printMessage(Diagnostic.Kind.NOTE, "typeElement>>> " + typeElement);
        return aVoid;
    }

    //访问类或接口的方法，构造函数或初始化器（静态或实例），包括注释类型元素。
    @Override
    public Void visitExecutable(ExecutableElement executableElement, Void aVoid) {
        messager.printMessage(Diagnostic.Kind.NOTE, "executableElement>>>>> " + executableElement);
        return aVoid;
    }

    //访问字段，枚举常量，方法或构造函数参数，局部变量或异常参数。
    @Override
    public Void visitVariable(VariableElement variableElement, Void aVoid) {
        messager.printMessage(Diagnostic.Kind.NOTE, "variableElement >>> " + variableElement.getKind()
                + "visitVariable >>> " + variableElement +
                "   >>>" + variableElement.asType()
        );

        //需要访问这个Element的类型，所以variableElement.asType()
        variableElement.asType().accept(new MySimpleTypeVisitor6(messager), null);
        return aVoid;
    }


    //表示泛型类，接口，方法或构造函数元素的形式类型参数。
    @Override
    public Void visitTypeParameter(TypeParameterElement typeParameterElement, Void aVoid) {
        messager.printMessage(Diagnostic.Kind.NOTE, "visitTypeParameter >>> " + typeParameterElement);
        return aVoid;
    }


}
