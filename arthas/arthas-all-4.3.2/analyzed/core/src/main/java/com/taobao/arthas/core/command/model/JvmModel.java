package com.taobao.arthas.core.command.model;

import java.util.*;

/**
 * jvm 命令的结构化结果：按分组组织的 JVM 运行时指标集合。
 * <p>
 * 内部使用 {@link LinkedHashMap} 保持分组顺序，外层 Map 为线程安全包装，
 * 便于命令执行过程中并发追加条目；{@link #group} 懒创建各组的条目列表。
 *
 * @author gongdewei 2020/4/24
 */
public class JvmModel extends ResultModel {

    /** 分组名 → 该组下的 JvmItemVO 列表（如 RUNTIME、THREAD、MEMORY） */
    private Map<String, List<JvmItemVO>> jvmInfo;

    public JvmModel() {
        // LinkedHashMap 保序 + synchronizedMap 支持多线程追加条目
        jvmInfo = Collections.synchronizedMap(new LinkedHashMap<String, List<JvmItemVO>>());
    }

    @Override
    public String getType() {
        return "jvm";
    }

    /** 向指定分组追加一条无说明的指标，支持链式调用 */
    public JvmModel addItem(String group, String name, Object value) {
        this.addItem(group, name, value, null);
        return this;
    }

    /** 向指定分组追加一条带可选 desc 的指标 */
    public JvmModel  addItem(String group, String name, Object value, String desc) {
        this.group(group).add(new JvmItemVO(name, value, desc));
        return this;
    }

    /**
     * 获取或创建指定分组的条目列表。
     * 对 jvmInfo 的 get/put 在 synchronized(this) 内完成，避免重复创建列表。
     */
    public List<JvmItemVO> group(String group) {
        synchronized (this) {
            List<JvmItemVO> list = jvmInfo.get(group);
            if (list == null) {
                list = new ArrayList<JvmItemVO>();
                jvmInfo.put(group, list);
            }
            return list;
        }
    }

    public Map<String, List<JvmItemVO>> getJvmInfo() {
        return jvmInfo;
    }

}
