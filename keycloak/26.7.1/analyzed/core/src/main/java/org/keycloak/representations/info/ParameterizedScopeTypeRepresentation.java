package org.keycloak.representations.info;

/**
 * 参数化 OAuth Scope 类型的 REST 表示，描述 scope 名称及是否允许多次声明。
 */
public class ParameterizedScopeTypeRepresentation {

    /** Scope 类型名称。 */
    private String name;
    /** 同一请求中是否可重复出现该 scope。 */
    private boolean repeatable;

    /** 默认构造函数，供 JSON 反序列化使用。 */
    public ParameterizedScopeTypeRepresentation() {
    }

    /**
     * 构造参数化 scope 类型描述。
     *
     * @param name       scope 名称
     * @param repeatable 是否可重复
     */
    public ParameterizedScopeTypeRepresentation(String name, boolean repeatable) {
        this.name = name;
        this.repeatable = repeatable;
    }

    /** @return scope 名称 */
    public String getName() {
        return name;
    }

    /** @param name scope 名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** @return 是否可重复声明 */
    public boolean isRepeatable() {
        return repeatable;
    }

    /** @param repeatable 是否可重复声明 */
    public void setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
    }
}
