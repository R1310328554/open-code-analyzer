package org.keycloak.representations.info;

/**
 * CPU 信息的 REST 表示，用于服务器信息端点报告可用处理器数量。
 */
public class CpuInfoRepresentation {

    /** 可用处理器（逻辑核心）数量。 */
    protected long processorCount;

    /** 从当前 JVM 运行时采集 CPU 信息并创建实例。 */
    public static CpuInfoRepresentation create() {
        Runtime runtime = Runtime.getRuntime();
        CpuInfoRepresentation rep = new CpuInfoRepresentation();
        rep.setProcessorCount(runtime.availableProcessors());
        return rep;
    }

    /** @return 处理器数量 */
    public long getProcessorCount() {
        return processorCount;
    }

    /** @param processorCount 处理器数量 */
    public void setProcessorCount(long processorCount) {
        this.processorCount = processorCount;
    }
}
