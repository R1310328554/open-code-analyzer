package com.taobao.arthas.common;

/**
 * Arthas 全局常量：Netty 本地地址、HTTP/Telnet 端口、会话与认证相关键名。
 *
 * @author hengyunabc 2020-09-02
 */
public class ArthasConstants {
    /**
     * JVM 内 Netty 本地通道地址名，用于 Agent 与 Boot 进程通信。
     *
     * @see io.netty.channel.local.LocalAddress
     * @see io.netty.channel.local.LocalChannel
     */
    public static final String NETTY_LOCAL_ADDRESS = "arthas-netty-LocalAddress";

    /** HTTP 请求体最大长度（10MB） */
    public static final int MAX_HTTP_CONTENT_LENGTH = 1024 * 1024 * 10;

    public static final String ARTHAS_OUTPUT = "arthas-output";

    public static final String APP_NAME = "app-name";

    public static final String PROJECT_NAME = "project.name";
    public static final String SPRING_APPLICATION_NAME = "spring.application.name";

    /** 默认 Telnet 监听端口 */
    public static final int TELNET_PORT = 3658;

    public static final String DEFAULT_WEBSOCKET_PATH = "/ws";
    public static final int WEBSOCKET_IDLE_SECONDS = 10;

    /** HTTP 会话 Cookie 键名 */
    public static final String ASESSION_KEY = "asession";

    /** 默认登录用户名 */
    public static final String DEFAULT_USERNAME = "arthas";
    public static final String SUBJECT_KEY = "subject";
    public static final String AUTH = "auth";
    public static final String USERNAME_KEY = "username";
    public static final String PASSWORD_KEY = "password";
    public static final String USER_ID_KEY = "userId";
}
