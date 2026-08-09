package com.alibaba.arthas.nat.agent.common.constants;

/**
 * Native Agent 集群组件共用的 HTTP 与路径常量。
 *
 * @description: hello world
 * @author：flzjkl
 * @date: 2024-09-22 0:47
 */
public class NativeAgentConstants {

    /** Arthas Server 默认 HTTP 端口 */
    public static final int ARTHAS_SERVER_HTTP_PORT = 8563;

    /** HTTP 请求体最大长度（10MB） */
    public static final int MAX_HTTP_CONTENT_LENGTH = 1024 * 1024 * 10;

    /** Native Agent 注册与 API 的路径前缀 */
    public static final String NATIVE_AGENT_KEY = "/native-agent";

    /** Native Agent 代理层 API 的路径前缀 */
    public static final String NATIVE_AGENT_PROXY_KEY = "/native-agent-proxy";

}
