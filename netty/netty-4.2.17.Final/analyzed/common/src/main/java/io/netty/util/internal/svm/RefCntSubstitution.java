/*
 * Copyright 2025 The Netty Project
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
 * <p>GraalVM Native Image 对 {@code RefCnt.UnsafeRefCnt} 的字段替换配置。</p>
 * <p>引用计数实现依赖 {@code sun.misc.Unsafe} 获取 {@code RefCnt.value} 字段偏移量以执行
 * CAS 操作。native-image 构建时须通过 {@link RecomputeFieldValue} 在目标平台重新计算该偏移，
 * 否则 Unsafe 访问将指向错误内存位置。</p>
 */
@TargetClass(className = "io.netty.util.internal.RefCnt$UnsafeRefCnt")
final class RefCntSubstitution {
    private RefCntSubstitution() {
    }

    /** {@code RefCnt.value} 字段偏移量，native-image 构建时重新计算。 */
    @Alias
    @RecomputeFieldValue(
            kind = RecomputeFieldValue.Kind.FieldOffset,
            declClassName = "io.netty.util.internal.RefCnt",
            name = "value"
    )
    public static long VALUE_OFFSET;

}
