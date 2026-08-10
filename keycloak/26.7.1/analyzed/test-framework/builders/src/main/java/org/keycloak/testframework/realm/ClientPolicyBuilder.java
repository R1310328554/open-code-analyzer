package org.keycloak.testframework.realm;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.keycloak.representations.idm.ClientPolicyConditionConfigurationRepresentation;
import org.keycloak.representations.idm.ClientPolicyConditionRepresentation;
import org.keycloak.representations.idm.ClientPolicyRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyMode;
import org.keycloak.services.clientpolicy.condition.ClientScopesCondition;
import org.keycloak.services.clientpolicy.condition.GrantTypeCondition;
import org.keycloak.services.clientpolicy.condition.IdentityProviderCondition;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * {@link ClientPolicyRepresentation} 的流式构建器，用于在测试中组装客户端策略（条件、关联配置档等）。
 *
 * @author rmartinc
 */
public class ClientPolicyBuilder extends Builder<ClientPolicyRepresentation> {

    /** 基于已有表示对象构造构建器。 */
    private ClientPolicyBuilder(ClientPolicyRepresentation rep) {
        super(rep);
    }

    /** 创建默认启用的客户端策略构建器。 */
    public static ClientPolicyBuilder create() {
        return new ClientPolicyBuilder(new ClientPolicyRepresentation()).enabled(true);
    }

    /** 基于已有策略表示对象创建更新用构建器。 */
    public static ClientPolicyBuilder update(ClientPolicyRepresentation rep) {
        return new ClientPolicyBuilder(rep);
    }

    /**
     * 构建授权类型条件配置。
     *
     * @param negativeLogic 是否取反逻辑
     * @param types 授权类型列表
     */
    public static GrantTypeCondition.Configuration grantTypeConditionConfiguration(boolean negativeLogic, String... types) {
        GrantTypeCondition.Configuration config = new GrantTypeCondition.Configuration();
        config.setNegativeLogic(negativeLogic);
        if (types != null && types.length > 0) {
            config.setGrantTypes(List.of(types));
        }
        return config;
    }

    /**
     * 构建身份提供者条件配置。
     *
     * @param negativeLogic 是否取反逻辑
     * @param aliases 身份提供者别名
     */
    public static IdentityProviderCondition.Configuration identityProviderConditionConfiguration(boolean negativeLogic, String... aliases) {
        IdentityProviderCondition.Configuration config = new IdentityProviderCondition.Configuration();
        config.setNegativeLogic(negativeLogic);
        if (aliases != null && aliases.length > 0) {
            config.setIdentityProviderAliases(List.of(aliases));
        }
        return config;
    }

    /**
     * 构建客户端作用域条件配置。
     *
     * @param negativeLogic 是否取反逻辑
     * @param type 作用域类型
     * @param scopes 作用域名称
     */
    public static ClientScopesCondition.Configuration clientScopesConditionConfiguration(boolean negativeLogic, String type, String... scopes) {
        ClientScopesCondition.Configuration config = new ClientScopesCondition.Configuration();
        config.setNegativeLogic(negativeLogic);
        config.setType(type);
        config.setScopes(List.of(scopes));
        return config;
    }

    /** 设置策略是否启用。 */
    public ClientPolicyBuilder enabled(boolean enabled) {
        rep.setEnabled(enabled);
        return this;
    }

    /** 设置策略名称。 */
    public ClientPolicyBuilder name(String name) {
        rep.setName(name);
        return this;
    }

    /** 设置策略描述。 */
    public ClientPolicyBuilder description(String description) {
        rep.setDescription(description);
        return this;
    }

    /** 设置策略运行模式（如 ENFORCE、PERMISSIVE）。 */
    public ClientPolicyBuilder mode(ClientPolicyMode mode) {
        rep.setMode(mode.toString());
        return this;
    }

    /** 追加一条策略条件及其提供者配置。 */
    public ClientPolicyBuilder condition(String providerId, ClientPolicyConditionConfigurationRepresentation config) {
        ClientPolicyConditionRepresentation condition = new ClientPolicyConditionRepresentation();
        condition.setConditionProviderId(providerId);
        if (config == null) {
            config = new ClientPolicyConditionConfigurationRepresentation();
        }
        try {
            condition.setConfiguration(JsonSerialization.mapper.readValue(JsonSerialization.mapper.writeValueAsBytes(config), JsonNode.class));
        } catch(IOException e) {
            throw new IllegalArgumentException("Invalid configuration", e);
        }
        List<ClientPolicyConditionRepresentation> conditions = rep.getConditions();
        if (conditions == null) {
            conditions = new LinkedList<>();
            rep.setConditions(conditions);
        }
        conditions.add(condition);
        return this;
    }

    /** 关联一个或多个客户端配置档名称。 */
    public ClientPolicyBuilder profile(String... profile) {
        List<String> profiles = rep.getProfiles();
        if (profiles == null) {
            profiles = new LinkedList<>();
            rep.setProfiles(profiles);
        }
        profiles.addAll(List.of(profile));
        return this;
    }

}
