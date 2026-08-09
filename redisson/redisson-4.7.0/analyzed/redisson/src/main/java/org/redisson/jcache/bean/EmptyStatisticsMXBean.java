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
package org.redisson.jcache.bean;

/**
 * 统计未启用时的空实现 {@link JCacheStatisticsMXBean}。
 * <p>
 * 所有 add* 方法均为 no-op，避免 JCache 热路径空指针判断。
 *
 * @author Nikita Koksharov
 *
 */
public class EmptyStatisticsMXBean extends JCacheStatisticsMXBean {

    /** 空实现：忽略驱逐计数。 */
    @Override
    public void addEvictions(long value) {
    }
    
    /** 空实现：忽略读耗时。 */
    @Override
    public void addGetTime(long value) {
    }
    
    /** 空实现：忽略命中。 */
    @Override
    public void addHits(long value) {
    }
    
    /** 空实现：忽略未命中。 */
    @Override
    public void addMisses(long value) {
    }
    
    /** 空实现：忽略写入次数。 */
    @Override
    public void addPuts(long value) {
    }
    
    /** 空实现：忽略写耗时。 */
    @Override
    public void addPutTime(long value) {
    }
    
    /** 空实现：忽略删除次数。 */
    @Override
    public void addRemovals(long value) {
    }
    
    /** 空实现：忽略删耗时。 */
    @Override
    public void addRemoveTime(long value) {
    }
    
}
