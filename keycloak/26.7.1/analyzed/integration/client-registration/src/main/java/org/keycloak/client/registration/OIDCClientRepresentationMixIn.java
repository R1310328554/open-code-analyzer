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

package org.keycloak.client.registration;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Jackson MixIn：控制 {@link org.keycloak.representations.oidc.OIDCClientRepresentation} 的 JSON 序列化行为。
 * <p>
 * 忽略 OIDC 动态注册规范中的只读/服务端签发字段（如 {@code registration_access_token}、
 * {@code client_id_issued_at} 等），防止客户端误将这些字段写回注册请求。
 * </p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
abstract class OIDCClientRepresentationMixIn {

    /** 客户端 ID 签发时间戳，序列化时忽略。 */
    @JsonIgnore
    private Integer client_id_issued_at;

    /** 客户端密钥过期时间，序列化时忽略。 */
    @JsonIgnore
    private Long client_secret_expires_at;

    /** 注册管理 URI，序列化时忽略。 */
    @JsonIgnore
    private String registration_client_uri;

    /** 注册访问令牌，序列化时忽略。 */
    @JsonIgnore
    private String registration_access_token;

}
