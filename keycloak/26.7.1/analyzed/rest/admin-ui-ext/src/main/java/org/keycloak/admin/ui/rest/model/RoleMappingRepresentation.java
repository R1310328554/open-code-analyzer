package org.keycloak.admin.ui.rest.model;

import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 角色映射的 REST 表示，汇总用户在领域级与客户端级的角色分配。
 * <p>
 * 用于管理 UI 展示或编辑用户的有效角色映射，包含领域角色列表及按客户端 ID 分组的客户端角色。
 */
public final class RoleMappingRepresentation {
    /** 领域级角色映射列表。 */
    @Schema(description = "Realm role mappings")
    private List<RoleRepresentation> realmMappings;

    /** 按客户端 ID 索引的客户端角色映射。 */
    @Schema(description = "Client role mappings keyed by client ID")
    private Map<String, ClientMappingRepresentation> clientMappings;

    public RoleMappingRepresentation() {
    }

    public RoleMappingRepresentation(List<RoleRepresentation> realmMappings, Map<String, ClientMappingRepresentation> clientMappings) {
        this.realmMappings = realmMappings;
        this.clientMappings = clientMappings;
    }

    public List<RoleRepresentation> getRealmMappings() {
        return realmMappings;
    }

    public void setRealmMappings(List<RoleRepresentation> realmMappings) {
        this.realmMappings = realmMappings;
    }

    public Map<String, ClientMappingRepresentation> getClientMappings() {
        return clientMappings;
    }

    public void setClientMappings(Map<String, ClientMappingRepresentation> clientMappings) {
        this.clientMappings = clientMappings;
    }

    /**
     * 单个角色的简要表示，可用于领域角色或客户端角色。
     */
    public static class RoleRepresentation {
        /** 角色内部 ID。 */
        private String id;
        /** 角色名称。 */
        private String name;
        /** 角色描述。 */
        private String description;
        /** 是否为复合角色（包含子角色）。 */
        private boolean composite;
        /** 是否为客户端角色（false 表示领域角色）。 */
        private boolean clientRole;
        /** 角色所属容器 ID（领域或客户端的内部 ID）。 */
        private String containerId;

        public RoleRepresentation() {
        }

        public RoleRepresentation(String id, String name, String description, boolean composite, boolean clientRole, String containerId) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.composite = composite;
            this.clientRole = clientRole;
            this.containerId = containerId;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public boolean isComposite() {
            return composite;
        }

        public void setComposite(boolean composite) {
            this.composite = composite;
        }

        public boolean isClientRole() {
            return clientRole;
        }

        public void setClientRole(boolean clientRole) {
            this.clientRole = clientRole;
        }

        public String getContainerId() {
            return containerId;
        }

        public void setContainerId(String containerId) {
            this.containerId = containerId;
        }
    }

    /**
     * 某个客户端下的角色映射集合。
     */
    public static class ClientMappingRepresentation {
        /** 客户端内部 ID。 */
        private String id;
        /** 客户端可读 clientId。 */
        private String client;
        /** 该客户端下分配的角色列表。 */
        private List<RoleRepresentation> mappings;

        public ClientMappingRepresentation() {
        }

        public ClientMappingRepresentation(String id, String client, List<RoleRepresentation> mappings) {
            this.id = id;
            this.client = client;
            this.mappings = mappings;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getClient() {
            return client;
        }

        public void setClient(String client) {
            this.client = client;
        }

        public List<RoleRepresentation> getMappings() {
            return mappings;
        }

        public void setMappings(List<RoleRepresentation> mappings) {
            this.mappings = mappings;
        }
    }
}
