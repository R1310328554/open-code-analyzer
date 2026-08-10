/*
 * Copyright 2019 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.util.internal.svm;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.RecomputeFieldValue;
import com.oracle.svm.core.annotate.TargetClass;

/**
 * <p>GraalVM Native Image 对 {@link io.netty.util.internal.PlatformDependent0} 的字段替换配置。</p>
 * <p>构建 native-image 时，Substrate VM 会在镜像生成阶段解析 {@code java.nio.Buffer.address}
 * 的字段偏移量；{@link RecomputeFieldValue} 确保 {@code ADDRESS_FIELD_OFFSET} 在运行时
 * 按目标平台重新计算，而非固化构建机上的值。</p>
 */
@TargetClass(className = "io.netty.util.internal.PlatformDependent0")
final class PlatformDependent0Substitution {
    private PlatformDependent0Substitution() {
    }

    /** {@code java.nio.Buffer.address} 字段偏移量，native-image 构建时重新计算。 */
    @Alias
    @RecomputeFieldValue(
        kind = RecomputeFieldValue.Kind.FieldOffset,
        declClassName = "java.nio.Buffer",
        name = "address")
    private static long ADDRESS_FIELD_OFFSET;
}
