package org.keycloak.testframework.realm;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.keycloak.representations.idm.ClientPolicyExecutorConfigurationRepresentation;
import org.keycloak.representations.idm.ClientPolicyExecutorRepresentation;
import org.keycloak.representations.idm.ClientProfileRepresentation;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * {@link ClientProfileRepresentation} 的流式构建器，用于在测试中定义客户端配置档及执行器。
 *
 * @author rmartinc
 */
public class ClientProfileBuilder extends Builder<ClientProfileRepresentation> {

    /** 基于已有表示对象构造构建器。 */
    private ClientProfileBuilder(ClientProfileRepresentation rep) {
        super(rep);
    }

    /** 创建空的客户端配置档构建器。 */
    public static ClientProfileBuilder create() {
        return new ClientProfileBuilder(new ClientProfileRepresentation());
    }

    /** 基于已有配置档表示对象创建更新用构建器。 */
    public static ClientProfileBuilder update(ClientProfileRepresentation rep) {
        return new ClientProfileBuilder(rep);
    }

    /** 设置配置档名称。 */
    public ClientProfileBuilder name(String name) {
        rep.setName(name);
        return this;
    }

    /** 设置配置档描述。 */
    public ClientProfileBuilder description(String description) {
        rep.setDescription(description);
        return this;
    }

    /**
     * 追加策略执行器及其配置。
     *
     * @param providerId 执行器提供者 ID
     * @param config 执行器配置
     */
    public ClientProfileBuilder executor(String providerId, ClientPolicyExecutorConfigurationRepresentation config) {
        ClientPolicyExecutorRepresentation executor = new ClientPolicyExecutorRepresentation();
        executor.setExecutorProviderId(providerId);
        if (config == null) {
            config = new ClientPolicyExecutorConfigurationRepresentation();
        }
        try {
            executor.setConfiguration(JsonSerialization.mapper.readValue(JsonSerialization.mapper.writeValueAsBytes(config), JsonNode.class));
        } catch(IOException e) {
            throw new IllegalArgumentException("Invalid configuration", e);
        }
        List<ClientPolicyExecutorRepresentation> executors = rep.getExecutors();
        if (executors == null) {
            executors = new LinkedList<>();
            rep.setExecutors(executors);
        }
        executors.add(executor);
        return this;
    }

}
