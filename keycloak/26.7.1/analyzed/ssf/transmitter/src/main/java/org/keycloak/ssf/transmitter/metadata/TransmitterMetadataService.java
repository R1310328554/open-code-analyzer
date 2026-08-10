package org.keycloak.ssf.transmitter.metadata;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.keycloak.models.KeycloakSession;
import org.keycloak.ssf.Ssf;
import org.keycloak.ssf.metadata.TransmitterMetadata;
import org.keycloak.ssf.transmitter.SsfTransmitterConfig;
import org.keycloak.ssf.transmitter.support.SsfTransmitterUrls;

/**
 * 管理 SSF 发送方元数据的服务，组装并缓存 Well-Known 端点所需的配置信息。
 */
public class TransmitterMetadataService {

    /** 当前 Keycloak 会话。 */
    protected final KeycloakSession session;

    /** 根据会话生成 issuer URL 的函数。 */
    protected  final Function<KeycloakSession, String> issuerGenerator;

    /** 发送方 SPI 配置。 */
    protected final SsfTransmitterConfig transmitterConfig;

    public TransmitterMetadataService(KeycloakSession session,
                                      Function<KeycloakSession, String> issuerGenerator,
                                      SsfTransmitterConfig transmitterConfig) {
        this.session = session;
        this.issuerGenerator = issuerGenerator;
        this.transmitterConfig = transmitterConfig;
    }

    /**
     * 返回 SSF 发送方配置元数据，同一会话内缓存以避免重复构建。
     *
     * @return SSF 发送方配置元数据
     */
    public TransmitterMetadata getTransmitterMetadata() {

        TransmitterMetadata cached = (TransmitterMetadata)session.getAttribute("ssfTransmitterMetadata");
        if (cached != null) {
            return cached;
        }

        TransmitterMetadata transmitterMetadata = createTransmitterMetadata();
        session.setAttribute("ssfTransmitterMetadata", transmitterMetadata);

        return transmitterMetadata;
    }

    protected TransmitterMetadata createTransmitterMetadata() {

        TransmitterMetadata metadata = new TransmitterMetadata();

        metadata.setSpecVersion(Ssf.SSF_VERSION_1_0);

        String issuerUrl = issuerGenerator.apply(session);

        metadata.setIssuer(issuerUrl);
        metadata.setJwksUri(createJwksUri(issuerUrl));
        metadata.setDeliveryMethodSupported(createDeliveryMethods());

        // 流管理端点
        metadata.setConfigurationEndpoint(SsfTransmitterUrls.getStreamsEndpointUrl(issuerUrl));
        metadata.setStatusEndpoint(SsfTransmitterUrls.getStreamStatusEndpointUrl(issuerUrl));
        metadata.setVerificationEndpoint(SsfTransmitterUrls.getStreamVerificationEndpointUrl(issuerUrl));

        // 主体管理端点（仅在启用时对外公布）
        if (transmitterConfig.isSubjectManagementEnabled()) {
            metadata.setAddSubjectEndpoint(SsfTransmitterUrls.getAddSubjectEndpointUrl(issuerUrl));
            metadata.setRemoveSubjectEndpoint(SsfTransmitterUrls.getRemoveSubjectEndpointUrl(issuerUrl));
        }

        metadata.setAuthorizationSchemes(createAuthorizationSchemes());

        metadata.setDefaultSubjects(transmitterConfig.getDefaultSubjects().name());

        // critical_subject_members 告知接收方必须能解析的复合主体成员键
        //（如 "user"、"session"、"tenant"）。配置为空/null 时不输出该字段。
        Set<String> critical = transmitterConfig.getCriticalSubjectMembers();
        if (critical != null && !critical.isEmpty()) {
            metadata.setCriticalSubjectMembers(new LinkedHashSet<>(critical));
        }

        return metadata;
    }

    protected Set<String> createDeliveryMethods() {
        // 规范标准的 SSF 1.0 投递方式始终对外公布。
        // RISC 变体（Apple Business Manager / Apple School Manager 互操作）
        // 受 sse-caep-enabled SPI 标志控制，未集成 Apple 风格接收方的部署
        // 可仅公布规范标准 URI。
        Set<String> deliveryMethods = new LinkedHashSet<>();
        // PUSH 投递（RFC 8935）
        deliveryMethods.add(Ssf.DELIVERY_METHOD_PUSH_URI);
        // POLL 轮询（RFC 8936）
        deliveryMethods.add(Ssf.DELIVERY_METHOD_POLL_URI);
        if (transmitterConfig.isSseCaepEnabled()) {
            // 遗留 RISC PUSH URI（Apple Business Manager）
            deliveryMethods.add(Ssf.DELIVERY_METHOD_RISC_PUSH_URI);
            // 遗留 RISC POLL URI
            deliveryMethods.add(Ssf.DELIVERY_METHOD_RISC_POLL_URI);
        }
        return deliveryMethods;
    }

    protected String createJwksUri(String issuerUrl) {
        return issuerUrl + "/protocol/openid-connect/certs";
    }

    protected List<Map<String, Object>> createAuthorizationSchemes() {
        return Collections.singletonList(createOAuthAuthorizationScheme());
    }

    protected Map<String, Object> createOAuthAuthorizationScheme() {
        Map<String, Object> oauthScheme = new HashMap<>();
        oauthScheme.put("spec_urn", Ssf.SSF_OAUTH_AUTHORIZATION_SCHEME_URN);
        return oauthScheme;
    }
}
