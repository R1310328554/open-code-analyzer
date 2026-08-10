package org.keycloak.workflow.admin.resource;

import jakarta.ws.rs.BadRequestException;
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
import jakarta.ws.rs.core.Response;

import org.keycloak.models.ModelException;
import org.keycloak.models.workflow.ResourceType;
import org.keycloak.models.workflow.Workflow;
import org.keycloak.models.workflow.WorkflowProvider;
import org.keycloak.representations.workflows.WorkflowRepresentation;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.resources.KeycloakOpenAPI;

import com.fasterxml.jackson.jakarta.rs.yaml.YAMLMediaTypes;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * 单个工作流的管理 REST 子资源：删除、更新、查询及按资源激活/停用工作流。
 */
@Extension(name = KeycloakOpenAPI.Profiles.ADMIN, value = "")
public class WorkflowResource {

    /** 工作流领域提供者。 */
    private final WorkflowProvider provider;
    /** 当前操作的目标工作流实体。 */
    private final Workflow workflow;

    /** @param provider 工作流提供者 @param workflow 目标工作流 */
    public WorkflowResource(WorkflowProvider provider, Workflow workflow) {
        this.provider = provider;
        this.workflow = workflow;
    }

    @DELETE
    @Tag(name = KeycloakOpenAPI.Admin.Tags.WORKFLOWS)
    @Operation(
            summary = "Delete workflow",
            description = "Delete the workflow and its configuration."
    )
    @APIResponses(value = {
            @APIResponse(responseCode = "204", description = "No Content"),
            @APIResponse(responseCode = "400", description = "Bad Request")
    })
    /** 删除工作流及其配置。 */
    public void delete() {
        try {
            provider.removeWorkflow(workflow);
        } catch (ModelException me) {
            throw ErrorResponse.error(me.getMessage(), Response.Status.BAD_REQUEST);
        }
    }

    /**
     * 更新工作流配置（不更新工作流步骤）。
     */
    @PUT
    @Consumes({YAMLMediaTypes.APPLICATION_JACKSON_YAML, MediaType.APPLICATION_JSON})
    @Tag(name = KeycloakOpenAPI.Admin.Tags.WORKFLOWS)
    @Operation(
            summary = "Update workflow",
            description = "Update the workflow configuration. This method does not update the workflow steps."
    )
    @APIResponses(value = {
            @APIResponse(responseCode = "204", description = "No Content"),
            @APIResponse(responseCode = "400", description = "Bad Request")
    })
    public void update(WorkflowRepresentation rep) {
        try {
            rep.setId(workflow.getId());
            provider.updateWorkflow(workflow, rep);
        } catch (ModelException me) {
            throw ErrorResponse.error(me.getMessage(), Response.Status.BAD_REQUEST);
        }
    }

    @GET
    @Produces({YAMLMediaTypes.APPLICATION_JACKSON_YAML, MediaType.APPLICATION_JSON})
    @Tag(name = KeycloakOpenAPI.Admin.Tags.WORKFLOWS)
    @Operation(
            summary = "Get workflow",
            description = "Get the workflow representation. Optionally exclude the workflow id from the response."
    )
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = WorkflowRepresentation.class))),
            @APIResponse(responseCode = "400", description = "Bad Request")
    })
    /** 获取工作流表示；{@code includeId=false} 时从响应中排除工作流与步骤 ID。 */
    public WorkflowRepresentation toRepresentation(
            @Parameter(
                    description = "Indicates whether the workflow and step ids should be included in the representation or not - defaults to true"
            )
            @QueryParam("includeId") Boolean includeIds
    ) {
        WorkflowRepresentation rep = provider.toRepresentation(workflow);
        if (Boolean.FALSE.equals(includeIds)) {
            rep.setId(null);
            rep.getSteps().forEach(step -> step.setId(null));
        }
        return rep;
    }

    /**
     * 为指定资源激活工作流。
     *
     * @param type 资源类型
     * @param resourceId 资源 ID
     * @param notBefore 可选的首步调度时间，覆盖步骤 {@code after} 配置：整数表示秒数、整数加 {@code ms} 表示毫秒，或 ISO-8601 日期字符串
     */
    @POST
    @Path("activate/{type}/{resourceId}")
    @Tag(name = KeycloakOpenAPI.Admin.Tags.WORKFLOWS)
    @Operation(
            summary = "Activate workflow for resource",
            description = "Activate the workflow for the given resource type and identifier. Optionally schedule the first step using the notBefore parameter."
    )
    @APIResponses(value = {
            @APIResponse(responseCode = "204", description = "No Content"),
            @APIResponse(responseCode = "400", description = "Bad Request")
    })
    public void activate(
            @Parameter(description = "Resource type")
            @PathParam("type") ResourceType type,
            @Parameter(description = "Resource identifier")
            @PathParam("resourceId") String resourceId,
            @Parameter(
                    description = "Optional value representing the time to schedule the first workflow step. " +
                            "The value is either an integer representing the seconds from now, " +
                            "an integer followed by 'ms' representing milliseconds from now, " +
                            "or an ISO-8601 date string."
            )
            @QueryParam("notBefore") String notBefore
    ) {
        Object resource = provider.getResourceTypeSelector(type).resolveResource(resourceId);

        if (resource == null) {
            throw new BadRequestException("Resource with id " + resourceId + " not found");
        }

        if (notBefore != null) {
            workflow.setNotBefore(notBefore);
        }

        provider.activate(workflow, type, resourceId);
    }

    /**
     * 为指定资源停用工作流。
     *
     * @param type 资源类型
     * @param resourceId 资源 ID
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("deactivate/{type}/{resourceId}")
    @Tag(name = KeycloakOpenAPI.Admin.Tags.WORKFLOWS)
    @Operation(
            summary = "Deactivate workflow for resource",
            description = "Deactivate the workflow for the given resource type and identifier."
    )
    @APIResponses(value = {
            @APIResponse(responseCode = "204", description = "No Content"),
            @APIResponse(responseCode = "400", description = "Bad Request")
    })
    public void deactivate(
            @Parameter(description = "Resource type")
            @PathParam("type") ResourceType type,
            @Parameter(description = "Resource identifier")
            @PathParam("resourceId") String resourceId
    ) {
        Object resource = provider.getResourceTypeSelector(type).resolveResource(resourceId);

        if (resource == null) {
            throw new BadRequestException("Resource with id " + resourceId + " not found");
        }

        provider.deactivate(workflow, resourceId);
    }

}
