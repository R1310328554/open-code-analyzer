/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.representations;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OIDC {@code claims} 请求参数的 JSON 表示，指定 ID Token 与 UserInfo 中需返回的声明。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 * @see <a href="https://openid.net/specs/openid-connect-core-1_0.html#ClaimsParameter">Claims Parameter</a>
 */
public class ClaimsRepresentation {

    @JsonProperty("id_token")
    private Map<String, ClaimValue> idTokenClaims;

    @JsonProperty("userinfo")
    private Map<String, ClaimValue> userinfoClaims;

    public Map<String, ClaimValue> getIdTokenClaims() {
        return idTokenClaims;
    }

    public void setIdTokenClaims(Map<String, ClaimValue> idTokenClaims) {
        this.idTokenClaims = idTokenClaims;
    }

    public Map<String, ClaimValue> getUserinfoClaims() {
        return userinfoClaims;
    }

    public void setUserinfoClaims(Map<String, ClaimValue> userinfoClaims) {
        this.userinfoClaims = userinfoClaims;
    }

    // 辅助判断方法

    /**
     * 判断声明是否出现在 claims 参数中（含“空声明”或带值的声明）。
     *
     * @param claimName 声明名
     * @param ctx 目标上下文：ID Token 或 UserInfo
     * @return 存在时返回 {@code true}
     */
    public boolean isPresent(String claimName, ClaimContext ctx) {
        if (ctx == ClaimContext.ID_TOKEN) {
            return idTokenClaims != null && idTokenClaims.containsKey(claimName);
        } else if (ctx == ClaimContext.USERINFO){
            return userinfoClaims != null && userinfoClaims.containsKey(claimName);
        } else {
            throw new IllegalArgumentException("Invalid claim context");
        }
    }

    /**
     * 判断声明是否以 OIDC“空声明”（值为 null）形式出现。
     *
     * @param claimName 声明名
     * @param ctx 目标上下文
     * @return 为空声明时返回 {@code true}
     */
    public boolean isPresentAsNullClaim(String claimName, ClaimContext ctx) {
        if (!isPresent(claimName, ctx)) return false;

        if (ctx == ClaimContext.ID_TOKEN) {
            return idTokenClaims.get(claimName) == null;
        } else if (ctx == ClaimContext.USERINFO){
            return userinfoClaims.get(claimName) == null;
        } else {
            throw new IllegalArgumentException("Invalid claim context");
        }
    }

    /**
     * 获取指定上下文中某声明的 {@link ClaimValue}。
     *
     * @param claimName 声明名
     * @param ctx 目标上下文
     * @param claimType 声明值类型（用于泛型推断）
     * @return 声明值包装，不存在时返回 {@code null}
     */
    public <CLAIM_TYPE> ClaimValue<CLAIM_TYPE> getClaimValue(String claimName, ClaimContext ctx, Class<CLAIM_TYPE> claimType) {
        if (!isPresent(claimName, ctx)) return null;

        if (ctx == ClaimContext.ID_TOKEN) {
            return (ClaimValue<CLAIM_TYPE>) idTokenClaims.get(claimName);
        } else if (ctx == ClaimContext.USERINFO){
            return (ClaimValue<CLAIM_TYPE>) userinfoClaims.get(claimName);
        } else {
            throw new IllegalArgumentException("Invalid claim context");
        }
    }

    /** 声明应出现在 ID Token 还是 UserInfo 响应中。 */
    public enum ClaimContext {
        /** ID Token 声明块。 */
        ID_TOKEN,
        /** UserInfo 声明块。 */
        USERINFO
    }

    /**
     * 单个声明的请求方式：是否 essential、单值或多值。
     *
     * @param <CLAIM_TYPE> 声明值类型
     */
    public static class ClaimValue<CLAIM_TYPE> {

        private Boolean essential;

        private CLAIM_TYPE value;

        private List<CLAIM_TYPE> values;

        public Boolean getEssential() {
            return essential;
        }

        public boolean isEssential() {
            return essential != null && essential;
        }

        public void setEssential(Boolean essential) {
            this.essential = essential;
        }

        public CLAIM_TYPE getValue() {
            return value;
        }

        public void setValue(CLAIM_TYPE value) {
            this.value = value;
        }

        public List<CLAIM_TYPE> getValues() {
            return values;
        }

        public void setValues(List<CLAIM_TYPE> values) {
            this.values = values;
        }
    }
}
