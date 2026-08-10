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

package org.keycloak.adapters.saml.config;

import java.io.Serializable;

/**
 * SAML 密钥配置：支持 PEM 内联或 KeyStore 引用，可标记为签名或加密用途。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class Key implements Serializable {

    /** KeyStore 文件或 classpath 资源及别名配置。 */
    public static class KeyStoreConfig implements Serializable {
        /** 文件系统路径。 */
        private String file;
        /** classpath 资源路径。 */
        private String resource;
        private String password;
        /** KeyStore 类型，默认 JKS。 */
        private String type;
        private String alias;
        /** 私钥条目别名。 */
        private String privateKeyAlias;
        private String privateKeyPassword;
        /** 证书条目别名。 */
        private String certificateAlias;


        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public String getResource() {
            return resource;
        }

        public void setResource(String resource) {
            this.resource = resource;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getPrivateKeyAlias() {
            return privateKeyAlias;
        }

        public void setPrivateKeyAlias(String privateKeyAlias) {
            this.privateKeyAlias = privateKeyAlias;
        }

        public String getPrivateKeyPassword() {
            return privateKeyPassword;
        }

        public void setPrivateKeyPassword(String privateKeyPassword) {
            this.privateKeyPassword = privateKeyPassword;
        }

        public String getCertificateAlias() {
            return certificateAlias;
        }

        public void setCertificateAlias(String certificateAlias) {
            this.certificateAlias = certificateAlias;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getAlias() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }
    }


    /** 是否用作 SP/IdP 出站消息签名。 */
    private boolean signing;
    /** 是否用作断言解密。 */
    private boolean encryption;
    private KeyStoreConfig keystore;
    /** PEM 编码私钥（与 keystore 二选一）。 */
    private String privateKeyPem;
    private String publicKeyPem;
    private String certificatePem;


    public boolean isSigning() {
        return signing;
    }

    public void setSigning(Boolean signing) {
        this.signing = signing != null && signing;
    }

    public boolean isEncryption() {
        return encryption;
    }

    public void setEncryption(Boolean encryption) {
        this.encryption = encryption != null && encryption;
    }

    public KeyStoreConfig getKeystore() {
        return keystore;
    }

    public void setKeystore(KeyStoreConfig keystore) {
        this.keystore = keystore;
    }

    public String getPrivateKeyPem() {
        return privateKeyPem;
    }

    public void setPrivateKeyPem(String privateKeyPem) {
        this.privateKeyPem = privateKeyPem;
    }

    public String getPublicKeyPem() {
        return publicKeyPem;
    }

    public void setPublicKeyPem(String publicKeyPem) {
        this.publicKeyPem = publicKeyPem;
    }

    public String getCertificatePem() {
        return certificatePem;
    }

    public void setCertificatePem(String certificatePem) {
        this.certificatePem = certificatePem;
    }
}
