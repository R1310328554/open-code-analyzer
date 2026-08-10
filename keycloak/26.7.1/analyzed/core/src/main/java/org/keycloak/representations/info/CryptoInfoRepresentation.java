/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.keycloak.representations.info;

import java.util.List;

/**
 * 加密子系统信息的 REST 表示，报告当前加密提供方及支持的算法与密钥库类型。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class CryptoInfoRepresentation {

    /** 当前使用的加密提供方名称。 */
    private String cryptoProvider;
    /** 支持的密钥库类型列表。 */
    private List<String> supportedKeystoreTypes;

    /** 客户端签名支持的 symmetric 算法列表。 */
    private List<String> clientSignatureSymmetricAlgorithms;

    /** 客户端签名支持的 asymmetric 算法列表。 */
    private List<String> clientSignatureAsymmetricAlgorithms;

    /** @return 加密提供方名称 */
    public String getCryptoProvider() {
        return cryptoProvider;
    }

    /** @param cryptoProvider 加密提供方名称 */
    public void setCryptoProvider(String cryptoProvider) {
        this.cryptoProvider = cryptoProvider;
    }

    /** @return 支持的密钥库类型 */
    public List<String> getSupportedKeystoreTypes() {
        return supportedKeystoreTypes;
    }

    /** @param supportedKeystoreTypes 支持的密钥库类型 */
    public void setSupportedKeystoreTypes(List<String> supportedKeystoreTypes) {
        this.supportedKeystoreTypes = supportedKeystoreTypes;
    }

    /** @return 客户端对称签名算法 */
    public List<String> getClientSignatureSymmetricAlgorithms() {
        return clientSignatureSymmetricAlgorithms;
    }

    /** @param clientSignatureSymmetricAlgorithms 客户端对称签名算法 */
    public void setClientSignatureSymmetricAlgorithms(List<String> clientSignatureSymmetricAlgorithms) {
        this.clientSignatureSymmetricAlgorithms = clientSignatureSymmetricAlgorithms;
    }

    /** @return 客户端非对称签名算法 */
    public List<String> getClientSignatureAsymmetricAlgorithms() {
        return clientSignatureAsymmetricAlgorithms;
    }

    /** @param clientSignatureAsymmetricAlgorithms 客户端非对称签名算法 */
    public void setClientSignatureAsymmetricAlgorithms(List<String> clientSignatureAsymmetricAlgorithms) {
        this.clientSignatureAsymmetricAlgorithms = clientSignatureAsymmetricAlgorithms;
    }
}
