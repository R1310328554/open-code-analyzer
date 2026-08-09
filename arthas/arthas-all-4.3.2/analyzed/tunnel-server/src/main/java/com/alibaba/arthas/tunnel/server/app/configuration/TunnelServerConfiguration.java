package com.alibaba.arthas.tunnel.server.app.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.arthas.tunnel.server.TunnelServer;
import com.alibaba.arthas.tunnel.server.cluster.TunnelClusterStore;

/**
 * {@link TunnelServer} Bean 自动配置：从 {@link ArthasProperties} 注入监听参数与可选集群存储。
 *
 * @author hengyunabc 2020-10-27
 *
 */
@Configuration
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class TunnelServerConfiguration {

    @Autowired
    ArthasProperties arthasProperties;

    /**
     * 创建 Tunnel Server 实例，Spring 容器生命周期内自动 start/stop。
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    public TunnelServer tunnelServer(@Autowired(required = false) TunnelClusterStore tunnelClusterStore) {
        TunnelServer tunnelServer = new TunnelServer();

        tunnelServer.setHost(arthasProperties.getServer().getHost());
        tunnelServer.setPort(arthasProperties.getServer().getPort());
        tunnelServer.setSsl(arthasProperties.getServer().isSsl());
        tunnelServer.setPath(arthasProperties.getServer().getPath());
        tunnelServer.setClientConnectHost(arthasProperties.getServer().getClientConnectHost());
        // 集群模式下注入路由存储，单机可省略
        if (tunnelClusterStore != null) {
            tunnelServer.setTunnelClusterStore(tunnelClusterStore);
        }
        return tunnelServer;
    }

}
