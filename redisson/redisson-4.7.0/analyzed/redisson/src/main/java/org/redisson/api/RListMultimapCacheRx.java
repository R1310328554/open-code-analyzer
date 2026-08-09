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

/**
 * {@link RListMultimapCache} 的 RxJava API。
 * <p>各方法返回 {@link Single} 或 {@link io.reactivex.rxjava3.core.Completable}。
 *
 * @author Marnix Kammer
 * @param <K> 键类型
 * @param <V> 值类型
 */
public interface RListMultimapCacheRx<K, V> extends RListMultimapRx<K, V>, RMultimapCacheRx<K, V> {

}
