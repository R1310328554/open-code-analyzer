package org.keycloak.rest.admin.api.client;

import java.util.stream.Stream;

import jakarta.annotation.Nonnull;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.admin.api.ListOptions;
import org.keycloak.admin.api.client.ClientApi;
import org.keycloak.admin.api.client.ClientsApi;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.admin.v2.BaseClientRepresentation;
import org.keycloak.services.client.ClientService;
import org.keycloak.services.client.ClientService.ClientProjectionOptions;
import org.keycloak.services.client.ClientService.ClientSortAndSliceOptions;
import org.keycloak.services.client.DefaultClientService;
import org.keycloak.services.client.query.ClientQueryException;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;

/**
 * {@link org.keycloak.admin.api.client.ClientsApi} 默认实现：客户端列表、创建与按 clientId 路由到 {@link DefaultClientApi}。
 */
public class DefaultClientsApi implements ClientsApi {

    private final KeycloakSession session;
    private final AdminPermissionEvaluator permissions;
    private final RealmModel realm;
    private final ClientService clientService;

    /** 绑定会话、realm 与权限评估器。 */
    public DefaultClientsApi(@Nonnull KeycloakSession session,
                             @Nonnull RealmModel realm,
                             @Nonnull AdminPermissionEvaluator permissions) {
        this.session = session;
        this.realm = realm;
        this.permissions = permissions;
        this.clientService = new DefaultClientService(session, realm, permissions);
    }

    /** {@inheritDoc} 分页/投影/排序查询客户端列表。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Stream<BaseClientRepresentation> getClients(ListOptions params) {
        try {
            var searchOptions = params.getQuery() != null ? new ClientService.ClientSearchOptions(params.getQuery()) : null;
            return clientService.getClients(realm, new ClientProjectionOptions(params.getFields()), searchOptions,
                    ClientSortAndSliceOptions.fromQuery(params));
        } catch (ClientQueryException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    /** {@inheritDoc} 创建新客户端并返回 201。 */
    @POST
    @Override
    public Response createClient(@Valid BaseClientRepresentation client) {
        return Response.status(Response.Status.CREATED)
                .entity(clientService.createClient(realm, client))
                .build();
    }

    /**
     * 路径 {@code clientId} 不存在时，若调用方无权列出客户端则返回 403（防 clientId 钓鱼），与 Admin API v1 {@code ClientsResource#getClient} 一致。
     */
    private void enforceAntiPhishingIfClientMissing(String clientId) {
        if (realm.getClientByClientId(clientId) == null && !permissions.clients().canList()) {
            throw new ForbiddenException();
        }
    }

    /** {@inheritDoc} 路由到单客户端子资源。 */
    @Path("{id}")
    @Override
    public ClientApi client(@PathParam("id") String clientId) {
        enforceAntiPhishingIfClientMissing(clientId);
        return new DefaultClientApi(session, realm, clientId, permissions);
    }

}
