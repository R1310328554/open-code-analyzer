package org.keycloak.services.clientregistration.policy.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.clientregistration.ClientRegistrationContext;
import org.keycloak.services.clientregistration.ClientRegistrationProvider;
import org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy;
import org.keycloak.services.clientregistration.policy.ClientRegistrationPolicyException;

/**
 * 注册 Web Origins 白名单策略。
 * <p>通过 {@link #getAllowedOrigins()} 向客户端注册服务返回允许的 Origin 列表，用于 CORS 与动态注册端点的访问控制。</p>
 */
public class RegistrationWebOriginsPolicy implements ClientRegistrationPolicy {

    /** 组件配置中允许的 Web Origin 列表 */
    private final List<String> allowedWebOrigins;

    /** 从组件配置加载允许的 Web Origins。
     * @param session Keycloak 会话（未使用，保留工厂签名一致性）
     * @param model 策略组件模型
     */
    public RegistrationWebOriginsPolicy(KeycloakSession session, ComponentModel model) {
        allowedWebOrigins = model.getConfig().getOrDefault(RegistrationWebOriginsPolicyFactory.WEB_ORIGINS, Collections.emptyList());
    }

    /** {@inheritDoc} 注册前无额外校验 */
    @Override
    public void beforeRegister(ClientRegistrationContext context) throws ClientRegistrationPolicyException {
    }

    /** {@inheritDoc} 注册后无额外处理 */
    @Override
    public void afterRegister(ClientRegistrationContext context, ClientModel clientModel) {
    }

    /** {@inheritDoc} 更新前无额外校验 */
    @Override
    public void beforeUpdate(ClientRegistrationContext context, ClientModel clientModel) throws ClientRegistrationPolicyException {
    }

    /** {@inheritDoc} 更新后无额外处理 */
    @Override
    public void afterUpdate(ClientRegistrationContext context, ClientModel clientModel) {
    }

    /** {@inheritDoc} 查看前无额外校验 */
    @Override
    public void beforeView(ClientRegistrationProvider provider, ClientModel clientModel) throws ClientRegistrationPolicyException {
    }

    /** {@inheritDoc} 删除前无额外校验 */
    @Override
    public void beforeDelete(ClientRegistrationProvider provider, ClientModel clientModel) throws ClientRegistrationPolicyException {
    }

    /** {@inheritDoc} 返回配置的允许 Web Origin 集合 */
    @Override
    public Collection<String> getAllowedOrigins() {
        return allowedWebOrigins;
    }

}
