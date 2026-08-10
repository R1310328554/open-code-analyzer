/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.api.annotation;

import com.alibaba.nacos.api.PropertyKeyConst;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Nacos 客户端连接与配置属性注解。
 *
 * <p>定义 {@code nacos.*} 前缀下的 endpoint、namespace、server-addr 等键名，
 * 并提供 Spring 占位符默认值；可与 {@link NacosInjected} 配合注入 {@link PropertyKeyConst} 属性。</p>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertyKeyConst
 * @since 0.2.1
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NacosProperties {
    
    /** Nacos 配置项名称前缀。 */
    String PREFIX = "nacos.";
    
    /** 配置键名：{@code endpoint}。 */
    String ENDPOINT = "endpoint";
    
    /** 配置键名：{@code namespace}。 */
    String NAMESPACE = "namespace";
    
    /** 配置键名：{@code access-key}。 */
    String ACCESS_KEY = "access-key";
    
    /** 配置键名：{@code secret-key}。 */
    String SECRET_KEY = "secret-key";
    
    /** 配置键名：{@code server-addr}。 */
    String SERVER_ADDR = "server-addr";
    
    /** 配置键名：{@code context-path}。 */
    String CONTEXT_PATH = "context-path";
    
    /** 配置键名：{@code cluster-name}。 */
    String CLUSTER_NAME = "cluster-name";
    
    /** 配置键名：{@code encode}。 */
    String ENCODE = "encode";
    
    /** 配置键名：长轮询超时 {@code configLongPollTimeout}。 */
    String CONFIG_LONG_POLL_TIMEOUT = "configLongPollTimeout";
    
    /** 配置键名：配置重试间隔 {@code configRetryTime}。 */
    String CONFIG_RETRY_TIME = "configRetryTime";
    
    /** 配置键名：最大重试次数 {@code maxRetry}。 */
    String MAX_RETRY = "maxRetry";
    
    /** 配置键名：是否启用远程配置同步 {@code enableRemoteSyncConfig}。 */
    String ENABLE_REMOTE_SYNC_CONFIG = "enableRemoteSyncConfig";
    
    /** 配置键名：{@code username}。 */
    String USERNAME = "username";
    
    /** 配置键名：{@code password}。 */
    String PASSWORD = "password";
    
    /** endpoint 占位符，值为 <code>"${nacos.endpoint:}"</code>。 */
    String ENDPOINT_PLACEHOLDER = "${" + PREFIX + ENDPOINT + ":}";
    
    /** namespace 占位符，值为 <code>"${nacos.namespace:}"</code>。 */
    String NAMESPACE_PLACEHOLDER = "${" + PREFIX + NAMESPACE + ":}";
    
    /** access-key 占位符，值为 <code>"${nacos.access-key:}"</code>。 */
    String ACCESS_KEY_PLACEHOLDER = "${" + PREFIX + ACCESS_KEY + ":}";
    
    /** secret-key 占位符，值为 <code>"${nacos.secret-key:}"</code>。 */
    String SECRET_KEY_PLACEHOLDER = "${" + PREFIX + SECRET_KEY + ":}";
    
    /** server-addr 占位符，值为 <code>"${nacos.server-addr:}"</code>。 */
    String SERVER_ADDR_PLACEHOLDER = "${" + PREFIX + SERVER_ADDR + ":}";
    
    /** context-path 占位符，值为 <code>"${nacos.context-path:}"</code>。 */
    String CONTEXT_PATH_PLACEHOLDER = "${" + PREFIX + CONTEXT_PATH + ":}";
    
    /** cluster-name 占位符，值为 <code>"${nacos.cluster-name:}"</code>。 */
    String CLUSTER_NAME_PLACEHOLDER = "${" + PREFIX + CLUSTER_NAME + ":}";
    
    /** {@link NacosProperties#ENCODE encode} 占位符，默认 <code>"${nacos.encode:UTF-8}"</code>。 */
    String ENCODE_PLACEHOLDER = "${" + PREFIX + ENCODE + ":UTF-8}";
    
    /** {@link NacosProperties#CONFIG_LONG_POLL_TIMEOUT configLongPollTimeout} 占位符。 */
    String CONFIG_LONG_POLL_TIMEOUT_PLACEHOLDER = "${" + PREFIX + CONFIG_LONG_POLL_TIMEOUT + ":}";
    
    /** {@link NacosProperties#CONFIG_RETRY_TIME configRetryTime} 占位符。 */
    String CONFIG_RETRY_TIME_PLACEHOLDER = "${" + PREFIX + CONFIG_RETRY_TIME + ":}";
    
    /** {@link NacosProperties#MAX_RETRY maxRetry} 占位符。 */
    String MAX_RETRY_PLACEHOLDER = "${" + PREFIX + MAX_RETRY + ":}";
    
    /** {@link NacosProperties#ENABLE_REMOTE_SYNC_CONFIG enableRemoteSyncConfig} 占位符。 */
    String ENABLE_REMOTE_SYNC_CONFIG_PLACEHOLDER = "${" + PREFIX + ENABLE_REMOTE_SYNC_CONFIG + ":}";
    
    /** username 占位符，值为 <code>"${nacos.username:}"</code>。 */
    String USERNAME_PLACEHOLDER = "${" + PREFIX + USERNAME + ":}";
    
    /** password 占位符，值为 <code>"${nacos.password:}"</code>。 */
    String PASSWORD_PLACEHOLDER = "${" + PREFIX + PASSWORD + ":}";
    
    /**
     * endpoint 属性值。
     *
     * @return 默认为空
     * @see #ENDPOINT_PLACEHOLDER
     */
    String endpoint() default ENDPOINT_PLACEHOLDER;
    
    /**
     * namespace 属性值。
     *
     * @return 默认为空
     * @see #NAMESPACE_PLACEHOLDER
     */
    String namespace() default NAMESPACE_PLACEHOLDER;
    
    /**
     * access-key 属性值。
     *
     * @return 默认为空
     * @see #ACCESS_KEY_PLACEHOLDER
     */
    String accessKey() default ACCESS_KEY_PLACEHOLDER;
    
    /**
     * secret-key 属性值。
     *
     * @return 默认为空
     * @see #SECRET_KEY_PLACEHOLDER
     */
    String secretKey() default SECRET_KEY_PLACEHOLDER;
    
    /**
     * server-addr 属性值。
     *
     * @return 默认为空
     * @see #SERVER_ADDR_PLACEHOLDER
     */
    String serverAddr() default SERVER_ADDR_PLACEHOLDER;
    
    /**
     * context-path 属性值。
     *
     * @return 默认为空
     * @see #CONTEXT_PATH_PLACEHOLDER
     */
    String contextPath() default CONTEXT_PATH_PLACEHOLDER;
    
    /**
     * cluster-name 属性值。
     *
     * @return 默认为空
     * @see #CLUSTER_NAME_PLACEHOLDER
     */
    String clusterName() default CLUSTER_NAME_PLACEHOLDER;
    
    /**
     * 字符编码属性值。
     *
     * @return 默认 {@code UTF-8}
     * @see #ENCODE_PLACEHOLDER
     */
    String encode() default ENCODE_PLACEHOLDER;
    
    /**
     * 配置长轮询超时（毫秒）。
     *
     * @return 默认为空
     * @see #CONFIG_LONG_POLL_TIMEOUT_PLACEHOLDER
     */
    String configLongPollTimeout() default CONFIG_LONG_POLL_TIMEOUT_PLACEHOLDER;
    
    /**
     * 配置拉取失败重试间隔（毫秒）。
     *
     * @return 默认为空
     * @see #CONFIG_RETRY_TIME_PLACEHOLDER
     */
    String configRetryTime() default CONFIG_RETRY_TIME_PLACEHOLDER;
    
    /**
     * 最大重试次数。
     *
     * @return 默认为空
     * @see #MAX_RETRY
     */
    String maxRetry() default MAX_RETRY_PLACEHOLDER;
    
    /**
     * 是否启用远程配置同步。
     *
     * @return 默认为空
     * @see #ENABLE_REMOTE_SYNC_CONFIG
     */
    String enableRemoteSyncConfig() default ENABLE_REMOTE_SYNC_CONFIG_PLACEHOLDER;
    
    /**
     * 认证用户名。
     *
     * @return 默认为空
     * @see #USERNAME_PLACEHOLDER
     */
    String username() default USERNAME_PLACEHOLDER;
    
    /**
     * 认证密码。
     *
     * @return 默认为空
     * @see #PASSWORD_PLACEHOLDER
     */
    String password() default PASSWORD_PLACEHOLDER;
    
}
