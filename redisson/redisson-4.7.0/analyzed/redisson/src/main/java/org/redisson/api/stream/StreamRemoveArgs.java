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
 * {@link org.redisson.api.RStream#remove} 方法的参数对象。
 * <p>
 * 用于指定要从流中删除的消息 ID。
 *
 * @author seakider
 *
 */
public interface StreamRemoveArgs extends StreamReferencesArgs<StreamRemoveArgs> {

    /**
     * 指定待删除的消息 ID。
     *
     * @param ids 待删除的消息 ID
     * @return 参数对象
     */
    static StreamRemoveArgs ids(StreamMessageId... ids) {
        return new StreamRemoveParams(ids);
    }
}
