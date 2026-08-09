package com.alibaba.arthas.tunnel.common;

/**
 * tunnel 协议 URI query 参数名常量，与 {@link MethodConstants} 配合描述 WebSocket 信令。
 *
 * @author hengyunabc 2020-10-22
 *
 */
public class URIConstans {

    /** 操作类型参数名，取值见 {@link MethodConstants} */
    public static final String METHOD = "method";
    /** 应答 URI 使用的伪 scheme，用于组装 TextWebSocketFrame 文本 */
    public static final String RESPONSE = "response";

    /** agent 在 tunnel server 上的唯一标识 */
    public static final String ID = "id";

    /**
     * tunnel server 用于区分浏览器侧待中继连接的临时 id
     */
    public static final String CLIENT_CONNECTION_ID = "clientConnectionId";

    /**
     * tunnel server 向 tunnel client 请求 HTTP 代理时的目标 URL
     * 
     * @see com.alibaba.arthas.tunnel.common.MethodConstants#HTTP_PROXY
     */
    public static final String TARGET_URL = "targetUrl";

    /**
     * 标识一次 proxy 请求，随机生成
     */
    public static final String PROXY_REQUEST_ID = "requestId";

    /**
     * proxy 响应体，经 Base64 编码的 {@link SimpleHttpResponse} 序列化字节
     */
    public static final String PROXY_RESPONSE_DATA = "responseData";
 
        /** agent 注册时上报的 Arthas 版本 */
    public static final String ARTHAS_VERSION = "arthasVersion";

        /** 应用名，可选；用于生成带前缀的 agent id */
    public static final String APP_NAME = "appName";
}
