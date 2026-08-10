package org.keycloak.ssf.transmitter.support;

import org.keycloak.ssf.Ssf;

/** SSF 发送方 REST 端点 URL 构建工具。 */
public final class SsfTransmitterUrls {

    /** 发送方基础路径后缀（相对 issuer）。 */
    public static final String SSF_TRANSMITTER_BASE_PATH_SUFFIX = "%s/%s".formatted(Ssf.SSF_REALM_RESOURCE_PATH, Ssf.SSF_TRANSMITTER_PATH);

    private SsfTransmitterUrls() {
    }

    /** @param issuerUrl 领域 issuer URL @return 发送方 API 基础路径 */
    public static String getSsfTransmitterBasePath(String issuerUrl) {
        return issuerUrl + "/" + SSF_TRANSMITTER_BASE_PATH_SUFFIX;
    }

    /** @param issuerUrl 领域 issuer URL @return 流列表端点 URL */
    public static String getStreamsEndpointUrl(String issuerUrl) {
        return getSsfTransmitterBasePath(issuerUrl) + "/streams";
    }

    /** @param issuerUrl 领域 issuer URL @return 流状态端点 URL */
    public static String getStreamStatusEndpointUrl(String issuerUrl) {
        return getSsfTransmitterBasePath(issuerUrl) + "/streams/status";
    }

    /** @param issuerUrl 领域 issuer URL @return 流验证端点 URL */
    public static String getStreamVerificationEndpointUrl(String issuerUrl) {
        return getSsfTransmitterBasePath(issuerUrl) + "/verify";
    }

    /** @param issuerUrl 领域 issuer URL @return 添加主题端点 URL */
    public static String getAddSubjectEndpointUrl(String issuerUrl) {
        return getSsfTransmitterBasePath(issuerUrl) + "/subjects/add";
    }

    /** @param issuerUrl 领域 issuer URL @return 移除主题端点 URL */
    public static String getRemoveSubjectEndpointUrl(String issuerUrl) {
        return getSsfTransmitterBasePath(issuerUrl) + "/subjects/remove";
    }

    /**
     * 为给定接收方客户端与流 id 构建 poll 端点 URL。发送方按 SSF §6.1.2 将此 URL 写入
     * 流创建响应的 {@code delivery.endpoint_url}（规范要求发送方而非接收方拥有 poll 投递 URL）。
     *
     * @param issuerUrl 领域 issuer URL
     * @param clientId 接收方 OAuth client_id
     * @param streamId 流标识
     */
    public static String getPollEndpointUrl(String issuerUrl, String clientId, String streamId) {
        return getSsfTransmitterBasePath(issuerUrl)
                + "/receivers/" + clientId
                + "/streams/" + streamId
                + "/poll";
    }
}
