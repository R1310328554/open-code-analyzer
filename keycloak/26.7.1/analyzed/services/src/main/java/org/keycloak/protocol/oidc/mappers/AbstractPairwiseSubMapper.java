package org.keycloak.protocol.oidc.mappers;

import java.util.LinkedList;
import java.util.List;

import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperContainerModel;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.ProtocolMapperConfigException;
import org.keycloak.protocol.oidc.utils.PairwiseSubMapperUtils;
import org.keycloak.protocol.oidc.utils.PairwiseSubMapperValidator;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.IDToken;
import org.keycloak.representations.LogoutToken;

/**
 * 成对（Pairwise）subject 映射器抽象基类：将 {@code sub} 声明设为按 sector 隔离的成对标识符。
 * <p>适用于 OpenID Connect Pairwise Identifier 场景，保护用户跨客户端关联性。</p>
 *
 * @author <a href="mailto:martin.hardselius@gmail.com">Martin Hardselius</a>
 */
public abstract class AbstractPairwiseSubMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper, TokenIntrospectionTokenMapper, LogoutTokenMapper {
    /** Provider ID 后缀 */
    public static final String PROVIDER_ID_SUFFIX = "-pairwise-sub-mapper";

    /** @return Provider ID 前缀（如 hash、sha256 等） */
    public abstract String getIdPrefix();

    /**
     * 生成成对 subject 标识符。
     * @param mappingModel 映射器配置
     * @param sectorIdentifier 客户端 sector 标识
     * @param localSub 本地 subject（用户 ID）
     * @return 成对 subject 字符串
     */
    public abstract String generateSub(ProtocolMapperModel mappingModel, String sectorIdentifier, String localSub);

    /**
     * 子类可追加额外配置项；默认仅含 sector identifier URI。
     * @return 提供者配置属性列表
     */
    public List<ProviderConfigProperty> getAdditionalConfigProperties() {
        return new LinkedList<>();
    }

    /**
     * 子类可追加配置校验逻辑（管理端创建/更新映射器时调用）。
     * @param mapperContainer 客户端或客户端范围
     * @throws ProtocolMapperConfigException 配置无效时抛出
     */
    public void validateAdditionalConfig(KeycloakSession session, RealmModel realm, ProtocolMapperContainerModel mapperContainer, ProtocolMapperModel mapperModel) throws ProtocolMapperConfigException {
    }

    @Override
    public final String getDisplayCategory() {
        return AbstractOIDCProtocolMapper.TOKEN_MAPPER_CATEGORY;
    }

    @Override
    public IDToken transformIDToken(IDToken token, ProtocolMapperModel mappingModel, KeycloakSession session, UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        setIDTokenSubject(token, generateSub(mappingModel, getSectorIdentifier(clientSessionCtx.getClientSession().getClient(), mappingModel), userSession.getUser().getId()));
        return token;
    }

    @Override
    public AccessToken transformAccessToken(AccessToken token, ProtocolMapperModel mappingModel, KeycloakSession session, UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        setAccessTokenSubject(token, generateSub(mappingModel, getSectorIdentifier(clientSessionCtx.getClientSession().getClient(), mappingModel), userSession.getUser().getId()));
        return token;
    }

    @Override
    public AccessToken transformIntrospectionToken(AccessToken token, ProtocolMapperModel mappingModel, KeycloakSession session, UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        setAccessTokenSubject(token, generateSub(mappingModel, getSectorIdentifier(clientSessionCtx.getClientSession().getClient(), mappingModel), userSession.getUser().getId()));
        return token;
    }

    @Override
    public LogoutToken transformLogoutToken(LogoutToken token, ProtocolMapperModel mappingModel, KeycloakSession session, UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        setLogoutTokenSubject(token, generateSub(mappingModel, getSectorIdentifier(clientSessionCtx.getClientSession().getClient(), mappingModel), userSession.getUser().getId()));
        return token;
    }
    @Override
    public AccessToken transformUserInfoToken(AccessToken token, ProtocolMapperModel mappingModel, KeycloakSession session, UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
        setUserInfoTokenSubject(token, generateSub(mappingModel, getSectorIdentifier(clientSessionCtx.getClientSession().getClient(), mappingModel), userSession.getUser().getId()));
        return token;
    }

    /** 设置 ID Token 的 sub 声明 */
    protected void setIDTokenSubject(IDToken token, String pairwiseSub) {
        token.setSubject(pairwiseSub);
    }

    /** 设置 Access Token 的 sub 声明 */
    protected void setAccessTokenSubject(IDToken token, String pairwiseSub) {
        token.setSubject(pairwiseSub);
    }

    /** 设置 Logout Token 的 sub 声明 */
    protected void setLogoutTokenSubject(LogoutToken token, String pairwiseSub) {
        token.setSubject(pairwiseSub);
    }

    /** 设置 UserInfo 响应中的 sub 声明 */
    protected void setUserInfoTokenSubject(IDToken token, String pairwiseSub) {
        token.getOtherClaims().put("sub", pairwiseSub);
    }

    @Override
    public final List<ProviderConfigProperty> getConfigProperties() {
        List<ProviderConfigProperty> configProperties = new LinkedList<>();
        configProperties.add(PairwiseSubMapperHelper.createSectorIdentifierConfig());
        configProperties.addAll(getAdditionalConfigProperties());
        return configProperties;
    }

    /** 解析有效的 sector identifier（配置 URI 或客户端 redirect URI） */
    private String getSectorIdentifier(ClientModel client, ProtocolMapperModel mappingModel) {
        String sectorIdentifierUri = PairwiseSubMapperHelper.getSectorIdentifierUri(mappingModel);
        if (sectorIdentifierUri != null && !sectorIdentifierUri.isEmpty()) {
            return PairwiseSubMapperUtils.resolveValidSectorIdentifier(sectorIdentifierUri);
        }
        return PairwiseSubMapperUtils.resolveValidSectorIdentifier(client.getRootUrl(), client.getRedirectUris());
    }

    /** {@inheritDoc} 校验 sector identifier 及子类额外配置 */
    @Override
    public final void validateConfig(KeycloakSession session, RealmModel realm, ProtocolMapperContainerModel mapperContainer, ProtocolMapperModel mapperModel) throws ProtocolMapperConfigException {
        ClientModel client = null;
        if (mapperContainer instanceof ClientModel) {
            client = (ClientModel) mapperContainer;
            PairwiseSubMapperValidator.validate(session, client, mapperModel);
        }
        validateAdditionalConfig(session, realm, mapperContainer, mapperModel);
    }

    /** {@inheritDoc} 返回 oidc-{prefix}-pairwise-sub-mapper */
    @Override
    public final String getId() {
        return "oidc-" + getIdPrefix() + PROVIDER_ID_SUFFIX;
    }
}
