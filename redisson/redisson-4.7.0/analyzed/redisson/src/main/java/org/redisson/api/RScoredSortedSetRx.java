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

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.redisson.api.RScoredSortedSet.Aggregate;
import org.redisson.client.protocol.RankedEntry;
import org.redisson.client.protocol.ScoredEntry;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 有序集合 {@link RScoredSortedSet} 的 RxJava3 API。
 * <p>封装 ZADD/ZREM、ZRANGE、ZUNION/ZINTER、阻塞 poll 及 MapReduce 等操作。
 *
 * @author Nikita Koksharov
 * @param <V> 成员类型
 */
public interface RScoredSortedSetRx<V> extends RExpirableRx, RSortableRx<Set<V>> {

    /**
     * 移除and returns first available tail element of <b>any</b> sorted set,。
     * waiting up to the specified wait time if necessary for an element to become available
     * in any of defined sorted sets <b>including</b> this one.
     * <p>
     * 需要 <b>Redis 5.0.0 及以上</b>。
     * 
     * @param queueNames 有序集合/队列名称
     * @param timeout 等待超时
     *        {@code unit}
     * @param unit 时间单位
     *        {@code timeout} parameter
     * @return the tail element, or {@code null} if all sorted sets are empty 
     */
    Maybe<V> pollLastFromAny(long timeout, TimeUnit unit, String... queueNames);

    /**
     * 移除and returns first available tail elements of <b>any</b> sorted set,。
     * waiting up to the specified wait time if necessary for elements to become available
     * in any of defined sorted sets <b>including</b> this one.
     * <p>
     * 需要 <b>Redis 7.0.0 及以上</b>。
     *
     * @param duration 等待时长
     * @param count 元素数量
     * @param queueNames 有序集合/队列名称
     * @return the tail elements
     */
    Maybe<List<V>> pollLastFromAny(Duration duration, int count, String... queueNames);

    /**
     * 移除and returns first available tail elements。
     * of <b>any</b> sorted set <b>including</b> this one.
     * <p>
     * 需要 <b>Redis 7.0.0 及以上</b>。
     *
     * @param count 元素数量
     * @param queueNames 有序集合/队列名称
     * @return the tail elements
     */
    Maybe<List<V>> pollLastFromAny(int count, String... queueNames);

    /**
     * 移除and returns first available tail entries。
     * of <b>any</b> sorted set <b>including</b> this one.
     * <p>
     * 需要 <b>Redis 7.0.0 及以上</b>。
     *
     * @param count 条目数量
     * @param queueNames 有序集合/队列名称
     * @return the head entries
     */
    Maybe<Map<String, Map<V, Double>>> pollLastEntriesFromAny(int count, String... queueNames);

    /**
     * 移除and returns first available tail entries of <b>any</b> sorted set,。
     * waiting up to the specified wait time if necessary for elements to become available
     * in any of defined sorted sets <b>including</b> this one.
     * <p>
     * 需要 <b>Redis 7.0.0 及以上</b>。
     *
     * @param duration 等待时长
     * @param count 条目数量
     * @param queueNames 有序集合/队列名称
     * @return the tail entries
     */
    Maybe<Map<String, Map<V, Double>>> pollLastEntriesFromAny(Duration duration, int count, String... queueNames);

    /**
     * 移除and returns first available head element of <b>any</b> sorted set,。
     * waiting up to the specified wait time if necessary for an element to become available
     * in any of defined sorted sets <b>including</b> this one.
     * <p>
     * 需要 <b>Redis 5.0.0 及以上</b>。
     * 
     * @param queueNames 有序集合/队列名称
     * @param timeout 等待超时
     *        {@code unit}
     * @param unit 时间单位
     *        {@code timeout} parameter
     * @return the head element, or {@code null} if all sorted sets are empty
     *  
     */
    Maybe<V> pollFirstFromAny(long timeout, TimeUnit unit, String... queueNames);

    /**
     * 移除and returns first available head elements of <b>any</b> sorted set,。
     * waiting up to the specified wait time if necessary for elements to become available
     * in any of defined sorted sets <b>including</b> this one.
     * <p>
     * 需要 <b>Redis 7.0.0 及以上</b>。
     *
     * @param duration 等待时长
     * @param count 元素数量
     * @param queueNames 有序集合/队列名称
     * @return the head elements
     */
    Maybe<List<V>> pollFirstFromAny(Duration duration, int count, String... queueNames);

    /**
     * 移除and returns first available head elements。
     * of <b>any</b> sorted set <b>including</b> this one.
     * <p>
     * 需要 <b>Redis 7.0.0 及以上</b>。
     *
     * @param count 元素数量
     * @param queueNames 有序集合/队列名称
     * @return the head elements
     */
    Maybe<List<V>> pollFirstFromAny(int count, String... queueNames);

    /**
     * 移除and returns first available head entries。
     * of <b>any</b> sorted set <b>including</b> this one.
     * <p>
     * 需要 <b>Redis 7.0.0 及以上</b>。
     *
     * @param count 条目数量
     * @param queueNames 有序集合/队列名称
     * @return the head elements
     */
    Maybe<Map<String, Map<V, Double>>> pollFirstEntriesFromAny(int count, String... queueNames);

    /**
     * 移除and returns first available head entries of <b>any</b> sorted set,。
     * waiting up to the specified wait time if necessary for elements to become available
     * in any of defined sorted sets <b>including</b> this one.
     * <p>
     * 需要 <b>Redis 7.0.0 及以上</b>。
     *
     * @param duration 等待时长
     * @param count 条目数量
     * @param queueNames 有序集合/队列名称
     * @return the head entries
     */
    Maybe<Map<String, Map<V, Double>>> pollFirstEntriesFromAny(Duration duration, int count, String... queueNames);

    /**
     * 移除and returns the head element or {@code null} if this sorted set is empty.。
     * <p>
     * 需要 <b>Redis 5.0.0 及以上</b>。
     *
     * @param timeout 等待超时
     *        {@code unit}
     * @param unit 时间单位
     *        {@code timeout} parameter
     * @return the head element, 
     *         or {@code null} if this sorted set is empty
     */
    @Deprecated
    Maybe<V> pollFirst(long timeout, TimeUnit unit);

    /**
     * 移除and returns the head element or {@code null} if this sorted set is empty.。
     * <p>
     * 需要 <b>Redis 5.0.0 及以上</b>。
     *
     * @param duration 等待时长
     * @return the head element,
     *         or {@code null} if this sorted set is empty
     */
    Maybe<V> pollFirst(Duration duration);

    /**
     * 移除and returns the head elements.。
     * <p>
     * 需要 <b>Redis 7.0.0 及以上</b>。
     *
     * @param duration 等待时长
     * @param count 元素数量
     * @return the head element
     */
    Maybe<List<V>> pollFirst(Duration duration, int count);

    /**
     * 移除and returns the tail element or {@code null} if this sorted set is empty.。
     * <p>
     * 需要 <b>Redis 5.0.0 及以上</b>。
     *
     * @param timeout 等待超时
     *        {@code unit}
     * @param unit 时间单位
     *        {@code timeout} parameter
     * @return the tail element or {@code null} if this sorted set is empty
     */
    @Deprecated
    Maybe<V> pollLast(long timeout, TimeUnit unit);

    /**
     * 移除and returns the tail element or {@code null} if this sorted set is empty.。
     * <p>
     * 需要 <b>Redis 5.0.0 及以上</b>。
     *
     * @param duration 等待时长
     * @return the tail element or {@code null} if this sorted set is empty
     */
    Maybe<V> pollLast(Duration duration);

    /**
     * 移除and returns the tail elements.。
     * <p>
     * 需要 <b>Redis 7.0.0 及以上</b>。
     *
     * @param duration 等待时长
     * @return the tail elements
     */
    Maybe<List<V>> pollLast(Duration duration, int count);

    /**
     * 移除and returns the head elements of this sorted set.。
     *
     * @param count - elements amount
     * @return the head elements of this sorted set
     */
    Maybe<Collection<V>> pollFirst(int count);

    /**
     * 移除and returns the tail elements of this sorted set.。
     *
     * @param count - elements amount
     * @return the tail elements of this sorted set
     */
    Maybe<Collection<V>> pollLast(int count);

    /**
     * 移除and returns the head element or {@code null} if this sorted set is empty.。
     *
     * @return the head element, 
     *         or {@code null} if this sorted set is empty
     */
    Maybe<V> pollFirst();


    /**
     * 移除and returns the head entry (value and its score) or {@code null} if this sorted set is empty.。
     *
     * @return the head entry,
     * or {@code null} if this sorted set is empty
     */
    Maybe<ScoredEntry<V>> pollFirstEntry();

    /**
     * 移除and returns the head entries (value and its score) of this sorted set.。
     *
     * @param count 条目数量
     * @return the head entries of this sorted set
     */
    Maybe<List<ScoredEntry<V>>> pollFirstEntries(int count);

    /**
     * 移除and returns the head entries (value and its score).。
     * <p>
     * 需要 <b>Redis 7.0.0 及以上</b>。
     *
     * @param duration 等待时长
     * @param count 条目数量
     * @return the head entries
     */
    Maybe<List<ScoredEntry<V>>> pollFirstEntries(Duration duration, int count);

    /**
     * 移除and returns the tail element or {@code null} if this sorted set is empty.。
     *
     * @return the tail element or {@code null} if this sorted set is empty
     */
    Maybe<V> pollLast();

    /**
     * 移除and returns the tail entry (value and its score) or {@code null} if this sorted set is empty.。
     *
     * @return the tail entry or {@code null} if this sorted set is empty
     */
    Maybe<ScoredEntry<V>> pollLastEntry();

    /**
     * 移除and returns the tail entries (value and its score) of this sorted set.。
     *
     * @param count 条目数量
     * @return the tail entries of this sorted set
     */
    Maybe<List<ScoredEntry<V>>> pollLastEntries(int count);

    /**
     * 移除and returns the head entries (value and its score).。
     * <p>
     * 需要 <b>Redis 7.0.0 及以上</b>。
     *
     * @param duration 等待时长
     * @param count 条目数量
     * @return the tail entries
     */
    Maybe<List<ScoredEntry<V>>> pollLastEntries(Duration duration, int count);

    /**
     * 返回the head element or {@code null} if this sorted set is empty.。
     *
     * @return the head element or {@code null} if this sorted set is empty
     */
    Maybe<V> first();

    /**
     * 返回the head entry (value and its score) or {@code null} if this sorted set is empty.。
     *
     * @return the head entry or {@code null} if this sorted set is empty
     */
    Maybe<ScoredEntry<V>> firstEntry();

    /**
     * 返回the tail element or {@code null} if this sorted set is empty.。
     *
     * @return the tail element or {@code null} if this sorted set is empty
     */
    Maybe<V> last();

    /**
     * 返回the tail entry (value and its score) or {@code null} if this sorted set is empty.。
     *
     * @return the tail entry or {@code null} if this sorted set is empty
     */
    Maybe<ScoredEntry<V>> lastEntry();

    /**
     * 返回score of the head element or returns {@code null} if this sorted set is empty.。
     *
     * @return the tail element or {@code null} if this sorted set is empty
     */
    Maybe<Double> firstScore();

    /**
     * 返回score of the tail element or returns {@code null} if this sorted set is empty.。
     *
     * @return the tail element or {@code null} if this sorted set is empty
     */
    Maybe<Double> lastScore();

    /**
     * 返回random element from this sorted set。
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @return random element
     */
    Maybe<V> random();

    /**
     * 返回random elements from this sorted set limited by <code>count</code>。
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @param count - values amount to return
     * @return random elements
     */
    Maybe<Collection<V>> random(int count);

    /**
     * 返回random entries from this sorted set limited by <code>count</code>.。
     * Each map entry uses element as key and score as value.
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @param count - entries amount to return
     * @return random entries
     */
    Maybe<Map<V, Double>> randomEntries(int count);

    /**
     * 返回an iterator over elements in this set.。
     * If <code>pattern</code> is not null then only elements match this pattern are loaded.
     * 
     * @param pattern - search pattern
     * @return iterator
     */
    Flowable<V> iterator(String pattern);
    
    /**
     * 返回an iterator over elements in this set.。
     * Elements are loaded in batch. Batch size is defined by <code>count</code> param. 
     * 
     * @param count - size of elements batch
     * @return iterator
     */
    Flowable<V> iterator(int count);
    
    /**
     * 返回an iterator over elements in this set.。
     * Elements are loaded in batch. Batch size is defined by <code>count</code> param.
     * If pattern is not null then only elements match this pattern are loaded.
     * 
     * @param pattern - search pattern
     * @param count - size of elements batch
     * @return iterator
     */
    Flowable<V> iterator(String pattern, int count);

    /**
     * 返回an iterator over elements in this set.。
     *
     * @return iterator
     */
    Flowable<V> iterator();

    /**
     * 返回an iterator over entries (value and its score) in this set.。
     *
     * @return iterator
     */
    Flowable<ScoredEntry<V>> entryIterator();

    /**
     * 返回an iterator over entries (value and its score) in this set.。
     * If <code>pattern</code> is not null then only entries match this pattern are loaded.
     *
     * @param pattern search pattern
     * @return iterator
     */
    Flowable<ScoredEntry<V>> entryIterator(String pattern);

    /**
     * 返回an iterator over entries (value and its score) in this set.。
     * Entries are loaded in batch. Batch size is defined by <code>count</code> param.
     *
     * @param count size of elements batch
     * @return iterator
     */
    Flowable<ScoredEntry<V>> entryIterator(int count);

    /**
     * 返回an iterator over entries (value and its score) in this set.。
     * Entries are loaded in batch. Batch size is defined by <code>count</code> param.
     * If pattern is not null then only entries match this pattern are loaded.
     *
     * @param pattern search pattern
     * @param count size of entries batch
     * @return iterator
     */
    Flowable<ScoredEntry<V>> entryIterator(String pattern, int count);

    /**
     * 移除values by score range.。
     * 
     * @param startScore 起始分数. 
     *                     Use <code>Double.POSITIVE_INFINITY</code> or <code>Double.NEGATIVE_INFINITY</code> 
     *                     to define infinity numbers
     * @param startScoreInclusive - start score inclusive
     * @param endScore 结束分数
     *                     Use <code>Double.POSITIVE_INFINITY</code> or <code>Double.NEGATIVE_INFINITY</code> 
     *                     to define infinity numbers
     * 
     * @param endScoreInclusive - end score inclusive
     * @return number of elements removed
     */
    Single<Integer> removeRangeByScore(double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive);

    /**
     * 移除values by rank range. Indexes are zero based.。
     * <code>-1</code> means the highest score, <code>-2</code> means the second highest score.
     * 
     * @param startIndex 起始索引 
     * @param endIndex 结束索引
     * @return number of elements removed
     */
    Single<Integer> removeRangeByRank(int startIndex, int endIndex);

    /**
     * 返回rank of value, with the scores ordered from low to high.。
     * 
     * @param o - object
     * @return rank or <code>null</code> if value does not exist
     */
    Maybe<Integer> rank(V o);

    /**
     * 返回rank and score of specified <code>value</code>,。
     * with the ranks ordered from low to high.
     *
     * @param value object
     * @return ranked entry or <code>null</code> if value does not exist
     */
    Maybe<RankedEntry<V>> rankEntry(V value);

    /**
     * 返回rank of value, with the scores ordered from high to low.。
     * 
     * @param o - object
     * @return rank or <code>null</code> if value does not exist
     */
    Maybe<Integer> revRank(V o);

    /**
     * 返回rank and score of specified <code>value</code>,。
     * with the ranks ordered from high to low.
     *
     * @param value object
     * @return ranked entry or <code>null</code> if value does not exist
     */
    Maybe<RankedEntry<V>> revRankEntry(V value);

    /**
     * 返回ranks of elements, with the scores ordered from high to low.。
     *
     * @param elements - elements
     * @return ranks or <code>null</code> if value does not exist
     */
    Single<List<Integer>> revRank(Collection<V> elements);

    /**
     * 返回score of element or <code>null</code> if it doesn't exist.。
     * 
     * @param o - element
     * @return score
     */
    Maybe<Double> getScore(V o);

    /**
     * 返回scores of elements.。
     *
     * @param elements - elements
     * @return element scores
     */
    Single<List<Double>> getScore(Collection<V> elements);

    /**
     * 添加element to this set, overrides previous score if it has been already added.。
     *
     * @param score 成员分数
     * @param object - object itself
     * @return <code>true</code> if element has added and <code>false</code> if not.
     */
    Single<Boolean> add(double score, V object);

    /**
     * 添加all elements contained in the specified map to this sorted set.。
     * Map contains of score mapped by object. 
     * 
     * @param objects - map of elements to add
     * @return amount of added elements, not including already existing in this sorted set
     */
    Single<Integer> addAll(Map<V, Double> objects);

    /**
     * 批量添加仅当元素此前均不存在时生效的元素。
     * <p>
     * Requires <b>Redis 3.0.2 and higher.</b>
     *
     * @param objects map of elements to add
     * @return amount of added elements
     */
    Single<Integer> addAllIfAbsent(Map<V, Double> objects);

    /**
     * 批量添加仅当元素已存在时更新 TTL 的条目。
     * <p>
     * Requires <b>Redis 3.0.2 and higher.</b>
     *
     * @param objects map of elements to add
     * @return amount of added elements
     */
    Single<Integer> addAllIfExist(Map<V, Double> objects);

    /**
     * 添加elements to this set only if new scores greater than current score of existed elements.。
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @param objects map of elements to add
     * @return amount of added elements
     */
    Single<Integer> addAllIfGreater(Map<V, Double> objects);

    /**
     * 添加elements to this set only if new scores less than current score of existed elements.。
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @param objects map of elements to add
     * @return amount of added elements
     */
    Single<Integer> addAllIfLess(Map<V, Double> objects);

    /**
     * 添加element to this set, overrides previous score if it has been already added.。
     * Finally return the rank of the item
     * 
     * @param score 成员分数
     * @param object - object itself
     * @return rank
     */
    Single<Integer> addAndGetRank(double score, V object);

    /**
     * 添加element to this set, overrides previous score if it has been already added.。
     * Finally return the reverse rank of the item
     * 
     * @param score 成员分数
     * @param object - object itself
     * @return reverse rank
     */
    Single<Integer> addAndGetRevRank(double score, V object);

    /**
     * 添加elements to this set, overrides previous score if it has been already added.。
     * Finally returns reverse rank list of the items
     * @param map - map of object and scores, make sure to use an ordered map
     * @return collection of reverse ranks
     */
    Single<List<Integer>> addAndGetRevRank(Map<? extends V, Double> map);
    
    /**
     * 请改用 {@link #addIfAbsent(double, Object)}。（Redisson API）。
     *
     * @param score 成员分数
     * @param object - object itself
     * @return <code>true</code> if element has added and <code>false</code> if not.
     */
    @Deprecated
    Single<Boolean> tryAdd(double score, V object);

    /**
     * 仅当元素此前不存在时添加到集合。
     * <p>
     * Requires <b>Redis 3.0.2 and higher.</b>
     *
     * @param score 成员分数
     * @param object - object itself
     * @return <code>true</code> if element added and <code>false</code> if not.
     */
    Single<Boolean> addIfAbsent(double score, V object);

    /**
     * 仅当元素已存在时更新其 TTL 并保留在集合中。
     * <p>
     * Requires <b>Redis 3.0.2 and higher.</b>
     *
     * @param score 成员分数
     * @param object - object itself
     * @return <code>true</code> if element added and <code>false</code> if not.
     */
    Single<Boolean> addIfExists(double score, V object);

    /**
     * 添加element to this set only if new score less than current score of existed element.。
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @param score 成员分数
     * @param object - object itself
     * @return <code>true</code> if element added and <code>false</code> if not.
     */
    Single<Boolean> addIfLess(double score, V object);

    /**
     * 添加element to this set only if new score greater than current score of existed element.。
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @param score 成员分数
     * @param object - object itself
     * @return <code>true</code> if element added and <code>false</code> if not.
     */
    Single<Boolean> addIfGreater(double score, V object);

    /**
     * Replaces a previous <code>oldObject</code> with a <code>newObject</code>.
     * Returns <code>false</code> if previous object doesn't exist.
     *
     * @param oldObject old object
     * @param newObject new object
     * @return <code>true</code> if object has been replaced otherwise <code>false</code>.
     */
    Single<Boolean> replace(V oldObject, V newObject);

    /**
     * 移除a single instance of the specified element from this。
     * sorted set, if it is present.
     *
     * @param object element to be removed from this sorted set, if present
     * @return <code>true</code> if an element was removed as a result of this call
     */
    Single<Boolean> remove(V object);

    /**
     * 返回size of this set.。
     * 
     * @return size
     */
    Single<Integer> size();
    
    /**
     * 返回<code>true</code> if this sorted set contains encoded state of the specified element.。
     *
     * @param o element whose presence in this collection is to be tested
     * @return <code>true</code> if this sorted set contains the specified
     *         element and <code>false</code> otherwise
     */
    Single<Boolean> contains(V o);

    /**
     * 返回<code>true</code> if this sorted set contains all of the elements。
     * in encoded state in the specified collection.
     *
     * @param  c collection to be checked for containment in this sorted set
     * @return <code>true</code> if this sorted set contains all of the elements
     *         in the specified collection
     */
    Single<Boolean> containsAll(Collection<?> c);

    /**
     * 移除all of this sorted set's elements that are also contained in the。
     * specified collection.
     *
     * @param c sorted set containing elements to be removed from this collection
     * @return <code>true</code> if this sorted set changed as a result of the
     *         call
     */
    Single<Boolean> removeAll(Collection<?> c);

    /**
     * 有序集合（ZSET）相关操作：Retains only the elements in this sorted set that are contained in the。
     * specified collection.
     *
     * @param c collection containing elements to be retained in this collection
     * @return <code>true</code> if this sorted set changed as a result of the call
     */
    Single<Boolean> retainAll(Collection<?> c);

    /**
     * Increases score of specified element by value.
     * 
     * @param element - element whose score needs to be increased
     * @param value - value
     * @return updated score of element
     */
    Single<Double> addScore(V element, Number value);

    /**
     * 添加score to element and returns its reverse rank。
     * 
     * @param object - object itself
     * @param value - object score
     * @return reverse rank
     */
    Single<Integer> addScoreAndGetRevRank(V object, Number value);
    
    /**
     * 添加score to element and returns its rank。
     * 
     * @param object - object itself
     * @param value - object score
     * @return rank
     */
    Single<Integer> addScoreAndGetRank(V object, Number value);

    /**
     * Set 相关操作：Stores to defined ScoredSortedSet values by rank range. Indexes are zero based.。
     * <code>-1</code> means the highest score, <code>-2</code> means the second highest score.
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @param startIndex 起始索引
     * @param endIndex 结束索引
     * @return elements
     */
    Single<Integer> rangeTo(String destName, int startIndex, int endIndex);

    /**
     * Set 相关操作：Stores to defined ScoredSortedSet values between <code>startScore</code> and <code>endScore</code>.。
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @param startScore 起始分数.
     *                     Use <code>Double.POSITIVE_INFINITY</code> or <code>Double.NEGATIVE_INFINITY</code>
     *                     to define infinity numbers
     * @param startScoreInclusive - start score inclusive
     * @param endScore 结束分数
     *                     Use <code>Double.POSITIVE_INFINITY</code> or <code>Double.NEGATIVE_INFINITY</code>
     *                     to define infinity numbers
     *
     * @param endScoreInclusive - end score inclusive
     * @return values
     */
    Single<Integer> rangeTo(String destName, double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive);

    /**
     * Set 相关操作：Stores to defined ScoredSortedSet values between <code>startScore</code> and <code>endScore</code>.。
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @param startScore 起始分数.
     *                     Use <code>Double.POSITIVE_INFINITY</code> or <code>Double.NEGATIVE_INFINITY</code>
     *                     to define infinity numbers
     * @param startScoreInclusive - start score inclusive
     * @param endScore 结束分数
     *                     Use <code>Double.POSITIVE_INFINITY</code> or <code>Double.NEGATIVE_INFINITY</code>
     *                     to define infinity numbers
     *
     * @param endScoreInclusive - end score inclusive
     * @param offset - offset of sorted data
     * @param count - amount of sorted data
     * @return values
     */
    Single<Integer> rangeTo(String destName, double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive, int offset, int count);

    /**
     * Set 相关操作：Stores to defined ScoredSortedSet values in reversed order by rank range. Indexes are zero based.。
     * <code>-1</code> means the highest score, <code>-2</code> means the second highest score.
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @param startIndex 起始索引
     * @param endIndex 结束索引
     * @return elements
     */
    Single<Integer> revRangeTo(String destName, int startIndex, int endIndex);

    /**
     * Set 相关操作：Stores to defined ScoredSortedSet values in reversed order between <code>startScore</code> and <code>endScore</code>.。
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @param startScore 起始分数.
     *                     Use <code>Double.POSITIVE_INFINITY</code> or <code>Double.NEGATIVE_INFINITY</code>
     *                     to define infinity numbers
     * @param startScoreInclusive - start score inclusive
     * @param endScore 结束分数
     *                     Use <code>Double.POSITIVE_INFINITY</code> or <code>Double.NEGATIVE_INFINITY</code>
     *                     to define infinity numbers
     *
     * @param endScoreInclusive - end score inclusive
     * @return values
     */
    Single<Integer> revRangeTo(String destName, double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive);

    /**
     * Set 相关操作：Stores to defined ScoredSortedSet values in reversed order between <code>startScore</code> and <code>endScore</code>.。
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @param startScore 起始分数.
     *                     Use <code>Double.POSITIVE_INFINITY</code> or <code>Double.NEGATIVE_INFINITY</code>
     *                     to define infinity numbers
     * @param startScoreInclusive - start score inclusive
     * @param endScore 结束分数
     *                     Use <code>Double.POSITIVE_INFINITY</code> or <code>Double.NEGATIVE_INFINITY</code>
     *                     to define infinity numbers
     *
     * @param endScoreInclusive - end score inclusive
     * @param offset - offset of sorted data
     * @param count - amount of sorted data
     * @return values
     */
    Single<Integer> revRangeTo(String destName, double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive, int offset, int count);

    /**
     * 返回values by rank range. Indexes are zero based.。
     * <code>-1</code> means the highest score, <code>-2</code> means the second highest score.
     * 
     * @param startIndex 起始索引 
     * @param endIndex 结束索引
     * @return elements
     */
    Maybe<Collection<V>> valueRange(int startIndex, int endIndex);

    /**
     * 返回entries (value and its score) by rank range. Indexes are zero based.。
     * <code>-1</code> means the highest score, <code>-2</code> means the second highest score.
     * 
     * @param startIndex 起始索引 
     * @param endIndex 结束索引
     * @return entries
     */
    Maybe<Collection<ScoredEntry<V>>> entryRange(int startIndex, int endIndex);

    /**
     * 返回all values between <code>startScore</code> and <code>endScore</code>.。
     * 
     * @param startScore 起始分数. 
     *                     Use <code>Double.POSITIVE_INFINITY</code> or <code>Double.NEGATIVE_INFINITY</code> 
     *                     to define infinity numbers
     * @param startScoreInclusive - start score inclusive
     * @param endScore 结束分数
     *                     Use <code>Double.POSITIVE_INFINITY</code> or <code>Double.NEGATIVE_INFINITY</code> 
     *                     to define infinity numbers
     * 
     * @param endScoreInclusive - end score inclusive
     * @return values
     */
    Maybe<Collection<V>> valueRange(double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive);

    /**
     * 返回all entries (value and its score) between <code>startScore</code> and <code>endScore</code>.。
     * 
     * @param startScore 起始分数. 
     *                     Use <code>Double.POSITIVE_INFINITY</code> or <code>Double.NEGATIVE_INFINITY</code> 
     *                     to define infinity numbers
     * @param startScoreInclusive - start score inclusive
     * @param endScore 结束分数
     *                     Use <code>Double.POSITIVE_INFINITY</code> or <code>Double.NEGATIVE_INFINITY</code> 
     *                     to define infinity numbers
     * 
     * @param endScoreInclusive - end score inclusive
     * @return entries
     */
    Maybe<Collection<ScoredEntry<V>>> entryRange(double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive);

    /**
     * 返回all values between <code>startScore</code> and <code>endScore</code>.。
     * 
     * @param startScore 起始分数. 
     *                     Use <code>Double.POSITIVE_INFINITY</code> or <code>Double.NEGATIVE_INFINITY</code> 
     *                     to define infinity numbers
     * @param startScoreInclusive - start score inclusive
     * @param endScore 结束分数
     *                     Use <code>Double.POSITIVE_INFINITY</code> or <code>Double.NEGATIVE_INFINITY</code> 
     *                     to define infinity numbers
     * 
     * @param endScoreInclusive - end score inclusive
     * @param offset - offset of sorted data
     * @param count - amount of sorted data
     * @return values
     */
    Maybe<Collection<V>> valueRange(double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive, int offset, int count);

    /**
     * 返回all entries (value and its score) between <code>startScore</code> and <code>endScore</code>.。
     * 
     * @param startScore 起始分数. 
     *                     Use <code>Double.POSITIVE_INFINITY</code> or <code>Double.NEGATIVE_INFINITY</code> 
     *                     to define infinity numbers
     * @param startScoreInclusive - start score inclusive
     * @param endScore 结束分数
     *                     Use <code>Double.POSITIVE_INFINITY</code> or <code>Double.NEGATIVE_INFINITY</code> 
     *                     to define infinity numbers
     * 
     * @param endScoreInclusive - end score inclusive
     * @param offset - offset of sorted data
     * @param count - amount of sorted data
     * @return entries
     */
    Maybe<Collection<ScoredEntry<V>>> entryRange(double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive, int offset, int count);

    /**
     * 返回values by rank range in reverse order. Indexes are zero based.。
     * <code>-1</code> means the highest score, <code>-2</code> means the second highest score.
     * 
     * @param startIndex 起始索引 
     * @param endIndex 结束索引
     * @return elements
     */
    Maybe<Collection<V>> valueRangeReversed(int startIndex, int endIndex);
    
    /**
     * 返回all values between <code>startScore</code> and <code>endScore</code> in reversed order.。
     * 
     * @param startScore 起始分数. 
     *                     Use <code>Double.POSITIVE_INFINITY</code> or <code>Double.NEGATIVE_INFINITY</code> 
     *                     to define infinity numbers
     * @param startScoreInclusive - start score inclusive
     * @param endScore 结束分数
     *                     Use <code>Double.POSITIVE_INFINITY</code> or <code>Double.NEGATIVE_INFINITY</code> 
     *                     to define infinity numbers
     * 
     * @param endScoreInclusive - end score inclusive
     * @return values
     */
    Maybe<Collection<V>> valueRangeReversed(double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive);

    /**
     * 返回all values between <code>startScore</code> and <code>endScore</code> in reversed order.。
     * 
     * @param startScore 起始分数. 
     *                     Use <code>Double.POSITIVE_INFINITY</code> or <code>Double.NEGATIVE_INFINITY</code> 
     *                     to define infinity numbers
     * @param startScoreInclusive - start score inclusive
     * @param endScore 结束分数
     *                     Use <code>Double.POSITIVE_INFINITY</code> or <code>Double.NEGATIVE_INFINITY</code> 
     *                     to define infinity numbers
     * 
     * @param endScoreInclusive - end score inclusive
     * @param offset - offset of sorted data
     * @param count - amount of sorted data
     * @return values
     */
    Maybe<Collection<V>> valueRangeReversed(double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive, int offset, int count);
    
    /**
     * 返回entries (value and its score) by rank range in reverse order. Indexes are zero based.。
     * <code>-1</code> means the highest score, <code>-2</code> means the second highest score.
     * 
     * @param startIndex 起始索引 
     * @param endIndex 结束索引
     * @return entries
     */
    Maybe<Collection<ScoredEntry<V>>> entryRangeReversed(int startIndex, int endIndex);
    
    /**
     * 返回all entries (value and its score) between <code>startScore</code> and <code>endScore</code> in reversed order.。
     * 
     * @param startScore 起始分数. 
     *                     Use <code>Double.POSITIVE_INFINITY</code> or <code>Double.NEGATIVE_INFINITY</code> 
     *                     to define infinity numbers
     * @param startScoreInclusive - start score inclusive
     * @param endScore 结束分数
     *                     Use <code>Double.POSITIVE_INFINITY</code> or <code>Double.NEGATIVE_INFINITY</code> 
     *                     to define infinity numbers
     * 
     * @param endScoreInclusive - end score inclusive
     * @return entries
     */
    Maybe<Collection<ScoredEntry<V>>> entryRangeReversed(double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive);
    
    /**
     * 返回all entries (value and its score) between <code>startScore</code> and <code>endScore</code> in reversed order.。
     * 
     * @param startScore 起始分数. 
     *                     Use <code>Double.POSITIVE_INFINITY</code> or <code>Double.NEGATIVE_INFINITY</code> 
     *                     to define infinity numbers
     * @param startScoreInclusive - start score inclusive
     * @param endScore 结束分数
     *                     Use <code>Double.POSITIVE_INFINITY</code> or <code>Double.NEGATIVE_INFINITY</code> 
     *                     to define infinity numbers
     * 
     * @param endScoreInclusive - end score inclusive
     * @param offset - offset of sorted data
     * @param count - amount of sorted data
     * @return entries
     */
    Maybe<Collection<ScoredEntry<V>>> entryRangeReversed(double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive, int offset, int count);
    
    
    /**
     * 返回the number of elements with a score between <code>startScore</code> and <code>endScore</code>.。
     * 
     * @param startScore 起始分数
     * @param startScoreInclusive - start score inclusive
     * @param endScore 结束分数
     * @param endScoreInclusive - end score inclusive
     * @return count
     */
    Single<Integer> count(double startScore, boolean startScoreInclusive, double endScore, boolean endScoreInclusive);
    
    /**
     * 读取all values at once.。
     * 
     * @return values
     */
    Maybe<Collection<V>> readAll();

    /**
     * Set 相关操作：请改用 {@link #intersection(SetIntersectionArgs)}。。
     * <p>
     * Intersect provided ScoredSortedSets 
     * and store result to current ScoredSortedSet
     * 
     * @param names - names of ScoredSortedSet
     * @return length of intersection
     */
    @Deprecated
    Single<Integer> intersection(String... names);

    /**
     * Set 相关操作：请改用 {@link #intersection(SetIntersectionArgs)}。。
     * <p>
     * Intersect provided ScoredSortedSets with defined aggregation method 
     * and store result to current ScoredSortedSet
     * 
     * @param aggregate - score aggregation mode
     * @param names - names of ScoredSortedSet
     * @return length of intersection
     */
    @Deprecated
    Single<Integer> intersection(Aggregate aggregate, String... names);

    /**
     * Set 相关操作：请改用 {@link #intersection(SetIntersectionArgs)}。。
     * <p>
     * Intersect provided ScoredSortedSets mapped to weight multiplier 
     * and store result to current ScoredSortedSet
     * 
     * @param nameWithWeight - name of ScoredSortedSet mapped to weight multiplier
     * @return length of intersection
     */
    @Deprecated
    Single<Integer> intersection(Map<String, Double> nameWithWeight);

    /**
     * Set 相关操作：请改用 {@link #intersection(SetIntersectionArgs)}。。
     * <p>
     * Intersect provided ScoredSortedSets mapped to weight multiplier 
     * with defined aggregation method 
     * and store result to current ScoredSortedSet
     * 
     * @param aggregate - score aggregation mode
     * @param nameWithWeight - name of ScoredSortedSet mapped to weight multiplier
     * @return length of intersection
     */
    @Deprecated
    Single<Integer> intersection(Aggregate aggregate, Map<String, Double> nameWithWeight);
    
    /**
     * Set 相关操作：Intersect provided ScoredSortedSets mapped to weight multiplier。
     * with defined aggregation method
     * and store result to current ScoredSortedSet
     *
     * @param args object
     * @return length of intersection
     */
    Single<Integer> intersection(SetIntersectionArgs args);
    
    /**
     * Set 相关操作：请改用 {@link #readIntersection(SetIntersectionArgs)}。。
     * <p>
     * Intersect provided ScoredSortedSets
     * with current ScoredSortedSet without state change
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @param names - names of ScoredSortedSet
     * @return result of intersection
     */
    @Deprecated
    Maybe<Collection<V>> readIntersection(String... names);

    /**
     * Set 相关操作：请改用 {@link #readIntersection(SetIntersectionArgs)}。。
     * <p>
     * Intersect provided ScoredSortedSets with current ScoredSortedSet using defined aggregation method
     * without state change
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @param aggregate - score aggregation mode
     * @param names - names of ScoredSortedSet
     * @return result of intersection
     */
    @Deprecated
    Maybe<Collection<V>> readIntersection(Aggregate aggregate, String... names);

    /**
     * Set 相关操作：请改用 {@link #readIntersection(SetIntersectionArgs)}。。
     * <p>
     * Intersect provided ScoredSortedSets mapped to weight multiplier
     * with current ScoredSortedSet without state change
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @param nameWithWeight - name of ScoredSortedSet mapped to weight multiplier
     * @return result of intersection
     */
    @Deprecated
    Maybe<Collection<V>> readIntersection(Map<String, Double> nameWithWeight);

    /**
     * Set 相关操作：请改用 {@link #readIntersection(SetIntersectionArgs)}。。
     * <p>
     * Intersect provided ScoredSortedSets mapped to weight multiplier
     * with current ScoredSortedSet using defined aggregation method
     * without state change
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @param aggregate - score aggregation mode
     * @param nameWithWeight - name of ScoredSortedSet mapped to weight multiplier
     * @return result of intersection
     */
    @Deprecated
    Maybe<Collection<V>> readIntersection(Aggregate aggregate, Map<String, Double> nameWithWeight);

    /**
     * Set 相关操作：Intersect provided ScoredSortedSets。
     * with current ScoredSortedSet
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @param args object
     * @return result of intersection
     */
    Maybe<Collection<V>> readIntersection(SetIntersectionArgs args);

    /**
     * Set 相关操作：Intersect provided ScoredSortedSets。
     * with current ScoredSortedSet
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @param args object
     * @return result of intersection entries (value and its score)
     */
    Maybe<Collection<ScoredEntry<V>>> readIntersectionEntries(SetIntersectionArgs args);

    /**
     * 统计指定集合与当前集合交集结果的元素数量。
     * <p>
     * 需要 <b>Redis 7.0.0 及以上</b>。
     *
     * @param names - name of sets
     * @return amount of elements
     */
    Single<Integer> countIntersection(String... names);

    /**
     * 统计指定集合与当前集合交集结果的元素数量。
     * <p>
     * 需要 <b>Redis 7.0.0 及以上</b>。
     *
     * @param names - name of sets
     * @param limit - sets intersection limit
     * @return amount of elements
     */
    Single<Integer> countIntersection(int limit, String... names);

    /**
     * Set 相关操作：请改用 {@link #union(SetUnionArgs)}。。
     * <p>
     * Union provided ScoredSortedSets 
     * and store result to current ScoredSortedSet
     * 
     * @param names - names of ScoredSortedSet
     * @return length of union
     */
    @Deprecated
    Single<Integer> union(String... names);

    /**
     * Set 相关操作：请改用 {@link #union(SetUnionArgs)}。。
     * <p>
     * Union provided ScoredSortedSets with defined aggregation method 
     * and store result to current ScoredSortedSet
     * 
     * @param aggregate - score aggregation mode
     * @param names - names of ScoredSortedSet
     * @return length of union
     */
    @Deprecated
    Single<Integer> union(Aggregate aggregate, String... names);

    /**
     * Set 相关操作：请改用 {@link #union(SetUnionArgs)}。。
     * <p>
     * Union provided ScoredSortedSets mapped to weight multiplier 
     * and store result to current ScoredSortedSet
     * 
     * @param nameWithWeight - name of ScoredSortedSet mapped to weight multiplier
     * @return length of union
     */
    @Deprecated
    Single<Integer> union(Map<String, Double> nameWithWeight);

    /**
     * Set 相关操作：请改用 {@link #union(SetUnionArgs)}。。
     * <p>
     * Union provided ScoredSortedSets mapped to weight multiplier 
     * with defined aggregation method 
     * and store result to current ScoredSortedSet
     * 
     * @param aggregate - score aggregation mode
     * @param nameWithWeight - name of ScoredSortedSet mapped to weight multiplier
     * @return length of union
     */
    @Deprecated
    Single<Integer> union(Aggregate aggregate, Map<String, Double> nameWithWeight);
    
    /**
     * Set 相关操作：Union provided ScoredSortedSets mapped to weight multiplier。
     * with defined aggregation method
     * and store result to current ScoredSortedSet
     *
     * @param args object
     * @return length of union
     */
    Single<Integer> union(SetUnionArgs args);
    
    /**
     * Set 相关操作：请改用 {@link #readUnion(SetUnionArgs)}。。
     * <p>
     * Union ScoredSortedSets specified by name with current ScoredSortedSet
     * without state change.
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @param names - names of ScoredSortedSet
     * @return result of union
     */
    @Deprecated
    Maybe<Collection<V>> readUnion(String... names);

    /**
     * Set 相关操作：请改用 {@link #readUnion(SetUnionArgs)}。。
     * <p>
     * Union ScoredSortedSets specified by name with defined aggregation method
     * and current ScoredSortedSet without state change.
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @param aggregate - score aggregation mode
     * @param names - names of ScoredSortedSet
     * @return result of union
     */
    @Deprecated
    Maybe<Collection<V>> readUnion(Aggregate aggregate, String... names);

    /**
     * Set 相关操作：请改用 {@link #readUnion(SetUnionArgs)}。。
     * <p>
     * Union provided ScoredSortedSets mapped to weight multiplier
     * and current ScoredSortedSet without state change.
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @param nameWithWeight - name of ScoredSortedSet mapped to weight multiplier
     * @return result of union
     */
    @Deprecated
    Maybe<Collection<V>> readUnion(Map<String, Double> nameWithWeight);

    /**
     * Set 相关操作：请改用 {@link #readUnion(SetUnionArgs)}。。
     * <p>
     * Union provided ScoredSortedSets mapped to weight multiplier
     * with defined aggregation method
     * and current ScoredSortedSet without state change
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @param aggregate - score aggregation mode
     * @param nameWithWeight - name of ScoredSortedSet mapped to weight multiplier
     * @return result of union
     */
    @Deprecated
    Maybe<Collection<V>> readUnion(Aggregate aggregate, Map<String, Double> nameWithWeight);

    /**
     * Set 相关操作：Union provided ScoredSortedSets mapped to weight multiplier。
     * with defined aggregation method
     * and current ScoredSortedSet without state change
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @param args object
     * @return result of union
     */
    Maybe<Collection<V>> readUnion(SetUnionArgs args);

    /**
     * Set 相关操作：Union provided ScoredSortedSets mapped to weight multiplier。
     * with defined aggregation method
     * and current ScoredSortedSet without state change
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @param args object
     * @return result of union entries (value and its score)
     */
    Maybe<Collection<ScoredEntry<V>>> readUnionEntries(SetUnionArgs args);

    /**
     * Set 相关操作：Diff ScoredSortedSets specified by name。
     * with current ScoredSortedSet without state change.
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @param names - name of sets
     * @return result of diff
     */
    Maybe<Collection<V>> readDiff(String... names);

    /**
     * Set 相关操作：Diff ScoredSortedSets specified by name。
     * with current ScoredSortedSet without state change.
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @param names - name of sets
     * @return result of diff entries (value and its score)
     */
    Maybe<Collection<ScoredEntry<V>>> readDiffEntries(String... names);

    /**
     * Set 相关操作：Diff provided ScoredSortedSets。
     * and store result to current ScoredSortedSet
     * <p>
     * 需要 <b>Redis 6.2.0 及以上</b>。
     *
     * @param names - name of sets
     * @return length of diff
     */
    Single<Integer> diff(String... names);

    /**
     * 移除and returns the head element waiting if necessary for an element to become available.。
     *
     * @return the head element
     */
    Single<V> takeFirst();

    /**
     * 移除and returns the tail element waiting if necessary for an element to become available.。
     *
     * @return the tail element
     */
    Single<V> takeLast();

    /**
     * Redis Stream 相关操作：Retrieves and removes continues stream of elements from the head.。
     * Waits for next element become available.
     * 
     * @return stream of head elements
     */
    Flowable<V> takeFirstElements();

    /**
     * Redis Stream 相关操作：Retrieves and removes continues stream of elements from the tail.。
     * Waits for next element become available.
     * 
     * @return stream of tail elements
     */
    Flowable<V> takeLastElements();

    /**
     * 注册对象事件监听器。
     *
     * @see org.redisson.api.listener.TrackingListener
     * @see org.redisson.api.listener.ScoredSortedSetAddListener
     * @see org.redisson.api.listener.ScoredSortedSetRemoveListener
     * @see org.redisson.api.ExpiredObjectListener
     * @see org.redisson.api.DeletedObjectListener
     *
     * @param listener 对象事件监听器
     * @return 监听器 ID
     */
    Single<Integer> addListener(ObjectListener listener);


}
