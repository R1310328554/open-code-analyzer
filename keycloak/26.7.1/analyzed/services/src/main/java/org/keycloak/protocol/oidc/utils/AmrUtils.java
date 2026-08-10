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
 */

package org.keycloak.protocol.oidc.utils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.keycloak.common.util.Time;
import org.keycloak.models.Constants;
import org.keycloak.models.RealmModel;

import org.jboss.logging.Logger;

/**
 * AMR（Authenticator Method Reference）工具类：解析认证器执行引用值并校验有效期。
 * @author Ben Cresitello-Dittmar
 */
public class AmrUtils {
    private static final Logger logger = Logger.getLogger(AmrUtils.class);

    /**
     * 获取指定认证器执行 ID 对应的 AMR 引用值；未配置时返回 null 而非抛错。
     *
     * @param executions 认证器执行 ID 与认证时间的映射
     * @param realmModel 执行所在的 realm
     * @return 有效的 AMR 值列表
     */
    public static List<String> getAuthenticationExecutionReferences(Map<String, Integer> executions, RealmModel realmModel) {
        return executions.entrySet().stream()
            .map(
                entry -> {
                    try {
                        // 读取认证器配置并提取 AMR 引用值
                        Map<String, String> config = realmModel.getAuthenticatorConfigById(realmModel.getAuthenticationExecutionById(entry.getKey()).getAuthenticatorConfig()).getConfig();
                        if (isAmrValid(config, entry.getValue())){
                            return config.get(Constants.AUTHENTICATION_EXECUTION_REFERENCE_VALUE);
                        }
                    } catch (NullPointerException e){
                        return null;
                    }

                    return null;
                }
            ).filter(
                ref -> ref != null && !ref.isEmpty()
            ).collect(Collectors.toList());
    }

    /**
     * 校验 AMR 是否仍在有效期内：认证时间 + 配置 max age ≥ 当前时间。
     * @param config 认证器执行配置
     * @param authTime 认证发生时间（Unix 秒）
     * @return 若 AMR 对本会话仍有效则 true
     */
    public static boolean isAmrValid(Map<String, String> config, Integer authTime){
        try {
            int maxAge = Integer.parseInt(config.getOrDefault(Constants.AUTHENTICATION_EXECUTION_REFERENCE_MAX_AGE, "0"));
            return authTime + maxAge >= Time.currentTime();
        } catch (NumberFormatException e){
            logger.warnf("invalid authentication execution max age '%s'", config.get(Constants.AUTHENTICATION_EXECUTION_REFERENCE_MAX_AGE));
        }
        return false;
    }
}
