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

package org.keycloak.services.clientregistration;

/**
 * 动态客户端注册错误码常量。
 * <p>用于 {@link ErrorResponseException} 响应中的 {@code error} 字段。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface ErrorCodes {

    /** 重定向 URI 无效或未通过策略校验 */
    String INVALID_REDIRECT_URI = "invalid_redirect_uri";

    /** 客户端元数据格式或内容无效 */
    String INVALID_CLIENT_METADATA = "invalid_client_metadata";

}
