package com.alibaba.arthas.tunnel.server.endpoint;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import com.alibaba.arthas.tunnel.server.TunnelServer;
import com.alibaba.arthas.tunnel.server.app.configuration.ArthasProperties;

/**
 * Spring Boot Actuator 端点：暴露 Tunnel Server 版本、配置及当前 agent/客户端连接快照。
 */
@Endpoint(id = "arthas")
public class ArthasEndpoint {

    @Autowired
    ArthasProperties arthasProperties;
    @Autowired
    TunnelServer tunnelServer;

    /**
     * 读取端点数据：版本号、配置属性、在线 agent 与客户端连接信息。
     */
    @ReadOperation
    public Map<String, Object> invoke() {
        Map<String, Object> result = new HashMap<>(4);

        result.put("version", this.getClass().getPackage().getImplementationVersion());
        result.put("properties", arthasProperties);

        result.put("agents", tunnelServer.getAgentInfoMap());
        result.put("clientConnections", tunnelServer.getClientConnectionInfoMap());

        return result;
    }

}
