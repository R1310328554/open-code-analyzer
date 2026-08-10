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

package org.keycloak.device;

import java.io.IOException;
import java.util.Base64;

import jakarta.ws.rs.core.HttpHeaders;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserSessionModel;
import org.keycloak.representations.account.DeviceRepresentation;
import org.keycloak.util.JsonSerialization;

/**
 * 设备活动管理器：在用户会话上附加与读取客户端设备信息（User-Agent 解析结果）。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class DeviceActivityManager {

    /** 用户会话 note 键，存储 Base64 编码的 {@link DeviceRepresentation} JSON。 */
    private static final String DEVICE_NOTE = "KC_DEVICE_NOTE";

    /**
     * 读取 {@code userSession} 上已附加的设备信息。
     *
     * @param userSession 用户会话
     * @return 设备信息；未附加时返回 {@code null}
     */
    public static DeviceRepresentation getCurrentDevice(UserSessionModel userSession) {
        String deviceInfo = userSession.getNote(DEVICE_NOTE);

        if (deviceInfo == null) {
            return null;
        }

        try {
            return JsonSerialization.readValue(Base64.getMimeDecoder().decode(deviceInfo), DeviceRepresentation.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将当前请求 User-Agent 解析出的设备信息附加到 {@code userSession}。
     * <p>通过 {@link DeviceRepresentationProvider} 获取设备描述，序列化后以 note 形式存储。</p>
     *
     * @param userSession 用户会话
     * @param session 当前 Keycloak 会话
     */
    public static void attachDevice(UserSessionModel userSession, KeycloakSession session) {
        DeviceRepresentation current = session.getProvider(DeviceRepresentationProvider.class).deviceRepresentation();

        if (current != null) {
            try {
                userSession.setNote(DEVICE_NOTE, Base64.getEncoder().encodeToString(JsonSerialization.writeValueAsBytes(current)));
            } catch (IOException cause) {
                throw new RuntimeException(cause);
            }
        }
    }
}
