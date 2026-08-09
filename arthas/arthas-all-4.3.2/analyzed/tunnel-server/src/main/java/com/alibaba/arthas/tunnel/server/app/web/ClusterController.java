package com.alibaba.arthas.tunnel.server.app.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.arthas.tunnel.server.AgentClusterInfo;
import com.alibaba.arthas.tunnel.server.TunnelServer;
import com.alibaba.arthas.tunnel.server.cluster.TunnelClusterStore;

/**
 * 集群路由 API：根据 agentId 查询其当前连接的 Tunnel Server 地址。
 *
 * @author hengyunabc 2020-10-27
 *
 */
@Controller
public class ClusterController {
    private final static Logger logger = LoggerFactory.getLogger(ClusterController.class);

    @Autowired
    TunnelServer tunnelServer;

    /**
     * 查找指定 agent 所连接的 Tunnel Server 主机地址。
     *
     * @param agentId agent 唯一标识
     * @return 客户端连接地址，未找到时返回空字符串
     */
    @RequestMapping(value = "/api/cluster/findHost")
    @ResponseBody
    public String execute(@RequestParam(value = "agentId", required = true) String agentId) {
        TunnelClusterStore tunnelClusterStore = tunnelServer.getTunnelClusterStore();

        String host = null;
        if (tunnelClusterStore != null) {
            AgentClusterInfo info = tunnelClusterStore.findAgent(agentId);
            host = info.getClientConnectHost();
        }

        if (host == null) {
            host = "";
        }

        logger.info("arthas cluster findHost, agentId: {}, host: {}", agentId, host);

        return host;
    }
}
