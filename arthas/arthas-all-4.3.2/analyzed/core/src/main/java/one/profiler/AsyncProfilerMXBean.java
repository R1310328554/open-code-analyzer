/*
 * Copyright The async-profiler authors
 * SPDX-License-Identifier: Apache-2.0
 */

package one.profiler;

/**
 * 供 JMX 暴露的 AsyncProfiler 管理接口。
 * <p>
 * 注册 MBean 示例：
 *
 * <pre>{@code
 *     ManagementFactory.getPlatformMBeanServer().registerMBean(
 *             AsyncProfiler.getInstance(),
 *             new ObjectName("one.profiler:type=AsyncProfiler")
 *     );
 * }</pre>
 */
public interface AsyncProfilerMXBean {
    /** JMX ObjectName 字符串常量。 */
    String OBJECT_NAME = "one.profiler:type=AsyncProfiler";

    /** 启动采样并重置数据，见 {@link AsyncProfiler#start(String, long)}。 */
    void start(String event, long interval) throws IllegalStateException;
    /** 恢复采样不重置数据。 */
    void resume(String event, long interval) throws IllegalStateException;
    /** 停止采样。 */
    void stop() throws IllegalStateException;

    /** 已采集样本数。 */
    long getSamples();
    /** 代理版本号。 */
    String getVersion();

    /** 执行 agent 兼容命令。 */
    String execute(String command) throws IllegalArgumentException, IllegalStateException, java.io.IOException;

    /** 导出 collapsed 格式火焰图。 */
    String dumpCollapsed(Counter counter);
    /** 导出调用栈文本。 */
    String dumpTraces(int maxTraces);
    /** 导出 flat profile。 */
    String dumpFlat(int maxMethods);
    /** 导出 OTLP 格式数据（不稳定 API）。 */
    byte[] dumpOtlp();
}
