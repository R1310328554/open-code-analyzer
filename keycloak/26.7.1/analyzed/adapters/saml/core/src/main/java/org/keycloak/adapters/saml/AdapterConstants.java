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

package org.keycloak.adapters.saml;

/**
 * SAML 适配器内部使用的常量定义。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class AdapterConstants {
    /** Servlet 上下文参数名：内嵌 XML 适配器配置 */
    public static final String AUTH_DATA_PARAM_NAME="org.keycloak.saml.xml.adapterConfig";
    /** Servlet 上下文参数名：集群复制容器标识 */
    public static final String REPLICATION_CONFIG_CONTAINER_PARAM_NAME = "org.keycloak.saml.replication.container";
    /** Servlet 上下文参数名：SSO 会话复制缓存标识 */
    public static final String REPLICATION_CONFIG_SSO_CACHE_PARAM_NAME = "org.keycloak.saml.replication.cache.sso";
    /** 认证过期时的错误消息键 */
    public static final String AUTHENTICATION_EXPIRED_MESSAGE = "authentication_expired";
}
