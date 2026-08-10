package org.keycloak.protocol.oidc.resourceindicators;

import org.keycloak.OAuth2Constants;
import org.keycloak.OAuthErrorException;
import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.token.TokenInterceptorException;
import org.keycloak.protocol.oidc.token.TokenPostProcessor;
import org.keycloak.protocol.oidc.token.TokenPostProcessorContext;

/**
 * 资源指标令牌后处理器：校验 resource 参数、与原始请求对齐，并设置 access token audience。
 * <p>在 authorization_code 与 refresh_token grant 下强制 resource 与初始授权一致。</p>
 */
public class ResourceIndicatorsPostProcessor implements TokenPostProcessor {

    private final KeycloakSession session;

    /** @param session Keycloak 会话 */
    public ResourceIndicatorsPostProcessor(KeycloakSession session) {
        this.session = session;
    }

    /**
     * 校验 resource 指标、解析 audience，并写入 refresh token 的 resource 声明。
     * @param context 令牌后处理上下文
     * @throws TokenInterceptorException resource 无效或与原始请求不匹配
     */
        String requestedResource = context.clientSessionCtx().getAttribute(OAuth2Constants.RESOURCE, String.class);
        if (requestedResource != null && !ResourceIndicatorValidation.isValidResourceIndicator(requestedResource)) {
            throw new TokenInterceptorException(OAuthErrorException.INVALID_TARGET, ResourceIndicatorConstants.ERROR_INVALID_RESOURCE);
        }

        String grantType = context.clientSessionCtx().getAttribute(Constants.GRANT_TYPE, String.class);

        boolean originalResourceParamRequired = false;
        String originalResourceParam = null;
        if (OAuth2Constants.AUTHORIZATION_CODE.equals(grantType)) {
            originalResourceParam = context.code().getResource();
            originalResourceParamRequired = true;
        } else if (OAuth2Constants.REFRESH_TOKEN.equals(grantType)) {
            originalResourceParam = (String) context.requestRefreshToken().getOtherClaims().get(OAuth2Constants.RESOURCE);
            originalResourceParamRequired = true;
        }

        if (originalResourceParam == null && requestedResource == null) {
            return;
        }

        if (originalResourceParamRequired) {
            if (originalResourceParam == null) {
                throw new TokenInterceptorException(OAuthErrorException.INVALID_TARGET, ResourceIndicatorConstants.ERROR_NOT_MATCHING);
            }

            if (requestedResource == null) {
                requestedResource = originalResourceParam;
            } else if (!requestedResource.equals(originalResourceParam)){
                throw new TokenInterceptorException(OAuthErrorException.INVALID_TARGET, ResourceIndicatorConstants.ERROR_NOT_MATCHING);
            }
        }

        String audienceToSet;
        if (isClientUrn(requestedResource)) {
            audienceToSet = findAudienceByClientUrn(requestedResource, context.accessToken().getAudience());
        } else {
            audienceToSet = findAudienceByClientAttribute(requestedResource, context.accessToken().getAudience());
        }

        if (audienceToSet == null) {
            throw new TokenInterceptorException(OAuthErrorException.INVALID_TARGET, ResourceIndicatorConstants.ERROR_INVALID_RESOURCE);
        }

        context.refreshToken().getOtherClaims().put(OAuth2Constants.RESOURCE, requestedResource);
        context.accessToken().audience(audienceToSet);
    }

    /** 判断 resource 是否为 {@code urn:client:} 形式的客户端 URN */
    private boolean isClientUrn(String resource) {
        return resource.startsWith(ResourceIndicatorConstants.URN_CLIENT_PREFIX);
    }

    /** 从 URN 提取 clientId 并在 audience 列表中查找匹配项 */
    private String findAudienceByClientUrn(String resource, String[] audience) {
        String requestedClientId = resource.substring(ResourceIndicatorConstants.URN_CLIENT_PREFIX.length());
        return find(requestedClientId, audience);
    }

    /** 按客户端 {@link ResourceIndicatorConstants#CLIENT_RESOURCE_URL_ATTRIBUTE} 属性匹配 resource URL */
    private String findAudienceByClientAttribute(String resource, String[] audience) {
        for (String a : audience) {
            ClientModel client = session.clients().getClientByClientId(session.getContext().getRealm(), a);
            if (client != null) {
                String clientResourceUrl = client.getAttribute(ResourceIndicatorConstants.CLIENT_RESOURCE_URL_ATTRIBUTE);
                if (resource.equals(clientResourceUrl)) {
                    return resource;
                }
            }
        }
        return null;
    }

    /** 在 audience 数组中查找与 search 相等的项 */
    private String find(String search, String[] array) {
        for (String a : array) {
            if (a.equals(search)) {
                return a;
            }
        }
        return null;
    }

}
