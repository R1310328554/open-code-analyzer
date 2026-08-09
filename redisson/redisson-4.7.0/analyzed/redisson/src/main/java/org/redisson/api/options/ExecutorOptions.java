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
package org.redisson.api.options;

import org.redisson.api.IdGenerator;
import org.redisson.client.codec.Codec;

import java.time.Duration;

/**
 * {@link org.redisson.api.RExecutorService} 实例的配置选项。
 *
 * @author Nikita Koksharov
 *
 */
public interface ExecutorOptions extends CodecOptions<ExecutorOptions, Codec> {

    /**
     * 按对象实例名称创建选项。
     *
     * @param name 对象实例名称
     * @return 选项实例
     */
    static ExecutorOptions name(String name) {
        return new ExecutorParams(name);
    }

    /**
     * 设置任务重试间隔：自任务开始计时，到期后由 ExecutorService 工作线程再次执行。
     * <p>
     * 仅当任务曾处于执行中但未标记为完成（成功或失败）时生效。
     * <p>
     * 设为 <code>0</code> 可禁用。
     * <p>
     * 默认值为 <code>5 分钟</code>
     *
     * @param interval 重试间隔
     * @return 选项实例
     */
    ExecutorOptions taskRetryInterval(Duration interval);

    /**
     * 设置任务标识符生成器。
     *
     * @param idGenerator 标识符生成器
     * @return 选项实例
     */
    ExecutorOptions idGenerator(IdGenerator idGenerator);

}
