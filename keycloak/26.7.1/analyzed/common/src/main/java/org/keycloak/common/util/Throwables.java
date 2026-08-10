package org.keycloak.common.util;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 处理 {@link Throwable} 实例的工具类。
 */
public final class Throwables {

    /**
     * 检查 {@code throwable} 或其 cause 链（深度最多 3）是否属于指定异常类型之一。
     *
     * @param throwable 待检查的异常
     * @param type 候选异常类型
     * @return 若 cause 链中存在匹配类型则返回 true
     */
    @SafeVarargs
    public static boolean isCausedBy(Throwable throwable, Class<? extends Exception>... type) {
        Objects.requireNonNull(throwable, "Throwable must not be null");
        int limit = 3;
        List<Class<? extends Exception>> types = Arrays.asList(type);
        Throwable cause = throwable.getCause();

        while (cause != null) {
            if (limit-- == 0) {
                break;
            }
            if (types.contains(cause.getClass())) {
                return true;
            }
            cause = cause.getCause();
        }

        return false;
    }
}
