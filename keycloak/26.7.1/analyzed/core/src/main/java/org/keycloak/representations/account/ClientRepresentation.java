package org.keycloak.representations.account;

/**
 * 账户控制台展示的 OAuth/OIDC 客户端摘要信息，含同意状态、使用情况及客户端元数据 URI。
 *
 * Created by st on 29/03/17.
 */
public class ClientRepresentation {
    /** 客户端标识符（clientId）。 */
    private String clientId;
    /** 面向用户显示的客户端名称。 */
    private String clientName;
    /** 客户端描述文本。 */
    private String description;
    /** 是否要求用户显式同意授权。 */
    private boolean userConsentRequired;
    /** 当前用户是否正在使用该客户端（存在活跃会话或令牌）。 */
    private boolean inUse;
    /** 是否已授予离线访问（refresh token）权限。 */
    private boolean offlineAccess;
    /** 客户端根 URL。 */
    private String rootUrl;
    /** 客户端基础 URL。 */
    private String baseUrl;
    /** 解析后的有效访问 URL（综合 root/base 配置）。 */
    private String effectiveUrl;
    /** 用户对该客户端的同意详情。 */
    private ConsentRepresentation consent;
    /** 客户端 Logo URI。 */
    private String logoUri;
    /** 隐私政策 URI。 */
    private String policyUri;
    /** 服务条款 URI。 */
    private String tosUri;


    /** @return 客户端 ID */
    public String getClientId() {
        return clientId;
    }

    /** @param clientId 客户端 ID */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /** @return 显示名称 */
    public String getClientName() {
        return clientName;
    }

    /** @param clientName 显示名称 */
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    /** @return 描述 */
    public String getDescription() {
        return description;
    }

    /** @param description 描述 */
    public void setDescription(String description) {
        this.description = description;
    }

    /** @return 是否需要用户同意 */
    public boolean isUserConsentRequired() {
        return userConsentRequired;
    }

    /** @param userConsentRequired 是否需要用户同意 */
    public void setUserConsentRequired(boolean userConsentRequired) {
        this.userConsentRequired = userConsentRequired;
    }

    /** @return 是否正在使用 */
    public boolean isInUse() {
        return inUse;
    }

    /** @param inUse 是否正在使用 */
    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    /** @return 是否已授予离线访问 */
    public boolean isOfflineAccess() {
        return offlineAccess;
    }

    /** @param offlineAccess 是否已授予离线访问 */
    public void setOfflineAccess(boolean offlineAccess) {
        this.offlineAccess = offlineAccess;
    }

    /** @return 根 URL */
    public String getRootUrl() {
        return rootUrl;
    }

    /** @param rootUrl 根 URL */
    public void setRootUrl(String rootUrl) {
        this.rootUrl = rootUrl;
    }

    /** @return 基础 URL */
    public String getBaseUrl() {
        return baseUrl;
    }

    /** @param baseUrl 基础 URL */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /** @return 有效 URL */
    public String getEffectiveUrl() {
       return effectiveUrl;
    }

    /** @param effectiveUrl 有效 URL */
    public void setEffectiveUrl(String effectiveUrl) {
        this.effectiveUrl = effectiveUrl;
    }

    /** @return 同意信息 */
    public ConsentRepresentation getConsent() {
        return consent;
    }

    /** @param consent 同意信息 */
    public void setConsent(ConsentRepresentation consent) {
        this.consent = consent;
    }

    /** @return Logo URI */
    public String getLogoUri() {
        return logoUri;
    }

    /** @param logoUri Logo URI */
    public void setLogoUri(String logoUri) {
        this.logoUri = logoUri;
    }

    /** @return 隐私政策 URI */
    public String getPolicyUri() {
        return policyUri;
    }

    /** @param policyUri 隐私政策 URI */
    public void setPolicyUri(String policyUri) {
        this.policyUri = policyUri;
    }

    /** @return 服务条款 URI */
    public String getTosUri() {
        return tosUri;
    }

    /** @param tosUri 服务条款 URI */
    public void setTosUri(String tosUri) {
        this.tosUri = tosUri;
    }
}
