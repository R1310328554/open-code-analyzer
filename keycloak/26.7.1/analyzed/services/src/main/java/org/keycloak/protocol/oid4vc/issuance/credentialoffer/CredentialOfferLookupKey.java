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
package org.keycloak.protocol.oid4vc.issuance.credentialoffer;

/**
 * 将一次性缓存键（{@code credentialsOfferId}）嵌入对外可见的查找值（nonce、凭证标识符等）。
 * <p>这样在 {@code singleUseObjects} 中只需存储一条记录，即可通过 nonce 等公开值反查凭证发放状态。</p>
 */
public final class CredentialOfferLookupKey {

    /** 公开部分与 offerId 之间的分隔符。 */
    private static final char SEPARATOR = ':';

    /** 工具类，禁止实例化。 */
    private CredentialOfferLookupKey() {
    }

    /**
     * 将 offerId 嵌入公开查找值。
     * @param publicPart 对外可见部分（如随机 nonce）
     * @param offerId 凭证发放内部标识
     * @return {@code publicPart + ':' + offerId}
     */
    public static String embed(String publicPart, String offerId) {
        return publicPart + SEPARATOR + offerId;
    }

    /**
     * 从嵌入后的查找值中提取 offerId。
     * @param lookupValue 含分隔符的查找字符串
     * @return offerId；格式无效时返回 {@code null}
     */
    public static String extractOfferId(String lookupValue) {
        if (lookupValue == null) {
            return null;
        }
        int separatorIndex = lookupValue.lastIndexOf(SEPARATOR);
        if (separatorIndex < 0 || separatorIndex == lookupValue.length() - 1) {
            return null;
        }
        return lookupValue.substring(separatorIndex + 1);
    }
}
