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

package org.keycloak.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.keycloak.policy.PasswordPolicyConfigException;
import org.keycloak.policy.PasswordPolicyProvider;

import org.jboss.logging.Logger;

/**
 * 密码策略：封装 Realm 密码哈希、历史、过期与认证时效等策略配置。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class PasswordPolicy implements Serializable {

    protected static final Logger logger = Logger.getLogger(PasswordPolicy.class);

    /** 哈希算法策略 ID。 */
    public static final String HASH_ALGORITHM_ID = "hashAlgorithm";

    /** 哈希迭代次数策略 ID。 */
    public static final String HASH_ITERATIONS_ID = "hashIterations";

    /** 密码历史策略 ID。 */
    public static final String PASSWORD_HISTORY_ID = "passwordHistory";

    /** 强制过期密码变更策略 ID。 */
    public static final String FORCE_EXPIRED_ID = "forceExpiredPasswordChange";

    @Deprecated
    public static final int RECOVERY_CODES_WARNING_THRESHOLD_DEFAULT = 4;

    @Deprecated
    public static final String RECOVERY_CODES_WARNING_THRESHOLD_ID = "recoveryCodesWarningThreshold";

    /** 最大认证时效策略 ID。 */
    public static final String MAX_AUTH_AGE_ID = "maxAuthAge";

    /** 密码年龄策略 ID。 */
    public static final String PASSWORD_AGE = "passwordAge";

    private Map<String, Object> policyConfig;
    private Builder builder;

    /** @return 空密码策略 */
    public static PasswordPolicy empty() {
        return new PasswordPolicy(null, new HashMap<>());
    }

    /** @return 密码策略构建器 */
    public static Builder build() {
        return new Builder();
    }

    /** @param session 会话
     * @param policyString 策略字符串
     * @return 解析后的密码策略 */
    public static PasswordPolicy parse(KeycloakSession session, String policyString) {
        return new Builder(policyString).build(session);
    }

    private PasswordPolicy(Builder builder, Map<String, Object> policyConfig) {
        this.builder = builder;
        this.policyConfig = policyConfig;
    }

    /** @return 已配置策略键集合 */
    public Set<String> getPolicies() {
        return policyConfig.keySet();
    }

    /** @param key 策略键
     * @return 策略配置值 */
    public <T> T getPolicyConfig(String key) {
        return (T) policyConfig.get(key);
    }

    /** @return 哈希算法名称，未配置时返回 null */
    public String getHashAlgorithm() {
        if (policyConfig.containsKey(HASH_ALGORITHM_ID)) {
            return getPolicyConfig(HASH_ALGORITHM_ID);
        } else {
            return null;
        }
    }

    /** @return 哈希迭代次数，未配置时返回 -1 */
    public int getHashIterations() {
        if (policyConfig.containsKey(HASH_ITERATIONS_ID)) {
            return getPolicyConfig(HASH_ITERATIONS_ID);
        } else {
            return -1;
        }
    }

    /** @return 密码历史保留数量，未配置时返回 -1 */
    public int getExpiredPasswords() {
        if (policyConfig.containsKey(PASSWORD_HISTORY_ID)) {
            return getPolicyConfig(PASSWORD_HISTORY_ID);
        } else {
            return -1;
        }
    }

    /** @return 密码最大使用天数，未配置时返回 -1 */
    public int getPasswordAgeInDays() {
        if (policyConfig.containsKey(PASSWORD_AGE)) {
            return getPolicyConfig(PASSWORD_AGE);
        } else {
            return -1;
        }
    }

    /** @return 强制变更密码前的剩余天数，未配置时返回 -1 */
    public int getDaysToExpirePassword() {
        if (policyConfig.containsKey(FORCE_EXPIRED_ID)) {
            return getPolicyConfig(FORCE_EXPIRED_ID);
        } else {
            return -1;
        }
    }

    @Deprecated
    public int getRecoveryCodesWarningThreshold() {
        if (policyConfig.containsKey(RECOVERY_CODES_WARNING_THRESHOLD_ID)) {
            logger.warnf("It is deprecated to use Warning Threshold password policy. Please use the configuration on Recovery Authentication Codes required action instead.");
            return getPolicyConfig(RECOVERY_CODES_WARNING_THRESHOLD_ID);
        } else {
            return 4;
        }
    }

    /**
     * 获取最大认证时效（秒）；超过该值将强制重新认证。
     * Policy to configure the maximum age of the authentication in seconds.
     *
     * If the user authentication is older than the given value, a reauthentication is enforced.
     *
     * Examples:
     * <ul>
     * <li>{@code maxAuthAge(0)} means the user has to reauthenticate immediately.</li>
     * <li>{@code maxAuthAge(60)} means the user has to reauthenticate if authentication is older than 60 seconds.</li>
     * </ul>
     * @return
     */
    public int getMaxAuthAge() {
        if (policyConfig.containsKey(MAX_AUTH_AGE_ID)) {
            return getPolicyConfig(MAX_AUTH_AGE_ID);
        } else {
            return -1;
        }
    }

    @Override
    public String toString() {
        return builder.asString();
    }

    /** @return 可修改的构建器副本 */
    public Builder toBuilder() {
        return builder.clone();
    }

    /** 密码策略字符串构建器。 */
    public static class Builder {

        private LinkedHashMap<String, String> map;

        private Builder() {
            this.map = new LinkedHashMap<>();
        }

        private Builder(LinkedHashMap<String, String> map) {
            this.map = map;
        }

        private Builder(String policyString) {
            map = new LinkedHashMap<>();

            if (policyString != null && !policyString.trim().isEmpty()) {
                for (String policy : policyString.split(" and ")) {
                    policy = policy.trim();

                    String key;
                    String config = null;

                    int i = policy.indexOf('(');
                    if (i == -1) {
                        key = policy.trim();
                    } else {
                        key = policy.substring(0, i).trim();
                        config = policy.substring(i + 1, policy.length() - 1);
                    }

                    map.put(key, config);
                }
            }
        }

        /** @param key 策略键
         * @return 是否包含该策略 */
        public boolean contains(String key) {
            return map.containsKey(key);
        }

        /** @param key 策略键
         * @return 策略配置字符串 */
        public String get(String key) {
            return map.get(key);
        }

        /** @param key 策略键
         * @param value 配置值
         * @return 当前构建器 */
        public Builder put(String key, String value) {
            map.put(key, value);
            return this;
        }

        /** @param key 策略键
         * @return 当前构建器 */
        public Builder remove(String key) {
            map.remove(key);
            return this;
        }

        /** @param session 会话
         * @return 构建的密码策略 */
        public PasswordPolicy build(KeycloakSession session) {
            Map<String, Object> config = new HashMap<>();
            for (Map.Entry<String, String> e : map.entrySet()) {

                PasswordPolicyProvider provider = session.getProvider(PasswordPolicyProvider.class, e.getKey());
                if (provider == null) {
                    throw new PasswordPolicyConfigException("Password policy not found");
                }

                Object o;
                try {
                    o = provider.parseConfig(e.getValue());
                } catch (PasswordPolicyConfigException ex) {
                    throw new ModelException("Invalid config for " + e.getKey() + ": " + ex.getMessage());
                }

                config.put(e.getKey(), o);
            }
            return new PasswordPolicy(this, config);
        }

        /** @return 策略字符串表示 */
        public String asString() {
            if (map.isEmpty()) {
                return null;
            }

            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, String> e : map.entrySet()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(" and ");
                }

                sb.append(e.getKey());

                String c = e.getValue();
                if (c != null && !c.trim().isEmpty()) {
                    sb.append("(");
                    sb.append(c);
                    sb.append(")");
                }
            }
            return sb.toString();
        }

        /** @return 构建器副本 */
        public Builder clone() {
            return new Builder((LinkedHashMap<String, String>) map.clone());
        }

    }

}
