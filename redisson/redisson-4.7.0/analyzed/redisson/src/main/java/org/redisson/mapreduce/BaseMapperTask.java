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
package org.redisson.mapreduce;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.redisson.api.RedissonClient;
import org.redisson.api.annotation.RInject;

/**
 * MapReduce Mapper 任务的可序列化基类。
 * <p>
 * 子类在分布式执行时通过 {@link RInject} 注入 {@link RedissonClient}；
 * 携带待扫描对象名列表、Collector Map 名、超时与 worker 数等调度参数。
 *
 * @author Nikita Koksharov
 *
 * @param <KOut> output key
 * @param <VOut> output value
 */
public abstract class BaseMapperTask<KOut, VOut> implements Runnable, Serializable {

    /** 序列化版本号。 */
    private static final long serialVersionUID = 6224632826989873592L;

    /** 任务在 Executor 上运行时注入的 Redisson 客户端。 */
    @RInject
    protected RedissonClient redisson;
    
    /** 输入 RObject 的实现/接口类型。 */
    protected Class<?> objectClass;
    /** 本任务负责的 Redis 对象名列表。 */
    protected List<String> objectNames = new ArrayList<String>();
    /** 输入对象使用的 Codec 类。 */
    protected Class<?> objectCodecClass;
    
    /** MapReduce  worker 数量。 */
    protected int workersAmount;
    /** Mapper 输出写入的 Collector {@link RMap} 名称。 */
    protected String collectorMapName;
    /** 任务执行超时（毫秒）。 */
    protected long timeout;
    
    /** 无参构造，供序列化框架使用。 */
    public BaseMapperTask() {
    }
    
    /** 指定输入对象类型与 Codec 类。 */
    public BaseMapperTask(Class<?> objectClass, Class<?> objectCodecClass) {
        super();
        this.objectClass = objectClass;
        this.objectCodecClass = objectCodecClass;
    }

    /** 追加一个待处理的 Redis 对象名。 */
    public void addObjectName(String objectName) {
        this.objectNames.add(objectName);
    }
    
    /** 清空对象名列表。 */
    public void clearObjectNames() {
        this.objectNames.clear();
    }
    
    /** 设置任务超时时间。 */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
    
    /** 设置 worker 数量。 */
    public void setWorkersAmount(int workersAmount) {
        this.workersAmount = workersAmount;
    }

    /** 设置 Collector Map 的 Redis 名称。 */
    public void setCollectorMapName(String collatorMapName) {
        this.collectorMapName = collatorMapName;
    }
    
}
