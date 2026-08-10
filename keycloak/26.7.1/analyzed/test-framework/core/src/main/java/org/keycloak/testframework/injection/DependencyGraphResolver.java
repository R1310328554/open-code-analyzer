package org.keycloak.testframework.injection;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.keycloak.testframework.injection.predicates.RequestedInstancePredicates;

import org.jboss.logging.Logger;

/**
 * 解析测试框架中所有 {@link RequestedInstance} 的依赖图。
 * <p>
 * 递归扫描 {@link Supplier#getDependencies}，自动补全缺失的隐式依赖实例，并检测循环依赖。
 */
public class DependencyGraphResolver {

    /** 本类日志记录器。 */
    private static final Logger log = Logger.getLogger(DependencyGraphResolver.class);

    /** 实例注册表。 */
    private final Registry registry;
    /** 扫描过程中自动创建的隐式请求实例。 */
    private final List<RequestedInstance<?, ?>> missingInstances;

    /** 已完成扫描的依赖节点。 */
    private Set<Dependency> visited = new HashSet<>();
    /** 当前递归栈上的依赖节点，用于环检测。 */
    private Set<Dependency> visiting = new HashSet<>();

    /**
     * 从注册表中所有已请求实例出发，构建完整依赖图并收集缺失节点。
     *
     * @param registry 实例注册表
     */
    public DependencyGraphResolver(Registry registry) {
        this.registry = registry;
        this.missingInstances = new LinkedList<>();

        for (RequestedInstance requestedInstance : registry.getRequestedInstances()) {
            List<Dependency> dependencies = requestedInstance.getSupplier().getDependencies(requestedInstance);
            requestedInstance.setDeclaredDependencies(dependencies);
            for (Dependency dependency : dependencies) {
                scan(dependency);
            }
        }
    }

    /** 返回扫描过程中自动创建的隐式依赖请求列表。 */
    public List<RequestedInstance<?, ?>> getMissingInstances() {
        return missingInstances;
    }

    /**
     * 深度优先扫描单个依赖：匹配或创建请求实例，并递归其 transitive 依赖。
     *
     * @param dependency 待扫描依赖
     * @throws RuntimeException 检测到依赖环时抛出
     */
    private void scan(Dependency dependency) {
        if (visited.contains(dependency)) {
            log.tracev("Skipping {0} already scanned", dependency);
        } else {
            log.tracev("Scanning dependency {0}", dependency);
        }

        if (visiting.contains(dependency)) {
            throw new RuntimeException("Dependency cycle detected in " + visiting.stream().map(Dependency::toString).collect(Collectors.joining(", ")));
        }

        visiting.add(dependency);

        RequestedInstance matchingInstance = registry.getRequestedInstances().stream().filter(RequestedInstancePredicates.matches(dependency.valueType(), dependency.ref())).findFirst().orElse(null);
        if (matchingInstance == null) {
            matchingInstance = missingInstances.stream().filter(RequestedInstancePredicates.matches(dependency.valueType(), dependency.ref())).findFirst().orElse(null);
        }

        if (matchingInstance == null) {
            Supplier<?, ?> supplier = registry.getExtensions().findSupplierByType(dependency.valueType());
            Annotation defaultAnnotation = DefaultAnnotationProxy.proxy(supplier.getAnnotationClass(), dependency.ref());
            matchingInstance = registry.createRequestedInstance(new Annotation[]{ defaultAnnotation }, dependency.valueType());
            missingInstances.add(matchingInstance);
        }

        List<Dependency> dependencies = matchingInstance.getSupplier().getDependencies(matchingInstance);
        matchingInstance.setDeclaredDependencies(dependencies);

        dependencies.forEach(this::scan);

        visiting.remove(dependency);
        visited.add(dependency);
    }
}
