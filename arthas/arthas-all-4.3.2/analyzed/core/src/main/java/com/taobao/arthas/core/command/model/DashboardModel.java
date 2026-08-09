package com.taobao.arthas.core.command.model;

import java.util.List;
import java.util.Map;

/**
 * {@code dashboard} 命令的聚合结果模型，一次性呈现 JVM 运行态快照。
 * <p>
 * 包含线程、内存分区、GC 统计、运行时信息与 Tomcat 指标（若存在）；
 * {@link #getType()} 固定返回 {@code "dashboard"} 供客户端路由渲染。
 *
 * Model of 'dashboard' command
 * @author gongdewei 2020/4/22
 */
public class DashboardModel extends ResultModel {
    /** 线程列表及 CPU 占用等摘要 */
    private List<ThreadVO> threads;
    /** 按内存区域（heap/non-heap 等）分组的内存条目 */
    private Map<String, List<MemoryEntryVO>> memoryInfo;
    /** 各 GC 收集器的次数与耗时 */
    private List<GcInfoVO> gcInfos;
    /** JVM 版本、启动参数、类加载数等运行时信息 */
    private RuntimeInfoVO runtimeInfo;
    /** 内嵌 Tomcat 的连接与请求统计（非 Tomcat 环境可能为空） */
    private TomcatInfoVO tomcatInfo;

    @Override
    public String getType() {
        return "dashboard";
    }

    public List<ThreadVO> getThreads() {
        return threads;
    }

    public void setThreads(List<ThreadVO> threads) {
        this.threads = threads;
    }

    public Map<String, List<MemoryEntryVO>> getMemoryInfo() {
        return memoryInfo;
    }

    public void setMemoryInfo(Map<String, List<MemoryEntryVO>> memoryInfo) {
        this.memoryInfo = memoryInfo;
    }

    public List<GcInfoVO> getGcInfos() {
        return gcInfos;
    }

    public void setGcInfos(List<GcInfoVO> gcInfos) {
        this.gcInfos = gcInfos;
    }

    public RuntimeInfoVO getRuntimeInfo() {
        return runtimeInfo;
    }

    public void setRuntimeInfo(RuntimeInfoVO runtimeInfo) {
        this.runtimeInfo = runtimeInfo;
    }

    public TomcatInfoVO getTomcatInfo() {
        return tomcatInfo;
    }

    public void setTomcatInfo(TomcatInfoVO tomcatInfo) {
        this.tomcatInfo = tomcatInfo;
    }
}
