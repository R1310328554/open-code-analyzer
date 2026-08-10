/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.ssf.transmitter.delivery.push;

/**
 * 向接收方端点单次 push 投递的结构化结果。
 * 替代原先的简单布尔返回值，使 {@link org.keycloak.ssf.transmitter.outbox.SsfPushDeliveryHandler
 * SsfPushDeliveryHandler} 能将接收方的 HTTP 状态与响应体——或底层传输异常——
 * 写入发件箱行的 {@code last_error} 摘要及 {@code metadata.lastFailure} 结构化详情。
 *
 * <p>三种终态形态：</p>
 * <ul>
 *   <li>{@link #delivered(int, String) delivered}：接收方返回 2xx，携带状态码，无错误字段。</li>
 *   <li>{@link #httpFailure(int, String, String) httpFailure}：接收方返回非 2xx，
 *       携带状态码及（可选截断的）响应体供运维查看。</li>
 *   <li>{@link #transportFailure(Throwable, String) transportFailure}：未收到 HTTP 响应
 *       （DNS 解析失败、连接被拒、套接字超时等），携带异常类名与消息；status/body 为 null。</li>
 * </ul>
 */
public record PushDeliveryOutcome(boolean delivered,
                                  Integer status,
                                  String responseBody,
                                  String exceptionClass,
                                  String exceptionMessage,
                                  String endpointUrl) {

    public static PushDeliveryOutcome delivered(int status, String endpointUrl) {
        return new PushDeliveryOutcome(true, status, null, null, null, endpointUrl);
    }

    public static PushDeliveryOutcome httpFailure(int status, String responseBody, String endpointUrl) {
        return new PushDeliveryOutcome(false, status, responseBody, null, null, endpointUrl);
    }

    public static PushDeliveryOutcome transportFailure(Throwable t, String endpointUrl) {
        String exClass = t.getClass().getName();
        String exMessage = t.getMessage();
        return new PushDeliveryOutcome(false, null, null, exClass, exMessage, endpointUrl);
    }

    /**
     * 流配置本身无效时使用（缺少 endpoint URL 或无 delivery 段）。
     * 不会发起 HTTP 请求；结果携带合成的 {@code exceptionClass} 标记，
     * 以便处理器识别该情况并将其路由至 ORPHANED 而非 RETRY。
     */
    public static PushDeliveryOutcome invalidConfig(String reason) {
        return new PushDeliveryOutcome(false, null, null, "InvalidStreamConfig", reason, null);
    }
}
