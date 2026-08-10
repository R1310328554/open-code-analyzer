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
 * Jackson MixIn：控制 {@link org.keycloak.representations.idm.ClientRepresentation} 的 JSON 序列化行为。
 * <p>
 * 将 {@code registrationAccessToken} 标记为 {@link JsonIgnore}，避免在对外输出或日志中泄露敏感注册令牌。
 * </p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
abstract class ClientRepresentationMixIn {

    /** 注册访问令牌，序列化时忽略。 */
    @JsonIgnore
    String registrationAccessToken;

}
