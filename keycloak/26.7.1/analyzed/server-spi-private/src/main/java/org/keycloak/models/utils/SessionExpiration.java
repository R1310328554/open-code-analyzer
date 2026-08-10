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

package org.keycloak.models.utils;

import org.keycloak.models.RealmModel;

/**
 * 认证会话（Authentication Session）生命周期计算工具。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class SessionExpiration {

    /** 取 realm 登录码、用户操作码、访问码三者中的最大生命周期（秒）。 */
    public static int getAuthSessionLifespan(RealmModel realm) {
        int lifespan = realm.getAccessCodeLifespanLogin();
        if (realm.getAccessCodeLifespanUserAction() > lifespan) {
            lifespan = realm.getAccessCodeLifespanUserAction();
        }
        if (realm.getAccessCodeLifespan() > lifespan) {
            lifespan = realm.getAccessCodeLifespan();
        }
        return lifespan;
    }

    /** 根据起始时间戳计算认证会话过期时间戳。 */
    public static long getAuthSessionExpiration(RealmModel realm, int timestamp) {
        return (long) timestamp + getAuthSessionLifespan(realm);
    }

}
