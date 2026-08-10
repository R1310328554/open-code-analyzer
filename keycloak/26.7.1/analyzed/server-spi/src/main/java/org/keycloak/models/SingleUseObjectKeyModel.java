/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 单次使用对象键模型：序列化 action token 的键组成部分。
 *
 * @author hmlnarik
 */
public interface SingleUseObjectKeyModel {

    /**
     * 返回令牌关联的用户 ID。
     * @return ID of user which this token is for.
     */
    String getUserId();

    /**
     * 返回令牌对应的操作标识符。
     * @return Action identifier this token is for.
     */
    String getActionId();

    /**
     * 返回令牌在 UTC 时区的绝对过期时间（自 epoch 起的秒数）。
     * Returns absolute number of seconds since the epoch in UTC timezone when the token expires.
     */
    Long getExp();

    /**
     * 已弃用：2038 年后 int 会溢出，请使用 {@link #getExp()}。
     * @deprecated int will overflow with values after 2038. Use {@link #getExp()} instead.
     */
    @Deprecated
    @JsonIgnore
    default int getExpiration() {
        return getExp().intValue();
    }

    /**
     * 返回用于验证操作是否允许的单次使用随机值。
     * @return Single-use random value used for verification whether the relevant action is allowed.
     */
    UUID getActionVerificationNonce();

    /** @return 序列化后的单次使用对象键字符串 */
    default String serializeKey() {
        String userId = getUserId();
        String encodedUserId = userId == null ? "" : Base64.getEncoder().encodeToString(userId.getBytes(StandardCharsets.UTF_8));
        return String.format("%s.%d.%s.%s", encodedUserId, getExp(), getActionVerificationNonce(), getActionId());
    }
}
