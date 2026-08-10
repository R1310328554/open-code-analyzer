package org.keycloak.testframework.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import org.keycloak.testframework.FatalTestClassException;
import org.keycloak.testframework.annotations.InjectDependency;
import org.keycloak.testframework.injection.predicates.DependencyPredicates;
import org.keycloak.testframework.injection.predicates.InstanceContextPredicates;

/**
 * {@link Supplier} 实现常用的反射与依赖注入辅助方法。
 * <p>
 * 支持无参构造实例化、注解属性读取及带 {@link org.keycloak.testframework.annotations.InjectDependency} 字段的配置对象装配。
 */
public class SupplierHelpers {

    /**
     * 实例化配置类并将 {@link InjectDependency} 字段注入已部署依赖值。
     *
     * @param clazz 配置类
     * @param instanceContext 当前供应器实例上下文
     * @return 字段已注入的配置实例
     */
    public static <T> T getInstanceWithInjectedFields(Class<T> clazz, InstanceContext<?, ?> instanceContext) {
        T configInstance = getInstance(clazz);

        List<Field> fields = ReflectionUtils.listFields(configInstance.getClass()).stream().filter(f -> f.getAnnotation(InjectDependency.class) != null).toList();
        if (!fields.isEmpty()) {
            List<InstanceContext<?, ?>> deployedInstances = instanceContext.getRegistry().getDeployedInstances();
            List<Dependency> dependencies = findAllDependencies(new LinkedList<>(), instanceContext.getDeclaredDependencies(), deployedInstances);

            fields.forEach(f -> {
                Dependency dependency = dependencies.stream().filter(DependencyPredicates.assignableTo(f.getType())).findFirst().orElseThrow(injectedDependencyNotFound(f, instanceContext.getSupplier()));
                InstanceContext<?, ?> instance = deployedInstances.stream()
                        .filter(InstanceContextPredicates.matches(f.getType(), dependency.ref()))
                        .findFirst()
                        .orElseThrow(dependencyNotFound(dependency));
                ReflectionUtils.setField(f, configInstance, instance.getValue());
            });
        }
        return configInstance;
    }

    /**
     * 通过无参构造创建实例（构造器设为可访问）。
     *
     * @param clazz 目标类
     * @return 新实例
     */
    public static <T> T getInstance(Class<T> clazz) {
        try {
            Constructor<T> declaredConstructor = clazz.getDeclaredConstructor();
            declaredConstructor.setAccessible(true);
            return declaredConstructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    /**
     * 按类名加载并实例化。
     *
     * @param clazzName 全限定类名
     * @return 新实例
     */
    public static <T> T getInstance(String clazzName) {
        try {
            Class<T> clazz = (Class<T>) SupplierHelpers.class.getClassLoader().loadClass(clazzName);
            return getInstance(clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** 读取注解属性，缺失时返回默认值。 */
    public static <T> T getAnnotationField(Annotation annotation, String name, T defaultValue) {
        T value = getAnnotationField(annotation, name);
        return value != null ? value : defaultValue;
    }

    @SuppressWarnings("unchecked")
    /** 读取注解属性，缺失时返回 {@code null}。 */
    public static <T> T getAnnotationField(Annotation annotation, String name) {
        if (annotation != null) {
            for (Method m : annotation.annotationType().getMethods()) {
                if (m.getName().equals(name)) {
                    try {
                        return (T) m.invoke(annotation);
                    } catch (Exception e){
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return null;
    }

    /** 生成日志/命名用标识：有 ref 时用 ref，否则 {@code default}。 */
    public static String createName(InstanceContext<?, ?> instanceContext) {
        return instanceContext.getRef() != null ? instanceContext.getRef() : "default";
    }

    /** 递归展开传递依赖并去重。 */
    private static List<Dependency> findAllDependencies(List<Dependency> allDependencies, List<Dependency> dependencies, List<InstanceContext<?, ?>> deployedInstances) {
        for (Dependency dependency : dependencies) {
            if (allDependencies.stream().noneMatch(DependencyPredicates.matches(dependency.valueType(), dependency.ref()))) {
                allDependencies.add(dependency);
                InstanceContext<?, ?> instance = deployedInstances.stream().filter(InstanceContextPredicates.matches(dependency.valueType(), dependency.ref())).findFirst().orElseThrow(dependencyNotFound(dependency));
                findAllDependencies(allDependencies, instance.getDeclaredDependencies(), deployedInstances);
            }
        }
        return allDependencies;
    }

    /** 构造依赖未在已部署实例中找到的致命异常供应商。 */
    private static Supplier<FatalTestClassException> dependencyNotFound(Dependency dependency) {
        return () -> new FatalTestClassException("Unexpected error in registry; requested dependency " + dependency.valueType().getName() + " not found in deployed instances");
    }

    /** 构造配置类字段注入依赖在依赖树中缺失的致命异常供应商。 */
    private static Supplier<FatalTestClassException> injectedDependencyNotFound(Field field, org.keycloak.testframework.injection.Supplier<?, ?> supplier) {
        return () -> new FatalTestClassException(field.getDeclaringClass().getName() + " requested injection of " + field.getType().getSimpleName() + " not found in dependency tree for supplier " + supplier.getClass().getName());
    }

}
