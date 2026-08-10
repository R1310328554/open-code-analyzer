package org.keycloak.ssf.transmitter.emit;

/**
 * 合成 SSF 事件发射尝试的结果类别。
 *
 * <p>返回给可信发射方（IAM 管理客户端），以便区分成功派发与被过滤器丢弃的事件。
 * 线格式值稳定，用作管理 emit 端点响应中的 {@code status} 字段。</p>
 */
public enum EmitEventStatus {

    /** 事件已接受，已签名的 SET 已入队等待 push 投递。 */
    DISPATCHED("dispatched"),

    /** 接收方未订阅此事件类型（{@code events_requested}）。 */
    DROPPED_FILTERED("dropped_filtered"),

    /** 主体未订阅接收该接收方的事件。 */
    DROPPED_UNSUBSCRIBED("dropped_unsubscribed"),

    /** 无法将主体解析为 Keycloak 用户或组织。 */
    SUBJECT_NOT_FOUND("subject_not_found"),

    /** 接收方尚未注册 SSF 流。 */
    STREAM_NOT_FOUND("stream_not_found"),

    /**
     * 接收方已配置为 SSF 接收方，但其 Keycloak 客户端当前已禁用。
     * 流配置保持完整；重新启用客户端即可恢复投递。
     * 与派发路径上的门控一致，防止合成发射方向运维已关闭的接收方投递
     *（keycloak/keycloak#50050）。
     */
    RECEIVER_DISABLED("receiver_disabled"),

    /** 流存在但未配置投递方式。 */
    NO_DELIVERY_CONFIG("no_delivery_config"),

    /** 事件类型别名/URI 未在发送方注册。 */
    UNKNOWN_EVENT_TYPE("unknown_event_type"),

    /**
     * 事件类型已注册但不允许通过合成发射 API 发出——例如 SSF 流管理事件
     *（verification、stream-updated），这些为协议内部事件，仅可由发送方自身签发。
     */
    EVENT_TYPE_NOT_EMITTABLE("event_type_not_emittable"),

    /** 载荷缺少必填字段或格式错误。 */
    INVALID_REQUEST("invalid_request"),

    /**
     * 事件载荷反序列化成功，但事件类自身的 {@code validate()} 钩子拒绝了它——
     * 通常是缺少规范要求的字段（例如 {@code CaepCredentialChange} 上的 {@code change_type}）。
     * 与 {@link #INVALID_REQUEST} 区分，便于管理端调用方区分 JSON 格式错误/类型错误与缺字段问题。
     * 线格式值与 {@link org.keycloak.ssf.event.SsfEventValidationException#MESSAGE_KEY} 一致，
     * 两层共用同一 i18n 键。
     */
    INVALID_EVENT_DATA("invalid_event_data");

    private final String wireValue;

    EmitEventStatus(String wireValue) {
        this.wireValue = wireValue;
    }

    public String wireValue() {
        return wireValue;
    }
}
