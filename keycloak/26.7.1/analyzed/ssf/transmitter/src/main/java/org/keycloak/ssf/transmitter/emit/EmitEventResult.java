package org.keycloak.ssf.transmitter.emit;

/**
 * 合成 SSF 事件发射的结果。携带派发状态；成功时还包含已发出 SET 的 {@code jti}，
 * 便于调用方与发送方日志/发件箱状态关联；可选的人类可读消息用于呈现校验失败
 *（例如载荷形状与已注册事件类不匹配），使管理端点可返回含有效信息的 400 响应。
 */
public record EmitEventResult(EmitEventStatus status, String jti, String message) {

    public static EmitEventResult dispatched(String jti) {
        return new EmitEventResult(EmitEventStatus.DISPATCHED, jti, null);
    }

    public static EmitEventResult dropped(EmitEventStatus status) {
        return new EmitEventResult(status, null, null);
    }

    public static EmitEventResult dropped(EmitEventStatus status, String message) {
        return new EmitEventResult(status, null, message);
    }
}
