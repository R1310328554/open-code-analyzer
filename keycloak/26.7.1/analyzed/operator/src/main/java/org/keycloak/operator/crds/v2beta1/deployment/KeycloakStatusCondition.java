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

package org.keycloak.operator.crds.v2beta1.deployment;

import org.keycloak.operator.crds.v2beta1.StatusCondition;

/**
 * Keycloak 部署 CR 的状态条件类型定义，继承通用 {@link StatusCondition}。
 *
 * <p>本类仅声明 Keycloak 特有的 condition type 常量，供 {@link KeycloakStatusAggregator}
 * 与 status 子资源序列化使用。
 *
 * @author Vaclav Muzikar <vmuzikar@redhat.com>
 */
public class KeycloakStatusCondition extends StatusCondition {
    /** 实例是否就绪（Pod 就绪且配置已应用）。 */
    public static final String READY = "Ready";
    /** 协调过程中是否出现错误或警告。 */
    public static final String HAS_ERRORS = "HasErrors";
    /** 是否正在进行滚动更新。 */
    public static final String ROLLING_UPDATE = "RollingUpdate";
    /** 本次更新是否采用了 Recreate 策略（值为 True 表示使用了 Recreate）。 */
    public static final String UPDATE_TYPE = "RecreateUpdateUsed";
}
