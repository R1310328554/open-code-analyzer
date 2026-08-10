package org.keycloak.quarkus.runtime.configuration;

import io.smallrye.config.SmallRyeConfigBuilder;
import io.smallrye.config.SmallRyeConfigBuilderCustomizer;

/**
 * SmallRye 配置构建器定制器：在类加载器匹配时为 Keycloak 注册配置拦截器。
 * <p>
 * 拦截器负责 CLI/环境变量与持久化属性之间的映射；类加载器不一致时跳过注册，
 * 避免在错误的类加载器上下文中隐式创建 {@link Configuration} 实例。
 */
public class ConfigBuilderCustomizer implements SmallRyeConfigBuilderCustomizer {

    @Override
    public void configBuilder(SmallRyeConfigBuilder builder) {
        if (builder.getClassLoader() == Thread.currentThread().getContextClassLoader()) {
            // 类加载器不匹配时不应启用 Keycloak 拦截器，否则会在错误类加载器中隐式创建 Configuration
            addInterceptors(builder);
        }
    }

    /** 向构建器注册属性映射与嵌套属性映射拦截器。 */
    static SmallRyeConfigBuilder addInterceptors(SmallRyeConfigBuilder builder) {
        return builder.withInterceptors(new PropertyMappingInterceptor(), new NestedPropertyMappingInterceptor());
    }

}
