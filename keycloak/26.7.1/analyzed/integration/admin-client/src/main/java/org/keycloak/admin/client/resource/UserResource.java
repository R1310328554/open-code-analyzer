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
import java.util.Map;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.FederatedIdentityRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.UserSessionRepresentation;

/**
 * 单个用户的管理 REST 资源。
 * <p>
 * 支持读取、更新、删除用户，管理凭据、组、会话、联合身份、
 * 角色映射、同意记录及可验证凭证等。
 *
 * @author rodrigo.sasaki@icarros.com.br
 */
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface UserResource {

    /** 获取用户表示对象。 */
    @GET
    UserRepresentation toRepresentation();

    /**
     * 获取用户表示对象。
     *
     * @param userProfileMetadata 是否包含用户配置文件元数据
     */
    @GET
    UserRepresentation toRepresentation(@QueryParam("userProfileMetadata") boolean userProfileMetadata);

    /** 更新用户信息。 */
    @PUT
    void update(UserRepresentation userRepresentation);

    /** 删除当前用户。 */
    @DELETE
    void remove();

    /** 列出用户所属的全部组。 */
    @Path("groups")
    @GET
    List<GroupRepresentation> groups();

    /**
     * 分页列出用户所属组。
     *
     * @param firstResult 分页起始偏移
     * @param maxResults 分页最大条数
     */
    @Path("groups")
    @GET
    List<GroupRepresentation> groups(@QueryParam("first") Integer firstResult,
                                     @QueryParam("max") Integer maxResults);

    @Path("groups")
    @GET
    List<GroupRepresentation> groups(@QueryParam("search") String search,
                                     @QueryParam("first") Integer firstResult,
                                     @QueryParam("max") Integer maxResults);

    @Path("groups")
    @GET
    List<GroupRepresentation> groups(@QueryParam("first") Integer firstResult,
                                     @QueryParam("max") Integer maxResults,
                                     @QueryParam("briefRepresentation") @DefaultValue("true") boolean briefRepresentation);

    @Path("groups")
    @GET
    List<GroupRepresentation> groups(@QueryParam("search") String search,
                                     @QueryParam("briefRepresentation") @DefaultValue("true") boolean briefRepresentation);

    @Path("groups")
    @GET
    List<GroupRepresentation> groups(@QueryParam("search") String search,
                                     @QueryParam("first") Integer firstResult,
                                     @QueryParam("max") Integer maxResults,
                                     @QueryParam("briefRepresentation") @DefaultValue("true") boolean briefRepresentation);

    /**
     * 统计用户所属组的数量。
     *
     * @param search 组名搜索关键字
     */
    @Path("groups/count")
    @GET
    Map<String, Long> groupsCount(@QueryParam("search") String search);

    /** 将用户加入指定组。 */
    @Path("groups/{groupId}")
    @PUT
    void joinGroup(@PathParam("groupId") String groupId);

    /** 将用户移出指定组。 */
    @Path("groups/{groupId}")
    @DELETE
    void leaveGroup(@PathParam("groupId") String groupId);




    /** 注销用户的所有会话。 */
    @POST
    @Path("logout")
    void logout();



    /** 列出用户的所有凭据。 */
    @GET
    @Path("credentials")
    @Produces(MediaType.APPLICATION_JSON)
    List<CredentialRepresentation> credentials();


    /**
     * 返回用户所在用户存储提供程序支持的凭据类型，例如 "password"、"otp" 等。
     * 对于未关联用户存储的本地用户，始终返回空列表。
     *
     * @return 凭据类型列表
     */
    @GET
    @Path("configured-user-storage-credential-types")
    @Produces(MediaType.APPLICATION_JSON)
    List<String> getConfiguredUserStorageCredentialTypes();

    /** 移除用户的指定凭据。 */
    @DELETE
    @Path("credentials/{credentialId}")
    void removeCredential(@PathParam("credentialId")String credentialId);

    /** 更新用户凭据的用户标签。 */
    @PUT
    @Consumes(jakarta.ws.rs.core.MediaType.TEXT_PLAIN)
    @Path("credentials/{credentialId}/userLabel")
    void setCredentialUserLabel(final @PathParam("credentialId") String credentialId, String userLabel);

    /**
     * 将凭据移至用户凭据列表的首位。
     *
     * @param credentialId 要移动的凭据 ID
     */
    @Path("credentials/{credentialId}/moveToFirst")
    @POST
    void moveCredentialToFirst(final @PathParam("credentialId") String credentialId);

    /**
     * 将凭据移动到另一凭据之后。
     *
     * @param credentialId 要移动的凭据 ID
     * @param newPreviousCredentialId 移动后位于其后的凭据 ID；为 null 时移至首位
     */
    @Path("credentials/{credentialId}/moveAfter/{newPreviousCredentialId}")
    @POST
    void moveCredentialAfter(final @PathParam("credentialId") String credentialId, final @PathParam("newPreviousCredentialId") String newPreviousCredentialId);


    /**
     * 禁用或删除指定类型的所有凭据，例如 "otp"、"password"。
     * <p>
     * 通常仅适用于由用户存储提供程序支持的用户。可通过
     * {@link UserRepresentation#getDisableableCredentialTypes()} 查看该用户可禁用的凭据类型。
     *
     * @param credentialTypes 要禁用的凭据类型列表
     */
    @Path("disable-credential-types")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    void disableCredentialType(List<String> credentialTypes);

    /** 重置用户密码。 */
    @PUT
    @Path("reset-password")
    void resetPassword(CredentialRepresentation credentialRepresentation);

    /**
     * 已弃用：请改用 {@link #executeActionsEmail(java.util.List)} 并传入 UPDATE_PASSWORD 必需操作。
     */
    @PUT
    @Path("reset-password-email")
    @Deprecated
    void resetPasswordEmail();

    /**
     * 已弃用：请改用 {@link #executeActionsEmail(java.util.List)} 并传入 UPDATE_PASSWORD 必需操作。
     *
     * @param clientId 客户端 ID
     */
    @PUT
    @Path("reset-password-email")
    @Deprecated
    void resetPasswordEmail(@QueryParam("client_id") String clientId);

    /**
     * 向用户发送包含操作链接的邮件。用户点击链接后需执行指定必需操作，
     * 例如 {@code VERIFY_EMAIL}、{@code UPDATE_PROFILE}、{@code CONFIGURE_TOTP}、
     * {@code UPDATE_PASSWORD}、{@code TERMS_AND_CONDITIONS} 等。
     *
     * @param actions {@link org.keycloak.models.UserModel.RequiredAction} 的字符串表示列表
     */
    @PUT
    @Path("execute-actions-email")
    void executeActionsEmail(List<String> actions);

    /**
     * 向用户发送包含操作链接的邮件，可指定链接令牌有效期。
     * <p>
     * 默认有效期为 12 小时。用户点击链接后需执行指定必需操作。
     *
     * @param actions {@link org.keycloak.models.UserModel.RequiredAction} 的字符串表示列表
     * @param lifespan 邮件链接中令牌的有效期（秒）
     */
    @PUT
    @Path("execute-actions-email")
    void executeActionsEmail(List<String> actions, @QueryParam("lifespan") Integer lifespan);

    /**
     * 向用户发送包含操作链接的邮件，可指定客户端、重定向 URI 及令牌有效期。
     * <p>
     * 若 redirectUri 不为 null，则必须指定 clientId。操作完成后将跳转至该 URI；
     * 若两者均为 null，则流程结束后不跳转任何页面。默认令牌有效期为 12 小时。
     *
     * @param clientId 客户端 ID
     * @param redirectUri 操作完成后的重定向 URI
     * @param lifespan 邮件链接中令牌的有效期（秒）
     * @param actions {@link org.keycloak.models.UserModel.RequiredAction} 的字符串表示列表
     */
    @PUT
    @Path("execute-actions-email")
    void executeActionsEmail(@QueryParam("client_id") String clientId,
                             @QueryParam("redirect_uri") String redirectUri,
                             @QueryParam("lifespan") Integer lifespan,
                             List<String> actions);

    /**
     * 向用户发送包含操作链接的邮件，可指定客户端与重定向 URI。
     * <p>
     * 若 redirectUri 不为 null，则必须指定 clientId；若两者均为 null，则流程结束后不跳转任何页面。
     *
     * @param clientId 客户端 ID
     * @param redirectUri 操作完成后的重定向 URI
     * @param actions {@link org.keycloak.models.UserModel.RequiredAction} 的字符串表示列表
     */
    @PUT
    @Path("execute-actions-email")
    void executeActionsEmail(@QueryParam("client_id") String clientId, @QueryParam("redirect_uri") String redirectUri, List<String> actions);

    /** 向用户发送邮箱验证邮件。 */
    @PUT
    @Path("send-verify-email")
    void sendVerifyEmail();

    @PUT
    @Path("send-verify-email")
    void sendVerifyEmail(@QueryParam("client_id") String clientId);

    @PUT
    @Path("send-verify-email")
    void sendVerifyEmail(@QueryParam("client_id") String clientId, @QueryParam("redirect_uri") String redirectUri);

    @PUT
    @Path("send-verify-email")
    void sendVerifyEmail(@QueryParam("lifespan") Integer lifespan);

    /**
     * 向用户发送邮箱验证邮件。
     * <p>
     * 邮件包含验证链接。redirectUri 与 clientId 为可选参数；
     * 默认重定向至 account 客户端，默认令牌有效期为 12 小时。
     *
     * @param clientId 客户端 ID
     * @param redirectUri 重定向 URI
     * @param lifespan 生成令牌的有效期（秒）
     */
    @PUT
    @Path("send-verify-email")
    void sendVerifyEmail(@QueryParam("client_id") String clientId, @QueryParam("redirect_uri") String redirectUri, @QueryParam("lifespan") Integer lifespan);

    /** 获取用户的在线会话列表。 */
    @GET
    @Path("sessions")
    List<UserSessionRepresentation> getUserSessions();

    /**
     * 获取用户在指定客户端下的离线会话。
     *
     * @param clientId 客户端 ID
     */
    @GET
    @Path("offline-sessions/{clientId}")
    List<UserSessionRepresentation> getOfflineSessions(@PathParam("clientId") String clientId);

    /** 列出用户的联合身份信息。 */
    @GET
    @Path("federated-identity")
    List<FederatedIdentityRepresentation> getFederatedIdentity();

    /**
     * 为用户添加联合身份。
     *
     * @param provider 身份提供程序名称
     * @param rep 联合身份表示对象
     */
    @POST
    @Path("federated-identity/{provider}")
    Response addFederatedIdentity(@PathParam("provider") String provider, FederatedIdentityRepresentation rep);

    /** 移除用户的指定联合身份。 */
    @Path("federated-identity/{provider}")
    @DELETE
    void removeFederatedIdentity(final @PathParam("provider") String provider);

    /** 获取用户角色映射子资源。 */
    @Path("role-mappings")
    RoleMappingResource roles();


    /** 列出用户已授予的客户端同意记录。 */
    @GET
    @Path("consents")
    List<Map<String, Object>> getConsents();

    /** 撤销用户对指定客户端的同意。 */
    @DELETE
    @Path("consents/{client}")
    void revokeConsent(@PathParam("client") String clientId);

    /**
     * 获取用户可验证凭证子资源。
     *
     * @return 用于管理用户可验证凭证及已签发凭证的 {@link UserVerifiableCredentialResource}
     * @since Keycloak server 26.7.0
     */
    @Path("vc")
    UserVerifiableCredentialResource verifiableCredentials();

    /** 以当前用户身份发起模拟登录。 */
    @POST
    @Path("impersonation")
    @Produces(MediaType.APPLICATION_JSON)
    Map<String, Object> impersonate();

    /**
     * 获取用户的非托管属性。
     *
     * @return 非托管属性映射
     * @since Keycloak server 24.0.6
     */
    @GET
    @Path("unmanagedAttributes")
    @Produces(MediaType.APPLICATION_JSON)
    Map<String, List<String>> getUnmanagedAttributes();
}
