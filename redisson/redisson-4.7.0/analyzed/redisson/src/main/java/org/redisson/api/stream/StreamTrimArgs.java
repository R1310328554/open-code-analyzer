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
 * 流裁剪（trim）方法的参数对象。
 * <p>
 * 提供 {@link #maxLen(int)} 与 {@link #minId(StreamMessageId)} 两种裁剪策略入口。
 *
 * @author Nikita Koksharov
 *
 */
public interface StreamTrimArgs {

    /**
     * 使用 MAXLEN 策略裁剪流。
     * <p>
     * 移除位置超过指定长度阈值的条目。
     *
     * @param threshold 裁剪长度阈值
     * @return 参数对象
     */
    static StreamTrimReferencesArgs<StreamTrimArgs> maxLen(int threshold) {
        return new StreamTrimParams(threshold);
    }

    /**
     * 使用 MINID 策略裁剪流。
     * <p>
     * 移除 ID 低于指定消息 ID 阈值的条目。
     *
     * @param messageId 裁剪最小消息 ID
     * @return 参数对象
     */
    static StreamTrimReferencesArgs<StreamTrimArgs> minId(StreamMessageId messageId) {
        return new StreamTrimParams(messageId);
    }

}
