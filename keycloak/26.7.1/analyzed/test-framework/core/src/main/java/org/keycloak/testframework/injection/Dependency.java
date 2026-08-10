package org.keycloak.testframework.injection;

/**
 * 描述对某一托管实例类型的依赖（可选 {@code ref} 区分多实例）。
 *
 * @param valueType 依赖的值类型
 * @param ref 实例引用标识，空字符串会在 compact 构造中转为 {@code null}
 */
public record Dependency(Class<?> valueType, String ref) {

    /** 规范化 ref：空字符串转为 {@code null}。 */
    public Dependency {
        ref = StringUtil.convertEmptyToNull(ref);
    }

    /** 返回 {@code SimpleName:ref} 形式的调试字符串。 */
    @Override
    public String toString() {
        return valueType.getSimpleName() + ":" + ref;
    }
}
