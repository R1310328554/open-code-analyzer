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
 * <p>GraalVM Native Image 对 {@link io.netty.util.internal.PlatformDependent} 的字段替换配置。</p>
 * <p>{@link io.netty.util.internal.PlatformDependent} 在镜像构建期间会从
 * {@link io.netty.util.internal.PlatformDependent0} 缓存 {@code byte[]} 数组基址偏移量。
 * Substrate VM 能正确重算 {@code PlatformDependent0} 中的值，但 {@code PlatformDependent}
 * 中的缓存字段不会自动更新，因此需要显式声明 {@link RecomputeFieldValue}。</p>
 */
@TargetClass(className = "io.netty.util.internal.PlatformDependent")
final class PlatformDependentSubstitution {
    private PlatformDependentSubstitution() {
    }

    /**
     * The class PlatformDependent caches the byte array base offset by reading the
     * field from PlatformDependent0. The automatic recomputation of Substrate VM
     * correctly recomputes the field in PlatformDependent0, but since the caching
     * in PlatformDependent happens during image building, the non-recomputed value
     * is cached.
     *
     * <p>{@code byte[]} 数组基址偏移量；native-image 构建时重新计算，避免缓存构建机上的旧值。</p>
     */
    @Alias
    @RecomputeFieldValue(
        kind = RecomputeFieldValue.Kind.ArrayBaseOffset,
        declClass = byte[].class)
    private static long BYTE_ARRAY_BASE_OFFSET;
}
