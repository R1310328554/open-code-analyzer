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
 * <p>GraalVM Native Image 对 shade 版 {@code UnsafeRefArrayAccess} 的字段替换配置。</p>
 * <p>JCTools 队列通过 {@code Object[]} 的索引位移（index shift）计算元素地址。
 * 该值取决于目标 JVM 的对象引用宽度，必须在 native-image 构建时按平台重算，
 * 不能沿用构建机上的常量。</p>
 */
@TargetClass(className = "io.netty.util.internal.shaded.org.jctools.util.UnsafeRefArrayAccess")
final class UnsafeRefArrayAccessSubstitution {
    private UnsafeRefArrayAccessSubstitution() {
    }

    /** {@code Object[]} 元素索引位移量，native-image 构建时重新计算。 */
    @Alias
    @RecomputeFieldValue(
        kind = RecomputeFieldValue.Kind.ArrayIndexShift,
        declClass = Object[].class)
    public static int REF_ELEMENT_SHIFT;
}
