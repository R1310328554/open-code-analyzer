package com.taobao.arthas.core.command.model;

import java.util.List;

/**
 * dashboard 命令中的 Tomcat 监控摘要：连接器 QPS/RT 与线程池占用。
 * <p>
 * 通过 JMX 或 Tomcat 内部 MBean 采集；无 Tomcat 或 MBean 不可用时对应列表可能为空。
 *
 * @author gongdewei 2020/4/23
 */
public class TomcatInfoVO {

    /** 各 HTTP 连接器（Coyote Connector）的吞吐与错误率统计 */
    private List<ConnectorStats> connectorStats;
    /** Executor / 线程池 busy 与 capacity 快照 */
    private List<ThreadPool> threadPools;

    public TomcatInfoVO() {
    }

    public List<ConnectorStats> getConnectorStats() {
        return connectorStats;
    }

    public void setConnectorStats(List<ConnectorStats> connectorStats) {
        this.connectorStats = connectorStats;
    }

    public List<ThreadPool> getThreadPools() {
        return threadPools;
    }

    public void setThreadPools(List<ThreadPool> threadPools) {
        this.threadPools = threadPools;
    }

    /** 单个 Connector 的请求指标（名称通常含端口与协议） */
    public static class ConnectorStats {
        /** Connector 名称或 JMX ObjectName 摘要 */
        private String name;
        /** 每秒请求数（采样窗口内估算） */
        private double qps;
        /** 平均响应时间（毫秒） */
        private double rt;
        /** 错误率（0~1 或百分比，取决于采集端归一化） */
        private double error;
        /** 累计接收字节数 */
        private long received;
        /** 累计发送字节数 */
        private long sent;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getQps() {
            return qps;
        }

        public void setQps(double qps) {
            this.qps = qps;
        }

        public double getRt() {
            return rt;
        }

        public void setRt(double rt) {
            this.rt = rt;
        }

        public double getError() {
            return error;
        }

        public void setError(double error) {
            this.error = error;
        }

        public long getReceived() {
            return received;
        }

        public void setReceived(long received) {
            this.received = received;
        }

        public long getSent() {
            return sent;
        }

        public void setSent(long sent) {
            this.sent = sent;
        }
    }

    /** Tomcat 线程池 busy / max 线程数 */
    public static class ThreadPool {
        /** 线程池名称（如 http-nio-8080） */
        private String name;
        /** 当前活跃（busy）线程数 */
        private long busy;
        /** 池容量或最大线程数 */
        private long total;

        public ThreadPool() {
        }

        public ThreadPool(String name, long busy, long total) {
            this.name = name;
            this.busy = busy;
            this.total = total;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getBusy() {
            return busy;
        }

        public void setBusy(long busy) {
            this.busy = busy;
        }

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }
    }
}
