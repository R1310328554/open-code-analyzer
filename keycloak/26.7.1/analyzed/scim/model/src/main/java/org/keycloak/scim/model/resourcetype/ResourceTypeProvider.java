package org.keycloak.scim.model.resourcetype;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.Model;
import org.keycloak.scim.protocol.ForbiddenException;
import org.keycloak.scim.protocol.request.SearchRequest;
import org.keycloak.scim.resource.ResourceTypeRepresentation;
import org.keycloak.scim.resource.config.ServiceProviderConfig;
import org.keycloak.scim.resource.resourcetype.ResourceType;
import org.keycloak.scim.resource.resourcetype.ResourceType.SchemaExtension;
import org.keycloak.scim.resource.schema.ModelSchema;
import org.keycloak.scim.resource.schema.Schema;
import org.keycloak.scim.resource.spi.ScimResourceTypeProvider;
import org.keycloak.scim.resource.spi.ScimResourceTypeProviderFactory;

import static org.keycloak.scim.resource.Scim.hasDiscoveryEndpointPermission;

/**
 * SCIM ResourceTypes Discovery 提供者。
 * <p>聚合已注册 {@link ScimResourceTypeProviderFactory}，生成各资源类型的元数据表示。</p>
 */
public class ResourceTypeProvider implements ScimResourceTypeProvider<ResourceType> {

    /** Discovery 列表中排除的元资源类型。 */
    private static final List<Class<? extends ResourceTypeRepresentation>> EXCLUDED_RESOURCE_TYPES = List.of(ServiceProviderConfig.class, ResourceType.class, Schema.class);
    /** 当前 Keycloak 会话。 */
    private final KeycloakSession session;

    public ResourceTypeProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void close() {
    }

    @Override
    public Class<ResourceType> getResourceType() {
        return ResourceType.class;
    }

    @Override
    public ResourceType create(ResourceType resource) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ResourceType update(ResourceType resourceType) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ResourceType get(String id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** 列出所有可发现的 SCIM 资源类型（需 Discovery 权限）。 */
    @Override
    public Stream<ResourceType> getAll(SearchRequest searchRequest) {
        if (hasDiscoveryEndpointPermission(session)) {
            return session.getKeycloakSessionFactory().getProviderFactoriesStream(ScimResourceTypeProvider.class)
                    .map(ScimResourceTypeProviderFactory.class::cast)
                    .map(this::toRepresentation)
                    .filter(Objects::nonNull);
        }
        throw new ForbiddenException();
    }

    @Override
    public Long count(SearchRequest searchRequest) {
        return getAll(searchRequest).count();
    }

    /** 将工厂转换为 {@link ResourceType} Discovery 表示。 */
    private ResourceType toRepresentation(ScimResourceTypeProviderFactory<? extends ScimResourceTypeProvider<? extends ResourceTypeRepresentation>> factory) {
        ScimResourceTypeProvider<? extends ResourceTypeRepresentation> provider = factory.create(session);

        if (EXCLUDED_RESOURCE_TYPES.contains(provider.getResourceType())) {
            return null;
        }

        ResourceType representation = new ResourceType();
        ResourceTypeRepresentation resourceType;

        try {
            resourceType = provider.getResourceType().getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Could not instantiate resource type representation for provider " + factory.getId(), e);
        }

        representation.setId(provider.getName());
        representation.setName(provider.getName());
        representation.setDescription(provider.getDescription());
        representation.setEndpoint("/" + factory.getId());
        representation.setSchema(provider.getSchema());

        List<SchemaExtension> schemaExtensions = new ArrayList<>();

        for (String name : provider.getSchemaExtensions()) {
            SchemaExtension extension = new SchemaExtension();
            extension.setSchema(name);
            schemaExtensions.add(extension);
        }

        representation.setSchemaExtensions(schemaExtensions);

        return representation;
    }

    @Override
    public boolean delete(String id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getSchema() {
        return ResourceType.SCHEMA;
    }

    @Override
    public <M extends Model> List<ModelSchema<M, ResourceType>> getSchemas() {
        return List.of();
    }
}
