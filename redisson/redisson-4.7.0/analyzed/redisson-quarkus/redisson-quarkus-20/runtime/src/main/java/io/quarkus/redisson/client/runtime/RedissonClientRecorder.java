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

import io.quarkus.arc.Arc;
import io.quarkus.runtime.annotations.Recorder;

/**
 * Quarkus 运行时 Recorder：在 RUNTIME_INIT 阶段触发 {@link RedissonClientProducer} 实例化。
 * <p>确保 CDI 容器在应用启动时完成 Redisson 客户端的 eagerly 初始化。
 *
 * @author Nikita Koksharov
 */
@Recorder
public class RedissonClientRecorder {

    /** 通过 Arc 容器获取并初始化 {@link RedissonClientProducer}。 */
    public void createProducer() {
        Arc.container().instance(RedissonClientProducer.class).get();
    }

}
