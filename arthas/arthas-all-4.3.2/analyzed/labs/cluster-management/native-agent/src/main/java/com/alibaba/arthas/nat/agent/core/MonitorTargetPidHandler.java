package com.alibaba.arthas.nat.agent.core;

import com.alibaba.arthas.nat.agent.common.constants.NativeAgentConstants;
import com.taobao.arthas.common.SocketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 目标 PID 监控处理器：检查 Arthas HTTP 端口占用情况，必要时触发 JVM Attach。
 *
 * @description: monitor target pid
 * @author：flzjkl
 * @date: 2024-09-22 7:12
 */
public class MonitorTargetPidHandler {

    private static final Logger logger = LoggerFactory.getLogger(MonitorTargetPidHandler.class);

    /**
     * 确保目标 PID 上的 Arthas Agent 已就绪：端口空闲则 Attach，已被同 PID 占用则直接成功。
     *
     * @param pid 待监控的 Java 进程 PID
     * @return 监控就绪返回 true；端口被其他进程占用返回 false
     */
    public static boolean monitorTargetPid (Integer pid)  {
        long tcpListenProcess = SocketUtils.findTcpListenProcess(NativeAgentConstants.ARTHAS_SERVER_HTTP_PORT);

        if (tcpListenProcess == -1) {
            // 端口未被监听，Attach 目标 JVM
            try {
                JvmAttachmentHandler.attachJvmByPid(pid);
                return true;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (tcpListenProcess == pid) {
            // 目标 PID 已在监听，无需重复 Attach
            return true;
        }

        if (tcpListenProcess != pid) {
            String errorMsg = "Target port: " + NativeAgentConstants.ARTHAS_SERVER_HTTP_PORT
                    + " has been occupied by pid: " + tcpListenProcess;
            logger.error(errorMsg);
            return false;
        }

        return false;
    }

}
