/*
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

/*
 * Original License: https://github.com/JCTools/JCTools/blob/master/LICENSE
 * Original location: https://github.com/JCTools/JCTools/blob/master/jctools-core/src/main/java/org/jctools/util/Pow2.java
 */

package io.reactivex.rxjava4.internal.util;

/**
 * 2 的幂次工具（源自 JCTools Pow2）。
 * 供队列/哈希表容量对齐使用。
 */
public final class Pow2 {
    /** 工具类禁止实例化。 */
    private Pow2() {
        throw new IllegalStateException("No instances!");
    }

    /**
     * 返回不小于 value 的最小 2 的幂（value 本身为 2 的幂则原样返回）。
     * @param value 输入值
     * @return 2 的幂
     */
    public static int roundToPowerOfTwo(final int value) {
        return 1 << (32 - Integer.numberOfLeadingZeros(value - 1));
    }

    /**
     * 判断 value 是否为 2 的幂。
     * @param value 待测值
     * @return 是 2 的幂则 true
     */
    public static boolean isPowerOfTwo(final int value) {
        return (value & (value - 1)) == 0;
    }
}
