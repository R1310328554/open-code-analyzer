package org.keycloak.ssf.transmitter.stream;

/**
 * 流上的来源/归属标记——记录谁注册了该流以及哪个界面在概念上「拥有」配置。
 * 在创建时设定且此后不可变。
 *
 * <p>两种模式均允许管理员与接收方驱动的更新；该标记 purely 供信息展示，
 * 使管理 UI 能渲染流的归属界面，并在操作员即将覆盖接收方托管配置时发出警告。
 * 分发器的门控不读取此字段——属于管理/审计关注点。</p>
 */
public enum ManagedBy {

    /**
     * 通过面向接收方的 {@code POST /streams} 端点（SSF §8.1.1）注册的流。
     * 接收方客户端是配置的概念所有者；仍允许管理员编辑，但 UI 会标记为覆盖操作。
     *
     * <p>引入该标记之前已持久化的流默认为此值（属性缺失 → {@code RECEIVER}）。</p>
     */
    RECEIVER,

    /**
     * 通过管理端 {@code POST /admin/realms/{realm}/ssf/clients/{clientId}/stream}
     * 端点（由管理 UI 的 Stream 选项卡驱动）注册的流。接收方从未调用 SSF 规范端点；
     * 管理员拥有配置。仍接受接收方驱动的更新，但生命周期由操作员主导。
     */
    KEYCLOAK;

    /**
     * 读取持久化属性时由 {@link org.keycloak.ssf.transmitter.stream.storage.client.ClientStreamStore}
     * 使用的防御性解析。未知/不可解析的值回退为 {@link #RECEIVER}（遗留默认值），
     * 避免对手动篡改客户端属性导致流陷入无法识别的模式。
     */
    public static ManagedBy parseOrDefault(String raw, ManagedBy fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}
