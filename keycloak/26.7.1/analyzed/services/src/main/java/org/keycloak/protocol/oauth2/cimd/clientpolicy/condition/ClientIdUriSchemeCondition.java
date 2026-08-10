package org.keycloak.protocol.oauth2.cimd.clientpolicy.condition;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.keycloak.OAuth2Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.idm.ClientPolicyConditionConfigurationRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.clientpolicy.ClientPolicyVote;
import org.keycloak.services.clientpolicy.condition.AbstractClientPolicyConditionProvider;
import org.keycloak.services.clientpolicy.context.PreAuthorizationRequestContext;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jboss.logging.Logger;

/**
 * 客户端策略条件：在 {@code PRE_AUTHORIZATION_REQUEST} 事件中校验 {@code client_id} 是否为 URI，
 * 且其 scheme 与 host 分别匹配配置中的允许 scheme 列表与受信域名列表。
 * <p>用于 CIMD 场景下识别 URL 形式的 client_id。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class ClientIdUriSchemeCondition extends AbstractClientPolicyConditionProvider<ClientIdUriSchemeCondition.Configuration> {

    private static final Logger logger = Logger.getLogger(ClientIdUriSchemeCondition.class);

    /** @param session Keycloak 会话 */
    public ClientIdUriSchemeCondition(KeycloakSession session) {
        super(session);
    }

    @Override
    public Class<ClientIdUriSchemeCondition.Configuration> getConditionConfigurationClass() {
        return ClientIdUriSchemeCondition.Configuration.class;
    }

    /** 条件配置：允许的 URI scheme 与受信域名列表。 */
    public static class Configuration extends ClientPolicyConditionConfigurationRepresentation {
        @JsonProperty(ClientIdUriSchemeConditionFactory.CLIENT_ID_URI_SCHEME)
        /** 允许的 client_id URI scheme 列表（如 {@code https}）。 */
        protected List<String> clientIdUriSchemes = Collections.emptyList();

        @JsonProperty(ClientIdUriSchemeConditionFactory.TRUSTED_DOMAINS)
        /** 受信域名列表，支持 {@code *.example.org} 通配符。 */
        protected List<String> trustedDomains = null;

        /** @return 允许的 URI scheme 列表 */
        public List<String> getClientIdUriSchemes() {
            return clientIdUriSchemes;
        }

        /** @param clientIdUriSchemes 允许的 URI scheme 列表 */
        public void setClientIdUriSchemes(List<String> clientIdUriSchemes) {
            this.clientIdUriSchemes = clientIdUriSchemes;
        }

        /** @return 受信域名列表 */
        public List<String> getTrustedDomains() {
            return trustedDomains;
        }

        /** @param permittedDomains 受信域名列表 */
        public void setTrustedDomains(List<String> permittedDomains) {
            this.trustedDomains = permittedDomains;
        }
    }

    @Override
    public String getProviderId() {
        return ClientIdUriSchemeConditionFactory.PROVIDER_ID;
    }

    @Override
    /**
     * 在预授权请求阶段评估 client_id 是否为符合配置的 URI。
     * @param context 客户端策略上下文
     * @return {@link ClientPolicyVote#YES} 匹配时，否则 {@link ClientPolicyVote#NO}
     */
    public ClientPolicyVote applyPolicy(ClientPolicyContext context) throws ClientPolicyException {
        switch (context.getEvent()) {
            case PRE_AUTHORIZATION_REQUEST:
                PreAuthorizationRequestContext paContext = (PreAuthorizationRequestContext) context;
                String clientId = paContext.getRequestParameters().getFirst(OAuth2Constants.CLIENT_ID);
                if (clientId == null || configuration.getClientIdUriSchemes() == null || configuration.getClientIdUriSchemes().isEmpty()) {
                    return ClientPolicyVote.NO;
                }
                final URI uri;
                try {
                    uri = new URI(clientId);
                } catch (URISyntaxException e) {
                    logger.debugv("not URL: clientId = {0}", clientId);
                    return ClientPolicyVote.NO;
                }
                if (isUriSchemeMatched(uri) && isTrustedDomainMatched(uri)) return ClientPolicyVote.YES;

                return ClientPolicyVote.NO;
            default:
                return ClientPolicyVote.ABSTAIN;
        }
    }

    /** 判断 URI scheme 是否在配置允许列表中。 */
    private boolean isUriSchemeMatched(URI uri) {
        return configuration.getClientIdUriSchemes().stream().anyMatch(i->i.equals(uri.getScheme()));
    }

    /** 判断 URI host 是否匹配任一受信域名（含通配符）。 */
    private boolean isTrustedDomainMatched(URI uri) {
        List<String> trustedDomains = convertContentFilledList(configuration.getTrustedDomains());
        if (trustedDomains.isEmpty()) {
            logger.debug("trusted domain list is vacant.");
            return false;
        }

        if (uri.getHost() == null) {
            logger.warn("not trusted domain: host = null");
            return false;
        }

        if (trustedDomains.stream().noneMatch(i->checkTrustedDomain(uri.getHost(), i))) {
            logger.warnv("not trusted domain: host = {0}", uri.getHost());
            return false;
        }

        return true;
    }

    // 返回去重、非空、非空白字符串列表；输入为 null 时返回空列表
    /** 过滤并去重配置列表中的有效条目。 */
    private List<String> convertContentFilledList(List<String> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().filter(Objects::nonNull).filter(i->!i.isBlank()).distinct().toList();
    }

    // 与 TrustedHostClientRegistrationPolicy 相同的域名通配符匹配逻辑
    /** 单条受信域名规则匹配（支持 {@code *.domain} 前缀通配）。 */
    private boolean checkTrustedDomain(String hostname, String trustedDomain) {
        if (trustedDomain.startsWith("*.")) {
            String domain = trustedDomain.substring(2);
            return hostname.equals(domain) || hostname.endsWith("." + domain);
        }
        return hostname.equals(trustedDomain);
    }
}
