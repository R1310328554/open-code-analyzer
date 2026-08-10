/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.operator.crds.v2alpha1.client;

import org.keycloak.operator.crds.v2beta1.StatusCondition;

/**
 * Keycloak 客户端 CR 的状态条件，继承通用 {@link StatusCondition}。
 *
 * <p>在需要客户端特有 condition type 前，可复用基类字段；当前额外定义 {@link #HAS_ERRORS}。
 */
// TODO: 若长期无 specialization 需求，可考虑移除此类直至需要时再引入
public class KeycloakClientStatusCondition extends StatusCondition {
    /** 表示客户端同步或配置存在错误的 condition type。 */
    public static final String HAS_ERRORS = "HasErrors";

    /** 无参构造，供 Jackson/Builder 使用。 */
    public KeycloakClientStatusCondition() {

    }

    /**
     * 全字段构造。
     *
     * @param type 条件类型
     * @param status 条件是否为 True
     * @param message 人类可读说明
     * @param lastTransitionTime 上次状态变更时间
     * @param observedGeneration 观察到的 generation
     */
    public KeycloakClientStatusCondition(String type, Boolean status, String message, String lastTransitionTime,
            Long observedGeneration) {
        super(type, status, message, lastTransitionTime, observedGeneration);
    }

}
