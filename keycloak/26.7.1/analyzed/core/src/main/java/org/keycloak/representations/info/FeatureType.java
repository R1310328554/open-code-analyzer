/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.keycloak.representations.info;

/**
 * Keycloak 功能开关的分类类型，决定特性在默认配置下的启用策略与生命周期阶段。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public enum FeatureType {
    /** 默认启用。 */
    DEFAULT,
    /** 默认禁用，需显式开启。 */
    DISABLED_BY_DEFAULT,
    /** 预览功能，通常默认启用。 */
    PREVIEW,
    /** 预览功能且默认禁用。 */
    PREVIEW_DISABLED_BY_DEFAULT,
    /** 实验性功能，稳定性无保证。 */
    EXPERIMENTAL,
    /** 已废弃，将在后续版本移除。 */
    DEPRECATED;
}
