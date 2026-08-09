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
package io.quarkus.redisson.client.runtime.graal;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import io.netty.channel.EventLoopGroup;
import org.redisson.config.Config;

/** GraalVM 替代：Native 模式下禁用 IOUring EventLoop 组创建。 */
@TargetClass(className = "org.redisson.connection.ServiceManager")
final class ServiceManagerSubstitute {

    /** 抛出异常表明 IOUring 与 Native Image 不兼容。 */
    @Substitute
    private static EventLoopGroup createIOUringGroup(Config cfg) {
        throw new IllegalArgumentException("IOUring isn't compatible with native mode");
    }

}

/** GraalVM 替代：跳过 JsonJacksonCodec 构建时 warmup（Native 下不可用）。 */
@TargetClass(className = "org.redisson.codec.JsonJacksonCodec")
final class JsonJacksonCodecSubstitute {

    /** Native 模式下空实现，避免反射预热失败。 */
    @Substitute
    private void warmup() {
    }

}