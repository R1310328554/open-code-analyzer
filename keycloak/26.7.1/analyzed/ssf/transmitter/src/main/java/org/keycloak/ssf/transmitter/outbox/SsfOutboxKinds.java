package org.keycloak.ssf.transmitter.outbox;

/**
 * 通用发件箱（{@link org.keycloak.events.outbox.OutboxStore}）的 SSF 专用 {@code entryKind} 常量。
 * push 与 poll 运行时路径不同，故有两种 kind：
 *
 * <ul>
 *   <li>{@link #PUSH} — 由服务端 {@link org.keycloak.events.outbox.OutboxDrainerTask}
 *       排空并将行投递到接收方 HTTP 端点。</li>
 *   <li>{@link #POLL} — 永不排空；行等待接收方 POLL 请求从发件箱读取。</li>
 * </ul>
 *
 * <p>两种 kind 下，行的 {@code entryType} 携带 SSF 安全事件类型（如 {@code session-revoked}），
 * {@code metadata} 携带无通用列的 SSF 扩展（streamId 等）。</p>
 */
public final class SsfOutboxKinds {

    /** 通过 HTTP push 投递的 SET 行发件箱 kind（RFC 8935）。 */
    public static final String PUSH = "ssf-push";

    /** 在接收方 POLL 上提供的 SET 行发件箱 kind（RFC 8936）。 */
    public static final String POLL = "ssf-poll";

    private SsfOutboxKinds() {
    }
}
