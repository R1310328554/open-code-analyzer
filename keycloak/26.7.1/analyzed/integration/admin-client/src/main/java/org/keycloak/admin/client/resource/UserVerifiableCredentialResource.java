package org.keycloak.admin.client.resource;

import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.keycloak.representations.idm.oid4vc.IssuedVerifiableCredentialRepresentation;
import org.keycloak.representations.idm.oid4vc.UserVerifiableCredentialRepresentation;
import org.keycloak.representations.idm.oid4vc.VerifiableCredentialOfferActionConfig;

/**
 * 用户可验证凭证（Verifiable Credential）的管理 REST 资源。
 * <p>
 * 所有子端点自 Keycloak 26.7.0 起可用。需启用特性
 * {@link org.keycloak.common.Profile.Feature#OID4VC_VCI}，且领域内须开启可验证凭证功能。
 *
 * @since Keycloak 26.7.0 All the child endpoints are also available since that version<p>
 *
 * This endpoint including all the child endpoints requires feature {@link org.keycloak.common.Profile.Feature#OID4VC_VCI} to be enabled and also requires "verifiable credentials" to be enabled for the realm<p>
 */
public interface UserVerifiableCredentialResource {

    /** 为用户创建可验证凭证配置。 */
    @POST
    @Path("credentials")
    @Consumes({MediaType.APPLICATION_JSON})
    UserVerifiableCredentialRepresentation createCredential(UserVerifiableCredentialRepresentation representation);

    /** 列出用户的可验证凭证配置。 */
    @GET
    @Path("credentials")
    @Produces(MediaType.APPLICATION_JSON)
    List<UserVerifiableCredentialRepresentation> getCredentials();

    /** 撤销指定作用域名称的可验证凭证配置。 */
    @DELETE
    @Path("credentials/{credentialScopeName}")
    void revokeCredential(@PathParam("credentialScopeName") String credentialScopeName);

    /** 更新指定作用域名称的可验证凭证配置。 */
    @PUT
    @Path("credentials/{credentialScopeName}")
    @Produces(MediaType.APPLICATION_JSON)
    UserVerifiableCredentialRepresentation updateCredential(@PathParam("credentialScopeName") String credentialScopeName);

    /** 列出用户已签发的可验证凭证。 */
    @GET
    @Path("issued-credentials")
    @Produces(MediaType.APPLICATION_JSON)
    List<IssuedVerifiableCredentialRepresentation> getIssuedCredentials();

    /** 撤销已签发的可验证凭证。 */
    @DELETE
    @Path("issued-credentials/{id}")
    void revokeIssuedCredential(@PathParam("id") String credentialId);

    /**
     * 向用户发送可验证凭证发放邮件。
     *
     * @param clientId 客户端 ID
     * @param redirectUri 操作完成后的重定向 URI
     * @param lifespan 邮件链接中令牌的有效期（秒）
     * @param credentialOfferConfig 凭证发放动作配置
     */
    @PUT
    @Path("credentials/send-credential-offer")
    @Consumes(MediaType.APPLICATION_JSON)
    void sendCredentialOffer(@QueryParam("client_id") String clientId,
                             @QueryParam("redirect_uri") String redirectUri,
                             @QueryParam("lifespan") Integer lifespan,
                             VerifiableCredentialOfferActionConfig credentialOfferConfig);
}
