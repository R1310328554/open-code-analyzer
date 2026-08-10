/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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
 *
 */

package org.keycloak.models.credential.dto;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * WebAuthn 凭据公开数据 DTO：AAGUID、凭据 ID、签名计数器、认证声明与公钥等。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class WebAuthnCredentialData {

    private final String aaguid;
    private final String credentialId;
    private long counter;
    private String attestationStatement;
    private String credentialPublicKey;
    private String attestationStatementFormat;
    private Set<String> transports;

    /** Jackson 反序列化构造器。 */
    @JsonCreator
    public WebAuthnCredentialData(@JsonProperty("aaguid") String aaguid,
                                  @JsonProperty("credentialId") String credentialId,
                                  @JsonProperty("counter") long counter,
                                  @JsonProperty("attestationStatement") String attestationStatement,
                                  @JsonProperty("credentialPublicKey") String credentialPublicKey,
                                  @JsonProperty("attestationStatementFormat") String attestationStatementFormat,
                                  @JsonProperty("transports") Set<String> transports) {
        this.aaguid = aaguid;
        this.credentialId = credentialId;
        this.counter = counter;
        this.attestationStatement = attestationStatement;
        this.credentialPublicKey = credentialPublicKey;
        this.attestationStatementFormat = attestationStatementFormat;
        this.transports = transports;
    }

    /** @return 认证器 AAGUID */
    public String getAaguid() {
        return aaguid;
    }

    /** @return 凭据 ID（Base64） */
    public String getCredentialId() {
        return credentialId;
    }

    /** @return 认证声明 */
    public String getAttestationStatement() {
        return attestationStatement;
    }

    /** @return 凭据公钥（COSE Key） */
    public String getCredentialPublicKey() {
        return credentialPublicKey;
    }

    /** @return 签名计数器（防克隆） */
    public long getCounter() {
        return counter;
    }

    /** @param counter 签名计数器 */
    public void setCounter(long counter) {
        this.counter = counter;
    }

    /** @return 认证声明格式（如 packed、fido-u2f） */
    public String getAttestationStatementFormat() {
        return attestationStatementFormat;
    }

    /** @param attestationStatementFormat 认证声明格式 */
    public void setAttestationStatementFormat(String attestationStatementFormat) {
        this.attestationStatementFormat = attestationStatementFormat;
    }

    /** @return 认证器传输方式集合（如 usb、nfc） */
    public Set<String> getTransports() {
        return transports != null ? transports : Collections.emptySet();
    }

    /** @param transports 认证器传输方式 */
    public void setTransports(Set<String> transports) {
        this.transports = transports;
    }

    @Override
    public String toString() {
        return "WebAuthnCredentialData { " +
                "aaguid='" + aaguid + '\'' +
                ", credentialId='" + credentialId + '\'' +
                ", counter=" + counter +
                ", credentialPublicKey=" + credentialPublicKey +
                ", attestationStatement='" + attestationStatement + '\'' +
                ", credentialPublicKey='" + credentialPublicKey + '\'' +
                ", attestationStatementFormat='" + attestationStatementFormat + '\'' +
                ", transports=" + Arrays.toString(getTransports().toArray()) +
                " }";
    }
}
