package com.taobao.arthas.core.command.model;

import java.util.Collection;
import java.util.Map;

/**
 * logger 命令的结构化结果：各 logger 的配置快照及 ClassLoader 匹配信息。
 * <p>
 * {@link #loggerInfoMap} 外层 key 为 logger 名称，内层 Map 存放级别、appender、
 * 类来源等键值（由 {@link com.taobao.arthas.core.command.logger.LoggerHelper} 定义）；
 * 多 ClassLoader 场景下 {@link #matchedClassLoaders} 提示用户指定 -c。
 *
 * @author gongdewei 2020/4/22
 */
public class LoggerModel extends ResultModel {

    /** logger 名 → 属性键值对（LEVEL、APPENDER、LOGGER 等） */
    private Map<String, Map<String, Object>> loggerInfoMap;
    /** 命中多个 ClassLoader 时的候选列表 */
    private Collection<ClassLoaderVO> matchedClassLoaders;
    /** 用户通过 -c 指定的 ClassLoader 类名 */
    private String classLoaderClass;

    public LoggerModel() {
    }

    public LoggerModel(Map<String, Map<String, Object>> loggerInfoMap) {
        this.loggerInfoMap = loggerInfoMap;
    }

    public Map<String, Map<String, Object>> getLoggerInfoMap() {
        return loggerInfoMap;
    }

    public void setLoggerInfoMap(Map<String, Map<String, Object>> loggerInfoMap) {
        this.loggerInfoMap = loggerInfoMap;
    }

    public String getClassLoaderClass() {
        return classLoaderClass;
    }

    public LoggerModel setClassLoaderClass(String classLoaderClass) {
        this.classLoaderClass = classLoaderClass;
        return this;
    }

    public Collection<ClassLoaderVO> getMatchedClassLoaders() {
        return matchedClassLoaders;
    }

    public LoggerModel setMatchedClassLoaders(Collection<ClassLoaderVO> matchedClassLoaders) {
        this.matchedClassLoaders = matchedClassLoaders;
        return this;
    }

    @Override
    public String getType() {
        return "logger";
    }

}
