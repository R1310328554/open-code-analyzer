package org.keycloak.protocol.oauth2.cimd.clientpolicy.context;

import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyEvent;
import org.keycloak.services.clientpolicy.context.ClientCRUDContext;

/**
 * CIMD 客户端注册事件上下文：携带待创建的 {@link ClientRepresentation}。
 * <p>对应 {@link ClientPolicyEvent#REGISTER}，供客户端策略在动态注册前介入。</p>
 */
public class CimdClientRegisterContext implements ClientCRUDContext {

    /** 提议注册的客户端表示（尚未持久化）。 */
    private final ClientRepresentation proposedClientRepresentation;

    /** @param proposedClientRepresentation 待注册客户端表示 */
    public CimdClientRegisterContext(ClientRepresentation proposedClientRepresentation) {
        this.proposedClientRepresentation = proposedClientRepresentation;
    }

    @Override
    /** @return {@link ClientPolicyEvent#REGISTER} */
    public ClientPolicyEvent getEvent() {
        return ClientPolicyEvent.REGISTER;
    }

    @Override
    /** @return 提议的客户端表示 */
    public ClientRepresentation getProposedClientRepresentation() {
        return proposedClientRepresentation;
    }
}
