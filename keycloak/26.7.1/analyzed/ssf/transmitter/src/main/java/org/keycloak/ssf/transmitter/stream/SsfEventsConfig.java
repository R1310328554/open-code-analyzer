package org.keycloak.ssf.transmitter.stream;

import java.util.Set;

/**
 * SSF 流的事件集合配置：发送方支持、接收方请求与实际投递的事件类型。
 *
 * @param eventsSupported 发送方对该接收方支持的事件类型 URI 集合
 * @param eventsRequested 接收方请求的事件类型集合
 * @param eventsDelivered 实际投递的事件类型（{@code events_requested ∩ events_supported}）
 */
public record SsfEventsConfig(Set<String> eventsSupported, Set<String> eventsRequested, Set<String> eventsDelivered) {
}
