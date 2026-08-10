package org.keycloak.rest.admin.api.client;

import java.io.InputStream;

import jakarta.annotation.Nonnull;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import org.keycloak.admin.api.client.ClientApi;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.admin.v2.BaseClientRepresentation;
import org.keycloak.services.PatchType;
import org.keycloak.services.ServiceException;
import org.keycloak.services.client.ClientService;
import org.keycloak.services.client.DefaultClientService;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;


/**
 * {@link org.keycloak.admin.api.client.ClientApi} 默认实现：单客户端 CRUD 与 JSON Merge Patch。
 */
public class DefaultClientApi implements ClientApi {
    private final KeycloakSession session;
    private final String clientId;
    private final RealmModel realm;
    private final AdminPermissionEvaluator permissions;
    private final ClientService clientService;

    /** 绑定会话、realm、clientId 与权限评估器。 */
    public DefaultClientApi(@Nonnull KeycloakSession session,
                            @Nonnull RealmModel realm,
                            @Nonnull String clientId,
                            @Nonnull AdminPermissionEvaluator permissions) {
        this.session = session;
        this.clientId = clientId;
        this.realm = realm;
        this.permissions = permissions;
        this.clientService = new DefaultClientService(session, realm, permissions);
    }

    /** {@inheritDoc} 按 clientId 获取客户端表示。 */
    @GET
    @Override
    public BaseClientRepresentation getClient() {
        try {
            return clientService.getClient(realm, clientId)
                    .orElseThrow(() -> new NotFoundException("Cannot find the specified client"));
        } catch (ServiceException e) {
            throw e.toWebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    /** {@inheritDoc} PUT 创建或全量更新客户端。 */
    @PUT
    @Override
    public Response createOrUpdateClient(BaseClientRepresentation client) {
        var result = clientService.createOrUpdateClient(realm, clientId, client);
        return Response.status(result.created() ? Response.Status.CREATED : Response.Status.OK).entity(result.representation()).build();
    }

    /** {@inheritDoc} 按 Content-Type 解析 {@link org.keycloak.services.PatchType} 并合并补丁。 */
    @PATCH
    @Override
    public BaseClientRepresentation patchClient(InputStream patch) {
        String contentType = session.getContext().getHttpRequest().getHttpHeaders().getHeaderString(HttpHeaders.CONTENT_TYPE);
        PatchType patchType = PatchType.getByMediaType(contentType)
                .orElseThrow(() -> new WebApplicationException("Unsupported media type", Response.Status.UNSUPPORTED_MEDIA_TYPE));

        return clientService.patchClient(realm, clientId, patchType, patch);
    }

    /** {@inheritDoc} 删除指定 clientId 的客户端。 */
    @DELETE
    @Override
    public Response deleteClient() {
        clientService.deleteClient(realm, clientId);
        return Response.noContent().build();
    }
}
