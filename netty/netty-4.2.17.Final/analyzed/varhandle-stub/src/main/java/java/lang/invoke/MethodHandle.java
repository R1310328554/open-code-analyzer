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
package java.lang.invoke;

/**
 * {@link MethodHandle} 编译桩：供 Java 8 编译期识别签名多态注解。
 * <p>仅用于 varhandle-stub 模块在 JDK 8 下通过编译； 运行时不会加载此桩类（{@code java.lang.invoke} 为特权包）。</p>
 */
public class MethodHandle {
    @java.lang.annotation.Target({java.lang.annotation.ElementType.METHOD})
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    /**
     * 标记签名多态方法/本机方法，配合 JLS 15.12.3 在编译期解析调用类型。
     * <p>供 {@link VarHandle} 桩上的 native 方法使用。</p>
     */
    @interface PolymorphicSignature {
    }
}
