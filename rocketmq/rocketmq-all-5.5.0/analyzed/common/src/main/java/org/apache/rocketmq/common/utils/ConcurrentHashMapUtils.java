/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.common.utils;

import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * {@link ConcurrentMap} 工具：针对 JDK 8 的 computeIfAbsent 性能缺陷提供兼容实现。
 */
public abstract class ConcurrentHashMapUtils {

    /** 当前 JVM 是否为 Java 8。 */
    private static boolean isJdk8;

    static {
        // Java 8 使用 1.8.x 版本号前缀；Java 9+ 为 9、11、17 等
        try {
            isJdk8 = System.getProperty("java.version").startsWith("1.8.");
        } catch (Exception ignore) {
            isJdk8 = true;
        }
    }

    /**
     * 针对 Java 8 特有性能问题 JDK-8161372 的临时规避方案，语义同 {@link ConcurrentMap#computeIfAbsent}。
     * <p>要求：<strong>映射函数在计算期间不得修改此 map。</strong>
     *
     * @see <a href="https://bugs.openjdk.java.net/browse/JDK-8161372">https://bugs.openjdk.java.net/browse/JDK-8161372</a>
     */
    public static <K, V> V computeIfAbsent(ConcurrentMap<K, V> map, K key, Function<? super K, ? extends V> func) {
        Objects.requireNonNull(func);
        if (isJdk8) {
            V v = map.get(key);
            if (null == v) {
                // 此规避实现可能导致 func.apply 被多次调用
                v = func.apply(key);
                if (null == v) {
                    return null;
                }
                final V res = map.putIfAbsent(key, v);
                if (null != res) {
                    // 已有其他线程写入，putIfAbsent 未生效，返回已存在的值
                    return res;
                }
            }
            return v;
        } else {
            return map.computeIfAbsent(key, func);
        }
    }
}
