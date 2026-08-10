/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.models;

import java.util.HashMap;
import java.util.Map;

/**
 * OAuth 2.0 设备授权流程的用户码（user code）到设备码映射模型。
 * <p>用户码供终端用户输入，内部映射到对应的设备码。</p>
 *
 * @author <a href="mailto:h2-wada@nri.co.jp">Hiroyuki Wada</a>
 */
public class OAuth2DeviceUserCodeModel {

    private static final String DEVICE_CODE_NOTE = "dc";

    private final RealmModel realm;
    private final String deviceCode;
    private final String userCode;

    public OAuth2DeviceUserCodeModel(RealmModel realm, String deviceCode, String userCode) {
        this.realm = realm;
        this.deviceCode = deviceCode;
        this.userCode = userCode;
    }

    /** 从缓存 Map 反序列化用户码映射。 */
    public static OAuth2DeviceUserCodeModel fromCache(RealmModel realm, String userCode, Map<String, String> data) {
        return new OAuth2DeviceUserCodeModel(realm, userCode, data);
    }

    private OAuth2DeviceUserCodeModel(RealmModel realm, String userCode, Map<String, String> data) {
        this.realm = realm;
        this.userCode = userCode;
        this.deviceCode = data.get(DEVICE_CODE_NOTE);
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public String getUserCode() {
        return userCode;
    }

    /** 生成用户码在单用途存储中的缓存键。 */
    public static String createKey(RealmModel realm, String userCode) {
        return String.format("%s.uc.%s", realm.getId(), userCode);
    }

    public String serializeKey() {
        return createKey(realm, userCode);
    }

    /** 序列化为缓存值 Map（含关联设备码）。 */
    public Map<String, String> serializeValue() {
        Map<String, String> result = new HashMap<>();
        result.put(DEVICE_CODE_NOTE, deviceCode);
        return result;
    }
}
