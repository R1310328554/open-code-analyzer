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

package org.keycloak.common.util;

import org.keycloak.common.Profile;

/**
 * 多站点（Multi-Site）部署相关的 Profile 特性探测。
 */
public class MultiSiteUtils {

    /** 是否启用了 {@link Profile.Feature#MULTI_SITE} 特性。 */
    public static boolean isMultiSiteEnabled() {
        return Profile.isFeatureEnabled(Profile.Feature.MULTI_SITE);
    }

    /**
     * 用户会话是否持久化到数据库。
     *
     * @return 启用 {@code PERSISTENT_USER_SESSIONS}，或多站点且非 {@code CLUSTERLESS} 时为 true；
     *         多站点且启用远程缓存时会话可能不在 DB
     */
    public static boolean isPersistentSessionsEnabled() {
        return Profile.isFeatureEnabled(Profile.Feature.PERSISTENT_USER_SESSIONS) || (isMultiSiteEnabled() && !Profile.isFeatureEnabled(Profile.Feature.CLUSTERLESS));
    }
}
