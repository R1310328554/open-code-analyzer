package org.keycloak.testframework.realm;

import java.util.List;
import java.util.Map;

import org.keycloak.representations.idm.GroupRepresentation;

/**
 * {@link GroupRepresentation} 的流式构建器，用于在测试中定义领域组、角色映射与子组层次。
 */
public class GroupBuilder extends Builder<GroupRepresentation> {

    /** 基于已有组表示对象构造构建器。 */
    private GroupBuilder(GroupRepresentation rep) {
        super(rep);
    }

    /** 创建空的组构建器。 */
    public static GroupBuilder create() {
        return new GroupBuilder(new GroupRepresentation());
    }

    /** 创建并命名组。 */
    public static GroupBuilder create(String name) {
        return create().name(name);
    }

    /** 基于已有组表示对象创建更新用构建器。 */
    public static GroupBuilder update(GroupRepresentation rep) {
        return new GroupBuilder(rep);
    }

    /** 设置组名称。 */
    public GroupBuilder name(String name) {
        rep.setName(name);
        return this;
    }

    /** 设置组在领域中的路径。 */
    public GroupBuilder path(String path) {
        rep.setPath(path);
        return this;
    }

    /** 追加领域级角色映射。 */
    public GroupBuilder realmRoles(String... realmRoles) {
        rep.setRealmRoles(combine(rep.getRealmRoles(), realmRoles));
        return this;
    }

    /** 为指定客户端追加客户端角色映射。 */
    public GroupBuilder clientRoles(String client, String... clientRoles) {
        rep.setClientRoles(combine(rep.getClientRoles(), client, clientRoles));
        return this;
    }

    /** 添加组属性（多值）。 */
    public GroupBuilder attribute(String key, String... value) {
        rep.setAttributes(combine(rep.getAttributes(), key, value));
        return this;
    }

    /** 直接设置完整属性映射。 */
    public GroupBuilder setAttributes(Map<String, List<String>> attributes) {
        rep.setAttributes(attributes);
        return this;
    }

    /** 追加子组（支持表示对象、构建器或名称）。 */
    public GroupBuilder subGroups(GroupRepresentation... subGroups) {
        rep.setSubGroups(combine(rep.getSubGroups(), subGroups));
        return this;
    }

    /** 追加子组（支持表示对象、构建器或名称）。 */
    public GroupBuilder subGroups(GroupBuilder... subGroups) {
        rep.setSubGroups(combine(rep.getSubGroups(), subGroups));
        return this;
    }

    /** 追加子组（支持表示对象、构建器或名称）。 */
    public GroupBuilder subGroups(String... subGroups) {
        rep.setSubGroups(combine(GroupBuilder::create, rep.getSubGroups(), subGroups));
        return this;
    }

}
