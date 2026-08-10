package org.keycloak.protocol.oidc.ext;

import org.keycloak.events.EventBuilder;
import org.keycloak.provider.Provider;

/**
 * OpenID Connect 扩展 Provider：允许在 OIDC 流程中注入扩展行为。
 */
public interface OIDCExtProvider extends Provider {

    /** 绑定当前 OIDC 事件构建器 @param event 事件构建器 */
    void setEvent(EventBuilder event);

    @Override
    default void close() {
    }

}
