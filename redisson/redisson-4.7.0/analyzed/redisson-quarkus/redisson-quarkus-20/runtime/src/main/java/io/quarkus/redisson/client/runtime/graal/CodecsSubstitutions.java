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

/** Native Image 中禁用 IOUring EventLoop，避免与 GraalVM 不兼容。 */
@TargetClass(className = "org.redisson.connection.ServiceManager")
final class ServiceManagerSubstitute {

    /** 替换 IOUring 组创建逻辑，Native 模式下直接抛出异常。 */
    @Substitute
    private static EventLoopGroup createIOUringGroup(Config cfg) {
        throw new IllegalArgumentException("IOUring isn't compatible with native mode");
    }

}

/** 跳过 JsonJacksonCodec 启动预热，缩短 Native Image 启动时间。 */
@TargetClass(className = "org.redisson.codec.JsonJacksonCodec")
final class JsonJacksonCodecSubstitute {

    /** 空实现替代 Jackson 编解码器 warmup。 */
    @Substitute
    private void warmup() {
    }

}