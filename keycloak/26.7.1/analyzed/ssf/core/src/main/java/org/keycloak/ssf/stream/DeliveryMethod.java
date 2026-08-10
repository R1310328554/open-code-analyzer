/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

import org.keycloak.ssf.Ssf;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * SSF SET 投递方式枚举，映射规范定义的投递 URI。
 * <p>含标准 SSF（RFC 8935/8936）与旧版 RISC 变体。</p>
 */
public enum DeliveryMethod {

    /** 标准 SSF HTTP Push 投递（RFC 8935）。 */
    PUSH(Ssf.DELIVERY_METHOD_PUSH_URI),
    /** 标准 SSF HTTP Poll 投递（RFC 8936）。 */
    POLL(Ssf.DELIVERY_METHOD_POLL_URI),

    /** 旧版 RISC Push 投递 URI。 */
    RISC_PUSH(Ssf.DELIVERY_METHOD_RISC_PUSH_URI),
    /** 旧版 RISC Poll 投递 URI。 */
    RISC_POLL(Ssf.DELIVERY_METHOD_RISC_POLL_URI);

    private final String specUrn;

    DeliveryMethod(String specUrn) {
        this.specUrn = specUrn;
    }

    /**
     * 按规范 URI 解析 {@link DeliveryMethod}。
     * @param deliveryMethod 投递方式 URI 字符串
     * @return 匹配的枚举常量
     * @throws IllegalArgumentException 未知 URI
     */
    public static DeliveryMethod valueOfUri(String deliveryMethod) {
        for(DeliveryMethod dm : values()) {
            if (dm.specUrn.equals(deliveryMethod)) {
                return dm;
            }
        }
        throw new IllegalArgumentException("Unknown delivery method: " + deliveryMethod);
    }

    /**
     * 粗粒度 PUSH/POLL 族分类。每种传输的标准与旧版 RISC 变体
     * （RFC 8935 + RISC PUSH；RFC 8936 + RISC POLL）归入同一族，
     * 使 per-client 白名单基于运维心智模型而非四个独立 URI。
     */
    public DeliveryMethodFamily family() {
        return switch (this) {
            case PUSH, RISC_PUSH -> DeliveryMethodFamily.PUSH;
            case POLL, RISC_POLL -> DeliveryMethodFamily.POLL;
        };
    }

    /** @return 规范定义的投递方式 URI（Jackson 序列化值） */
    @JsonValue
    public String getSpecUri() {
        return specUrn;
    }

    @Override
    public String toString() {
        return specUrn;
    }
}
