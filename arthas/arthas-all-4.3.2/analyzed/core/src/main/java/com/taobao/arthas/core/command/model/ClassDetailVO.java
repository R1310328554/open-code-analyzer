package com.taobao.arthas.core.command.model;

/**
 * 类详细信息视图，扩展 {@link ClassVO} 包含修饰符、继承关系、字段与注解等。
 * <p>
 * 由 sc -d、classloader 等命令填充，供 JSON 输出与 Web 控制台展示。
 *
 * @author gongdewei 2020/4/8
 */
public class ClassDetailVO extends ClassVO {

    /** 类概要描述字符串（类似 javap -verbose 摘要） */
    private String classInfo;
    /** 类定义来源（jar 路径等） */
    private String codeSource;
    /** 是否为 interface */
    private boolean isInterface;
    /** 是否为 annotation 类型 */
    private boolean isAnnotation;
    /** 是否为 enum */
    private boolean isEnum;
    /** 是否为匿名类 */
    private boolean isAnonymousClass;
    /** 是否为数组类型 */
    private boolean isArray;
    /** 是否为局部类 */
    private boolean isLocalClass;
    /** 是否为成员内部类 */
    private boolean isMemberClass;
    /** 是否为基本类型 */
    private boolean isPrimitive;
    /** 是否为合成类（编译器生成） */
    private boolean isSynthetic;
    /** 简单类名 */
    private String simpleName;
    /** 访问修饰符字符串（public final 等） */
    private String modifier;
    /** 类级别注解名列表 */
    private String[] annotations;
    /** 直接实现的接口名 */
    private String[] interfaces;
    /** 父类/超类链 */
    private String[] superClass;
    /** 字段详情列表 */
    private FieldVO[] fields;

    public String getClassInfo() {
        return classInfo;
    }

    public void setClassInfo(String classInfo) {
        this.classInfo = classInfo;
    }

    public String getCodeSource() {
        return codeSource;
    }

    public void setCodeSource(String codeSource) {
        this.codeSource = codeSource;
    }

    public boolean isInterface() {
        return isInterface;
    }

    public void setInterface(boolean anInterface) {
        isInterface = anInterface;
    }

    public boolean isAnnotation() {
        return isAnnotation;
    }

    public void setAnnotation(boolean annotation) {
        isAnnotation = annotation;
    }

    public boolean isEnum() {
        return isEnum;
    }

    public void setEnum(boolean anEnum) {
        isEnum = anEnum;
    }

    public boolean isAnonymousClass() {
        return isAnonymousClass;
    }

    public void setAnonymousClass(boolean anonymousClass) {
        isAnonymousClass = anonymousClass;
    }

    public boolean isArray() {
        return isArray;
    }

    public void setArray(boolean array) {
        isArray = array;
    }

    public boolean isLocalClass() {
        return isLocalClass;
    }

    public void setLocalClass(boolean localClass) {
        isLocalClass = localClass;
    }

    public boolean isMemberClass() {
        return isMemberClass;
    }

    public void setMemberClass(boolean memberClass) {
        isMemberClass = memberClass;
    }

    public boolean isPrimitive() {
        return isPrimitive;
    }

    public void setPrimitive(boolean primitive) {
        isPrimitive = primitive;
    }

    public boolean isSynthetic() {
        return isSynthetic;
    }

    public void setSynthetic(boolean synthetic) {
        isSynthetic = synthetic;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
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

    public String[] getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(String[] interfaces) {
        this.interfaces = interfaces;
    }

    public String[] getSuperClass() {
        return superClass;
    }

    public void setSuperClass(String[] superClass) {
        this.superClass = superClass;
    }

    public FieldVO[] getFields() {
        return fields;
    }

    public void setFields(FieldVO[] fields) {
        this.fields = fields;
    }

}
