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

package org.keycloak.admin.client.resource;

import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.representations.idm.UserRepresentation;

/**
 * 用户集合的管理 REST 资源。
 * <p>
 * 支持创建、搜索、统计、删除用户，并提供多种过滤、分页与属性查询方式。
 */
public interface UsersResource {

    /** 按用户名、姓名、邮箱等条件搜索用户。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<UserRepresentation> search(@QueryParam("username") String username,
                                    @QueryParam("firstName") String firstName,
                                    @QueryParam("lastName") String lastName,
                                    @QueryParam("email") String email,
                                    @QueryParam("first") Integer firstResult,
                                    @QueryParam("max") Integer maxResults);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<UserRepresentation> search(@QueryParam("username") String username,
                                    @QueryParam("firstName") String firstName,
                                    @QueryParam("lastName") String lastName,
                                    @QueryParam("email") String email,
                                    @QueryParam("first") Integer firstResult,
                                    @QueryParam("max") Integer maxResults,
                                    @QueryParam("enabled") Boolean enabled,
                                    @QueryParam("briefRepresentation") Boolean briefRepresentation);

    /**
     * 按给定过滤条件搜索用户。
     *
     * @param username 用户名包含的值
     * @param firstName 名字包含的值
     * @param lastName 姓氏包含的值
     * @param email 邮箱包含的值
     * @param emailVerified 邮箱是否已验证
     * @param idpAlias 身份提供程序别名
     * @param idpUserId 身份提供程序中的用户 ID
     * @param firstResult 分页起始偏移
     * @param maxResults 分页最大条数
     * @param enabled 是否仅返回启用或禁用的用户
     * @param briefRepresentation 是否仅返回基本信息（保证包含 id、username、created、姓名、email、
     *        enabled 状态、邮箱验证状态、联合链接及 access；不包含用户属性、必需操作及 notBefore）
     * @return {@link UserRepresentation} 列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<UserRepresentation> search(@QueryParam("username") String username,
                                    @QueryParam("firstName") String firstName,
                                    @QueryParam("lastName") String lastName,
                                    @QueryParam("email") String email,
                                    @QueryParam("emailVerified") Boolean emailVerified,
                                    @QueryParam("idpAlias") String idpAlias,
                                    @QueryParam("idpUserId") String idpUserId,
                                    @QueryParam("first") Integer firstResult,
                                    @QueryParam("max") Integer maxResults,
                                    @QueryParam("enabled") Boolean enabled,
                                    @QueryParam("briefRepresentation") Boolean briefRepresentation);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<UserRepresentation> search(@QueryParam("username") String username,
                                    @QueryParam("firstName") String firstName,
                                    @QueryParam("lastName") String lastName,
                                    @QueryParam("email") String email,
                                    @QueryParam("emailVerified") Boolean emailVerified,
                                    @QueryParam("first") Integer firstResult,
                                    @QueryParam("max") Integer maxResults,
                                    @QueryParam("enabled") Boolean enabled,
                                    @QueryParam("briefRepresentation") Boolean briefRepresentation);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<UserRepresentation> search(@QueryParam("emailVerified") Boolean emailVerified,
                                    @QueryParam("first") Integer firstResult,
                                    @QueryParam("max") Integer maxResults,
                                    @QueryParam("enabled") Boolean enabled,
                                    @QueryParam("briefRepresentation") Boolean briefRepresentation);

    /** 按用户名搜索用户。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<UserRepresentation> search(@QueryParam("username") String username);

    /** 按自定义属性查询表达式搜索用户。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    List<UserRepresentation> searchByAttributes(@QueryParam("q") String searchQuery);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    List<UserRepresentation> searchByAttributes(@QueryParam("q") String searchQuery,
                                                @QueryParam("exact") Boolean exact);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    List<UserRepresentation> searchByAttributes(@QueryParam("first") Integer firstResult,
                                                @QueryParam("max") Integer maxResults,
                                                @QueryParam("enabled") Boolean enabled,
                                                @QueryParam("briefRepresentation") Boolean briefRepresentation,
                                                @QueryParam("q") String searchQuery);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    List<UserRepresentation> searchByAttributes(@QueryParam("first") Integer firstResult,
                                                @QueryParam("max") Integer maxResults,
                                                @QueryParam("enabled") Boolean enabled,
                                                @QueryParam("exact") Boolean exact,
                                                @QueryParam("briefRepresentation") Boolean briefRepresentation,
                                                @QueryParam("q") String searchQuery);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<UserRepresentation> search(@QueryParam("username") String username, @QueryParam("exact") Boolean exact);

    /** 按用户名搜索用户，可指定是否精确匹配。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<UserRepresentation> searchByUsername(@QueryParam("username") String username, @QueryParam("exact") Boolean exact);

    /** 按邮箱搜索用户，可指定是否精确匹配。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<UserRepresentation> searchByEmail(@QueryParam("email") String email, @QueryParam("exact") Boolean exact);

    /** 按名字搜索用户，可指定是否精确匹配。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<UserRepresentation> searchByFirstName(@QueryParam("firstName") String email, @QueryParam("exact") Boolean exact);

    /** 按姓氏搜索用户，可指定是否精确匹配。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<UserRepresentation> searchByLastName(@QueryParam("lastName") String email, @QueryParam("exact") Boolean exact);

    /**
     * 按给定过滤条件搜索用户，支持精确匹配。
     *
     * @param username 用户名包含的值
     * @param firstName 名字包含的值
     * @param lastName 姓氏包含的值
     * @param email 邮箱包含的值
     * @param firstResult 分页起始偏移
     * @param maxResults 分页最大条数
     * @param enabled 是否仅返回启用或禁用的用户
     * @param briefRepresentation 是否仅返回基本信息
     * @param exact 是否对 username、email、firstName、lastName 精确匹配
     * @return {@link UserRepresentation} 列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<UserRepresentation> search(@QueryParam("username") String username,
                                    @QueryParam("firstName") String firstName,
                                    @QueryParam("lastName") String lastName,
                                    @QueryParam("email") String email,
                                    @QueryParam("first") Integer firstResult,
                                    @QueryParam("max") Integer maxResults,
                                    @QueryParam("enabled") Boolean enabled,
                                    @QueryParam("briefRepresentation") Boolean briefRepresentation,
                                    @QueryParam("exact") Boolean exact);

    /**
     * 搜索用户名或邮箱匹配 {@code search} 的用户。{@code search} 也支持按特定属性查询，例如：
     * <ul>
     *     <li><i>id:</i> - 按标识符查找，如 <i>id:aa497859-bbf5-44ac-bf1a-74dbffcaf197</i></li>
     * </ul>
     *
     * @param search 搜索值，可为 username、email 或支持的属性查询表达式
     * @param firstResult 分页起始偏移
     * @param maxResults 分页最大条数
     * @return {@link UserRepresentation} 列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<UserRepresentation> search(@QueryParam("search") String search,
                                    @QueryParam("first") Integer firstResult,
                                    @QueryParam("max") Integer maxResults);

    /**
     * 搜索用户名或邮箱匹配 {@code search} 的用户，可控制返回详略。
     *
     * @param search 搜索值
     * @param firstResult 分页起始偏移
     * @param maxResults 分页最大条数
     * @param briefRepresentation 是否仅返回基本信息
     * @return {@link UserRepresentation} 列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<UserRepresentation> search(@QueryParam("search") String search,
                                    @QueryParam("first") Integer firstResult,
                                    @QueryParam("max") Integer maxResults,
                                    @QueryParam("briefRepresentation") Boolean briefRepresentation);

    /**
     * 搜索用户名、姓名或邮箱匹配 {@code search} 的用户，可按启用状态过滤。
     *
     * @param search 搜索值
     * @param enabled 为 true 时仅返回启用的用户
     * @param firstResult 分页起始偏移
     * @param maxResults 分页最大条数
     * @return {@link UserRepresentation} 列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<UserRepresentation> search(@QueryParam("search") String search,
      @QueryParam("enabled") Boolean enabled,
      @QueryParam("first") Integer firstResult,
      @QueryParam("max") Integer maxResults);

    /**
     * 返回当前用户有权查看且匹配给定过滤条件的用户列表。
     *
     * @param search 通用搜索字符串
     * @param last 姓氏
     * @param first 名字
     * @param email 邮箱
     * @param emailVerified 邮箱是否已验证
     * @param username 用户名
     * @param enabled 用户是否启用
     * @return 匹配的用户列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<UserRepresentation> search(@QueryParam("search") String search,
                  @QueryParam("lastName") String last,
                  @QueryParam("firstName") String first,
                  @QueryParam("email") String email,
                  @QueryParam("emailVerified") Boolean emailVerified,
                  @QueryParam("username") String username,
                  @QueryParam("enabled") Boolean enabled,
                  @QueryParam("q") String searchQuery);

    /**
     * 按给定过滤条件搜索用户，支持创建时间范围过滤。
     *
     * @param username 用户名包含的值
     * @param firstName 名字包含的值
     * @param lastName 姓氏包含的值
     * @param email 邮箱包含的值
     * @param emailVerified 邮箱是否已验证
     * @param idpAlias 身份提供程序别名
     * @param idpUserId 身份提供程序中的用户 ID
     * @param firstResult 分页起始偏移
     * @param maxResults 分页最大条数
     * @param enabled 是否仅返回启用或禁用的用户
     * @param briefRepresentation 是否仅返回基本信息
     * @param createdAfter 仅返回此日期（含）之后创建的用户，ISO-8601（yyyy-MM-dd）或 epoch 毫秒
     * @param createdBefore 仅返回此日期（含）之前创建的用户，ISO-8601（yyyy-MM-dd）或 epoch 毫秒
     * @return {@link UserRepresentation} 列表
     * @since Keycloak server 26.7.0
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<UserRepresentation> search(@QueryParam("username") String username,
                                    @QueryParam("firstName") String firstName,
                                    @QueryParam("lastName") String lastName,
                                    @QueryParam("email") String email,
                                    @QueryParam("emailVerified") Boolean emailVerified,
                                    @QueryParam("idpAlias") String idpAlias,
                                    @QueryParam("idpUserId") String idpUserId,
                                    @QueryParam("first") Integer firstResult,
                                    @QueryParam("max") Integer maxResults,
                                    @QueryParam("enabled") Boolean enabled,
                                    @QueryParam("briefRepresentation") Boolean briefRepresentation,
                                    @QueryParam("createdAfter") String createdAfter,
                                    @QueryParam("createdBefore") String createdBefore);

    /** 分页列出用户。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<UserRepresentation> list(@QueryParam("first") Integer firstResult,
                                  @QueryParam("max") Integer maxResults);

    /** 列出所有用户。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<UserRepresentation> list();

    /** 创建新用户。 */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response create(UserRepresentation userRepresentation);

    /**
     * 返回当前用户有权查看的用户总数。
     *
     * @return 用户数量
     */
    @Path("count")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Integer count();

    /**
     * 返回匹配搜索条件的用户数量；未指定条件时等价于 {@link #count()}。
     *
     * @param search 搜索条件
     * @return 匹配的用户数量
     */
    @Path("count")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Integer count(@QueryParam("search") String search);

    /**
     * 返回匹配给定过滤条件的用户数量；未指定任何过滤条件时等价于 {@link #count()}。
     *
     * @param last 姓氏
     * @param first 名字
     * @param email 邮箱
     * @param username 用户名
     * @return 匹配的用户数量
     */
    @Path("count")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Integer count(@QueryParam("lastName") String last,
                  @QueryParam("firstName") String first,
                  @QueryParam("email") String email,
                  @QueryParam("username") String username);

    /**
     * 返回匹配给定过滤条件的用户数量；未指定任何过滤条件时等价于 {@link #count()}。
     *
     * @param last 姓氏
     * @param first 名字
     * @param email 邮箱
     * @param emailVerified 邮箱是否已验证
     * @param username 用户名
     * @return 匹配的用户数量
     */
    @Path("count")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Integer count(@QueryParam("lastName") String last,
                  @QueryParam("firstName") String first,
                  @QueryParam("email") String email,
                  @QueryParam("emailVerified") Boolean emailVerified,
                  @QueryParam("username") String username);

    /**
     * 返回匹配给定过滤条件的用户数量；未指定任何过滤条件时等价于 {@link #count()}。
     *
     * @param search 通用搜索字符串
     * @param last 姓氏
     * @param first 名字
     * @param email 邮箱
     * @param emailVerified 邮箱是否已验证
     * @param username 用户名
     * @param enabled 用户是否启用
     * @return 匹配的用户数量
     */
    @Path("count")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Integer count(@QueryParam("search") String search,
                  @QueryParam("lastName") String last,
                  @QueryParam("firstName") String first,
                  @QueryParam("email") String email,
                  @QueryParam("emailVerified") Boolean emailVerified,
                  @QueryParam("username") String username,
                  @QueryParam("enabled") Boolean enabled,
                  @QueryParam("q") String searchQuery);

    /**
     * 返回匹配给定过滤条件的用户数量；未指定任何过滤条件时等价于 {@link #count()}。
     *
     * @param search 通用搜索字符串
     * @param last 姓氏
     * @param first 名字
     * @param email 邮箱
     * @param emailVerified 邮箱是否已验证
     * @param username 用户名
     * @param enabled 用户是否启用
     * @param idpAlias 关联身份提供程序别名（自 Keycloak 26.4.0 起支持）
     * @param idpUserId 关联身份提供程序中的用户 ID（自 Keycloak 26.4.0 起支持）
     * @return 匹配的用户数量
     */
    @Path("count")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Integer count(@QueryParam("search") String search,
                  @QueryParam("lastName") String last,
                  @QueryParam("firstName") String first,
                  @QueryParam("email") String email,
                  @QueryParam("emailVerified") Boolean emailVerified,
                  @QueryParam("username") String username,
                  @QueryParam("enabled") Boolean enabled,
                  @QueryParam("idpAlias") String idpAlias,
                  @QueryParam("idpUserId") String idpUserId,
                  @QueryParam("q") String searchQuery);

    /**
     * 返回匹配给定过滤条件的用户数量，支持精确匹配。
     *
     * @param search 通用搜索字符串
     * @param last 姓氏
     * @param first 名字
     * @param email 邮箱
     * @param emailVerified 邮箱是否已验证
     * @param username 用户名
     * @param enabled 用户是否启用
     * @param idpAlias 关联身份提供程序别名
     * @param idpUserId 关联身份提供程序中的用户 ID
     * @param exact 是否精确匹配各参数
     * @param searchQuery 自定义属性查询表达式
     * @return 匹配的用户数量
     */
    @Path("count")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Integer count(@QueryParam("search") String search,
                  @QueryParam("lastName") String last,
                  @QueryParam("firstName") String first,
                  @QueryParam("email") String email,
                  @QueryParam("emailVerified") Boolean emailVerified,
                  @QueryParam("username") String username,
                  @QueryParam("enabled") Boolean enabled,
                  @QueryParam("idpAlias") String idpAlias,
                  @QueryParam("idpUserId") String idpUserId,
                  @QueryParam("exact") Boolean exact,
                  @QueryParam("q") String searchQuery);

    /**
     * 返回匹配给定过滤条件的用户数量，支持创建时间范围过滤。
     *
     * @param search 通用搜索字符串
     * @param last 姓氏
     * @param first 名字
     * @param email 邮箱
     * @param emailVerified 邮箱是否已验证
     * @param username 用户名
     * @param enabled 用户是否启用
     * @param idpAlias 关联身份提供程序别名
     * @param idpUserId 关联身份提供程序中的用户 ID
     * @param exact 是否精确匹配各参数
     * @param searchQuery 自定义属性查询表达式
     * @param createdAfter 仅统计此日期（含）之后创建的用户
     * @param createdBefore 仅统计此日期（含）之前创建的用户
     * @return 匹配的用户数量
     */
    @Path("count")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Integer count(@QueryParam("search") String search,
                  @QueryParam("lastName") String last,
                  @QueryParam("firstName") String first,
                  @QueryParam("email") String email,
                  @QueryParam("emailVerified") Boolean emailVerified,
                  @QueryParam("username") String username,
                  @QueryParam("enabled") Boolean enabled,
                  @QueryParam("idpAlias") String idpAlias,
                  @QueryParam("idpUserId") String idpUserId,
                  @QueryParam("exact") Boolean exact,
                  @QueryParam("q") String searchQuery,
                  @QueryParam("createdAfter") String createdAfter,
                  @QueryParam("createdBefore") String createdBefore);

    /**
     * 返回邮箱验证状态匹配给定条件的用户数量。
     *
     * @param emailVerified 邮箱是否已验证
     * @return 匹配的用户数量
     */
    @Path("count")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Integer countEmailVerified(@QueryParam("emailVerified") Boolean emailVerified);

    /** 按 ID 获取单个用户子资源。 */
    @Path("{id}")
    UserResource get(@PathParam("id") String id);

    /** 删除指定 ID 的用户。 */
    @Path("{id}")
    @DELETE
    Response delete(@PathParam("id") String id);

    /** 获取用户配置文件子资源。 */
    @Path("profile")
    UserProfileResource userProfile();

}
