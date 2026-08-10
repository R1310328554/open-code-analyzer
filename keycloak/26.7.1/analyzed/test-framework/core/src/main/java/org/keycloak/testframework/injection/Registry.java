package org.keycloak.testframework.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.keycloak.testframework.FatalTestClassException;
import org.keycloak.testframework.TestFrameworkExecutor;
import org.keycloak.testframework.annotations.TestCleanup;
import org.keycloak.testframework.annotations.TestSetup;
import org.keycloak.testframework.injection.predicates.DependencyPredicates;
import org.keycloak.testframework.injection.predicates.InstanceContextPredicates;
import org.keycloak.testframework.injection.predicates.RequestedInstancePredicates;
import org.keycloak.testframework.injection.predicates.TestFrameworkExecutorPredicates;
import org.keycloak.testframework.server.KeycloakServer;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

@SuppressWarnings({"rawtypes", "unchecked"})
/**
 * Keycloak 测试框架的核心依赖注入注册表。
 * <p>
 * 在 JUnit 5 扩展生命周期中扫描测试类/方法/字段的注入需求，部署 {@link Supplier} 实例，
 * 解析依赖图、注入字段，并在各生命周期阶段销毁或清理托管资源。
 */
public class Registry implements AutoCloseable {

    /** 注册表操作日志记录器。 */
    private final RegistryLogger logger;

    /** 当前 JUnit 扩展上下文。 */
    private ExtensionContext currentContext;
    /** 全局扩展与供应器注册中心。 */
    private final Extensions extensions;
    /** 已部署的托管实例列表。 */
    private final List<InstanceContext<?, ?>> deployedInstances = new LinkedList<>();
    /** 当前测试方法待部署的请求实例列表。 */
    private final List<RequestedInstance<?, ?>> requestedInstances = new LinkedList<>();
    /** 测试类级致命错误，导致后续方法被跳过。 */
    private FatalTestClassException fatalTestClassException;

    /** 当前测试类实例，用于 {@link org.keycloak.testframework.annotations.TestSetup}/{@link org.keycloak.testframework.annotations.TestCleanup}。 */
    private Object currentTestInstance;

    /** 构造注册表并初始化扩展与日志记录器。 */
    public Registry() {
        extensions = Extensions.getInstance();
        logger = new RegistryLogger(extensions.getValueTypeAlias());
    }

    /** @return 内部日志记录器 */
    RegistryLogger getLogger() {
        return logger;
    }

    /** @return 扩展注册中心 */
    Extensions getExtensions() {
        return extensions;
    }

    /** @return 当前 JUnit 扩展上下文 */
    public ExtensionContext getCurrentContext() {
        return currentContext;
    }

    /** 设置当前 JUnit 扩展上下文。 */
    public void setCurrentContext(ExtensionContext currentContext) {
        this.currentContext = currentContext;
    }

    /**
     * 为已部署实例解析并返回已声明的依赖值。
     *
     * @param typeClass 依赖类型
     * @param ref 实例引用标识
     * @param dependent 请求依赖的实例上下文
     * @return 依赖对象值
     */
    public <T> T getDependency(Class<T> typeClass, String ref, InstanceContext dependent) {
        ref = StringUtil.convertEmptyToNull(ref);

        List<Dependency> declaredDependencies = dependent.getDeclaredDependencies();
        if (declaredDependencies.stream().noneMatch(DependencyPredicates.matches(typeClass, ref))) {
            throw new RuntimeException("Tried to retrieve non-declared dependency " + typeClass.getSimpleName() + ":" + ref);
        }

        T dependency;
        dependency = getDeployedDependency(typeClass, ref, dependent);
        if (dependency != null) {
            return dependency;
        } else {
            dependency = getRequestedDependency(typeClass, ref, dependent);
            if (dependency != null) {
                return dependency;
            }
        }

        throw new RuntimeException("Dependency not found: " + typeClass);
    }

    /** @return 已部署实例列表 */
    public List<InstanceContext<?, ?>> getDeployedInstances() {
        return deployedInstances;
    }

    /** @return 当前待部署请求实例列表 */
    public List<RequestedInstance<?, ?>> getRequestedInstances() {
        return requestedInstances;
    }

    /** 从已部署实例中查找依赖并登记反向依赖关系。 */
    private <T> T getDeployedDependency(Class<T> typeClass, String ref, InstanceContext dependent) {
        InstanceContext dependency = getDeployedInstance(typeClass, ref);
        if (dependency != null) {
            dependency.registerDependent(dependent);

            logger.logDependencyInjection(dependent, dependency, RegistryLogger.InjectionType.EXISTING);

            return (T) dependency.getValue();
        }
        return null;
    }

    /** 将待部署请求实例即时部署并作为依赖返回。 */
    private <T> T getRequestedDependency(Class<T> typeClass, String ref, InstanceContext dependent) {
        RequestedInstance requestedDependency = getRequestedInstance(typeClass, ref);
        if (requestedDependency != null) {
            InstanceContext dependency = new InstanceContext<Object, Annotation>(requestedDependency.getInstanceId(), this, requestedDependency.getSupplier(), requestedDependency.getAnnotation(), requestedDependency.getValueType(), requestedDependency.getDeclaredDependencies());
            dependency.setValue(requestedDependency.getSupplier().getValue(dependency));
            dependency.registerDependent(dependent);
            deployedInstances.add(dependency);

            requestedInstances.remove(requestedDependency);

            logger.logDependencyInjection(dependent, dependency, RegistryLogger.InjectionType.REQUESTED);

            return (T) dependency.getValue();
        }
        return null;
    }

    /**
     * 每个测试方法执行前的核心准备流程：扫描请求、匹配/部署实例、注入字段并运行 {@link TestSetup}。
     *
     * @param testInstance 测试类实例
     * @param testMethod 即将执行的测试方法
     */
    public void beforeEach(Object testInstance, Method testMethod) {
        if (fatalTestClassException != null) {
            skipTestMethod();
        }

        try {
            findRequestedInstances(testInstance, testMethod);
            destroyIncompatibleInstances();
            matchDeployedInstancesWithRequestedInstances();
            deployRequestedInstances();
            invokeBeforeEachOnSuppliers();
            injectFields(testInstance);

            if (currentTestInstance == null || testInstance.getClass() != currentTestInstance.getClass()) {
                executeSetup(testInstance, TestSetup.class);
                currentTestInstance = testInstance;
            }

        } catch (FatalTestClassException e) {
            requestedInstances.clear();
            fatalTestClassException = e;
            skipTestMethod();
        }
    }

    /** 因致命测试类错误而中止当前测试方法。 */
    private void skipTestMethod() {
        Assumptions.abort("Skipping test method due to fatal test class error");
    }

    /**
     * JUnit 调用拦截：若存在匹配的 {@link TestFrameworkExecutor} 则由其执行并跳过原方法。
     */
    public void intercept(InvocationInterceptor.Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext) throws Throwable {
        Class<?> testClass = invocationContext.getTargetClass();
        Method testMethod = invocationContext.getExecutable();

        TestFrameworkExecutor testFrameworkExecutor = getExecutor(testMethod);
        if (testFrameworkExecutor != null) {
            testFrameworkExecutor.execute(this, testClass, testMethod);
            invocation.skip();
        } else {
            invocation.proceed();
        }
    }

    /** 判断测试方法参数是否由测试框架执行器支持注入。 */
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Method testMethod = (Method) parameterContext.getParameter().getDeclaringExecutable();
        Class<?> parameterType = parameterContext.getParameter().getType();
        TestFrameworkExecutor testFrameworkExecutor = getExecutor(testMethod);
        return testFrameworkExecutor != null && testFrameworkExecutor.supportsParameter(testMethod, parameterType);
    }

    /** 扫描始终启用类型、方法级类型、类/字段注解及隐式依赖，构建请求实例列表。 */
    private void findRequestedInstances(Object testInstance, Method testMethod) {
        List<Class<?>> alwaysEnabledValueTypes = extensions.getAlwaysEnabledValueTypes();
        for (Class<?> valueType : alwaysEnabledValueTypes) {
            RequestedInstance requestedInstance = createRequestedInstance(null, valueType);
            if (requestedInstance != null) {
                requestedInstances.add(requestedInstance);
            }
        }

        List<Class<?>> methodValueTypes = extensions.getMethodValueTypes(testMethod);
        for (Class<?> valueType : methodValueTypes) {
            RequestedInstance requestedInstance = createRequestedInstance(null, valueType);
            if (requestedInstance != null) {
                requestedInstances.add(requestedInstance);
            }
        }

        Class testClass = testInstance.getClass();
        RequestedInstance requestedServerInstance = createRequestedInstance(testClass.getAnnotations(), KeycloakServer.class);
        if (requestedServerInstance != null) {
            requestedInstances.add(requestedServerInstance);
        }

        for (Field f : ReflectionUtils.listFields(testClass)) {
            RequestedInstance requestedInstance = createRequestedInstance(f.getAnnotations(), f.getType());
            if (requestedInstance != null) {
                requestedInstances.add(requestedInstance);
            }
        }

        DependencyGraphResolver dependencyGraphResolver = new DependencyGraphResolver(this);
        List<RequestedInstance<?, ?>> missingInstances = dependencyGraphResolver.getMissingInstances();
        requestedInstances.addAll(missingInstances);

        logger.logRequestedInstances(requestedInstances);
    }

    /** 销毁与当前请求不兼容的已部署实例。 */
    private void destroyIncompatibleInstances() {
        for (RequestedInstance<?, ?> requestedInstance : requestedInstances) {
            InstanceContext deployedInstance = getDeployedInstance(requestedInstance);
            if (deployedInstance != null) {
                boolean compatible = requestedInstance.getLifeCycle().equals(deployedInstance.getLifeCycle()) && deployedInstance.getSupplier().compatible(deployedInstance, requestedInstance);
                if (!compatible) {
                    logger.logDestroyIncompatible(deployedInstance);
                    destroy(deployedInstance);
                }
            }
        }
    }

    /** 复用生命周期与配置均兼容的已部署实例，并从请求列表移除。 */
    private void matchDeployedInstancesWithRequestedInstances() {
        Iterator<RequestedInstance<?, ?>> itr = requestedInstances.iterator();
        while (itr.hasNext()) {
            RequestedInstance<?, ?> requestedInstance = itr.next();
            InstanceContext deployedInstance = getDeployedInstance(requestedInstance);
            if (deployedInstance != null) {
                if (requestedInstance.getLifeCycle().equals(deployedInstance.getLifeCycle()) && deployedInstance.getSupplier().compatible(deployedInstance, requestedInstance)) {
                    logger.logReusingCompatibleInstance(deployedInstance);
                    itr.remove();
                }
            }
        }
    }

    /** 按依赖顺序与供应器 {@link Supplier#order()} 部署所有待请求实例。 */
    private void deployRequestedInstances() {
        requestedInstances.sort(RequestedInstanceComparator.INSTANCE);

        while (!requestedInstances.isEmpty()) {
            RequestedInstance nextToDeploy = requestedInstances.stream().filter(r -> {
                List<Dependency> declaredDependencies = r.getDeclaredDependencies();
                for (Dependency d : declaredDependencies) {
                    if (deployedInstances.stream().noneMatch(InstanceContextPredicates.matches(d.valueType(), d.ref()))) {
                        return false;
                    }
                }
                return true;
            }).findFirst().orElseThrow(() -> new RuntimeException("Failed to resolve next requested instance to deploy"));

            requestedInstances.remove(nextToDeploy);

            if (getDeployedInstance(nextToDeploy) == null) {
                InstanceContext instance = new InstanceContext(nextToDeploy.getInstanceId(), this, nextToDeploy.getSupplier(), nextToDeploy.getAnnotation(), nextToDeploy.getValueType(), nextToDeploy.getDeclaredDependencies());
                instance.setValue(nextToDeploy.getSupplier().getValue(instance));
                deployedInstances.add(instance);

                if (!nextToDeploy.getDependents().isEmpty()) {
                    Set<InstanceContext<?,?>> dependencies = nextToDeploy.getDependents();
                    dependencies.forEach(instance::registerDependent);
                }

                logger.logCreatedInstance(nextToDeploy, instance);
            }
        }
    }

    /** 将已部署实例值注入测试类中带注入注解的字段。 */
    private void injectFields(Object testInstance) {
        for (Field f : ReflectionUtils.listFields(testInstance.getClass())) {
            InstanceContext<?, ?> instance = getDeployedInstance(f.getType(), f.getAnnotations());
            if (instance == null) { // 测试类可能含有非注入用途的字段
                continue;
            }
            ReflectionUtils.setField(f, testInstance, instance.getValue());
        }
    }

    /** 调用测试类上带指定注解且无参的方法（如 {@link TestSetup}、{@link TestCleanup}）。 */
    private void executeSetup(Object testInstance, Class<? extends Annotation> annotation) {
        for (Method m : ReflectionUtils.listMethods(testInstance.getClass(), annotation)) {
            if (m.getParameterCount() != 0) {
                throw new RuntimeException("Method with " + annotation.getName() + " has required parameters: " + m); // Update when https://github.com/keycloak/keycloak/pull/45869 is merged
            }
            try {
                m.invoke(testInstance);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Method with " + annotation.getName() + " not accessible: " + m); // Update when https://github.com/keycloak/keycloak/pull/45869 is merged
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /** 测试类结束后运行 {@link TestCleanup}、销毁 CLASS 生命周期实例并抛出累积的致命错误。 */
    public void afterAll() {
        FatalTestClassException exception = fatalTestClassException;
        fatalTestClassException = null;

        if (exception == null && currentTestInstance != null) {
            executeSetup(currentTestInstance, TestCleanup.class);
        }

        logger.logAfterAll();
        List<InstanceContext<?, ?>> destroy = deployedInstances.stream().filter(InstanceContextPredicates.hasLifeCycle(LifeCycle.CLASS)).toList();
        destroy.forEach(this::destroy);

        if (exception != null) {
            throw exception;
        }
    }

    /** 每个测试方法结束后销毁 METHOD 生命周期实例，并对 {@link ManagedTestResource} 执行清理或重建。 */
    public void afterEach() {
        logger.logAfterEach();
        List<InstanceContext<?, ?>> destroy = deployedInstances.stream().filter(InstanceContextPredicates.hasLifeCycle(LifeCycle.METHOD)).toList();
        destroy.forEach(this::destroy);

        List<InstanceContext<?, ?>> cleanup = deployedInstances.stream().filter(InstanceContextPredicates.isInstanceof(ManagedTestResource.class)).toList();
        for (InstanceContext<?, ?> c : cleanup) {
            ManagedTestResource managedTestResource = (ManagedTestResource) c.getValue();
            if (managedTestResource.isDirty()) {
                logger.logDestroyDirty(c);
                destroy(c);
            } else {
                logger.logCleanup(c);
                managedTestResource.runCleanup();
            }
        }
    }

    /** 关闭注册表，按相反顺序销毁所有剩余已部署实例。 */
    public void close() {
        logger.logClose();
        List<InstanceContext<?, ?>> destroy = deployedInstances.stream().sorted(InstanceContextComparator.INSTANCE.reversed()).toList();
        destroy.forEach(this::destroy);
    }

    /** @return 已加载供应器列表 */
    List<Supplier<?, ?>> getSuppliers() {
        return extensions.getSuppliers();
    }

    /**
     * 根据注解数组或值类型创建 {@link RequestedInstance}。
     *
     * @param annotations 字段/类注解，可为 {@code null} 表示按类型查找
     * @param valueType 期望的值类型
     * @return 请求实例，无匹配供应器时返回 {@code null}
     */
    RequestedInstance<?, ?> createRequestedInstance(Annotation[] annotations, Class<?> valueType) {
        if (annotations != null) {
            for (Annotation annotation : annotations) {
                Supplier<?, ?> supplier = extensions.findSupplierByAnnotation(annotation);
                if (supplier != null) {
                    if (!supplier.getValueType().isAssignableFrom(valueType)) {
                        throw typeMismatch(annotation.annotationType(), supplier.getValueType(), valueType);
                    }
                    return new RequestedInstance(supplier, annotation, valueType);
                }
            }
        } else {
            Supplier<?, ?> supplier = extensions.findSupplierByType(valueType);
            if (supplier != null) {
                Annotation defaultAnnotation = DefaultAnnotationProxy.proxy(supplier.getAnnotationClass(), null);
                return new RequestedInstance(supplier, defaultAnnotation, valueType);
            }
        }
        return null;
    }

    /** 按字段注解与值类型匹配已部署实例。 */
    private InstanceContext<?, ?> getDeployedInstance(Class<?> valueType, Annotation[] annotations) {
        for (Annotation a : annotations) {
            for (InstanceContext<?, ?> i : deployedInstances) {
                Supplier supplier = i.getSupplier();
                if (supplier.getAnnotationClass().equals(a.annotationType())
                        && valueType.isAssignableFrom(i.getValue().getClass())
                        && Objects.equals(supplier.getRef(a), i.getRef())) {
                    return i;
                }
            }
        }
        return null;
    }

    /** 递归销毁实例及其依赖方，并调用供应器 {@link Supplier#close}。 */
    private void destroy(InstanceContext instanceContext) {
        boolean removed = deployedInstances.remove(instanceContext);
        if (removed) {
            Set<InstanceContext> dependencies = instanceContext.getDependents();
            dependencies.forEach(this::destroy);
            instanceContext.getSupplier().close(instanceContext);

            logger.logDestroy(instanceContext);
        }
    }

    /** 按 ref 与值类型/供应器匹配已部署实例。 */
    private InstanceContext getDeployedInstance(RequestedInstance requestedInstance) {
        String requestedRef = requestedInstance.getRef();
        Class requestedValueType = requestedInstance.getValueType();
        for (InstanceContext<?, ?> i : deployedInstances) {
            if (!Objects.equals(i.getRef(), requestedRef)) {
                continue;
            }

            if (requestedValueType != null) {
                if (requestedValueType.isAssignableFrom(i.getValue().getClass())) {
                    return i;
                }
            } else if (i.getSupplier().equals(requestedInstance.getSupplier())) {
                return i;
            }
        }
        return null;
    }

    /** 按类型与 ref 查找已部署实例。 */
    private InstanceContext getDeployedInstance(Class typeClass, String ref) {
        return deployedInstances.stream()
                .filter(InstanceContextPredicates.matches(typeClass, ref))
                .findFirst().orElse(null);
    }

    /** 按类型与 ref 查找待部署请求实例。 */
    private RequestedInstance getRequestedInstance(Class typeClass, String ref) {
        return requestedInstances.stream()
                .filter(RequestedInstancePredicates.matches(typeClass, ref))
                .findFirst().orElse(null);
    }

    /** 对所有已部署实例调用供应器 {@link Supplier#onBeforeEach}。 */
    private void invokeBeforeEachOnSuppliers() {
        for (InstanceContext i : deployedInstances) {
            i.getSupplier().onBeforeEach(i);
        }
    }

    /** 返回应拦截指定测试方法的第一个执行器。 */
    private TestFrameworkExecutor getExecutor(Method testMethod) {
        return extensions.getTestFrameworkExecutors().stream().filter(TestFrameworkExecutorPredicates.shouldExecute(testMethod)).findFirst().orElse(null);
    }

    /** 构造注解与字段类型不匹配的致命测试类异常。 */
    private FatalTestClassException typeMismatch(
            Class<? extends Annotation> annotation,
            Class<?> expectedType,
            Class<?> providedType) {
        return new FatalTestClassException(
                String.format("@%s requires %s (or its subclass) but field has type %s",
                        annotation.getSimpleName(),
                        expectedType.getName(),
                        providedType.getName())
        );
    }

    /** 按供应器 {@link Supplier#order()} 比较请求实例部署顺序。 */
    private static class RequestedInstanceComparator implements Comparator<RequestedInstance> {

        /** 单例比较器实例。 */
        static final RequestedInstanceComparator INSTANCE = new RequestedInstanceComparator();

        @Override
        public int compare(RequestedInstance o1, RequestedInstance o2) {
            return Integer.compare(o1.getSupplier().order(), o2.getSupplier().order());
        }
    }

    /** 按供应器顺序比较已部署实例，用于关闭时的销毁顺序。 */
    private static class InstanceContextComparator implements Comparator<InstanceContext> {

        /** 单例比较器实例。 */
        static final InstanceContextComparator INSTANCE = new InstanceContextComparator();

        @Override
        public int compare(InstanceContext o1, InstanceContext o2) {
            return Integer.compare(o1.getSupplier().order(), o2.getSupplier().order());
        }
    }

}
