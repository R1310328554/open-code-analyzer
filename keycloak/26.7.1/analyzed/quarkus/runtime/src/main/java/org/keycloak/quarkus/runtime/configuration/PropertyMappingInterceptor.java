/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.quarkus.runtime.configuration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import jakarta.annotation.Priority;

import org.keycloak.config.OptionCategory;
import org.keycloak.quarkus.runtime.configuration.mappers.PropertyMapper;
import org.keycloak.quarkus.runtime.configuration.mappers.PropertyMappers;
import org.keycloak.quarkus.runtime.configuration.mappers.WildcardPropertyMapper;

import io.smallrye.config.ConfigSourceInterceptor;
import io.smallrye.config.ConfigSourceInterceptorContext;
import io.smallrye.config.ConfigValue;
import io.smallrye.config.Priorities;
import org.apache.commons.collections4.IteratorUtils;

import static org.keycloak.quarkus.runtime.configuration.MicroProfileConfigProvider.NS_KEYCLOAK_PREFIX;

/**
 * <p>This interceptor is responsible for mapping Keycloak properties to their corresponding properties in Quarkus.
 * 负责将 Keycloak（{@code kc.*}）属性映射到 Quarkus 侧对应属性。

 *
 * <p>A single property in Keycloak may span a single or multiple properties on Quarkus and for each property we want to map
 * from Quarkus we should configure a {@link PropertyMapper}.
 * 单个 kc 属性可 1:1 或 1:N 映射到 Quarkus；每个映射由 {@link PropertyMapper} 定义。

 *
 * <p>The {@link PropertyMapper} can either perform a 1:1 mapping where the value of a property from
 * Keycloak (e.g.: https.port) is mapped to a single properties in Quarkus, or perform a 1:N mapping where the value of a property
 * from Keycloak (e.g.: database) is mapped to multiple properties in Quarkus.
 * 支持一对一（如 https.port）与一对多（如 database）两种映射模式。

 *
 * <p>This interceptor must execute after the {@link io.smallrye.config.ExpressionConfigSourceInterceptor} so that expressions
 * are properly resolved before executing this interceptor.
 * 须在 {@link io.smallrye.config.ExpressionConfigSourceInterceptor} 之后执行，确保表达式已展开。

 *
 * <p>The {@link NestedPropertyMappingInterceptor} catches property mappings that need to be performed within expressions.
 * 表达式内嵌套映射由 {@link NestedPropertyMappingInterceptor} 二次处理。

 *
 * <p>
 * The reason for the used priority is to always execute the interceptor before default Application Config Source interceptors
 */
 * 优先级设为 APPLICATION-10，保证在默认应用拦截器之前运行。

@Priority(Priorities.APPLICATION - 10)
public class PropertyMappingInterceptor implements ConfigSourceInterceptor {

    /** 临时禁用映射逻辑（如校验原始 kc 值时）。 */
    private static final ThreadLocal<Boolean> disable = new ThreadLocal<>();

    private Boolean augmenting;

    public static void disable() {
        disable.set(true);
    }

    public static void enable() {
        disable.remove();
    }

    /**
     * Provides a curated iteration of names based upon the mapping logic.
     * Quarkus logic, such as config mapping, is dependent upon seeing the quarkus
     * form of the key. We want to expose that here, rather than in the config sources
     * because we lack a simple way to do name mapping for some sources, such as the
     * keystore config source.
     * <p>
     * We currently expose:
     * <li>anything based upon a property mapper that has a map to a quarkus property - including
     * our kc. properties that have defaults.
     * <li>wildcard key names for wildcard keys that map from a keycloak property (e.g. kc.log-level)
     *
     * We selectively exclude:
     * <li>Config keystore properties at build time
     */
 * 按映射规则裁剪属性名迭代：暴露 Quarkus 侧键名及通配符键，构建时排除 config-keystore 相关项。

    @Override
    public Iterator<String> iterateNames(ConfigSourceInterceptorContext context) {
        Iterable<String> iterable = context::iterateNames;

        final Set<PropertyMapper<?>> mappersWithoutValues = PropertyMappers.getMappers();

        boolean filterRuntime = isAugmenting(context);

        // 配置未初始化时仅暴露引导级选项，对值存在性做简化假设

        // 仅做第一层发现，不递归检查推断值的通配符或关联选项

        var baseStream = StreamSupport.stream(iterable.spliterator(), false).flatMap(name -> {
            final PropertyMapper<?> mapper = PropertyMappers.getMapper(name);

            if (mapper == null) {
                return Stream.of(name);
            }
            if (filterRuntime && mapper.getCategory() == OptionCategory.CONFIG) {
                return Stream.of(); // advertising the keystore type causes the keystore to be used early
            }

            final PropertyMapper<?> mappedMapper = mapper.forKey(name);

            mappersWithoutValues.remove(mapper);

            // 仅在映射 from 侧追加别名，因映射未必双向
            if (!name.equals(mappedMapper.getFrom())) {
                return Stream.of(name);
            }

            List<String> allNames = new ArrayList<String>();
            allNames.add(name);
            if (!name.equals(mappedMapper.getTo()) && hasValue(mappedMapper.getTo(), context)) {
                allNames.add(mappedMapper.getTo());
            }

            appendWildcardsMappedFrom(context, name, mapper, allNames);

            if (mapper.hasWildcard()) {
                var wildcardMapper = (WildcardPropertyMapper<?>) mapper;

                var wildcardValue = wildcardMapper.extractWildcardValue(name).orElseThrow();

                if (mapper.hasConnectedOptions()) {
                    wildcardMapper.getConnectedOptions(wildcardValue).stream()
                            .map(option -> Optional.ofNullable(PropertyMappers.getMapper(NS_KEYCLOAK_PREFIX + option)).orElseThrow(() -> new IllegalArgumentException("Cannot find connected options")))
                            .map(m -> m.hasWildcard() ? ((WildcardPropertyMapper<?>) m).getTo(wildcardValue) : m.getTo())
                            .filter(key -> hasValue(key, context)).forEach(allNames::add);
                }
            }

            return allNames.stream();
        });

        // 追加仍有推断值的 mapper 的 to（通常为 Quarkus）键
        var inferredValueStream = mappersWithoutValues.stream()
                .filter(m -> hasInferredValue(m, context))
                .map(m -> m.getTo());

        return IteratorUtils.chainedIterator(baseStream.iterator(), inferredValueStream.iterator());
    }

    private boolean isAugmenting(ConfigSourceInterceptorContext context) {
        if (augmenting == null) {
            // BuildTimeConfigurationReader：排除系统属性时表示处于 augment 阶段
            augmenting = context.proceed("file.separator") == null;
        }
        return augmenting;
    }

    private void appendWildcardsMappedFrom(ConfigSourceInterceptorContext context, String name,
            final PropertyMapper<?> mapper, List<String> names) {
        var wildCards = PropertyMappers.getWildcardsMappedFrom(mapper.getOption());
        if (wildCards.isEmpty()) {
            return;
        }
        ConfigValue value = context.proceed(name);
        if (value == null || value.getValue() == null) {
            return;
        }
        if (mapper.hasWildcard()) {
            var wildcardMapper = (WildcardPropertyMapper<?>) mapper;
            var wildcardValue = wildcardMapper.extractWildcardValue(name).orElseThrow();
            wildCards.stream().map(w -> w.getTo(wildcardValue)).filter(to -> hasValue(to, context)).forEach(names::add);
        } else {
            // 非通配符键的值可能展开为多个通配符目标（如 log-level 语法）
            wildCards.stream().flatMap(w -> w.getToFromWildcardTransformer(value.getValue())).forEach(names::add);
        }
    }

    private boolean hasInferredValue(PropertyMapper<?> m, ConfigSourceInterceptorContext context) {
        if (m.getCategory() == OptionCategory.CONFIG // 过早暴露 keystore 类型会导致 keystore 被提前启用
                || m.hasWildcard()
                || (m.getDefaultValue().isEmpty() && m.getMapFrom() == null)
                || m.getTo().startsWith(NS_KEYCLOAK_PREFIX)) {
            return false;
        }

        if (m.getMapper() == null && m.getDefaultValue().isPresent() && m.getMapFrom() == null) {
            return true; // 无 mapper 时直接使用默认值
        }

        if (Configuration.isInitialized()) {
            return hasValue(m.getTo(), context);
        }

        return m.getDefaultValue().isPresent(); // 初始化前简化假设：有默认值即视为存在
    }

    private boolean hasValue(String key, ConfigSourceInterceptorContext context) {
        try {
            return !Configuration.isInitialized()
                    || key.startsWith(NS_KEYCLOAK_PREFIX) // kc 前缀键在初始化后始终视为“有值”
                    || Optional.ofNullable(context.restart(key)).map(ConfigValue::getValue).isPresent();
        } catch (Exception e) {
            return false; // 校验失败等边角情况，不视为有值
        }
    }

    @Override
    public ConfigValue getValue(ConfigSourceInterceptorContext context, String name) {
        if (Boolean.TRUE.equals(disable.get())) {
            return context.proceed(name);
        }

        // 经 NestedPropertyMappingInterceptor 解析并跟踪当前正在求值的属性
        return NestedPropertyMappingInterceptor.getValueFromPropertyMappers(context, name, isAugmenting(context));
    }
}
