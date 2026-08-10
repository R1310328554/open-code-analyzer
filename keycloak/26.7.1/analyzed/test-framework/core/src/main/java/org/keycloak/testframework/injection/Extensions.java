package org.keycloak.testframework.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import org.keycloak.testframework.TestFrameworkExecutor;
import org.keycloak.testframework.TestFrameworkExtension;
import org.keycloak.testframework.config.Config;

import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * 测试框架扩展与 {@link Supplier} 的全局注册中心（单例）。
 * <p>
 * 通过 {@link java.util.ServiceLoader} 加载 {@link org.keycloak.testframework.TestFrameworkExtension}，
 * 聚合值类型别名、供应器列表及始终启用的值类型，并依据 {@link org.keycloak.testframework.config.Config} 过滤供应器。
 */
public class Extensions {

    /** 供应器加载阶段的日志记录器。 */
    private final RegistryLogger logger;
    /** 值类型到配置别名的映射表。 */
    private final ValueTypeAlias valueTypeAlias;
    /** 当前已加载且通过配置筛选的供应器列表。 */
    private final List<Supplier<?, ?>> suppliers;
    /** 无需注解即可自动请求的值类型列表。 */
    private final List<Class<?>> alwaysEnabledValueTypes;

    /** 单例实例。 */
    private static Extensions INSTANCE;
    /** 已加载的测试框架扩展。 */
    private final List<TestFrameworkExtension> extensions;

    /** @return 全局 {@link Extensions} 单例 */
    public static Extensions getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Extensions();
        }
        return INSTANCE;
    }

    /** 重置单例，供测试套件结束或重新初始化时调用。 */
    public static void reset() {
        INSTANCE = null;
    }

    /** 私有构造：加载扩展、别名、供应器并注册到 {@link Config}。 */
    private Extensions() {
        extensions = loadExtensions();
        valueTypeAlias = loadValueTypeAlias(extensions);
        Config.registerValueTypeAlias(valueTypeAlias);
        logger = new RegistryLogger(valueTypeAlias);
        suppliers = loadSuppliers(extensions);
        alwaysEnabledValueTypes = loadAlwaysEnabledValueTypes(extensions);
    }

    /** @return 值类型别名映射 */
    public ValueTypeAlias getValueTypeAlias() {
        return valueTypeAlias;
    }

    /** @return 已加载的供应器列表 */
    public List<Supplier<?, ?>> getSuppliers() {
        return suppliers;
    }

    /** @return 始终自动启用的值类型列表 */
    public List<Class<?>> getAlwaysEnabledValueTypes() {
        return alwaysEnabledValueTypes;
    }

    /** @return 已加载扩展中实现 {@link TestFrameworkExecutor} 的实例列表 */
    public List<TestFrameworkExecutor> getTestFrameworkExecutors() {
        return extensions.stream()
                .filter(e -> e instanceof TestFrameworkExecutor)
                .map(e -> (TestFrameworkExecutor) e)
                .toList();
    }

    /**
     * 汇总所有执行器对指定测试方法声明的方法级值类型。
     *
     * @param method 测试方法
     * @return 值类型 Class 列表
     */
    public List<Class<?>> getMethodValueTypes(Method method) {
        return getTestFrameworkExecutors()
                .stream()
                .flatMap(e -> e.getMethodValueTypes(method).stream()).toList();
    }

    @SuppressWarnings("unchecked")
    /**
     * 按值类型查找第一个匹配的供应器。
     *
     * @param typeClass 值类型
     * @return 匹配的供应器，未找到时返回 {@code null}
     */
    public <T> Supplier<T, ?> findSupplierByType(Class<T> typeClass) {
        return (Supplier<T, ?>) suppliers.stream().filter(s -> s.getValueType().equals(typeClass)).findFirst().orElse(null);
    }

    @SuppressWarnings("unchecked")
    /**
     * 按注入注解类型查找第一个匹配的供应器。
     *
     * @param annotation 注入注解实例
     * @return 匹配的供应器，未找到时返回 {@code null}
     */
    public <T> Supplier<T, ?> findSupplierByAnnotation(Annotation annotation) {
        return (Supplier<T, ?>) suppliers.stream().filter(s -> s.getAnnotationClass().equals(annotation.annotationType())).findFirst().orElse(null);
    }

    /** 通过 {@link ServiceLoader} 加载所有 {@link TestFrameworkExtension}。 */
    private List<TestFrameworkExtension> loadExtensions() {
        List<TestFrameworkExtension> extensions = new LinkedList<>();
        ServiceLoader.load(TestFrameworkExtension.class).iterator().forEachRemaining(extensions::add);
        return extensions;
    }

    /** 合并各扩展声明的值类型别名。 */
    private ValueTypeAlias loadValueTypeAlias(List<TestFrameworkExtension> extensions) {
        ValueTypeAlias valueTypeAlias = new ValueTypeAlias();
        extensions.forEach(e -> valueTypeAlias.addAll(e.valueTypeAliases()));
        return valueTypeAlias;
    }

    /**
     * 从扩展收集供应器，按配置 include/exclude 及选中别名过滤，并注入 {@link ConfigProperty} 字段。
     */
    private List<Supplier<?, ?>> loadSuppliers(List<TestFrameworkExtension> extensions) {
        List<Supplier<?, ?>> suppliers = new LinkedList<>();
        List<Supplier<?, ?>> skippedSuppliers = new LinkedList<>();
        Set<Class<?>> loadedValueTypes = new HashSet<>();

        for (TestFrameworkExtension extension : extensions) {
            for (var supplier : extension.suppliers()) {
                Class<?> valueType = supplier.getValueType();
                String requestedSupplier = Config.getSelectedSupplier(valueType);
                if (isSupplierIncluded(supplier) && (supplier.getAlias().equals(requestedSupplier) || (requestedSupplier == null && !loadedValueTypes.contains(valueType)))) {
                    configureSupplier(supplier);
                    suppliers.add(supplier);
                    loadedValueTypes.add(valueType);
                } else {
                    skippedSuppliers.add(supplier);
                }
            }
        }

        logger.logSuppliers(suppliers, skippedSuppliers);

        return suppliers;
    }

    /** 判断供应器是否通过 include/exclude 配置筛选。 */
    private boolean isSupplierIncluded(Supplier<?, ?> supplier) {
        String includedSuppliers = Config.getIncludedSuppliers(supplier.getValueType());
        if (includedSuppliers != null) {
            if (Arrays.stream(includedSuppliers.split(",")).noneMatch(s -> s.equals(supplier.getAlias()))) {
                return false;
            }
        }

        String excludedSuppliers = Config.getExcludedSuppliers(supplier.getValueType());
        if (excludedSuppliers != null) {
            return Arrays.stream(excludedSuppliers.split(",")).noneMatch(s -> s.equals(supplier.getAlias()));
        }

        return true;
    }

    /** 合并各扩展声明的始终启用值类型。 */
    private List<Class<?>> loadAlwaysEnabledValueTypes(List<TestFrameworkExtension> extensions) {
        return extensions.stream().flatMap(s -> s.alwaysEnabledValueTypes().stream()).toList();
    }

    /** 将 {@link ConfigProperty} 标注的配置值反射注入到供应器字段。 */
    private void configureSupplier(Supplier<?, ?> supplier) {
        for (Field f : ReflectionUtils.listFields(supplier.getClass())) {
            ConfigProperty annotation = f.getAnnotation(ConfigProperty.class);
            if (annotation != null) {
                Object configValue = Config.getValueTypeConfig(supplier.getValueType(), annotation.name(), annotation.defaultValue(), f.getType());
                ReflectionUtils.setField(f, supplier, configValue);
            }
        }

    }

}
