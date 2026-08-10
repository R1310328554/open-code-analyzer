package org.keycloak.ssf.event;

import org.keycloak.ssf.SsfException;

/**
 * {@link SsfEvent#validate()} 在事件实例缺少 SSF / CAEP / RISC 规范 REQUIRED 字段
 * 或自定义事件违反不变量时抛出。
 * <p>携带稳定 {@link #MESSAGE_KEY}（{@code invalid_event_data}）及结构化
 * {@code eventAlias}、{@code field}，供 REST emit 响应与管理 UI 本地化消息。</p>
 * <p>合成 emit 管道捕获后以 {@code invalid_event_payload} 返回，便于操作员定位缺失字段。</p>
 * <p>原生事件生产不会抛出；异常位于本层以便 {@link SsfEvent} 子类在 validate 中使用，
 * 无需依赖 transmitter 模块。</p>
 */
public class SsfEventValidationException extends SsfException {

    /** 所有验证失败的稳定、可 i18n 的消息键；配合 {@link #getEventAlias()} / {@link #getField()} 本地化。 */
    public static final String MESSAGE_KEY = "invalid_event_data";

    private final String eventAlias;
    private final String field;

    public SsfEventValidationException(String eventAlias, String field) {
        super(MESSAGE_KEY + ": " + eventAlias + "." + field);
        this.eventAlias = eventAlias;
        this.field = field;
    }

    public String getMessageKey() {
        return MESSAGE_KEY;
    }

    /** 验证失败的事件别名（如 {@code CaepCredentialChange}），来自抛出点的 {@link SsfEvent#getAlias()}。 */
    public String getEventAlias() {
        return eventAlias;
    }

    /** 验证失败的字段 wire 名（如 {@code change_type}），使用 {@code @JsonProperty} 值而非 Java 字段名。 */
    public String getField() {
        return field;
    }
}
