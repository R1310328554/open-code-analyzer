package org.keycloak.scim.services;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriBuilder;

import org.keycloak.events.admin.OperationType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.scim.protocol.ForbiddenException;
import org.keycloak.scim.protocol.request.PatchRequest;
import org.keycloak.scim.protocol.request.SearchRequest;
import org.keycloak.scim.protocol.response.ListResponse;
import org.keycloak.scim.resource.ResourceTypeRepresentation;
import org.keycloak.scim.resource.Scim;
import org.keycloak.scim.resource.common.Meta;
import org.keycloak.scim.resource.spi.ScimResourceTypeProvider;
import org.keycloak.scim.resource.spi.SingletonResourceTypeProvider;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.jboss.logging.Logger;

import static org.keycloak.scim.services.Error.badRequest;
import static org.keycloak.scim.services.Error.forbidden;
import static org.keycloak.scim.services.Error.invalidSyntax;
import static org.keycloak.scim.services.Error.resourceNotFound;
import static org.keycloak.scim.services.Error.toResponse;

/**
 * 单个 SCIM 资源类型的 CRUD 与搜索端点实现。
 * <p>处理 create/get/search/update/patch/delete，并记录管理事件与 meta 信息。</p>
 */
public class ScimResourceTypeResource<R extends ResourceTypeRepresentation> {

    private static final Logger logger = Logger.getLogger(ScimResourceTypeResource.class);
    /** SCIM JSON 媒体类型。 */
    private static final String APPLICATION_SCIM_JSON = "application/scim+json";

    /** 当前 Keycloak 会话。 */
    private final KeycloakSession session;
    /** 资源类型 SPI 提供者。 */
    private final ScimResourceTypeProvider<R> resourceTypeProvider;
    /** 资源表示类的 Jackson 反序列化类型。 */
    private final Class<? extends ResourceTypeRepresentation> resourceTypeClazz;
    /** 管理审计事件构建器。 */
    private final AdminEventBuilder adminEvent;

    /** 绑定会话、提供者与管理事件上下文。 */
    public ScimResourceTypeResource(KeycloakSession session, ScimResourceTypeProvider<R> resourceTypeProvider, AdminEventBuilder adminEvent) {
        this.session = session;
        this.resourceTypeProvider = resourceTypeProvider;
        this.resourceTypeClazz = resourceTypeProvider.getResourceType();
        this.adminEvent = adminEvent.resource(resourceTypeProvider.getAdminEventResourceType());
    }

    /** SCIM POST：创建资源，请求体不得预置 id。 */
    @POST
    @Consumes({APPLICATION_SCIM_JSON, MediaType.APPLICATION_JSON})
    @Produces(APPLICATION_SCIM_JSON)
    public Response create(InputStream is) {
        R resource = parseResourceTypePayload(is);

        if (resource.getId() != null) {
            return invalidSyntax("Unexpected identifier");
        }

        return onPersist(resource, Status.CREATED,
                (rScimResourceTypeProvider, r) -> {
                    R created = resourceTypeProvider.create(r);
                    logger.debugf("SCIM CREATE %s id=%s", resourceTypeProvider.getName(), created.getId());
                    adminEvent.operation(OperationType.CREATE)
                            .resourcePath(session.getContext().getUri(), created.getId())
                            .representation(created)
                            .success();
                    return created;
                });
    }

    /** SCIM GET by id，支持 attributes/excludedAttributes 过滤返回字段。 */
    @Path("{id}")
    @GET
    @Produces(APPLICATION_SCIM_JSON)
    public Response get(@PathParam("id") String id,
                        @QueryParam("attributes") String attributes,
                        @QueryParam("excludedAttributes") String excludedAttributes) {
        logger.debugf("SCIM GET %s id=%s", resourceTypeProvider.getName(), id);
        List<String> attrList = attributes != null ? List.of(attributes.split(",")) : null;
        List<String> excludedList = excludedAttributes != null ? List.of(excludedAttributes.split(",")) : null;

        R resource = getResource(id, attrList, excludedList);

        if (resource == null) {
            return resourceNotFound(id);
        }

        setMetadata(resource);

        return Response.ok().entity(resource).build();
    }

    /** SCIM GET 集合：将查询参数封装为 {@link SearchRequest} 并委托 search。 */
    @GET
    @Produces(APPLICATION_SCIM_JSON)
    public Response getAll(@QueryParam("filter") String filterExpression,
                           @QueryParam("attributes") String attributes,
                           @QueryParam("excludedAttributes") String excludedAttributes,
                           @QueryParam("sortBy") String sortBy,
                           @QueryParam("sortOrder") String sortOrder,
                           @QueryParam("startIndex") Integer startIndex,
                           @QueryParam("count") Integer count) {
        // 委托给统一的 search 逻辑
        return search(SearchRequest.builder().withFilter(filterExpression)
                        .withAttributes(attributes != null ? List.of(attributes.split(",")) : null)
                        .withExcludedAttributes(excludedAttributes != null ? List.of(excludedAttributes.split(",")) : null)
                        .withSortBy(sortBy)
                        .withSortOrder(sortOrder)
                        .withStartIndex(startIndex)
                        .withCount(count).build());
    }

    /** SCIM .search POST：按 filter/分页/排序查询资源列表或单例资源。 */
    @Path(".search")
    @POST
    @Consumes({APPLICATION_SCIM_JSON, MediaType.APPLICATION_JSON})
    @Produces(APPLICATION_SCIM_JSON)
    public Response search(SearchRequest searchRequest) {
        logger.debugf("SCIM SEARCH %s filter=%s", resourceTypeProvider.getName(), searchRequest.getFilter());
        try {
            Stream<R> stream = resourceTypeProvider.getAll(searchRequest)
                    .peek(this::setMetadata);

            if (resourceTypeProvider instanceof SingletonResourceTypeProvider<R>) {
                return Response.ok().entity(stream
                                .findAny().orElseThrow(NotFoundException::new))
                        .build();
            }

            List<R> resources = stream.toList();
            Long totalResults = resourceTypeProvider.count(searchRequest);
            ListResponse<R> response = new ListResponse<>();

            response.setResources(resources);
            response.setTotalResults(totalResults.intValue());
            response.setStartIndex(searchRequest.getStartIndex() != null ? searchRequest.getStartIndex() : 1);
            response.setItemsPerPage(resources.size());

            return Response.ok().entity(response).build();
        } catch (Exception e) {
            return toResponse(session, e);
        }
    }

    /** SCIM DELETE：删除指定 id 的资源。 */
    @Path("{id}")
    @DELETE
    @Produces(APPLICATION_SCIM_JSON)
    public Response delete(@PathParam("id") String id) {
        logger.debugf("SCIM DELETE %s id=%s", resourceTypeProvider.getName(), id);
        try {
            R resource = getResource(id);

            if (resource == null) {
                return resourceNotFound(id);
            }

            if (resourceTypeProvider.delete(id)) {
                adminEvent.operation(OperationType.DELETE)
                        .resourcePath(session.getContext().getUri())
                        .representation(resource)
                        .success();
                return Response.noContent().build();
            }

            return badRequest("Could not delete resource not found with id " + id);
        } catch (Exception e) {
            return toResponse(session, e);
        }
    }

    /** SCIM PUT：全量替换资源，路径 id 须与 body id 一致。 */
    @Path("{id}")
    @PUT
    @Consumes({APPLICATION_SCIM_JSON, MediaType.APPLICATION_JSON})
    @Produces(APPLICATION_SCIM_JSON)
    public Response update(@PathParam("id") String id, InputStream is) {
        logger.debugf("SCIM UPDATE %s id=%s", resourceTypeProvider.getName(), id);
        R existing = getResource(id);

        if (existing == null) {
            return resourceNotFound(id);
        }

        R resource = parseResourceTypePayload(is);

        if (!existing.getId().equals(resource.getId())) {
            return invalidSyntax("Invalid reference to resource");
        }

        return onPersist(resource, Status.OK,
                (rScimResourceTypeProvider, r) -> {
                    R updated = resourceTypeProvider.update(r);
                    adminEvent.operation(OperationType.UPDATE)
                            .resourcePath(session.getContext().getUri())
                            .representation(updated)
                            .success();
                    return updated;
                });
    }

    /** SCIM PATCH：按 PatchOp 操作局部更新资源。 */
    @Path("{id}")
    @PATCH
    @Consumes({APPLICATION_SCIM_JSON, MediaType.APPLICATION_JSON})
    @Produces(APPLICATION_SCIM_JSON)
    public Response patch(@PathParam("id") String id, PatchRequest request) {
        logger.debugf("SCIM PATCH %s id=%s", resourceTypeProvider.getName(), id);
        R existing = getResource(id);

        if (existing == null) {
            return resourceNotFound(id);
        }

        if (!request.getSchemas().contains(Scim.PATCH_OP_CORE_SCHEMA)) {
            return invalidSyntax("No PATCH op schema provided in request");
        }

        return onPersist(existing, Status.OK, (rScimResourceTypeProvider, r) -> {
            resourceTypeProvider.patch(existing, request.getOperations());
            R patched = getResource(id);
            adminEvent.operation(OperationType.UPDATE)
                    .resourcePath(session.getContext().getUri())
                    .representation(patched)
                    .success();
            return patched;
        });
    }

    @SuppressWarnings("unchecked")
    /** 反序列化请求体；未知属性或解析失败转为 400。 */
    private R parseResourceTypePayload(InputStream is) {
        try {
            return  (R) JsonSerialization.readValue(is, resourceTypeClazz);
        } catch (UnrecognizedPropertyException upe) {
            String message = "Unrecognized attribute: " + upe.getPropertyName();
            throw new BadRequestException(invalidSyntax(message));
        } catch (Exception e) {
            throw new BadRequestException(badRequest("Unknown error parsing the request"));
        }
    }

    /** 填充 meta（resourceType、created、lastModified、location）。 */
    private void setMetadata(R resource) {
        Meta meta = new Meta();
        meta.setResourceType(resourceTypeProvider.getName());
        Long createdTimestamp = resource.getCreatedTimestamp();
        Long lastModifiedTimestamp = resource.getLastModifiedTimestamp();
        if (createdTimestamp != null) {
            meta.setCreated(Instant.ofEpochMilli(createdTimestamp).toString());
        }
        if (lastModifiedTimestamp != null) {
            meta.setLastModified(Instant.ofEpochMilli(lastModifiedTimestamp).toString());
        }
        UriBuilder location = session.getContext().getUri().getAbsolutePathBuilder();
        if (resource.getId() != null) {
            String path = session.getContext().getUri().getAbsolutePath().getPath();
            if (!path.endsWith("/" + resource.getId())) {
                location.path(resource.getId());
            }
        }
        meta.setLocation(location.build().toString());
        resource.setMeta(meta);
    }

    /** 执行持久化回调，成功时设置 meta 并返回指定 HTTP 状态。 */
    private Response onPersist(R resource, Status status, BiFunction<ScimResourceTypeProvider<R>, R, R> consumer) {
        try {
            R r = consumer.apply(resourceTypeProvider, resource);

            setMetadata(r);

            return Response.status(status).entity(r).build();
        } catch (Exception e) {
            return toResponse(session, e);
        }
    }

    /** 按 id 获取资源（无属性过滤）。 */
    private R getResource(String id) {
        return getResource(id, null, null);
    }

    /** 按 id 获取资源，支持属性包含/排除列表。 */
    private R getResource(String id, List<String> attributes, List<String> excludedAttributes) {
        if (id == null) {
            return null;
        }

        try {
            return resourceTypeProvider.get(id, attributes, excludedAttributes);
        } catch (ForbiddenException fe) {
            throw new jakarta.ws.rs.ForbiddenException(forbidden());
        }
    }
}
