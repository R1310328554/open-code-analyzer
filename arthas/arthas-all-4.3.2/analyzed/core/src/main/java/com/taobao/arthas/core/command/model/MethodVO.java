package com.taobao.arthas.core.command.model;

/**
 * 方法或构造器的摘要视图：供 sc / sm / jad 等命令展示类成员签名。
 * <p>
 * {@link #constructor} 为 true 时表示 &lt;init&gt; 构造器；
 * {@link #descriptor} 为 JVM 方法描述符，与 {@link #parameters} 人类可读形式互补。
 *
 * @author gongdewei 2020/4/9
 */
public class MethodVO {

    /** 声明该方法的类全限定名 */
    private String declaringClass;
    /** 方法名；构造器时为类简单名或 &lt;init&gt; */
    private String methodName;
    /** 访问修饰符文本（public static 等） */
    private String modifier;
    /** 方法上的注解全限定名列表 */
    private String[] annotations;
    /** 参数类型的人类可读描述数组 */
    private String[] parameters;
    /** 返回类型描述，构造器通常为空或 void */
    private String returnType;
    /** throws 子句中的异常类型列表 */
    private String[] exceptions;
    /** 所属 ClassLoader 的 hash，用于多 ClassLoader 消歧 */
    private String classLoaderHash;
    /** JVM 方法描述符（(I)Ljava/lang/String; 形式） */
    private String descriptor;
    /** 是否为构造器 */
    private boolean constructor;

    public String getDeclaringClass() {
        return declaringClass;
    }

    public void setDeclaringClass(String declaringClass) {
        this.declaringClass = declaringClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public String[] getAnnotations() {
        return annotations;
    }

    public void setAnnotations(String[] annotations) {
        this.annotations = annotations;
    }

    public String[] getParameters() {
        return parameters;
    }

    public void setParameters(String[] parameters) {
        this.parameters = parameters;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String[] getExceptions() {
        return exceptions;
    }

    public void setExceptions(String[] exceptions) {
        this.exceptions = exceptions;
    }

    public String getClassLoaderHash() {
        return classLoaderHash;
    }

    public void setClassLoaderHash(String classLoaderHash) {
        this.classLoaderHash = classLoaderHash;
    }

    public boolean isConstructor() {
        return constructor;
    }

    public void setConstructor(boolean constructor) {
        this.constructor = constructor;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }
}
