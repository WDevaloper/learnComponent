package com.wfy.complier;

import java.util.List;

import javax.annotation.processing.Messager;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.SimpleTypeVisitor6;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

public class MySimpleTypeVisitor6 extends SimpleTypeVisitor6<Void, Void> {
    Messager messager;

    public MySimpleTypeVisitor6(Messager messager) {
        this.messager = messager;
    }

    @Override
    public Void visitDeclared(DeclaredType declaredType, Void aVoid) {
        messager.printMessage(Diagnostic.Kind.NOTE, "declaredType >>> " + declaredType);
        List<? extends TypeMirror> arguments = declaredType.getTypeArguments();
        if (arguments != null && arguments.size() > 0) {//拿到泛型参数
            // java.util.ArrayList<com.wfy.common.User>  ===> com.wfy.common.User
            messager.printMessage(Diagnostic.Kind.NOTE, "arguments>>> " + arguments);
        }
        return aVoid;
    }

    @Override
    public Void visitArray(ArrayType arrayType, Void aVoid) {
        messager.printMessage(Diagnostic.Kind.NOTE, "arrayType >>> " + arrayType.getComponentType() + "  " + arrayType);
        return aVoid;
    }

    @Override
    public Void visitPrimitive(PrimitiveType primitiveType, Void aVoid) {
        messager.printMessage(Diagnostic.Kind.NOTE, "primitiveType >>> " + primitiveType);
        return aVoid;
    }


    @Override
    public Void visitWildcard(WildcardType wildcardType, Void aVoid) {
        messager.printMessage(Diagnostic.Kind.NOTE, "wildcardType >>> " + wildcardType);
        return aVoid;
    }

    @Override
    public Void visitTypeVariable(TypeVariable typeVariable, Void aVoid) {
        messager.printMessage(Diagnostic.Kind.NOTE, "typeVariable >>> " + typeVariable);
        return aVoid;
    }
}
