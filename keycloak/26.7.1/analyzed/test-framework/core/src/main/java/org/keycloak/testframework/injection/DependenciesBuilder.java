package org.keycloak.testframework.injection;

import java.util.LinkedList;
import java.util.List;

/**
 * {@link Dependency} 列表的流式构建器。
 * <p>
 * 供 {@link Supplier#getDependencies} 声明对其他托管实例的依赖。
 */
public class DependenciesBuilder {

    /**
     * 创建仅含单一类型依赖的构建器。
     *
     * @param valueType 依赖值类型
     * @return 新构建器
     */
    public static DependenciesBuilder create(Class<?> valueType) {
        return new DependenciesBuilder().add(valueType);
    }

    /**
     * 创建含指定 ref 的单一依赖构建器。
     *
     * @param valueType 依赖值类型
     * @param ref 实例引用标识
     * @return 新构建器
     */
    public static DependenciesBuilder create(Class<?> valueType, String ref) {
        return new DependenciesBuilder().add(valueType, ref);
    }

    /** 累积的依赖列表。 */
    private final List<Dependency> dependencies;

    /**
     * 追加无 ref 的依赖。
     *
     * @param valueType 依赖值类型
     * @return 当前构建器
     */
    public DependenciesBuilder add(Class<?> valueType) {
        dependencies.add(new Dependency(valueType, null));
        return this;
    }

    /**
     * 追加带 ref 的依赖。
     *
     * @param valueType 依赖值类型
     * @param ref 实例引用标识
     * @return 当前构建器
     */
    public DependenciesBuilder add(Class<?> valueType, String ref) {
        dependencies.add(new Dependency(valueType, ref));
        return this;
    }

    /** 创建空的依赖构建器。 */
    public DependenciesBuilder() {
        this.dependencies = new LinkedList<>();
    }

    /** 返回不可变语义下的依赖列表快照。 */
    public List<Dependency> build() {
        return dependencies;
    }

}
