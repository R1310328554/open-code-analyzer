package com.taobao.arthas.core.command.model;

import java.util.HashMap;
import java.util.Map;

/**
 * sysprop 命令的结构化结果：封装 JVM 系统属性（{@code System.getProperties()}）键值对。
 * <p>
 * 与 {@link SystemEnvModel} 对称，但使用 {@link HashMap} 存储（sysprop 通常不要求排序）；
 * 单条查询时 value 可能为 null（属性已定义但无值）。
 *
 * @author gongdewei 2020/4/2
 */
public class SystemPropertyModel extends ResultModel {

    /** 系统属性映射 */
    private Map<String, String> props = new HashMap<String, String>();

    public SystemPropertyModel() {
    }

    /** 从已有 Map 批量导入属性 */
    public SystemPropertyModel(Map props) {
        this.putAll(props);
    }

    /** 构造单属性查询结果（sysprop key） */
    public SystemPropertyModel(String name, String value) {
        this.put(name, value);
    }

    public Map<String, String> getProps() {
        return props;
    }

    public String put(String key, String value) {
        return props.put(key, value);
    }

    public void putAll(Map m) {
        props.putAll(m);
    }

    @Override
    public String getType() {
        return "sysprop";
    }
}
