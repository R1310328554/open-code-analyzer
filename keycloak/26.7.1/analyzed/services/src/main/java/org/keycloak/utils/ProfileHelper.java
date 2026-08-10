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

package org.keycloak.utils;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.keycloak.common.Profile;

/**
 * Profile 特性检查辅助类。
 * <p>在 REST 端点中校验 {@link Profile.Feature} 是否已启用，未启用时抛出 501。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ProfileHelper {

    /**
     * 要求指定 Profile 特性已启用，否则抛出 {@link WebApplicationException}（501）。
     *
     * @param feature 待检查的 Profile 特性
     */
    public static void requireFeature(Profile.Feature feature) {
        if (!Profile.isFeatureEnabled(feature)) {
            throw new WebApplicationException("Feature not enabled", Response.Status.NOT_IMPLEMENTED);
        }
    }

}
