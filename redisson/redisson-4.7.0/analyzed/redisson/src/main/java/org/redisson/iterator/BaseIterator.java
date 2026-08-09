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
package org.redisson.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.redisson.ScanResult;
import org.redisson.client.RedisClient;
import org.redisson.client.RedisNodeNotFoundException;

/**
 * 基于 Redis SCAN 的同步迭代器抽象基类。
 * <p>
 * 封装游标推进、节点切换与 {@link RedisNodeNotFoundException} 重试逻辑，
 * 子类实现 {@link #iterator} 与 {@link #remove} 完成具体数据结构遍历。
 *
 * @author Nikita Koksharov
 *
 * @param <E> SCAN 原始条目类型
 * @param <V> 对外暴露的值类型
 */
public abstract class BaseIterator<V, E> implements Iterator<V> {

    /** 当前批次内元素迭代器。 */
    private Iterator<E> lastIter;
    /** 下一批 SCAN 游标。 */
    protected String nextIterPos;
    /** 当前连接的 Redis 节点。 */
    protected RedisClient client;

    /** 当前 SCAN 轮次是否已扫完（游标回到初始值）。 */
    private boolean finished;
    /** {@link #remove()} 是否已删除当前元素。 */
    private boolean currentElementRemoved;
    /** {@link #next()} 返回的当前条目引用，供 remove 使用。 */
    protected E value;

    /** 初始化游标为起始值。 */
    protected BaseIterator() {
        nextIterPos = initValue();
    }

    /** SCAN 起始游标，默认 "0"。 */
    protected String initValue() {
        return "0";
    }

    /** 节点切换时的钩子，子类可重置集群相关状态。 */
    protected void reset() {
    }

    /** 同步判断是否还有元素，必要时循环 SCAN 直至有数据或彻底结束。 */
    @Override
    public boolean hasNext() {
        if (lastIter == null || !lastIter.hasNext()) {
            if (finished) {
                currentElementRemoved = false;
                client = null;
                nextIterPos = initValue();

                if (!tryAgain()) {
                    return false;
                }
                finished = false;
            }
            do {
                ScanResult<E> res;
                try {
                    res = iterator(client, nextIterPos);
                } catch (RedisNodeNotFoundException e) {
                    if (client != null) {
                        client = null;
                        nextIterPos = initValue();
                    }
                    reset();
                    res = iterator(client, nextIterPos);
                }
                
                client = res.getRedisClient();
                
                lastIter = res.getValues().iterator();
                nextIterPos = res.getPos();

                if (initValue().equals(res.getPos())) {
                    finished = true;
                    if (res.getValues().isEmpty()) {
                        currentElementRemoved = false;
                        
                        client = null;
                        nextIterPos = initValue();
                        if (tryAgain()) {
                            continue;
                        }
                        
                        return false;
                    }
                }
            } while (!lastIter.hasNext());
        }
        return lastIter.hasNext();
    }
    
    /** 一轮 SCAN 结束且无元素时是否重新开始（如集群 slot 迁移场景）。 */
    protected boolean tryAgain() {
        return false;
    }

    /** 子类实现：执行一次 SCAN 并返回结果与下一游标。 */
    protected abstract ScanResult<E> iterator(RedisClient client, String nextIterPos);

    /** 返回下一元素并缓存 {@link #value} 供 remove 使用。 */
    @Override
    public V next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No such element");
        }

        value = lastIter.next();
        currentElementRemoved = false;
        return getValue(value);
    }

    /** 将 SCAN 条目映射为对外值类型。 */
    protected abstract V getValue(E entry);
    
    /** 删除 {@link #next()} 最后返回的元素（需子类实现 {@link #remove(E)}）。 */
    @Override
    public void remove() {
        if (currentElementRemoved) {
            throw new IllegalStateException("Element been already deleted");
        }
        if (lastIter == null || value == null) {
            throw new IllegalStateException();
        }

        lastIter.remove();
        remove(value);
        currentElementRemoved = true;
    }

    /** 子类实现：从 Redis 删除指定条目。 */
    protected abstract void remove(E value);

}
