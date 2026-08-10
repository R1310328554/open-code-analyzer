/*
 * Copyright 2018 Bosch Software Innovations GmbH
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

package org.keycloak.representations.idm;

/**
 * 细粒度管理权限启用状态的简单表示。
 *
 * @author <a href="mailto:leon.graser@bosch-si.com">Leon Graser</a>
 */
public class ManagementPermissionRepresentation {

    /** 是否已启用细粒度管理权限。 */
    private final boolean enabled;

    /**
     * @param enabled 是否启用细粒度管理权限
     */
    public ManagementPermissionRepresentation(boolean enabled) {
        this.enabled = enabled;
    }

    /** @return 是否启用细粒度管理权限 */
    public boolean isEnabled() {
        return enabled;
    }
}
