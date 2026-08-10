package org.keycloak.ssf.stream;

/**
 * 完全替换 SSF 流配置的请求体，语义上等价于 PUT 式全量更新。
 * <p>继承 {@link UpdateSsfStreamRequest}，与 SSF Management API 的 replace-stream 操作对应。</p>
 */
public class ReplaceSsfStreamRequest extends UpdateSsfStreamRequest {
}
