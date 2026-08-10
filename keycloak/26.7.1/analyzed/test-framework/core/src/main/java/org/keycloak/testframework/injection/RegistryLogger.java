package org.keycloak.testframework.injection;

import java.util.List;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;

@SuppressWarnings("rawtypes")
/**
 * {@link Registry} 与供应器加载过程的调试日志封装。
 * <p>
 * 在 DEBUG 级别记录依赖注入、实例创建/销毁/复用及供应器筛选信息。
 */
class RegistryLogger {

    /** JBoss Logging 记录器。 */
    private static final Logger LOGGER = Logger.getLogger(RegistryLogger.class);
    /** 值类型别名，用于供应器日志输出。 */
    private final ValueTypeAlias valueTypeAlias;

    /** @param valueTypeAlias 值类型别名映射 */
    public RegistryLogger(ValueTypeAlias valueTypeAlias) {
        this.valueTypeAlias = valueTypeAlias;
    }

    /** 记录依赖注入：依赖方、被依赖方及注入来源类型。 */
    public void logDependencyInjection(InstanceContext<?, ?> dependent, InstanceContext<?, ?> dependency, InjectionType injectionType) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debugv("Injecting {0} dependency {1}#{2,number,#} into {3}#{4,number,#}",
                    injectionType,
                    dependency.getSupplier().getClass().getSimpleName(),
                    dependency.getInstanceId(),
                    dependent.getSupplier().getClass().getSimpleName(),
                    dependent.getInstanceId());
        }
    }

    /** 记录当前测试方法请求的所有实例类型名称。 */
    public void logRequestedInstances(List<RequestedInstance<?, ?>> requestedInstances) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debugv("Requested instances: {0}",
                    requestedInstances.stream().map(r -> r.getSupplier().getValueType().getSimpleName()).collect(Collectors.joining(", ")));
        }
    }

    /** 记录复用兼容的已部署实例。 */
    public void logReusingCompatibleInstance(InstanceContext instance) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debugv("Reusing compatible: {0}#{1,number,#}",
                    instance.getSupplier().getClass().getSimpleName(),
                    instance.getInstanceId());
        }
    }

    /** 记录实例关闭。 */
    public void logDestroy(InstanceContext instance) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debugv("Closed instance: {0}#{1,number,#}",
                    instance.getSupplier().getClass().getSimpleName(),
                    instance.getInstanceId());
        }
    }

    /** 记录因不兼容而关闭的实例。 */
    public void logDestroyIncompatible(InstanceContext instance) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debugv("Closing non-compatible instance: {0}#{1,number,#}",
                    instance.getSupplier().getClass().getSimpleName(),
                    instance.getInstanceId());
        }
    }

    /** 记录因脏标记而关闭的 {@link ManagedTestResource} 实例。 */
    public void logDestroyDirty(InstanceContext instance) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debugv("Closing dirty instance: {0}#{1,number,#}",
                    instance.getSupplier().getClass().getSimpleName(),
                    instance.getInstanceId());
        }
    }

    /** 记录 {@link ManagedTestResource#runCleanup()} 清理操作。 */
    public void logCleanup(InstanceContext instance) {
        LOGGER.debugv("Cleanup instance {0}#{1,number,#}", instance.getValue(), instance.getInstanceId());
    }

    /** 记录新创建的已部署实例。 */
    public void logCreatedInstance(RequestedInstance requestedInstance, InstanceContext instance) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debugv("Created instance: {0}#{1,number,#}",
                    requestedInstance.getSupplier().getClass().getSimpleName(), instance.getInstanceId());
        }
    }

    /** 记录测试类结束、即将关闭 CLASS 生命周期实例。 */
    public void logAfterAll() {
        LOGGER.debug("Closing instances with class lifecycle");
    }

    /** 记录测试方法结束、即将关闭 METHOD 生命周期实例。 */
    public void logAfterEach() {
        LOGGER.debug("Closing instances with method lifecycle");
    }

    /** 记录注册表关闭、即将销毁所有实例。 */
    public void logClose() {
        LOGGER.debug("Closing all instances");
    }

    /** 记录已加载与已跳过的供应器详情。 */
    public void logSuppliers(List<Supplier<?, ?>> suppliers, List<Supplier<?, ?>> skippedSuppliers) {
        if (LOGGER.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Loaded suppliers:");
            for (Supplier s : suppliers) {
                sb.append("\n - ");
                appendSupplierInfo(s, sb);
            }

            sb.append("\nSkipped suppliers:");
            for (Supplier s : skippedSuppliers) {
                sb.append("\n - ");
                appendSupplierInfo(s, sb);
            }

            LOGGER.debug(sb.toString());
        }
    }

    /** 向日志字符串追加单个供应器的类型、值类型与别名信息。 */
    private void appendSupplierInfo(Supplier s, StringBuilder sb) {
        sb.append("supplierType=");
        sb.append(s.getClass().getSimpleName());
        sb.append(", valueType=");
        sb.append(s.getValueType().getSimpleName());

        String alias = valueTypeAlias.getAlias(s.getValueType());
        if (!alias.equals(s.getValueType().getSimpleName())) {
            sb.append(", alias=");
            sb.append(alias);
        }

    }

    /** 记录值被拦截器供应器处理。 */
    public void logIntercepted(Object value, Supplier<?, ?> supplier) {
        LOGGER.debugv("{0} intercepted by {1}", value.getClass().getSimpleName(), supplier.getClass().getSimpleName());
    }

    /** 依赖注入来源类型。 */
    public enum InjectionType {

        /** 来自已部署实例。 */
        EXISTING("existing"),
        /** 来自待部署请求实例的即时部署。 */
        REQUESTED("requested"),
        /** 未配置或未声明的依赖注入。 */
        UN_CONFIGURED("un-configured");

        /** 日志字符串表示。 */
        private String stringRep;

        InjectionType(String stringRep) {
            this.stringRep = stringRep;
        }


        @Override
        public String toString() {
            return stringRep;
        }
    }

}
