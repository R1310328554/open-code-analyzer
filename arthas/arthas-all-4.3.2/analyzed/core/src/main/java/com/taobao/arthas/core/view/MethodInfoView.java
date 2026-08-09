package com.taobao.arthas.core.view;

import com.taobao.arthas.core.util.Constants;
import com.taobao.arthas.core.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;


/**
 * Java 方法信息展示控件：输出声明类、方法名、修饰符、注解、参数、返回值与异常列表。
 * Created by vlinux on 15/5/9.
 */
public class MethodInfoView implements View {

    /** 待展示的方法反射对象 */
    private final Method method;
    /** 表格第二列可用宽度 */
    private final int width;

    /**
     * @param method 目标方法
     * @param width 终端宽度
     */
    public MethodInfoView(Method method, int width) {
        this.method = method;
        this.width = width;
    }

    @Override
    public String draw() {
        return new TableView(new TableView.ColumnDefine[]{
                new TableView.ColumnDefine("declaring-class".length(), false, TableView.Align.RIGHT),
                // (列数-1) * 3 + 4 = 7
                new TableView.ColumnDefine(width - "declaring-class".length() - 7, false, TableView.Align.LEFT)
        })
                .addRow("declaring-class", method.getDeclaringClass().getName())
                .addRow("method-name", method.getName())
                .addRow("modifier", StringUtils.modifier(method.getModifiers(), ','))
                .addRow("annotation", drawAnnotation())
                .addRow("parameters", drawParameters())
                .addRow("return", drawReturn())
                .addRow("exceptions", drawExceptions())
                .padding(1)
                .hasBorder(true)
                .draw();
    }

    /** 方法级注解，逗号分隔 */
    private String drawAnnotation() {

        final StringBuilder annotationSB = new StringBuilder();
        final Annotation[] annotationArray = method.getDeclaredAnnotations();

        if (annotationArray.length > 0) {
            for (Annotation annotation : annotationArray) {
                annotationSB.append(StringUtils.classname(annotation.annotationType())).append(",");
            }
            if (annotationSB.length() > 0) {
                annotationSB.deleteCharAt(annotationSB.length() - 1);
            }
        } else {
            annotationSB.append(Constants.EMPTY_STRING);
        }

        return annotationSB.toString();
    }

    /** 形参类型列表，每行一个 */
    private String drawParameters() {
        final StringBuilder paramsSB = new StringBuilder();
        final Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes.length > 0) {
            for (Class<?> clazz : paramTypes) {
                paramsSB.append(StringUtils.classname(clazz)).append("\n");
            }
        }
        return paramsSB.toString();
    }

    /** 返回类型 */
    private String drawReturn() {
        final StringBuilder returnSB = new StringBuilder();
        final Class<?> returnTypeClass = method.getReturnType();
        returnSB.append(StringUtils.classname(returnTypeClass)).append("\n");
        return returnSB.toString();
    }

    /** throws 子句中的异常类型，每行一个 */
    private String drawExceptions() {
        final StringBuilder exceptionSB = new StringBuilder();
        final Class<?>[] exceptionTypes = method.getExceptionTypes();
        if (exceptionTypes.length > 0) {
            for (Class<?> clazz : exceptionTypes) {
                exceptionSB.append(StringUtils.classname(clazz)).append("\n");
            }
        }
        return exceptionSB.toString();
    }

}
