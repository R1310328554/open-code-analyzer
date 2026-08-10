/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.representations.account;

import java.net.URI;

/**
 * 账户控制台中发起身份提供者账户关联时返回的链接 URI 表示，包含防重放 nonce 与完整性校验 hash。
 *
 * @author Stan Silvert
 */
public class AccountLinkUriRepresentation {
    /** 用户应访问以完成账户关联的 URI。 */
    private URI accountLinkUri;
    /** 一次性随机数，用于防止 CSRF 与重放攻击。 */
    private String nonce;
    /** 对上述字段计算的完整性哈希值。 */
    private String hash;

    /** @return 账户关联跳转 URI */
    public URI getAccountLinkUri() {
        return accountLinkUri;
    }

    /** @param accountLinkUri 账户关联跳转 URI */
    public void setAccountLinkUri(URI accountLinkUri) {
        this.accountLinkUri = accountLinkUri;
    }

    /** @return 防重放 nonce */
    public String getNonce() {
        return nonce;
    }

    /** @param nonce 防重放 nonce */
    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    /** @return 完整性校验 hash */
    public String getHash() {
        return hash;
    }

    /** @param hash 完整性校验 hash */
    public void setHash(String hash) {
        this.hash = hash;
    }
}
