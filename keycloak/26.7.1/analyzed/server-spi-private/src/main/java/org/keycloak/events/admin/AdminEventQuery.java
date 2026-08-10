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

package org.keycloak.events.admin;

import java.util.Date;
import java.util.stream.Stream;

/**
 * 管理事件查询构建器，支持按 realm、操作者、资源与时间范围过滤，链式调用。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface AdminEventQuery {
    
    /**
     * 按被操作资源所属 realm 过滤。
     *
     * @param realmId realm ID
     * @return 本查询实例，便于链式调用
     */
    AdminEventQuery realm(String realmId);
    
    /**
     * 按操作者认证所在 realm 过滤。
     *
     * @param realmId realm ID
     * @return 本查询实例，便于链式调用
     */
    AdminEventQuery authRealm(String realmId);
    
    /**
     * 按已认证客户端 UUID 过滤。
     *
     * @param clientId 客户端 UUID
     * @return 本查询实例，便于链式调用
     */
    AdminEventQuery authClient(String clientId);

    /**
     * 按已认证用户 UUID 过滤。
     *
     * @param userId 用户 UUID
     * @return 本查询实例，便于链式调用
     */
    AdminEventQuery authUser(String userId);

    /**
     * 按请求源 IP 地址过滤。
     *
     * @param ipAddress IP 地址
     * @return 本查询实例，便于链式调用
     */
    AdminEventQuery authIpAddress(String ipAddress);

    /**
     * 按 {@link OperationType} 过滤。
     *
     * @param operations 一个或多个操作类型
     * @return 本查询实例，便于链式调用
     */
    AdminEventQuery operation(OperationType... operations);

    /**
     * 按 {@link ResourceType} 过滤。
     *
     * @param resourceTypes 一个或多个资源类型
     * @return 本查询实例，便于链式调用
     */
    AdminEventQuery resourceType(ResourceType ... resourceTypes);

    /**
     * 按资源路径过滤，支持通配符 {@code *}，例如：
     * <ul>
     * <li><b>*&#47;master</b> — 匹配 {@code realms/master}</li>
     * <li><b>realms/master&#47;*&#47;00d4b16f</b> — 匹配 {@code realms/master/clients/00d4b16f}</li>
     * <li><b>realms&#47;master&#47;*</b> — 匹配 {@code realms/master} 下任意路径</li>
     * </ul>
     *
     * @param resourcePath 资源路径模式
     * @return 本查询实例，便于链式调用
     */
    AdminEventQuery resourcePath(String resourcePath);

    /**
     * 过滤指定日期（含）之后的事件。
     *
     * @param fromTime 起始日期
     * @return 本查询实例，便于链式调用
     */
    @Deprecated
    AdminEventQuery fromTime(Date fromTime);

    /**
     * 过滤指定时间戳（含）之后的事件。
     *
     * @param fromTime 起始毫秒时间戳
     * @return 本查询实例，便于链式调用
     */
    AdminEventQuery fromTime(long fromTime);

    /**
     * 过滤指定日期（含）之前的事件。
     *
     * @param toTime 结束日期
     * @return 本查询实例，便于链式调用
     */
    @Deprecated
    AdminEventQuery toTime(Date toTime);

    /**
     * 过滤指定时间戳（含）之前的事件。
     *
     * @param toTime 结束毫秒时间戳
     * @return 本查询实例，便于链式调用
     */
    AdminEventQuery toTime(long toTime);

    /**
     * 分页：跳过前 {@code first} 条结果。
     *
     * @param first 起始偏移量
     * @return 本查询实例，便于链式调用
     */
    AdminEventQuery firstResult(int first);

    /**
     * 分页：最多返回 {@code max} 条结果。
     *
     * @param max 最大结果数
     * @return 本查询实例，便于链式调用
     */
    AdminEventQuery maxResults(int max);

    /**
     * 按时间降序排列结果。
     *
     * @return 本查询实例，便于链式调用
     */
    AdminEventQuery orderByDescTime();

    /**
     * 按时间升序排列结果。
     *
     * @return 本查询实例，便于链式调用
     */
    AdminEventQuery orderByAscTime();

    /**
     * 执行查询并以流形式返回管理事件。
     *
     * @return 管理事件流，永不为 {@code null}
     */
    Stream<AdminEvent> getResultStream();
}
