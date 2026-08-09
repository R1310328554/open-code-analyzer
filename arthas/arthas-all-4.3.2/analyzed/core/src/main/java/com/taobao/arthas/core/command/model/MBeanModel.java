package com.taobao.arthas.core.command.model;

import javax.management.MBeanInfo;
import java.util.List;
import java.util.Map;

/**
 * mbean 命令的结构化结果：JMX MBean 枚举、元数据及属性快照。
 * <p>
 * 仅列出名称时使用 {@link #mbeanNames}；
 * 带 -m 时填充 {@link #mbeanMetadata}（MBeanInfo）；
 * 带属性读取时 {@link #mbeanAttribute} 以 ObjectName 为 key 映射属性列表。
 *
 * @author gongdewei 2020/4/26
 */
public class MBeanModel extends ResultModel {

    /** 匹配到的 MBean ObjectName 字符串列表 */
    private List<String> mbeanNames;

    /** ObjectName → MBeanInfo 元数据（属性/操作/通知描述） */
    private Map<String, MBeanInfo> mbeanMetadata;

    /** ObjectName → 已读取的属性值列表 */
    private Map<String, List<MBeanAttributeVO>> mbeanAttribute;

    public MBeanModel() {
    }

    public MBeanModel(List<String> mbeanNames) {
        this.mbeanNames = mbeanNames;
    }

    @Override
    public String getType() {
        return "mbean";
    }

    public List<String> getMbeanNames() {
        return mbeanNames;
    }

    public void setMbeanNames(List<String> mbeanNames) {
        this.mbeanNames = mbeanNames;
    }

    public Map<String, MBeanInfo> getMbeanMetadata() {
        return mbeanMetadata;
    }

    public void setMbeanMetadata(Map<String, MBeanInfo> mbeanMetadata) {
        this.mbeanMetadata = mbeanMetadata;
    }

    public Map<String, List<MBeanAttributeVO>> getMbeanAttribute() {
        return mbeanAttribute;
    }

    public void setMbeanAttribute(Map<String, List<MBeanAttributeVO>> mbeanAttribute) {
        this.mbeanAttribute = mbeanAttribute;
    }
}
