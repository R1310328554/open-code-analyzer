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
package org.redisson.api.stream;

/**
 * {@code RStream.nack()} 否定确认方法的参数对象。
 *
 * @author lamnt2008
 *
 */
public interface StreamNackArgs {

    /**
     * 设置被否定确认消息的重试次数。
     *
     * @param count 重试次数
     * @return 参数对象
     */
    StreamNackArgs retryCount(long count);

    /**
     * 强制为尚未处于待处理状态的已有流消息创建待处理条目。
     *
     * @return 参数对象
     */
    StreamNackArgs force();

    /**
     * 指定消费者组名称及否定确认模式。
     *
     * @param groupName 消费者组名称
     * @param mode 否定确认模式
     * @return 下一步参数选项
     */
    static StreamMessageIdArgs<StreamNackArgs> group(String groupName, StreamNackMode mode) {
        return new StreamNackParams(groupName, mode);
    }
}
