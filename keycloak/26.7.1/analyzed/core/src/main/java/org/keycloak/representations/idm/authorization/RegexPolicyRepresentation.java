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
package org.keycloak.representations.idm.authorization;

/**
 * 正则表达式策略的 REST 表示，对指定声明值进行模式匹配以做出授权决策。
 *
 * @author <a href="mailto:yoshiyuki.tabata.jy@hitachi.com">Yoshiyuki Tabata</a>
 */
public class RegexPolicyRepresentation extends AbstractPolicyRepresentation {

    /** 待匹配的目标声明名称。 */
    private String targetClaim;
    /** 正则表达式模式。 */
    private String pattern;
    /** 是否从评估上下文的属性中查找目标声明。 */
    private boolean targetContextAttributes;

    /** @return 策略类型，固定为 {@code regex} */
    @Override
    public String getType() {
        return "regex";
    }

    /** @return 目标声明名称 */
    public String getTargetClaim() {
        return targetClaim;
    }

    /** @param targetClaim 目标声明名称 */
    public void setTargetClaim(String targetClaim) {
        this.targetClaim = targetClaim;
    }

    /** @return 正则表达式模式 */
    public String getPattern() {
        return pattern;
    }

    /** @param pattern 正则表达式模式 */
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /** @return 是否从上下文属性查找声明 */
    public boolean isTargetContextAttributes() {
        return targetContextAttributes;
    }

    /** @param targetContextAttributes 是否从上下文属性查找声明 */
    public void setTargetContextAttributes(boolean targetContextAttributes) {
        this.targetContextAttributes = targetContextAttributes;
    }

}
