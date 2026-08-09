package com.taobao.arthas.core.command.model;

import java.util.Map;
import java.util.TreeMap;

/**
 * sysenv 命令的结构化结果：封装进程环境变量键值对。
 * <p>
 * 内部使用 {@link TreeMap} 保证 key 按字典序排列，便于终端与 Web 控制台对比展示；
 * 支持批量 {@link #putAll} 或单条 {@link #put} 构造。
 *
 * @author gongdewei 2020/4/2
 */
public class SystemEnvModel extends ResultModel {

    /** 环境变量映射（key 为变量名，value 为字符串值；未设置时可能不在 map 中） */
    private Map<String, String> env = new TreeMap<String, String>();

    public SystemEnvModel() {
    }

    /** 从已有 Map 拷贝全部条目（泛型擦除接受任意 Map） */
    public SystemEnvModel(Map env) {
        this.putAll(env);
    }

    /** 构造仅含单个键值对的结果，常用于 sysenv -n 查询 */
    public SystemEnvModel(String name, String value) {
        this.put(name, value);
    }

    public Map<String, String> getEnv() {
        return env;
    }

    public String put(String key, String value) {
        return env.put(key, value);
    }

    public void putAll(Map m) {
        env.putAll(m);
    }

    @Override
    public String getType() {
        return "sysenv";
    }
}
