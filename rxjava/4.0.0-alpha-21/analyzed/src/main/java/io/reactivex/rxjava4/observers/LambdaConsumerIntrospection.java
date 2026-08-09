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

package io.reactivex.rxjava4.observers;

/**
 * 表明实现类型由多个组件构成，并暴露其行为的内省接口。
 *
 * <p><em>NOTE:</em> 此为只读公共 API，不建议外部实现。
 * <p>History: 2.1.4 - experimental
 * @since 2.2
 */
public interface LambdaConsumerIntrospection {

    /**
     * 判断是否提供了自定义 {@code onError} consumer。
     * @return 若提供了自定义 {@code onError} 实现则为 {@code true}；
     *         若缺少错误 consumer 而使用会抛异常的默认实现则为 {@code false}
     */
    boolean hasCustomOnError();

}
