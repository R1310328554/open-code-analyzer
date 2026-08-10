package org.keycloak.services.clientpolicy.context;

import org.keycloak.models.ClientModel;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyEvent;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.utils.StringUtil;

/**
 * 客户端密钥轮换上下文：扩展 {@link AdminClientUpdateContext}，在 {@link ClientPolicyEvent#UPDATED} 上携带当前密钥。
 * <p>Admin API 轮换客户端密钥时使用，供策略 Executor 审计或限制轮换行为。</p>
 */
public class ClientSecretRotationContext extends AdminClientUpdateContext {

    /** 轮换前的当前客户端密钥；为空表示非强制轮换。 */
    private final String currentSecret;

    /**
     * @param proposedClientRepresentation 含新密钥的客户端表示
     * @param targetClient 待轮换密钥的客户端
     * @param currentSecret 当前密钥（可为空）
     * @param adminAuth Admin REST 认证上下文
     */
                                       ClientModel targetClient, String currentSecret, AdminAuth adminAuth) {
        super(proposedClientRepresentation, targetClient, adminAuth);
        this.currentSecret = currentSecret;
    }

    /** {@inheritDoc} 密钥轮换仍映射为 UPDATED 事件 */
    @Override
    public ClientPolicyEvent getEvent() {
        return ClientPolicyEvent.UPDATED;
    }

    /** @return 轮换前的当前密钥 */
    public String getCurrentSecret() {
        return currentSecret;
    }

    /** @return 是否提供了当前密钥以强制验证轮换 */
    public boolean isForceRotation() {
        return StringUtil.isNotBlank(currentSecret);
    }
}
