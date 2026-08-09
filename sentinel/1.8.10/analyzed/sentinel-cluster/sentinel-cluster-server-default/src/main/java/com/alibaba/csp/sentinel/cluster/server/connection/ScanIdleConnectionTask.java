package com.alibaba.csp.sentinel.cluster.server.connection;

import java.util.List;

import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import com.alibaba.csp.sentinel.cluster.server.config.ServerTransportConfig;
import com.alibaba.csp.sentinel.log.RecordLog;

/**
 * 周期性扫描并关闭长空闲连接的 Runnable 任务。
 * <p>由 {@link ConnectionPool} 调度，依据 {@link ClusterServerConfigManager#getIdleSeconds()}
 * 判断连接是否超时未读，超时则记录日志并调用 {@link Connection#close()}。
 *
 * @author xuyue
 * @author Eric Zhao
 * @since 1.4.0
 */
public class ScanIdleConnectionTask implements Runnable {

    private final ConnectionPool connectionPool;

    public ScanIdleConnectionTask(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Override
    public void run() {
        try {
            int idleSeconds = ClusterServerConfigManager.getIdleSeconds();
            long idleTimeMillis = idleSeconds * 1000;
            if (idleTimeMillis < 0) {
                // 配置无效时回退到默认空闲超时。
                idleTimeMillis = ServerTransportConfig.DEFAULT_IDLE_SECONDS * 1000;
            }
            long now = System.currentTimeMillis();
            List<Connection> connections = connectionPool.listAllConnection();
            for (Connection conn : connections) {
                // 超过空闲阈值则关闭连接。
                if ((now - conn.getLastReadTime()) > idleTimeMillis) {
                    RecordLog.info("[ScanIdleConnectionTask] The connection <{}:{}> has been idle for <{}>s. It will be closed now.",
                        conn.getRemoteIP(), conn.getRemotePort(), idleSeconds);
                    conn.close();
                }
            }
        } catch (Throwable t) {
            RecordLog.warn("[ScanIdleConnectionTask] Failed to clean-up idle tasks", t);
        }
    }
}
