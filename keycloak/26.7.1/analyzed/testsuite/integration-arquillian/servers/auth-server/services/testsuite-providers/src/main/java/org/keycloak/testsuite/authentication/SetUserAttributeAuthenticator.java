package org.keycloak.testsuite.authentication;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

/**
 * 设置用户属性认证器：在认证流程中向当前用户写入或追加指定的用户属性值。
 */
public class SetUserAttributeAuthenticator implements Authenticator {
    /**
     * 读取配置的属性名与值，写入当前用户属性后标记认证成功。
     *
     * @param context 认证流程上下文
     */
    @Override
    public void authenticate(AuthenticationFlowContext context) {
        // 读取认证器配置
        Map<String, String> config = context.getAuthenticatorConfig().getConfig();
        String attrName = config.get(SetUserAttributeAuthenticatorFactory.CONF_ATTR_NAME);
        String attrValue = config.get(SetUserAttributeAuthenticatorFactory.CONF_ATTR_VALUE);

        UserModel user = context.getUser();
        List<String> attrValues = user.getAttributeStream(attrName).collect(Collectors.toList());
        if (attrValues.isEmpty()) {
            user.setSingleAttribute(attrName, attrValue);
        }
        else {
            if (!attrValues.contains(attrValue)) {
                attrValues.add(attrValue);
            }
            user.setAttribute(attrName, attrValues);
        }

        context.success();
    }

    /** {@inheritDoc} 不支持表单操作，直接返回内部错误。 */
    @Override
    public void action(AuthenticationFlowContext context) {
        context.failure(AuthenticationFlowError.INTERNAL_ERROR);
    }

    /** {@inheritDoc} 需要已登录用户才能设置属性。 */
    @Override
    public boolean requiresUser() {
        return true;
    }

    /** {@inheritDoc} 对所有用户均视为已配置。 */
    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

    }

    @Override
    public void close() {

    }
}
