package org.keycloak.admin.client.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import org.keycloak.representations.workflows.WorkflowRepresentation;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * 单个工作流（Workflow）的管理 REST 资源。
 * <p>
 * 支持读取、更新、删除工作流，以及对指定资源执行激活或停用操作。
 */
public interface WorkflowResource {

    /** 删除当前工作流。 */
    @DELETE
    Response delete();

    /** 更新当前工作流配置。 */
    @PUT
    @Consumes(APPLICATION_JSON)
    Response update(WorkflowRepresentation workflow);

    /** 获取当前工作流的表示对象。 */
    @GET
    @Produces(APPLICATION_JSON)
    WorkflowRepresentation toRepresentation();

    /**
     * 立即激活指定资源的工作流步骤。
     *
     * @param type 资源类型
     * @param resourceId 资源 ID
     */
    @Path("activate/{type}/{resourceId}")
    @POST
    void activate(@PathParam("type") String type, @PathParam("resourceId") String resourceId);

    /**
     * 在指定时间之后激活资源的工作流步骤。
     *
     * @param type 资源类型
     * @param resourceId 资源 ID
     * @param notBefore 最早激活时间
     */
    @Path("activate/{type}/{resourceId}")
    @POST
    void activate(@PathParam("type") String type, @PathParam("resourceId") String resourceId, @QueryParam("notBefore") String notBefore);

    /**
     * 停用指定资源的工作流步骤。
     *
     * @param type 资源类型
     * @param resourceId 资源 ID
     */
    @Path("deactivate/{type}/{resourceId}")
    @POST
    void deactivate(@PathParam("type") String type, @PathParam("resourceId") String resourceId);

}
