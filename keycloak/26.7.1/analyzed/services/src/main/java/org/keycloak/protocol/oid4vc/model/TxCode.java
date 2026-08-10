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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OID4VCI 凭证发放预授权 grant 中的交易码（tx_code）。
 * <p>描述用户输入方式、长度及展示说明。</p>
 * {@see https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#name-credential-offer}
 *
 * @author <a href="https://github.com/wistefan">Stefan Wiedemann</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TxCode {

    /** 用户输入模式（如 numeric）。 */
    @JsonProperty("input_mode")
    private String inputMode;

    /** 交易码长度。 */
    @JsonProperty("length")
    private int length;

    /** 向用户展示的交易码说明。 */
    @JsonProperty("description")
    private String description;

    /** @return 输入模式 */
    public String getInputMode() {
        return inputMode;
    }

    /** @param inputMode 输入模式
     * @return 当前实例 */
    public TxCode setInputMode(String inputMode) {
        this.inputMode = inputMode;
        return this;
    }

    /** @return 交易码长度 */
    public int getLength() {
        return length;
    }

    /** @param length 交易码长度
     * @return 当前实例 */
    public TxCode setLength(int length) {
        this.length = length;
        return this;
    }

    /** @return 展示说明 */
    public String getDescription() {
        return description;
    }

    /** @param description 展示说明
     * @return 当前实例 */
    public TxCode setDescription(String description) {
        this.description = description;
        return this;
    }
}
