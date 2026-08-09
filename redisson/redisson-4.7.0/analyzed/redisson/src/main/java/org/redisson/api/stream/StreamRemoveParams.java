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
 * {@link StreamRemoveArgs} 的实现类，承载流消息删除参数。
 *
 * @author seakider
 *
 */
public class StreamRemoveParams extends BaseReferencesParams<StreamRemoveArgs> implements StreamRemoveArgs {

    /** 待删除的消息 ID 数组。 */
    private final StreamMessageId[] ids;

    public StreamRemoveParams(StreamMessageId[] ids) {
        this.ids = ids;
    }

    public StreamMessageId[] getIds() {
        return ids;
    }
}
