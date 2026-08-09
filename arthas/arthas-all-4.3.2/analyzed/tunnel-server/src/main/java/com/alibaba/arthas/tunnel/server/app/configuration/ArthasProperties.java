package com.alibaba.arthas.tunnel.server.app.configuration;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.alibaba.arthas.tunnel.server.utils.InetAddressUtil;
import com.taobao.arthas.common.ArthasConstants;

/**
 * Arthas Tunnel Server 的配置属性，绑定 {@code arthas.*} 前缀。
 *
 * @author hengyunabc 2019-08-29
 *
 */
@Component
@ConfigurationProperties(prefix = "arthas")
public class ArthasProperties {

    /** Tunnel Server 监听与 WebSocket 相关配置 */
    private Server server;

    /** 内嵌 Redis 配置，主要用于测试环境 */
    private EmbeddedRedis embeddedRedis;

    /**
     * 是否启用详情页（apps.html / agents.html）。
     */
    private boolean enableDetailPages = false;

    /** 是否允许控制台页面在 iframe 中嵌入 */
    private boolean enableIframeSupport = true;

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public EmbeddedRedis getEmbeddedRedis() {
        return embeddedRedis;
    }

    public void setEmbeddedRedis(EmbeddedRedis embeddedRedis) {
        this.embeddedRedis = embeddedRedis;
    }

    public boolean isEnableDetailPages() {
        return enableDetailPages;
    }

    public void setEnableDetailPages(boolean enableDetailPages) {
        this.enableDetailPages = enableDetailPages;
    }

    public boolean isEnableIframeSupport() {
        return enableIframeSupport;
    }

    public void setEnableIframeSupport(boolean enableIframeSupport) {
        this.enableIframeSupport = enableIframeSupport;
    }

    /** Tunnel Server 网络监听与客户端连接地址配置 */
    public static class Server {
        /**
         * Tunnel Server 监听地址
         */
        private String host;
        /** 监听端口 */
        private int port;
        /** 是否启用 SSL */
        private boolean ssl;
        /** WebSocket 路径，默认使用 Arthas 常量 */
        private String path = ArthasConstants.DEFAULT_WEBSOCKET_PATH;

        /**
         * 客户端连接的地址。也用于保存到redis里，当部署tunnel server集群里需要。不配置则会自动获取
         */
        private String clientConnectHost = InetAddressUtil.getInetAddress();

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public boolean isSsl() {
            return ssl;
        }

        public void setSsl(boolean ssl) {
            this.ssl = ssl;
        }

        public String getClientConnectHost() {
            return clientConnectHost;
        }

        public void setClientConnectHost(String clientConnectHost) {
            this.clientConnectHost = clientConnectHost;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

    }

    /**
     * 内嵌 Redis 配置，便于本地或测试环境启动集群存储依赖。
     *
     * @author hengyunabc 2020-11-03
     *
     */
    public static class EmbeddedRedis {
        /** 是否启用内嵌 Redis */
        private boolean enabled = false;
        /** Redis 绑定地址 */
        private String host = "127.0.0.1";
        /** Redis 端口 */
        private int port = 6379;
        /** 额外 Redis 启动参数 */
        private List<String> settings = new ArrayList<String>();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public List<String> getSettings() {
            return settings;
        }

        public void setSettings(List<String> settings) {
            this.settings = settings;
        }
    }

}
