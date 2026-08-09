package com.taobao.arthas.core.command.model;

/**
 * <pre>
 * 包装一层，解决json输出问题
 * https://github.com/alibaba/arthas/issues/2261
 * </pre>
 * <p>
 * 将任意 Java 对象与展开深度 {@link #expand} 绑定，供 JSON 序列化时控制对象展开层级，
 * 避免直接输出复杂对象结构导致的序列化问题。
 * 
 * @author hengyunabc 2022-08-24
 *
 */
public class ObjectVO {
    /** 被包装的原始对象。 */
    private Object object;
    /** 对象展开深度，null 时使用默认值。 */
    private Integer expand;

    public ObjectVO(Object object, Integer expand) {
        this.object = object;
        this.expand = expand;
    }

    /**
     * 将对象数组批量包装为 ObjectVO 数组。
     * @param objects 原始对象数组，null 时返回空数组
     * @param expand 统一的展开深度
     */
    public static ObjectVO[] array(Object[] objects, Integer expand) {
        if (objects == null) {
            return new ObjectVO[0];
        }
        ObjectVO[] result = new ObjectVO[objects.length];
        for (int i = 0; i < objects.length; ++i) {
            result[i] = new ObjectVO(objects[i], expand);
        }
        return result;
    }

    /** 返回展开深度，未设置时默认为 1。 */
    public int expandOrDefault() {
        if (expand != null) {
            return expand;
        }
        return 1;
    }

    /** 是否需要展开对象（expand 非 null 且大于 0）。 */
    public boolean needExpand() {
        return null != expand && expand > 0;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Integer getExpand() {
        return expand;
    }

    public void setExpand(Integer expand) {
        this.expand = expand;
    }
}
