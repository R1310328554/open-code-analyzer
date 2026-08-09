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
package io.quarkus.redisson.client.deployment;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.ExtensionSslNativeSupportBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.HotDeploymentWatchedFileBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedClassBuildItem;
import io.quarkus.redisson.client.runtime.RedissonClientProducer;
import io.quarkus.redisson.client.runtime.RedissonClientRecorder;
import org.redisson.RedissonBucket;
import org.redisson.RedissonMultimap;
import org.redisson.RedissonObject;
import org.redisson.api.RBucket;
import org.redisson.api.RExpirable;
import org.redisson.api.RObject;
import org.redisson.api.RObjectReactive;
import org.redisson.codec.Kryo5Codec;
import org.redisson.config.*;
import org.redisson.executor.RemoteExecutorService;
import org.redisson.executor.RemoteExecutorServiceAsync;

import java.io.IOException;

/**
 * Quarkus 3.0 扩展部署处理器：注册 Redisson 客户端 CDI 生产者与 Native Image 反射配置。
 * <p>构建阶段加载 {@code redisson.yaml}、注册 {@link Kryo5Codec} 与配置类反射，
 * 运行时通过 {@link RedissonClientRecorder} 创建 {@link RedissonClientProducer}。
 *
 * @author Nikita Koksharov
 */
class QuarkusRedissonClientProcessor {

    private static final String FEATURE = "redisson";

    /** 向 Quarkus 注册 {@code redisson} 扩展特性名。 */
    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    /** 声明扩展支持 Native Image 下的 SSL/TLS。 */
    @BuildStep
    ExtensionSslNativeSupportBuildItem sslNativeSupport() {
        return new ExtensionSslNativeSupportBuildItem(FEATURE);
    }

    /** 将 {@link RedissonClientProducer} 注册为不可移除的 CDI Bean。 */
    @BuildStep
    AdditionalBeanBuildItem addProducer() {
        return AdditionalBeanBuildItem.unremovableOf(RedissonClientProducer.class);
    }

    /** 配置 Native Image 资源、热部署监听文件与 GraalVM 反射类列表。 */
    @BuildStep
    void addConfig(BuildProducer<NativeImageResourceBuildItem> nativeResources,
                   BuildProducer<HotDeploymentWatchedFileBuildItem> watchedFiles,
                   BuildProducer<RuntimeInitializedClassBuildItem> staticItems,
                   BuildProducer<ReflectiveClassBuildItem> reflectiveItems) {
        // 将 redisson.yaml 与 JBoss Marshalling SPI 描述符打包进 Native Image。
        nativeResources.produce(new NativeImageResourceBuildItem("redisson.yaml"));
        nativeResources.produce(new NativeImageResourceBuildItem("META-INF/services/org.jboss.marshalling.ProviderDescriptor"));
        // 开发模式下监听 redisson.yaml 变更以触发热重载。
        watchedFiles.produce(new HotDeploymentWatchedFileBuildItem("redisson.yaml"));

        // Kryo5 编解码器仅需类注册（无方法/字段反射）。
        reflectiveItems.produce(ReflectiveClassBuildItem.builder(Kryo5Codec.class)
                .methods(false)
                .fields(false)
                .build()
        );

        // 远程执行服务接口需方法反射以支持 RMI 代理。
        reflectiveItems.produce(ReflectiveClassBuildItem.builder(
                        RemoteExecutorService.class,
                        RemoteExecutorServiceAsync.class)
                .methods(true)
                .fields(false)
                .build()
        );

        // Redisson 配置层次结构需完整反射以支持 YAML/Properties 绑定。
        reflectiveItems.produce(ReflectiveClassBuildItem.builder(
                        Config.class,
                        BaseConfig.class,
                        BaseMasterSlaveServersConfig.class,
                        SingleServerConfig.class,
                        ReplicatedServersConfig.class,
                        SentinelServersConfig.class,
                        ClusterServersConfig.class)
                .methods(true)
                .fields(true)
                .build()
        );

        // 常用 Redis 对象 API 需方法与字段反射。
        reflectiveItems.produce(ReflectiveClassBuildItem.builder(
                        RBucket.class,
                        RedissonBucket.class,
                        RedissonObject.class,
                        RedissonMultimap.class)
                .methods(true)
                .fields(true)
                .build()
        );

        // 响应式与通用对象接口需方法反射。
        reflectiveItems.produce(ReflectiveClassBuildItem.builder(
                        RObjectReactive.class,
                        RExpirable.class,
                        RObject.class)
                .methods(true)
                .build()
        );


    }

    /** 运行时初始化：调用 Recorder 创建 CDI 生产者并返回构建标记项。 */
    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    RedissonClientItemBuild build(RedissonClientRecorder recorder) throws IOException {
        recorder.createProducer();
        return new RedissonClientItemBuild();
    }

}
