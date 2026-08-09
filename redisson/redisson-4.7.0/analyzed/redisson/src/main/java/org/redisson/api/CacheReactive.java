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

import java.util.Map;
import java.util.Set;

import javax.cache.CacheException;
import javax.cache.event.CacheEntryRemovedListener;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheWriter;

import reactor.core.publisher.Mono;

/**
 * JCache（JSR-107）的 Reactive 风格 API 接口。
 * <p>各方法返回 Project Reactor 的 {@link Mono}。
 *
 * @author Nikita Koksharov
 * @param <K> 键类型
 * @param <V> 值类型
 */
public interface CacheReactive<K, V> {

    /**
    * 从缓存中获取指定键的条目。
    *
    * 若启用 read-through 且缓存中不存在该键，
    * 则通过 {@link CacheLoader} 尝试加载。
    *
    * @param key 要返回值的键
    * @return 对应元素；不存在时返回 {@code null}
    * @throws IllegalStateException 缓存已关闭
    * @throws NullPointerException 键为 {@code null}
    * @throws CacheException 读取条目时出错
    */
    Mono<V> get(K key);
    
    /**
    * 根据给定键集合批量从 {@link CacheReactive} 获取条目，
    * 以 {@link Map} 形式返回键值映射。
    *
    * 若启用 read-through 且某键不在缓存中，
    * 将通过 {@link CacheLoader} 尝试加载；加载失败的键不会出现在结果 Map 中。
    *
    * @param keys 要返回值的键集合
    * @return 与给定键关联的条目 Map；未找到的键不在 Map 中
    * @throws NullPointerException {@code keys} 为 {@code null} 或包含 {@code null}
    * @throws IllegalStateException 缓存已关闭
    * @throws CacheException 批量读取条目时出错
    */
    Mono<Map<K, V>> getAll(Set<? extends K> keys);
    
    /**
    * 判断 {@link CacheReactive} 是否包含与给定键相等的映射。
    *
    * @param key 待检查的键
    * @return 存在映射时返回 {@code true}
    * @throws NullPointerException 键为 {@code null}
    * @throws IllegalStateException 缓存已关闭
    * @throws CacheException 访问缓存时出错
    */
    Mono<Boolean> containsKey(K key);
    
    /**
    * 将给定值写入缓存并与键关联。
    *
    * 若键已存在映射，则用新值替换旧值
    * （当且仅当 {@link #containsKey(Object) c.containsKey(k)} 返回 {@code true} 时）。
    *
    * @param key 要写入的键
    * @param value 与键关联的值
    * @return void
    * @throws NullPointerException 键或值为 {@code null}
    * @throws IllegalStateException 缓存已关闭
    * @throws CacheException 写入缓存时出错
    */
    Mono<Void> put(K key, V value);
    
    /**
    * 写入键值并返回被替换的旧值。
    *
    * 若键已存在映射则返回旧值并替换（当且仅当 {@link #containsKey(Object) c.containsKey(k)} 返回 {@code true}）；
    * 若原先不存在映射则返回 {@code null}。
    *
    * @param key 要写入的键
    * @param value 与键关联的值
    * @return 被替换的旧值；原先不存在时返回 {@code null}
    * @throws NullPointerException 键或值为 {@code null}
    * @throws IllegalStateException 缓存已关闭
    * @throws CacheException 写入缓存时出错
    */
    Mono<V> getAndPut(K key, V value);
    
    /**
    * 将给定 Map 中的全部条目复制到 {@link CacheReactive}。
    *
    * 等价于对 Map 中每个键值对调用一次 {@link #put(Object, Object) put(k, v)} on this cache one time for each mapping；
    * 各次写入顺序未定义。
    *
    * 若操作期间缓存或 Map 被并发修改，行为未定义。
    * 默认一致性模式下每次 put 原子，但整体 putAll 不原子，监听器可观察到单次更新。
    *
    * @param map 要复制到缓存的条目 Map
    * @return void
    * @throws NullPointerException Map 为 {@code null} 或含 {@code null} 键/值
    * @throws IllegalStateException 缓存已关闭
    * @throws CacheException 写入缓存时出错
    */
    Mono<Void> putAll(java.util.Map<? extends K, ? extends V> map);
    
    /**
    * 若键尚未关联值，则原子写入键值。
    *
    * @param key 要写入的键
    * @param value 与键关联的值
    * @return 成功写入时返回 {@code true}
    * @throws NullPointerException 键或值为 {@code null}
    * @throws IllegalStateException 缓存已关闭
    * @throws CacheException 写入缓存时出错
    */
    Mono<Boolean> putIfAbsent(K key, V value);
    
    /**
    * 若存在映射则删除指定键的条目。
    *
    * 当且仅当存在键 k 满足 {@code key==null ? k==null : key.equals(k)} 时删除；
    * 删除成功返回 {@code true}，无映射时返回 {@code false}。
    *
    * @param key 要删除映射的键
    * @return 删除成功返回 {@code true}，否则 {@code false}
    * @throws NullPointerException 键为 {@code null}
    * @throws IllegalStateException 缓存已关闭
    * @throws CacheException 访问缓存时出错
    */
    Mono<Boolean> remove(K key);
    
    /**
    * 仅当键当前映射为给定值时，原子删除该映射。
    *
    * @param key 要删除映射的键
    * @param oldValue 期望与键关联的旧值
    * @return 删除成功返回 {@code true}，否则 {@code false}
    * @throws NullPointerException 键为 {@code null}
    * @throws IllegalStateException 缓存已关闭
    * @throws CacheException 访问缓存时出错
    */
    Mono<Boolean> remove(K key, V oldValue);
    
    /**
    * 若键当前有映射，则原子删除并返回其值。
    *
    * @param key 给定键
    * @return 存在映射时返回值，否则 {@code null}
    * @throws NullPointerException 键为 {@code null}
    * @throws IllegalStateException 缓存已关闭
    * @throws CacheException 访问缓存时出错
    */
    Mono<V> getAndRemove(K key);
    
    /**
    * 仅当键当前映射为 {@code oldValue} 时，原子替换为 {@code newValue}。
    *
    * @param key 与旧值关联的键
    * @param oldValue 期望的旧值
    * @param newValue 替换后的新值
    * @return 替换成功返回 {@code true}，否则 {@code false}
    * @throws NullPointerException 键或值为 {@code null}
    * @throws IllegalStateException 缓存已关闭
    * @throws CacheException 访问缓存时出错
    */
    Mono<Boolean> replace(K key, V oldValue, V newValue);
    
    /**
    * 仅当键当前已有映射时，原子替换为新值。
    *
    * @param key 给定键
    * @param value 替换后的新值
    * @return 替换成功返回 {@code true}，否则 {@code false}
    * @throws NullPointerException 键或值为 {@code null}
    * @throws IllegalStateException 缓存已关闭
    * @throws CacheException 访问缓存时出错
    */
    Mono<Boolean> replace(K key, V value);
    
    /**
    * 若键当前有映射，则原子替换并返回旧值。
    *
    * @param key 给定键
    * @param value 替换后的新值
    * @return 被替换的旧值；原先无映射时返回 {@code null}
    * @throws NullPointerException 键或值为 {@code null}
    * @throws IllegalStateException 缓存已关闭
    * @throws CacheException 访问缓存时出错
    */
    Mono<V> getAndReplace(K key, V value);
    
    /**
    * 删除给定键集合对应的条目。
    *
    * 各条目删除顺序未定义。对每个键会触发已注册的
    * {@link CacheEntryRemovedListener}；若为 write-through 缓存还会调用
    * {@link CacheWriter}。键集合为空时不调用 {@link CacheWriter}。
    *
    * @param keys 要删除的键集合
    * @return void
    * @throws NullPointerException {@code keys} 为 {@code null} 或含 {@code null} 键
    * @throws IllegalStateException 缓存已关闭
    * @throws CacheException 访问缓存时出错
    */
    Mono<Void> removeAll(Set<? extends K> keys);

    /**
    * 清空缓存内容，不通知监听器或 {@link CacheWriter}。
    *
    * @return void
    * @throws IllegalStateException 缓存已关闭
    * @throws CacheException 访问缓存时出错
    */
    Mono<Void> clear();

    
}
