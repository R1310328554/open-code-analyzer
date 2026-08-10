package org.keycloak.testframework.config;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import io.smallrye.config.ConfigSourceInterceptor;
import io.smallrye.config.ConfigSourceInterceptorContext;
import io.smallrye.config.ConfigValue;

/**
 * 将 Quarkus 日志相关配置键映射到 {@code kc.test.log.*} 命名空间，
 * 使测试框架可用统一前缀配置日志级别与控制台颜色。
 */
public class LogConfigInterceptor implements ConfigSourceInterceptor {

    private static final Map<String, String> MAPPED = Map.of("quarkus.console.color", "kc.test.console.color");
    private static final Set<String> EXCLUDED = Set.of("kc.test.log.filter");
    private static final String QUARKUS_PREFIX = "quarkus.log.";
    private static final String KCT_PREFIX = "kc.test.log.";

    /** 读取时将 {@code quarkus.log.*} 重定向到 {@code kc.test.log.*}。 */
    @Override
    public ConfigValue getValue(ConfigSourceInterceptorContext context, String name) {
        if (name.startsWith(QUARKUS_PREFIX)) {
            ConfigValue mapped = context.proceed(name.replace(QUARKUS_PREFIX, KCT_PREFIX));
            if (mapped != null) {
                return mapped;
            }
        } else {
            String mappedName = MAPPED.get(name);
            if (mappedName != null) {
                ConfigValue mapped = context.proceed(mappedName);
                if (mapped != null) {
                    return mapped;
                }
            }
        }
        return context.proceed(name);
    }

    /** 枚举配置名时将 {@code kc.test.log.*} 暴露为 {@code quarkus.log.*}（排除 filter 键）。 */
    @Override
    public Iterator<String> iterateNames(ConfigSourceInterceptorContext context) {
        return new LogNamesIterator(context.iterateNames());
    }

    /** 迭代配置键名并做 kc.test ↔ quarkus 前缀转换。 */
    private static class LogNamesIterator implements Iterator<String> {

        private final Iterator<String> iterator;

        /** @param iterator 底层配置名迭代器 */
        public LogNamesIterator(Iterator<String> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public String next() {
            String next = iterator.next();
            if (next.startsWith(KCT_PREFIX) && !EXCLUDED.contains(next)) {
                return next.replace(KCT_PREFIX, QUARKUS_PREFIX);
            } else {
                return next;
            }
        }
    }

}
