package org.keycloak.testframework.injection;

import java.util.LinkedList;
import java.util.List;

/**
 * 供应器 {@link Supplier#getDependencies} 返回的必需依赖构建器。
 * <p>
 * 支持链式添加 {@link RequiredDependency}，或使用 {@link #none()} 表示无额外依赖。
 */
public class RequiredDependencies {

    /** 空依赖单例。 */
    private static final RequiredDependencies NONE = new RequiredDependencies();

    /** 已添加的依赖条目列表。 */
    private List<RequiredDependency> dependencies;

    /** @return 不含任何依赖的构建器 */
    public static RequiredDependencies none() {
        return NONE;
    }

    /** 创建含单个无 ref 依赖的构建器。 */
    public static RequiredDependencies create(Class<?> valueType) {
        return new RequiredDependencies().add(valueType);
    }

    /** 创建含单个指定 ref 依赖的构建器。 */
    public static RequiredDependencies create(Class<?> valueType, String ref) {
        return new RequiredDependencies().add(valueType, ref);
    }

    /** 追加无 ref 依赖并返回 {@code this}。 */
    public RequiredDependencies add(Class<?> valueType) {
        dependencies.add(new RequiredDependency(valueType, null));
        return this;
    }

    /** 追加指定 ref 依赖并返回 {@code this}。 */
    public RequiredDependencies add(Class<?> valueType, String ref) {
        dependencies.add(new RequiredDependency(valueType, ref));
        return this;
    }

    /** 构造空的依赖构建器。 */
    public RequiredDependencies() {
        this.dependencies = new LinkedList<>();
    }

    /** @return 内部依赖列表（包内可见） */
    List<RequiredDependency> getList() {
        return dependencies;
    }

    /**
     * 单条必需依赖描述。
     *
     * @param valueType 依赖值类型
     * @param ref 实例引用标识
     */
    public record RequiredDependency(Class<?> valueType, String ref) {
    }

}
