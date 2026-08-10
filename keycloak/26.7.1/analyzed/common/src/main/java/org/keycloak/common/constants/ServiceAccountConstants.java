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

package org.keycloak.common.constants;

/**
 * 服务账户（Service Account）相关常量。
 *
 * <p>涵盖客户端凭证流中的协议映射器名称、会话注记键与 OAuth scope 标识。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface ServiceAccountConstants {

    /** 客户端认证类型标识。 */
    String CLIENT_AUTH = "client_auth";

    /** 服务账户用户在 realm 中的用户名前缀。 */
    String SERVICE_ACCOUNT_USER_PREFIX = "service-account-";

    /** 将客户端 ID 写入令牌的协议映射器显示名。 */
    String CLIENT_ID_PROTOCOL_MAPPER = "Client ID";
    /** 将客户端主机名写入令牌的协议映射器显示名。 */
    String CLIENT_HOST_PROTOCOL_MAPPER = "Client Host";
    /** 将客户端 IP 写入令牌的协议映射器显示名。 */
    String CLIENT_ADDRESS_PROTOCOL_MAPPER = "Client IP Address";

    /** 用户会话注记：客户端 ID。 */
    String CLIENT_ID_SESSION_NOTE = "clientId";
    /** 访问令牌声明：客户端 ID。 */
    String CLIENT_ID = "client_id";
    /** 访问令牌声明：客户端主机。 */
    String CLIENT_HOST = "clientHost";
    /** 访问令牌声明：客户端 IP 地址。 */
    String CLIENT_ADDRESS = "clientAddress";

    /** 服务账户专用 OAuth scope 名称。 */
    String SERVICE_ACCOUNT_SCOPE = "service_account";

}
