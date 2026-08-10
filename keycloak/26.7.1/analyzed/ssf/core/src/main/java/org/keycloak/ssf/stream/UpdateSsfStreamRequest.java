package org.keycloak.ssf.stream;

/**
 * 更新 SSF 流配置的请求体，继承 {@link SsfStreamRepresentation} 的全部可配置字段。
 * <p>对应 SSF Management API 的 update-stream 操作，通常以 PATCH 语义部分更新。</p>
 */
public class UpdateSsfStreamRequest extends SsfStreamRepresentation {
}
