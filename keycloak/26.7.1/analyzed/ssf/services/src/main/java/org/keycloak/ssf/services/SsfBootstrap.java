package org.keycloak.ssf.services;

import org.keycloak.models.RealmModel;
import org.keycloak.ssf.transmitter.SsfScopes;

/**
 * SSF 启动引导：在 realm 创建或启用 SSF 发送方时初始化默认客户端作用域。
 */
public class SsfBootstrap {

    public static void addSsfSupport(RealmModel realm) {
        if (realm == null) {
            return;
        }
        SsfScopes.createDefaultClientScopes(realm);
    }
}
