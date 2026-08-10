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

package org.keycloak.tracing;

import io.opentelemetry.api.common.AttributeKey;

/**
 * 分布式追踪预分配属性键：用于 OpenTelemetry Span 上的 Keycloak 上下文属性。
 * Pre-allocated attribute keys for Tracing
 */
public interface TracingAttributes {
    /** Keycloak 追踪属性前缀。 */
    String KC_PREFIX = "kc.";
    /** 令牌相关追踪属性前缀。 */
    String KC_TOKEN_PREFIX = KC_PREFIX + "token.";

    /** Realm ID 属性键。 */
    AttributeKey<String> REALM_ID = AttributeKey.stringKey(KC_PREFIX + "realmId");
    /** Realm 名称属性键。 */
    AttributeKey<String> REALM_NAME = AttributeKey.stringKey(KC_PREFIX + "realmName");
    /** 客户端 ID 属性键。 */
    AttributeKey<String> CLIENT_ID = AttributeKey.stringKey(KC_PREFIX + "clientId");
    /** 用户 ID 属性键。 */
    AttributeKey<String> USER_ID = AttributeKey.stringKey(KC_PREFIX + "userId");
    /** 认证会话 ID 属性键。 */
    AttributeKey<String> AUTH_SESSION_ID = AttributeKey.stringKey(KC_PREFIX + "authenticationSessionId");
    /** 认证标签页 ID 属性键。 */
    AttributeKey<String> AUTH_TAB_ID = AttributeKey.stringKey(KC_PREFIX + "authenticationTabId");
    /** 用户会话 ID 属性键。 */
    AttributeKey<String> SESSION_ID = AttributeKey.stringKey(KC_PREFIX + "sessionId");
    /** 事件 ID 属性键。 */
    AttributeKey<String> EVENT_ID = AttributeKey.stringKey(KC_PREFIX + "eventId");
    /** 事件错误信息属性键。 */
    AttributeKey<String> EVENT_ERROR = AttributeKey.stringKey(KC_PREFIX + "eventError");

    // 令牌相关属性
    // Token
    /** 令牌签发者属性键。 */
    AttributeKey<String> TOKEN_ISSUER = AttributeKey.stringKey(KC_TOKEN_PREFIX + "issuer");
    /** 令牌会话 ID（sid）属性键。 */
    AttributeKey<String> TOKEN_SID = AttributeKey.stringKey(KC_TOKEN_PREFIX + "sid");
    /** 令牌 ID 属性键。 */
    AttributeKey<String> TOKEN_ID = AttributeKey.stringKey(KC_TOKEN_PREFIX + "id");

}
