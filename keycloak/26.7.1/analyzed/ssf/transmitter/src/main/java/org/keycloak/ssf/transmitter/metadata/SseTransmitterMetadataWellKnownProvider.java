package org.keycloak.ssf.transmitter.metadata;

import java.util.stream.Collectors;

import org.keycloak.models.KeycloakSession;
import org.keycloak.ssf.metadata.TransmitterMetadata;

/**
 * 旧版 SSE（Shared Signals and Events）协议元数据的 Well-Known 提供者实现。
 * 负责通过 Keycloak Well-Known 提供者基础设施暴露 SSE 相关元数据。
 */
public class SseTransmitterMetadataWellKnownProvider extends SsfTransmitterMetadataWellKnownProvider {

    public SseTransmitterMetadataWellKnownProvider(KeycloakSession session) {
        super(session);
    }

    @Override
    public TransmitterMetadata getConfig() {

        TransmitterMetadata sseMetadata = new TransmitterMetadata(super.getConfig());

        // Remove "new" PUSH and POLL delivery methods all urn:... URIs
        sseMetadata.setDeliveryMethodSupported(sseMetadata.getDeliveryMethodSupported()
                .stream()
                .filter(dm -> !dm.startsWith("urn:"))
                .collect(Collectors.toSet()));

        // Remove unsupported fields.
        sseMetadata.setDefaultSubjects(null);
        sseMetadata.setSpecVersion(null);
        sseMetadata.setStatusEndpoint(null);
        sseMetadata.setAuthorizationSchemes(null);

        return sseMetadata;
    }

    @Override
    public void close() {
        // NOOP
    }

}
