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

import org.redisson.client.codec.Codec;

/**
 * {@link org.redisson.api.RPatternTopic} 实例的选项配置。
 *
 * @author Nikita Koksharov
 *
 */
public interface PatternTopicOptions extends CodecOptions<PatternTopicOptions, Codec> {

    /**
     * 按主题匹配模式创建选项。
     *
     * @param pattern 对象实例的匹配模式
     * @return 选项实例
     */
    static PatternTopicOptions pattern(String pattern) {
        return new PatternTopicParams(pattern);
    }

}
