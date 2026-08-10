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

package org.keycloak.keys;

import java.security.PublicKey;
import java.security.cert.Certificate;

/**
 * RSA 密钥元数据：扩展 {@link KeyMetadata}，附加公钥与 X.509 证书。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class RsaKeyMetadata extends KeyMetadata {

    private PublicKey publicKey;
    private Certificate certificate;

    /** @return RSA 公钥 */
    public PublicKey getPublicKey() {
        return publicKey;
    }

    /** @param publicKey RSA 公钥 */
    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    /** @return 关联的 X.509 证书 */
    public Certificate getCertificate() {
        return certificate;
    }

    /** @param certificate X.509 证书 */
    public void setCertificate(Certificate certificate) {
        this.certificate = certificate;
    }

}
