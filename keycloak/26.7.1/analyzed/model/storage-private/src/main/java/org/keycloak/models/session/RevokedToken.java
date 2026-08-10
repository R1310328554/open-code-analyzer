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

package org.keycloak.models.session;

/**
 * 已撤销令牌记录：保存令牌标识及其过期时间，供撤销列表持久化与校验使用。
 *
 * @param tokenId 被撤销令牌的唯一标识
 * @param expiry  令牌过期时间（Unix 纪元秒）
 * @author Alexander Schwartz
 */
public record RevokedToken(String tokenId, long expiry) {
}
