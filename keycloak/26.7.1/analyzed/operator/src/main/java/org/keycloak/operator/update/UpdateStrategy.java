/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.operator.update;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Keycloak 集群更新策略枚举，对应 CR {@code spec.update.strategy} 字段。
 */
public enum UpdateStrategy {

    /** 镜像变更时先重建集群，否则滚动更新（Keycloak 26.0 默认行为）。 */
    @JsonProperty("RecreateOnImageChange")
    RECREATE_ON_IMAGE_CHANGE,

    /** 通过更新 Job 自动检测变更兼容性并选择滚动或重建。 */
    @JsonProperty("Auto")
    AUTO,

    /** 由外部操作者通过 revision 字段显式控制是否允许滚动更新。 */
    @JsonProperty("Explicit")
    EXPLICIT
}
