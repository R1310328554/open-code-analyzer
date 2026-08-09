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

import java.lang.reflect.AnnotatedElement;

/**
 * GraalVM Native Image 替代：ByteBuddy 形式类型变量注解解析在 Native 模式下不可用。
 */
@TargetClass(className = "net.bytebuddy.description.type.TypeDescription$Generic$AnnotationReader$ForTypeVariableBoundType$OfFormalTypeVariable")
final class OfFormalTypeVariableSubstitute {

    /** Native 模式下返回 {@code null}，跳过 ByteBuddy 注解读取。 */
    @Substitute
    public AnnotatedElement resolve() {
        return null;
    }

}

/** GraalVM 替代：类型变量上界注解解析的占位实现。 */
@TargetClass(className = "net.bytebuddy.description.type.TypeDescription$Generic$AnnotationReader$ForTypeVariableBoundType")
final class ForTypeVariableBoundTypeSubstitute {

    /** Native 模式下返回 {@code null}。 */
    @Substitute
    protected AnnotatedElement resolve(AnnotatedElement annotatedElement) {
        return null;
    }

}
