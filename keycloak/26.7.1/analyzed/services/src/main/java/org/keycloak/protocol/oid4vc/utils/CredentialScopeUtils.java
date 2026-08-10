package org.keycloak.protocol.oid4vc.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.oid4vci.CredentialScopeModel;
import org.keycloak.protocol.oid4vc.model.DisplayObject;
import org.keycloak.protocol.oidc.endpoints.request.AuthorizationEndpointRequest;
import org.keycloak.util.Strings;
import org.keycloak.utils.StringUtil;

import org.jboss.logging.Logger;

import static org.keycloak.constants.OID4VCIConstants.OID4VC_PROTOCOL;

/**
 * OID4VCI 凭证 scope（客户端 scope）查找与展示工具。
 * <p>按凭证配置 ID、scope 名称或授权请求解析 {@link CredentialScopeModel}，并生成本地化凭证显示名。</p>
 */
public class CredentialScopeUtils {

    /** 日志记录器。 */
    private static final Logger log = Logger.getLogger(CredentialScopeUtils.class);

    // 工具类，隐藏构造器
    private CredentialScopeUtils() {}

    /**
     * 按凭证配置 ID 查找唯一 {@link CredentialScopeModel}。
     * @param realmModel 领域
     * @param supplier 客户端 scope 流供应器
     * @param credConfigId 凭证配置标识
     * @return 匹配的凭证 scope，无匹配或多匹配时返回 null 并记录警告
     */
    public static CredentialScopeModel findCredentialScopeModelByConfigurationId(RealmModel realmModel, Supplier<Stream<ClientScopeModel>> supplier, String credConfigId) {
        if (Strings.isEmpty(credConfigId)) {
            return null;
        }
        List<CredentialScopeModel> credScopes = supplier.get()
                .filter(it -> it.getProtocol().equals(OID4VC_PROTOCOL))
                .map(CredentialScopeModel::new)
                .filter(it -> credConfigId.equals(it.getCredentialConfigurationId()))
                .toList();
        if (credScopes.size() > 1) {
            List<String> clientScopeNames = credScopes.stream().map(ClientScopeModel::getName).toList();
            log.warnf("Multiple client scopes found for credential configuration '%s' in realm '%s': %s",
                    credConfigId, realmModel.getName(), clientScopeNames);
            return null;
        } else if (credScopes.isEmpty()) {
            log.warnf("No client scopes found for credential configuration '%s' in realm '%s'",
                    credConfigId, realmModel.getName());
            return null;
        } else {
            return credScopes.get(0);
        }
    }

    /**
     * 按 scope 名称查找唯一 {@link CredentialScopeModel}。
     * @param realmModel 领域
     * @param supplier 客户端 scope 流供应器
     * @param scope scope 名称
     * @return 匹配的凭证 scope，无匹配或多匹配时返回 null
     */
    public static CredentialScopeModel findCredentialScopeModelByName(RealmModel realmModel, Supplier<Stream<ClientScopeModel>> supplier, String scope) {
        if (Strings.isEmpty(scope)) {
            return null;
        }
        List<CredentialScopeModel> credScopes =  supplier.get()
                .filter(it -> it.getProtocol().equals(OID4VC_PROTOCOL))
                .map(CredentialScopeModel::new)
                .filter(it -> scope.equals(it.getScope()))
                .toList();
        if (credScopes.size() > 1) {
            List<String> clientScopeNames = credScopes.stream().map(ClientScopeModel::getName).toList();
            log.warnf("Multiple client scopes found for scope '%s' in realm '%s': %s",
                    scope, realmModel.getName(), clientScopeNames);
            return null;
        }
        return !credScopes.isEmpty() ? credScopes.get(0) : null;
    }

    /**
     * 根据授权请求中的 scope 参数，返回与该客户端关联且被请求的 OID4VCI 凭证 scope 列表。
     * @param client 客户端
     * @param request 授权端点请求
     * @return 凭证 scope 列表
     */
    public static List<CredentialScopeModel> getCredentialScopesForAuthorization(ClientModel client, AuthorizationEndpointRequest request) {

        List<String> requestScopes = Optional.ofNullable(request.getScope())
                .map(it -> it.split("\\s"))
                .map(Arrays::asList)
                .orElse(List.of());

        // 筛选客户端已关联且请求中声明的 OID4VCI 凭证 scope
        //
        Map<String, ClientScopeModel> clientScopes = client.getClientScopes(false);
        List<CredentialScopeModel> credScopes = requestScopes.stream()
                .filter(clientScopes::containsKey)
                .map(clientScopes::get)
                .filter(it -> OID4VC_PROTOCOL.equals(it.getProtocol()))
                .map(CredentialScopeModel::new)
                .toList();

        return credScopes;
    }

    /**
     * 按用户语言偏好与 {@code vc.display} 属性返回凭证的友好显示名。
     * <p>无有效 display 配置时回退到凭证配置 ID 或客户端 scope 名称。</p>
     * @param session Keycloak 会话
     * @param user 用户
     * @param credScope OID4VCI 客户端 scope
     * @return 本地化后的凭证显示名
     */
    public static String getCredentialDisplayName(KeycloakSession session, UserModel user, CredentialScopeModel credScope) {
        List<DisplayObject> displayDatas = DisplayObject.parse(credScope);
        if (displayDatas != null) {
            String language = session.getContext().resolveLocale(user).getLanguage();
            String languageCountry = language + "-" + language.toUpperCase();
            for (DisplayObject displayData : displayDatas) {
                if (language.equals(displayData.getLocale()) || languageCountry.equals(displayData.getLocale())) {
                    return displayData.getName();
                }
            }
        }

        // 回退到配置 ID 或 scope 名称
        String display = credScope.getCredentialConfigurationId();
        return StringUtil.isNotBlank(display) ? display :  credScope.getName();
    }
}
