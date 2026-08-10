package org.keycloak.ssf.transmitter;

import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.ssf.SsfException;
import org.keycloak.ssf.transmitter.support.SsfUtil;

/**
 * SSF 发送方静态工具类：解析 {@link SsfTransmitterProvider} 与接收方客户端。
 */
public final class SsfTransmitter {

    private SsfTransmitter() {
    }

    /**
     * 解析给定 session 绑定的 {@link SsfTransmitterProvider}。
     * 统一查找入口，便于后续添加缓存、日志或测试插桩。
     *
     * @param session 当前 Keycloak session，不得为 {@code null}
     * @return 绑定于 {@code session} 的 SSF 发送方提供者
     */
    public static SsfTransmitterProvider of(KeycloakSession session) {
        return session.getProvider(SsfTransmitterProvider.class);
    }

    /**
     * 在当前 realm 中按 OAuth {@code client_id} 查找 SSF 接收方客户端。
     * 客户端不存在或未配置为 SSF 接收方时抛出 {@link SsfException}，
     * 避免程序化 emit 调用方因返回 {@code null} 而后续得到令人困惑的 {@code STREAM_NOT_FOUND}。
     *
     * @param session 当前 Keycloak session
     * @param clientClientId OAuth {@code client_id}（非内部 UUID）
     * @return 解析到的接收方客户端，永不为 {@code null}
     */
    public static ClientModel getReceiverClient(KeycloakSession session, String clientClientId) {
        RealmModel realm = session.getContext().getRealm();
        ClientModel client = realm.getClientByClientId(clientClientId);
        if (client == null) {
            throw new SsfException("No client with clientId '" + clientClientId + "' in realm '"
                    + realm.getName() + "'");
        }
        // Confirm the client is a receiver before checking its on/off state
        if (!SsfUtil.isReceiverClient(client)) {
            throw new SsfException("Client '" + clientClientId + "' is not an SSF Receiver");
        }
        if (!client.isEnabled()) {
            throw new SsfException("Client '" + clientClientId + "' is disabled");
        }
        return client;
    }

}
