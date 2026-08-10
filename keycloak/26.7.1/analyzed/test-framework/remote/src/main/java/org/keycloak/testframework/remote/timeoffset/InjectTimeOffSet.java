package org.keycloak.testframework.remote.timeoffset;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.keycloak.testframework.injection.LifeCycle;

/**
 * 向测试字段注入 {@link TimeOffSet}，用于调整 Keycloak 服务器的时间偏移。
 * <p>
 * 便于测试令牌过期、会话超时等依赖系统时间的场景。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectTimeOffSet {

    /** 控制 {@link TimeOffSet} 实例的生命周期范围。 */
    LifeCycle lifecycle() default LifeCycle.METHOD;

    /**
     * 是否将时间偏移同步到底层缓存（例如 Infinispan）。
     *
     * @return 为 {@code true} 时服务端缓存也应用相同偏移
     */
    boolean enableForCaches() default false;

    /** 初始时间偏移量（秒），正数表示将时钟拨快。 */
    int offset() default 0;
}
