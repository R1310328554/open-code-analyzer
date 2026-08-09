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
package io.quarkus.redisson.client.runtime;

import io.quarkus.arc.DefaultBean;
import io.quarkus.runtime.shutdown.ShutdownConfig;
import org.eclipse.microprofile.config.ConfigProvider;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.ConfigSupport;
import org.redisson.config.PropertiesConvertor;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Quarkus 2.x 运行时 CDI 生产者：从配置或 {@code redisson.yaml} 构建 {@link RedissonClient}。
 * <p>优先读取 {@code quarkus.redisson.file} 指定资源；否则将 {@code quarkus.redisson.*}
 * 属性转换为 YAML 后解析。应用关闭时按 {@link ShutdownConfig} 优雅停机。
 *
 * @author Nikita Koksharov
 */
@ApplicationScoped
public class RedissonClientProducer {

    private RedissonClient redisson;

    @Inject
    public ShutdownConfig shutdownConfig;

    /** 创建默认 {@link RedissonClient} Bean；配置缺失时抛出 {@link IllegalStateException}。 */
    @Produces
    @Singleton
    @DefaultBean
    public RedissonClient create() throws IOException {
        String config = null;
        Optional<String> configFile = ConfigProvider.getConfig().getOptionalValue("quarkus.redisson.file", String.class);
        String configFileName = configFile.orElse("redisson.yaml");
        try (InputStream configStream = Optional.ofNullable(getClass().getResourceAsStream(configFileName))
                .orElse(Thread.currentThread().getContextClassLoader().getResourceAsStream(configFileName))
        ) {
            if (configStream != null) {
                byte[] array = new byte[configStream.available()];
                if (configStream.read(array) != -1) {
                    config = new String(array, StandardCharsets.UTF_8);
                }
            }
        }
        if (config == null) {
            Stream<String> s = StreamSupport.stream(ConfigProvider.getConfig().getPropertyNames().spliterator(), false);
            config = PropertiesConvertor.toYaml("quarkus.redisson.", s.sorted().collect(Collectors.toList()), prop -> {
                return ConfigProvider.getConfig().getValue(prop, String.class);
            }, false);
        }

        if (config.trim().isEmpty()) {
            // 未找到 YAML 文件且 quarkus.redisson.* 属性为空。
            throw new IllegalStateException("Redisson settings aren't defined.");
        }

        ConfigSupport support = new ConfigSupport(true);
        Config c = support.fromYAML(config, Config.class);
        redisson = Redisson.create(c);
        return redisson;
    }

    /** 容器销毁时关闭 Redisson；若配置了 shutdown timeout 则分阶段等待。 */
    @PreDestroy
    public void close() {
        if (redisson != null) {
            if (shutdownConfig.isShutdownTimeoutSet()){
                Duration grace = shutdownConfig.timeout.get();
                redisson.shutdown(grace.toMillis(),grace.toMillis()*2, TimeUnit.MILLISECONDS);
            }else{
                redisson.shutdown();
            }
        }
    }

}
