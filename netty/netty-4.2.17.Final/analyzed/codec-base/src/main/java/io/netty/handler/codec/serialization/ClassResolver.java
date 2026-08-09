/*
 * Copyright 2012 The Netty Project
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
package io.netty.handler.codec.serialization;

/**
 * 反序列化时按类名解析 {@link Class} 的接口。
 * <p>
 * 请通过 {@link ClassResolvers} 工厂获取实例。
 * <p>
 * <strong>安全提示：</strong>Java 序列化存在安全风险，使用前应通过
 * {@code jdk.serialFilter} 等机制限制允许反序列化的类。
 * 详见 <a href="https://docs.oracle.com/en/java/javase/17/core/serialization-filtering1.html">
 * serialization filtering</a>。
 *
 * @deprecated 因序列化存在安全风险，本接口已弃用且无替代方案
 */
@Deprecated
public interface ClassResolver {

    /**
     * 按类名解析并返回 {@link Class} 对象。
     *
      * @param className 完全限定类名
     * @return 对应的 {@link Class}
      * @throws ClassNotFoundException 类不存在时抛出
     */
    Class<?> resolve(String className) throws ClassNotFoundException;

}
