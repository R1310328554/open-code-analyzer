package org.keycloak.ssf.transmitter.metadata;

import org.keycloak.models.KeycloakSession;
import org.keycloak.ssf.metadata.TransmitterMetadata;
import org.keycloak.ssf.transmitter.SsfTransmitterProvider;
import org.keycloak.wellknown.WellKnownProvider;

/**
 * SSF（共享信号与事件）协议元数据的 Well-Known Provider 实现。
 * 负责通过 Keycloak Well-Known Provider 基础设施对外暴露 SSF 相关元数据。
 */
public class SsfTransmitterMetadataWellKnownProvider implements WellKnownProvider {

    /** 当前 Keycloak 会话，用于获取 SSF 发送方 Provider。 */
    protected final KeycloakSession session;

    /**
     * @param session 当前请求会话
     */
    public SsfTransmitterMetadataWellKnownProvider(KeycloakSession session) {
        this.session = session;
    }

    /** 返回 SSF 发送方元数据配置。 */
    @Override
    public TransmitterMetadata getConfig() {
        SsfTransmitterProvider ssfProvider = session.getProvider(SsfTransmitterProvider.class);
        TransmitterMetadata transmitterMetadata = ssfProvider.metadataService().getTransmitterMetadata();
        return transmitterMetadata;
    }

    /** 关闭时无资源需释放。 */
    @Override
    public void close() {
        // NOOP
    }

}
