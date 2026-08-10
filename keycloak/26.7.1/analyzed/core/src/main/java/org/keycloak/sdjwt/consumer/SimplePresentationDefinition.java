/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.sdjwt.consumer;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.keycloak.common.VerificationException;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 简单的凭证展示定义，用于描述期望接收的凭证类型与必填声明。
 *
 * <p>
 * 凭证类型与必填声明通过正则表达式配置；匹配前会将字段值序列化为 JSON 字符串。
 * </p>
 *
 * @author <a href="mailto:Ingrid.Kamga@adorsys.com">Ingrid Kamga</a>
 */
public class SimplePresentationDefinition implements PresentationRequirements {

    /** 字段名到正则模式的映射，定义各必填声明的匹配规则。 */
    private final Map<String, Pattern> requirements;

    public SimplePresentationDefinition(Map<String, Pattern> requirements) {
        this.requirements = requirements;
    }

    /**
     * 校验给定 JSON 载荷是否满足全部必填字段的正则约束。
     *
     * <p>
     * 对每个必填字段，从已完全披露的签发者签名 JWT 载荷中取出对应值，
     * 与配置的正则模式进行匹配；若字段缺失或匹配失败，则抛出 {@link VerificationException}。
     * </p>
     *
     * @param disclosedPayload 已完全披露的签发者签名 JWT 载荷。
     * @throws VerificationException 若任一必填字段缺失或未通过模式校验。
     */
    @Override
    public void checkIfSatisfiedBy(JsonNode disclosedPayload) throws VerificationException {
        for (Map.Entry<String, Pattern> requirement : requirements.entrySet()) {
            String field = requirement.getKey();
            Pattern pattern = requirement.getValue();

            // 从载荷中读取必填字段的值
            JsonNode presented = disclosedPayload.get(field);

            // 检查必填字段是否存在于载荷中
            if (presented == null || presented.isNull()) {
                throw new VerificationException(
                        String.format("A required field was not presented: `%s`", field)
                );
            }

            // 提取字段值的 JSON 表示
            String json = presented.toString();

            // 将字段值与配置的正则模式进行匹配
            Matcher matcher = pattern.matcher(json);
            if (!matcher.matches()) {
                throw new VerificationException(String.format(
                        "Pattern matching failed for required field: `%s`. Expected pattern: /%s/, but got: %s",
                        field, pattern.pattern(), json
                ));
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    /** 用于逐步构建 {@link SimplePresentationDefinition} 的建造者。 */
    public static class Builder {
        private final Map<String, Pattern> requirements = new HashMap<>();

        /**
         * 添加一条声明约束：指定字段名及其正则模式。
         *
         * @param field 声明字段名
         * @param regexPattern 匹配该字段 JSON 值的正则表达式
         * @return 当前建造者实例，支持链式调用
         */
        public Builder addClaimRequirement(String field, String regexPattern) {
            this.requirements.put(field, Pattern.compile(regexPattern));
            return this;
        }

        public SimplePresentationDefinition build() {
            return new SimplePresentationDefinition(requirements);
        }
    }
}
