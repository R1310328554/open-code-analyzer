package org.keycloak.protocol.oauth2.cimd.clientpolicy.context;

import org.keycloak.models.ClientModel;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyEvent;
import org.keycloak.services.clientpolicy.context.ClientCRUDClientAvailableContext;

/**
 * CIMD 客户端更新事件上下文：同时携带待更新表示与现有 {@link ClientModel}。
 * <p>对应 {@link ClientPolicyEvent#UPDATE}，在客户端元数据变更提交前触发策略。</p>
 */
public class CimdClientUpdateContext implements ClientCRUDClientAvailableContext {

    /** 提议更新的客户端表示。 */
    private final ClientRepresentation proposedClientRepresentation;
    /** 待更新的现有客户端模型。 */
    private final ClientModel targetClient;

    /**
     * @param proposedClientRepresentation 提议的更新内容
     * @param targetClient 目标客户端
     */
    public CimdClientUpdateContext(ClientRepresentation proposedClientRepresentation, ClientModel targetClient) {
        this.proposedClientRepresentation = proposedClientRepresentation;
        this.targetClient = targetClient;
    }

    @Override
    /** @return {@link ClientPolicyEvent#UPDATE} */
    public ClientPolicyEvent getEvent() {
        return ClientPolicyEvent.UPDATE;
    }

    @Override
    /** @return 提议的客户端表示 */
    public ClientRepresentation getProposedClientRepresentation() {
        return proposedClientRepresentation;
    }

    @Override
    /** @return 待更新的客户端模型 */
    public ClientModel getTargetClient() {
        return targetClient;
    }
}
