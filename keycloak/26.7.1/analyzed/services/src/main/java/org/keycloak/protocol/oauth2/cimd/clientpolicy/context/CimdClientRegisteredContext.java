package org.keycloak.protocol.oauth2.cimd.clientpolicy.context;

import org.keycloak.models.ClientModel;
import org.keycloak.services.clientpolicy.ClientPolicyEvent;
import org.keycloak.services.clientpolicy.context.ClientCRUDClientAvailableContext;

/**
 * CIMD 客户端已注册事件上下文：携带刚创建完成的 {@link ClientModel}。
 * <p>对应 {@link ClientPolicyEvent#REGISTERED}，策略可在注册成功后执行后续校验或审计。</p>
 */
public class CimdClientRegisteredContext implements ClientCRUDClientAvailableContext {

    /** 已注册的目标客户端模型。 */
    private final ClientModel registeredClient;

    /** @param registeredClient 刚注册完成的客户端 */
    public CimdClientRegisteredContext(ClientModel registeredClient) {
        this.registeredClient = registeredClient;
    }

    @Override
    /** @return {@link ClientPolicyEvent#REGISTERED} */
    public ClientPolicyEvent getEvent() {
        return ClientPolicyEvent.REGISTERED;
    }

    @Override
    /** @return 已注册的客户端模型 */
    public ClientModel getTargetClient() {
        return registeredClient;
    }
}
