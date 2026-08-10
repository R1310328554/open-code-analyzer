package org.keycloak.testframework.realm;

import java.util.List;
import java.util.Map;

import org.keycloak.representations.idm.RoleRepresentation;

/**
 * {@link RoleRepresentation} 的流式构建器，用于在测试中定义领域或客户端角色及复合关系。
 */
public class RoleBuilder extends Builder<RoleRepresentation> {

    /** 基于已有角色表示对象构造构建器。 */
    private RoleBuilder(RoleRepresentation rep) {
        super(rep);
    }

    /** 创建空的角色构建器。 */
    public static RoleBuilder create() {
        return new RoleBuilder(new RoleRepresentation());
    }

    /** 创建并命名角色。 */
    public static RoleBuilder create(String name) {
        return create().name(name);
    }

    /** 基于已有角色表示对象创建更新用构建器。 */
    public static RoleBuilder update(RoleRepresentation rep) {
        return new RoleBuilder(rep);
    }

    /** 设置角色 ID。 */
    public RoleBuilder id(String id) {
        rep.setId(id);
        return this;
    }

    /** 设置角色名称。 */
    public RoleBuilder name(String name) {
        rep.setName(name);
        return this;
    }

    /** 设置角色描述。 */
    public RoleBuilder description(String description) {
        rep.setDescription(description);
        return this;
    }

    /** 添加单值角色属性。 */
    public RoleBuilder attribute(String key, String value) {
        rep.singleAttribute(key, value);
        return this;
    }

    /** 合并角色属性映射。 */
    public RoleBuilder attributes(Map<String, List<String>> attributes) {
        rep.setAttributes(combine(rep.getAttributes(), attributes));
        return this;
    }

    /** 设置是否为复合角色。 */
    public RoleBuilder composite(boolean enabled) {
        rep.setComposite(enabled);
        return this;
    }

    /** 追加领域级复合子角色。 */
    public RoleBuilder realmComposite(String... compositeRole) {
        rep.setComposites(createIfNull(rep.getComposites(), RoleRepresentation.Composites::new));
        rep.getComposites().setRealm(combine(rep.getComposites().getRealm(), compositeRole));
        return this;
    }

    /** 为指定客户端追加复合子角色。 */
    public RoleBuilder clientComposite(String client, String... compositeRole) {
        rep.setComposites(createIfNull(rep.getComposites(), RoleRepresentation.Composites::new));
        rep.getComposites().setClient(combine(rep.getComposites().getClient(), client, compositeRole));
        return this;
    }

}
