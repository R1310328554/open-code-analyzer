/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.representations.idm;

/**
 * 客户端或 realm 密钥材料的 PEM 表示，包含私钥、公钥、证书及 JWKS。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class CertificateRepresentation {

    /** PEM 编码的私钥。 */
    protected String privateKey;
    /** PEM 编码的公钥。 */
    protected String publicKey;
    /** PEM 编码的 X.509 证书。 */
    protected String certificate;
    /** 密钥 ID（kid），用于 JWT 头标识。 */
    protected String kid;
    /** JSON Web Key Set 字符串。 */
    protected String jwks;

    /** @return PEM 私钥 */
    public String getPrivateKey() {
        return privateKey;
    }

    /** @param privateKey PEM 私钥 */
    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    /** @return PEM 公钥 */
    public String getPublicKey() {
        return publicKey;
    }

    /** @param publicKey PEM 公钥 */
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    /** @return PEM 证书 */
    public String getCertificate() {
        return certificate;
    }

    /** @param certificate PEM 证书 */
    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    /** @return 密钥 ID */
    public String getKid() {
        return kid;
    }

    /** @param kid 密钥 ID */
    public void setKid(String kid) {
        this.kid = kid;
    }

    /** @return JWKS JSON 字符串 */
    public String getJwks() {
        return jwks;
    }

    /** @param jwks JWKS JSON 字符串 */
    public void setJwks(String jwks) {
        this.jwks = jwks;
    }
}
