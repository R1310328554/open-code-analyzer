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
package org.redisson.api;

import java.util.concurrent.CompletionStage;

/**
 * 表示异步计算结果的接口。
 * <p>扩展 {@link java.util.concurrent.Future} 与 {@link java.util.concurrent.CompletionStage}，Redisson 各异步 API 均返回此类型。
 *
 * @author Nikita Koksharov
 * @param <V> 结果值类型
 */
public interface RFuture<V> extends java.util.concurrent.Future<V>, CompletionStage<V> {

}
