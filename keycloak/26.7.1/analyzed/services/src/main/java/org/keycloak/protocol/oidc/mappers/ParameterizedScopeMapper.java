package org.keycloak.protocol.oidc.mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.ProtocolMapperUtils;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.rar.AuthorizationDetails;
import org.keycloak.rar.AuthorizationRequestContext;
import org.keycloak.representations.IDToken;
import org.keycloak.utils.StringUtil;

/**
 * 参数化 Scope 映射器：将参数化客户端范围的参数值直接映射为令牌声明。
 * <p>需启用 {@link org.keycloak.common.Profile.Feature#PARAMETERIZED_SCOPES} 特性。</p>
 */
public class ParameterizedScopeMapper extends AbstractOIDCProtocolMapper
        implements OIDCAccessTokenMapper, OIDCIDTokenMapper, TokenIntrospectionTokenMapper, EnvironmentDependentProviderFactory {

    /** SPI 提供者标识符 */
    public static final String PROVIDER_ID = "oidc-parameterized-scope-mapper";

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    static {
        OIDCAttributeMapperHelper.addAttributeConfig(configProperties, ParameterizedScopeMapper.class);
    }

    /** {@inheritDoc} 返回 {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** {@inheritDoc} 控制台显示名：Parameterized Scope Parameter */
    @Override
    public String getDisplayType() {
        return "Parameterized Scope Parameter";
    }

    /** {@inheritDoc} 归类为令牌映射器 */
    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    /** {@inheritDoc} 将参数化 scope 的参数值映射到令牌声明 */
    @Override
    public String getHelpText() {
        return "Maps the parameter value from a parameterized scope directly to a token claim.";
    }

    /** {@inheritDoc} 返回声明名、JSON 类型及包含目标等配置项 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    /** 解析参数化 scope 并调用 {@link #setClaim(IDToken, ProtocolMapperModel, UserSessionModel, KeycloakSession, List)} */
    @Override
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession,
                            KeycloakSession keycloakSession, ClientSessionContext clientSessionCtx) {
        ClientScopeModel clientScope = resolveClientScope(mappingModel, clientSessionCtx).orElse(null);
        if (clientScope == null) {
            return;
        }

        List<String> parameterValues = resolveParameterValues(clientScope, clientSessionCtx);
        ProtocolMapperModel model = new ProtocolMapperModel(mappingModel);
        model.getConfig().put(ProtocolMapperUtils.MULTIVALUED, Boolean.toString(TokenManager.isRepeatableScope(keycloakSession, clientScope)));
        if (!parameterValues.isEmpty()) {
            setClaim(token, model, userSession, keycloakSession, parameterValues);
        }
    }

    /**
     * 将解析到的参数值映射为令牌声明。
     * <p>{@code multivalued} 配置决定多值以 JSON 数组还是仅首值写入。</p>
     */
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession,
                            KeycloakSession keycloakSession, List<String> parameterValues) {
        OIDCAttributeMapperHelper.mapClaim(token, mappingModel, parameterValues);
    }

    /** 从授权详情中定位与当前映射器关联的参数化客户端范围 */
    protected Optional<ClientScopeModel> resolveClientScope(ProtocolMapperModel mappingModel, ClientSessionContext clientSessionCtx) {
        AuthorizationRequestContext ctx = clientSessionCtx.getAuthorizationRequestContext();
        if (ctx == null) {
            return Optional.empty();
        }

        return ctx.getAuthorizationDetailEntries().stream()
                .filter(d -> d.getClientScope() != null && d.isParameterizedScope()
                        && d.getClientScope().getProtocolMapperById(mappingModel.getId()) != null)
                .map(AuthorizationDetails::getClientScope)
                .findAny();
    }

    /** 收集指定客户端范围在授权请求中的全部参数值 */
    protected List<String> resolveParameterValues(ClientScopeModel clientScope, ClientSessionContext clientSessionCtx) {
        AuthorizationRequestContext ctx = clientSessionCtx.getAuthorizationRequestContext();
        if (ctx == null) {
            return List.of();
        }

        List<String> values = new ArrayList<>();
        for (AuthorizationDetails detail : ctx.getAuthorizationDetailEntries()) {
            if (detail.getClientScope() != null
                    && detail.getClientScope().getId().equals(clientScope.getId())) {
                String paramValue = detail.getParameterizedScopeParam();
                if (StringUtil.isNotBlank(paramValue)) {
                    values.add(paramValue);
                }
            }
        }
        return values;
    }

    /** {@inheritDoc} 需启用 PARAMETERIZED_SCOPES 特性 */
    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.PARAMETERIZED_SCOPES);
    }

    /**
     * 工厂方法：创建参数化 scope 映射器配置。
     * @param name 映射器名称
     * @param tokenClaimName 目标声明名
     * @param claimType JSON 类型
     * @param accessToken 是否写入 Access Token
     * @param idToken 是否写入 ID Token
     * @param introspectionEndpoint 是否写入内省响应
     */
        ProtocolMapperModel mapper = OIDCAttributeMapperHelper.createClaimMapper(
                name, null, tokenClaimName, claimType,
                accessToken, idToken, false, introspectionEndpoint,
                PROVIDER_ID);
        return mapper;
    }
}
