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
package org.redisson.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记返回 Reactor {@code Mono} 的方法：当底层结果为空的 {@link java.util.Map} 或
 * {@link java.util.Collection} 时，{@code Mono} 应以空完成结束，而不发出 {@code onNext} 信号。
 *
 * <p>未加此注解时，返回 {@code Mono<Map<K, V>>} 的方法在底层得到空 Map 时会通过 {@code onNext}
 * 发出该空 Map 再完成。加上此注解后，相同操作会以空 {@code Mono} 完成，便于响应式管道按
 * “零或一”语义使用 {@code switchIfEmpty}、{@code defaultIfEmpty}、{@code flatMap} 等算子。
 *
 * @author Nikita Koksharov
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EmptyAsAbsent {
}