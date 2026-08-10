package org.keycloak.scim.protocol.response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.keycloak.scim.resource.ResourceTypeRepresentation;
import org.keycloak.scim.resource.group.Group;
import org.keycloak.scim.resource.resourcetype.ResourceType;
import org.keycloak.scim.resource.schema.Schema;
import org.keycloak.scim.resource.user.User;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.keycloak.scim.resource.Scim.getCoreSchema;

/**
 * {@link ListResponse} 中 {@code Resources} 数组的自定义 Jackson 反序列化器。
 * <p>根据每条资源的 {@code schemas} 字段自动映射到对应的 {@link ResourceTypeRepresentation} 子类。</p>
 */
public class ListResponseDeserializer extends JsonDeserializer<List<ResourceTypeRepresentation>> {

    /** 将 JSON 数组反序列化为资源类型表示列表。 */
    @Override
    public List<ResourceTypeRepresentation> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        ObjectMapper mapper = (ObjectMapper) parser.getCodec();
        JsonNode nodes = mapper.readTree(parser);
        List<ResourceTypeRepresentation> resources = new ArrayList<>();

        if (nodes.isArray()) {
            for (JsonNode node : nodes) {
                ResourceTypeRepresentation resource = parseNode(mapper, node);

                if (resource != null) {
                    resources.add(resource);
                }
            }
        }

        return resources;
    }

    /** 根据 Schema 信息将单个 JSON 节点反序列化为具体资源类型。 */
    private ResourceTypeRepresentation parseNode(ObjectMapper mapper, JsonNode node) throws IOException {
        Class<? extends ResourceTypeRepresentation> resourceType = getResourceType(node);

        return mapper.treeToValue(node, resourceType);
    }

    /** 根据 {@code schemas} 字段推断资源的具体 Java 类型。 */
    private Class<? extends ResourceTypeRepresentation> getResourceType(JsonNode node) {
        Set<String> schemas = getSchemas(node);

        if (schemas.contains(getCoreSchema(User.class))) {
            return User.class;
        } else if (schemas.contains(getCoreSchema(Group.class))) {
            return Group.class;
        } else if (schemas.contains(getCoreSchema(ResourceType.class))) {
            return  ResourceType.class;
        } else if (schemas.contains(getCoreSchema(Schema.class))) {
            return Schema.class;
        }

        throw new IllegalArgumentException("Could not map resource type from any of the schemas: " + schemas);
    }

    /** 从 JSON 节点提取 {@code schemas} 字段为字符串集合。 */
    private Set<String> getSchemas(JsonNode node) {
        if (node.has("schemas")) {
            JsonNode schemasNode = node.get("schemas");

            if (schemasNode.isArray()) {
                return schemasNode.valueStream().map(JsonNode::asText).collect(Collectors.toSet());
            }

            return Set.of(schemasNode.asText());
        }

        throw new IllegalArgumentException("No schema set to JSON node");
    }
}
