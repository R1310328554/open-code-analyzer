/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.protocol.oid4vc.model;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * 凭证发放 URI 的返回形式。
 * <p>控制客户端收到 QR 码、纯 URI 或二者组合。</p>
 *
 * @author <a href="https://github.com/wistefan">Stefan Wiedemann</a>
 */
public enum OfferResponseType {

    /** 仅返回 QR 码内容。 */
    QR("qr"),
    /** 仅返回 URI 字符串。 */
    URI("uri"),
    /** 同时返回 URI 与 QR 码。 */
    URI_QR("uri_qr");

    /** 配置/序列化使用的字符串值。 */
    private final String value;

    OfferResponseType(String value) {
        this.value = value;
    }

    /** @return 枚举对应的字符串值 */
    public String getValue() {
        return value;
    }

    /**
     * 从字符串解析枚举值。
     * @param value 配置字符串
     * @return 对应枚举常量
     * @throws IllegalArgumentException 不支持的取值
     */
    @JsonCreator
    public static OfferResponseType fromString(String value) {
        return Optional.ofNullable(value)
                .map(v -> {
                    if (v.equals(QR.getValue())) {
                        return QR;
                    } else if (v.equals(URI.getValue())) {
                        return URI;
                    } else if (v.equals(URI_QR.getValue())) {
                        return URI_QR;
                    } else return null;
                })
                .orElseThrow(() -> new IllegalArgumentException(String.format("%s is not a supported OfferUriType.", value)));
    }
}
