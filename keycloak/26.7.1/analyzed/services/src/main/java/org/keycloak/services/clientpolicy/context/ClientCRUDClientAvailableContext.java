package org.keycloak.services.clientpolicy.context;

import org.keycloak.models.ClientModel;

/**
 * 客户端 CRUD 上下文扩展：目标 {@link ClientModel} 已可用（注册完成/读取/更新/注销等场景）。
 * <p>默认将 {@link #getTargetClient()} 作为 {@link ClientModelContext#getClient()} 返回值。</p>
 */
public interface ClientCRUDClientAvailableContext extends ClientCRUDContext, ClientModelContext {

    /** {@inheritDoc} 委托 {@link #getTargetClient()} */
    @Override
    default ClientModel getClient() {
        return getTargetClient();
    }
}
