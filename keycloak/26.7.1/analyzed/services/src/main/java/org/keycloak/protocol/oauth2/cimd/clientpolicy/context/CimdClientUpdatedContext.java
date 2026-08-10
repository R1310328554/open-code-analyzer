package org.keycloak.protocol.oauth2.cimd.clientpolicy.context;

import org.keycloak.models.ClientModel;
import org.keycloak.services.clientpolicy.ClientPolicyEvent;
import org.keycloak.services.clientpolicy.context.ClientCRUDClientAvailableContext;

/**
 * CIMD 客户端已更新事件上下文：携带更新完成后的 {@link ClientModel}。
 * <p>对应 {@link ClientPolicyEvent#UPDATED}，供策略在变更持久化后执行收尾逻辑。</p>
 */
public class CimdClientUpdatedContext implements ClientCRUDClientAvailableContext {

    /** 更新后的目标客户端模型。 */
    private final ClientModel updatedClient;

    /** @param updatedClient 更新完成后的客户端 */
    public CimdClientUpdatedContext(ClientModel updatedClient) {
        this.updatedClient = updatedClient;
    }

    @Override
    /** @return {@link ClientPolicyEvent#UPDATED} */
    public ClientPolicyEvent getEvent() {
        return ClientPolicyEvent.UPDATED;
    }

    @Override
    /** @return 更新后的客户端模型 */
    public ClientModel getTargetClient() {
        return updatedClient;
    }
}
