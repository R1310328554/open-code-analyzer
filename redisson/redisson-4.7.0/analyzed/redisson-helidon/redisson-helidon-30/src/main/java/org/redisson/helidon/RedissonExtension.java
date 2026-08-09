/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.helidon;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.*;
import jakarta.inject.Named;
import org.eclipse.microprofile.config.Config;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.PropertiesConvertor;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Helidon CDI 扩展：收集 {@link RedissonClient} 注入点上的限定符，
 * 在 {@link AfterBeanDiscovery} 阶段为每个限定符注册对应的生产者 Bean。
 * <p>配置从 MicroProfile {@link Config} 读取，键前缀为 {@code org.redisson.Redisson.<instanceName>.}。</p>
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonExtension implements Extension {

    private final Set<Annotation> qualifiers = new HashSet<>();

    /** 收集所有 {@link RedissonClient} 注入点使用的 CDI 限定符。 */
    private <T extends RedissonClient> void processRedissonInjectionPoint(@Observes ProcessInjectionPoint<?, T> point) {
        if (point == null) {
            return;
        }
        InjectionPoint injectionPoint = point.getInjectionPoint();
        if (injectionPoint == null) {
            return;
        }

        qualifiers.addAll(injectionPoint.getQualifiers());
    }

    /** 为每个已收集的限定符注册 {@link ApplicationScoped} 范围的 {@link RedissonClient} 生产者 Bean。 */
    private void addBeans(@Observes AfterBeanDiscovery discovery, BeanManager beanManager) {
        if (discovery == null || beanManager == null) {
            return;
        }

        for (Annotation qualifier : qualifiers) {
            Set<Annotation> qualifiers = Collections.singleton(qualifier);

            discovery.addBean()
                .scope(ApplicationScoped.class)
                .addQualifiers(qualifiers)
                .addTransitiveTypeClosure(RedissonClient.class)
                .produceWith(instance -> {

                    String instanceName = "default";
                    if (qualifier instanceof Named) {
                        instanceName = ((Named) qualifier).value();
                    }

                    Config cfg = instance.select(Config.class).get();
                    String yamlConfig = PropertiesConvertor.toYaml(Redisson.class.getName() + "." + instanceName + ".",
                            cfg.getPropertyNames(), prop -> {
                                return cfg.getValue(prop, String.class);
                    }, true);

                    try {
                        org.redisson.config.Config config = org.redisson.config.Config.fromYAML(yamlConfig);
                        return Redisson.create(config);
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                });
        }
    }


}
