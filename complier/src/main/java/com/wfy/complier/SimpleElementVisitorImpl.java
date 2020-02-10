package com.wfy.complier;

import javax.annotation.processing.Messager;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementScanner6;
import javax.tools.Diagnostic;

public class SimpleElementVisitorImpl extends ElementScanner6<Void, Void> {

    Messager messager;

    public SimpleElementVisitorImpl(Messager messager) {
        this.messager = messager;
    }

    @Override
    public Void visitTypeParameter(TypeParameterElement typeParameterElement, Void aVoid) {
        messager.printMessage(Diagnostic.Kind.NOTE, "visitTypeParameter >>> " + typeParameterElement);
        return aVoid;
    }


    @Override
    public Void visitVariable(VariableElement variableElement, Void aVoid) {
        messager.printMessage(Diagnostic.Kind.NOTE, "getKind >>> " + variableElement.getKind()
                + "visitVariable >>> " + variableElement +
                "   >>>" + variableElement.asType()
        );

        //需要访问这个Element的类型，所以variableElement.asType()
        variableElement.asType().accept(new MySimpleTypeVisitor6(messager), null);
        return aVoid;
    }
}
