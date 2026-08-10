package org.keycloak.services.clientpolicy.context;

import org.keycloak.models.ClientModel;
import org.keycloak.services.clientpolicy.ClientPolicyContext;

/**
 * 携带 {@link ClientModel} 的 {@link ClientPolicyContext}：供底层条件与 Executor 访问当前客户端。
 * <p>常见于授权请求、令牌请求及客户端 CRUD 完成后的上下文。</p>
 */
public interface ClientModelContext extends ClientPolicyContext {

    /** @return 当前策略事件关联的客户端 */
    ClientModel getClient();
}
