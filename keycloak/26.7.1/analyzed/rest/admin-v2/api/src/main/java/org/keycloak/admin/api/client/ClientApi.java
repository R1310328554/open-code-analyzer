package org.keycloak.admin.api.client;

import java.io.InputStream;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.admin.api.PatchTypeNames;
import org.keycloak.representations.admin.v2.BaseClientRepresentation;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

/**
 * 单个客户端的 Admin API v2 JAX-RS 资源。
 * <p>
 * 提供按 clientId 查询、创建/更新（PUT）、部分更新（JSON Merge Patch）及删除操作。
 */
public interface ClientApi {

    /**
     * 按 clientId 获取单个客户端。
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get a client", description = "Returns a single client by its clientId")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = BaseClientRepresentation.class))),
        @APIResponse(responseCode = "404", description = "Not Found")
    })
    BaseClientRepresentation getClient();

    /**
     * 创建或全量更新客户端（幂等 PUT）。
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create or update a client", description = "Creates or updates a client in the realm")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = BaseClientRepresentation.class))),
        @APIResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = BaseClientRepresentation.class)))
    })
    Response createOrUpdateClient(BaseClientRepresentation client);

    /**
     * 使用 JSON Merge Patch 部分更新客户端。
     */
    @PATCH
    @Consumes(PatchTypeNames.JSON_MERGE)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Patch a client", description = "Partially updates a client using JSON Merge Patch")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = BaseClientRepresentation.class))),
        @APIResponse(responseCode = "404", description = "Not Found")
    })
    @RequestBody(required = true, content = @Content(schema = @Schema(type = SchemaType.OBJECT)))
    BaseClientRepresentation patchClient(InputStream patch);

    /**
     * 从领域中删除该客户端。
     */
    @DELETE
    @Operation(summary = "Delete a client", description = "Deletes a client from the realm")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "Client successfully deleted"),
        @APIResponse(responseCode = "404", description = "Not Found")
    })
    Response deleteClient();
}
