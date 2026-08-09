package com.taobao.arthas.core.command.model;

import java.util.Collection;

/**
 * {@code getstatic} 命令的结果模型，读取并展示静态字段值。
 * <p>
 * 构造方式多样：可直接携带字段值，或仅返回 {@link #matchedClasses} 提示用户
 * 在多个 ClassLoader 中消歧；{@link #classLoaderClass} 记录用户指定的 Loader 类型过滤。
 *
 * Data model of GetStaticCommand
 * @author gongdewei 2020/4/20
 */
public class GetStaticModel extends ResultModel {

    /** 匹配到的类（歧义时列表长度 &gt; 1） */
    private Collection<ClassVO> matchedClasses;
    /** 请求的静态字段名 */
    private String fieldName;
    /** 字段值及对象展开结果 */
    private ObjectVO field;
    /** 匹配到的 ClassLoader 候选 */
    private Collection<ClassLoaderVO> matchedClassLoaders;
    /** 用户通过 -c 等选项指定的 ClassLoader 类名 */
    private String classLoaderClass;

    public GetStaticModel() {
    }

    /** 成功读取单字段时的便捷构造，expand 控制 ObjectVO 递归深度 */
    public GetStaticModel(String fieldName, Object fieldValue, int expand) {
        this.fieldName = fieldName;
        this.field = new ObjectVO(fieldValue, expand);
    }

    /** 仅返回匹配类列表，表示需要用户进一步指定 ClassLoader */
    public GetStaticModel(Collection<ClassVO> matchedClasses) {
        this.matchedClasses = matchedClasses;
    }

    public ObjectVO getField() {
        return field;
    }

    public void setField(ObjectVO field) {
        this.field = field;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Collection<ClassVO> getMatchedClasses() {
        return matchedClasses;
    }

    public void setMatchedClasses(Collection<ClassVO> matchedClasses) {
        this.matchedClasses = matchedClasses;
    }

    public String getClassLoaderClass() {
        return classLoaderClass;
    }

    public GetStaticModel setClassLoaderClass(String classLoaderClass) {
        this.classLoaderClass = classLoaderClass;
        return this;
    }

    public Collection<ClassLoaderVO> getMatchedClassLoaders() {
        return matchedClassLoaders;
    }

    public GetStaticModel setMatchedClassLoaders(Collection<ClassLoaderVO> matchedClassLoaders) {
        this.matchedClassLoaders = matchedClassLoaders;
        return this;
    }

    @Override
    public String getType() {
        return "getstatic";
    }
}
