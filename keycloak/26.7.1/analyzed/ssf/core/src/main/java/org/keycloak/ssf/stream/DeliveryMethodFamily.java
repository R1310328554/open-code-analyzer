/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
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
package org.keycloak.ssf.stream;

/**
 * 将 {@link DeliveryMethod} 粗粒度划分为两类投递族：PUSH（发送方主动 POST 至接收方）
 * 与 POLL（接收方从发送方拉取）。用于按接收方客户端属性 {@code ssf.allowedDeliveryMethods}
 * 的准入控制，使管理员只需配置 push/poll 两种值，而无需逐一列举规范 URI 及旧版 SSE-CAEP RISC 变体。
 *
 * <p>通过 {@link DeliveryMethod#family()} 将具体投递方式映射到其族，或使用
 * {@link #ofMethodValue(String)} 解析 wire/属性值（小写 {@code push} / {@code poll}，输入不区分大小写）。</p>
 */
public enum DeliveryMethodFamily {

    /** 发送方发起的 HTTP POST 投递（RFC 8935 及旧版 RISC PUSH）。 */
    PUSH,

    /** 接收方发起的 HTTP GET 拉取投递（RFC 8936 及旧版 RISC POLL）。 */
    POLL;

    /**
     * 将属性或 wire 值解析为 {@link DeliveryMethodFamily}。
     * 不区分大小写且忽略首尾空白；无法识别时返回 {@code null} 而非抛异常——
     * 读取每客户端允许投递方式列表时，调用方通常将未知项视为跳过。
     */
    public static DeliveryMethodFamily ofMethodValue(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase();
        return switch (normalized) {
            case "push" -> PUSH;
            case "poll" -> POLL;
            default -> null;
        };
    }

    /** 写入客户端属性时使用的规范小写字符串。 */
    public String getValue() {
        return name().toLowerCase();
    }
}
