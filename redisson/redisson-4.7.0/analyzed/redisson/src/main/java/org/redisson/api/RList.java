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

import org.redisson.api.mapreduce.RCollectionMapReduce;

import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;

/**
 * 分布式并发 {@link java.util.List} 实现。
 * <p>基于 Redis {@code LIST} 命令，支持跨 JVM 共享与并发访问。
 *
 * @author Nikita Koksharov
 * @param <V> 元素类型
 */
public interface RList<V> extends List<V>, RExpirable, RListAsync<V>, RSortable<List<V>>, RandomAccess {

    /**
     * 按指定下标批量加载元素。
     * 
     * @param indexes 元素下标
     * @return 元素列表
     */
    List<V> get(int... indexes);

    /**
     * 返回可在多应用间共享的元素迭代器。
     * 同一对象上多次调用本方法将复用同一共享迭代器。
     * 需独立迭代器请使用 {@linkplain RList#distributedIterator(String, int)}。
     * @param count 每批拉取数量
     * @return 共享元素迭代器
     */
    Iterator<V> distributedIterator(int count);

    /**
     * 返回匹配指定模式的元素迭代器，可在多应用间共享。
     * 同一对象上多次调用将复用同一共享迭代器。
     * 迭代器名称须与 list 名称路由到同一 hash slot。
     * @param count 每批拉取数量
     * @param iteratorName 保存游标的 Redis 对象名
     * @return 共享元素迭代器
     */
    Iterator<V> distributedIterator(String iteratorName, int count);

    /**
     * 返回与此 List 关联的 {@code RMapReduce} 对象。
     * 
     * @param <KOut> 输出键类型
     * @param <VOut> 输出值类型
     * @return MapReduce 实例
     */
    <KOut, VOut> RCollectionMapReduce<V, KOut, VOut> mapReduce();
    
    /**
     * 在 {@code elementToFind} 之后插入 {@code element}。
     * 
     * @param elementToFind 定位元素
     * @param element 待插入元素
     * @return 插入后列表长度
     */
    int addAfter(V elementToFind, V element);
    
    /**
     * 在 {@code elementToFind} 之前插入 {@code element}。
     * 
     * @param elementToFind 定位元素
     * @param element 待插入元素
     * @return 插入后列表长度
     */
    int addBefore(V elementToFind, V element);
    
    /**
     * 在 {@code index} 处设置元素（快速版，不返回旧值）。
     * 比 {@link #set(int, Object)} 更快，但不返回被替换元素。
     * 
     * @param index 下标
     * @param element 新元素
     */
    void fastSet(int index, V element);

    RList<V> subList(int fromIndex, int toIndex);

    /**
     * 一次性读取全部元素。
     *
     * @return 元素列表
     */
    List<V> readAll();

    /**
     * 裁剪列表，仅保留 {@code fromIndex} 到 {@code toIndex}（均含）区间内的元素。
     *
     * @param fromIndex 起始下标
     * @param toIndex 结束下标
     */
    void trim(int fromIndex, int toIndex);

    /**
     * 返回从 0 到 {@code toIndex} 的元素区间（下标从 0 起）。
     * {@code -1} 表示最后一个元素，{@code -2} 表示倒数第二个，依此类推。
     * 
     * @param toIndex 结束下标
     * @return 元素列表
     */
    List<V> range(int toIndex);
    
    /**
     * 返回 {@code fromIndex} 到 {@code toIndex}（均含）的元素区间。
     * 下标从 0 起；{@code -1} 表示最后一个元素，{@code -2} 表示倒数第二个。
     * 
     * @param fromIndex 起始下标
     * @param toIndex 结束下标
     * @return 元素列表
     */
    List<V> range(int fromIndex, int toIndex);
    
    /**
     * 按指定下标快速移除元素（不返回值）。
     * 
     * @param index 元素下标
     */
    void fastRemove(int index);
    
    /**
     * 移除至多 {@code count} 个与 {@code element} 相等的元素。
     * 
     * @param element 待移除元素
     * @param count 最多移除个数
     * @return 至少移除一个时为 {@code true}；未找到时为 {@code false}
     */
    boolean remove(Object element, int count);

    /**
     * 注册 List 对象事件监听器。
     *
     * @see org.redisson.api.listener.TrackingListener
     * @see org.redisson.api.ExpiredObjectListener
     * @see org.redisson.api.DeletedObjectListener
     * @see org.redisson.api.listener.ListAddListener
     * @see org.redisson.api.listener.ListInsertListener
     * @see org.redisson.api.listener.ListSetListener
     * @see org.redisson.api.listener.ListRemoveListener
     * @see org.redisson.api.listener.ListTrimListener
     *
     * @param listener 事件监听器
     * @return 监听器 ID
     */
    int addListener(ObjectListener listener);
}
