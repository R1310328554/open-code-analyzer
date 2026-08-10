package org.keycloak.admin.client.resource;

import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.representations.workflows.WorkflowRepresentation;

import com.fasterxml.jackson.jakarta.rs.yaml.YAMLMediaTypes;

/**
 * 工作流（Workflow）集合的管理 REST 资源。
 * <p>
 * 自 Keycloak 26.4.0 起可用。需启用特性
 * {@link org.keycloak.common.Profile.Feature#WORKFLOWS}。该特性在 26.5.0 中为预览状态，
 * 后续版本可能存在不兼容变更。
 *
 * @since Keycloak server 26.4.0. All the child endpoints are also available since that version<p>
 *
 * This endpoint including all the child endpoints require feature {@link org.keycloak.common.Profile.Feature#WORKFLOWS} to be enabled. Note that feature is preview in 26.5.0 and there might be
 * backwards incompatible changes in the future versions of admin-client and Keycloak server<p>
 */
public interface WorkflowsResource {

    /** 创建新工作流（支持 JSON 或 YAML 格式）。 */
    @POST
    @Consumes({MediaType.APPLICATION_JSON, YAMLMediaTypes.APPLICATION_JACKSON_YAML})
    Response create(WorkflowRepresentation representation);

    /** 列出所有工作流。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<WorkflowRepresentation> list();

    /**
     * 分页搜索工作流。
     *
     * @param search 搜索关键字
     * @param exact 是否精确匹配
     * @param firstResult 分页起始偏移
     * @param maxResults 分页最大条数
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<WorkflowRepresentation> list(
            @QueryParam("search") String search,
            @QueryParam("exact") Boolean exact,
            @QueryParam("first") Integer firstResult,
            @QueryParam("max") Integer maxResults
    );

    /**
     * 获取指定资源已调度的工作流列表。
     *
     * @param resourceId 资源 ID
     */
    @Path("scheduled/{resource-id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<WorkflowRepresentation> getScheduledWorkflows(@PathParam("resource-id") String resourceId);

    /**
     * 按 ID 获取单个工作流子资源。
     *
     * @param id 工作流 ID
     */
    @Path("{id}")
    WorkflowResource workflow(@PathParam("id") String id);

    /**
     * 将已调度资源从一个步骤迁移到另一个步骤。
     *
     * @param stepFrom 源步骤 ID
     * @param stepTo 目标步骤 ID
     * @return 成功时返回 204 无内容响应；失败时返回 400 错误响应
     * @since Keycloak server 26.6.0
     */
    @POST
    @Path("migrate")
    Response migrate(@QueryParam("from") String stepFrom, @QueryParam("to") String stepTo);
}
