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
package org.redisson.cache;

import java.io.Serializable;

/**
 * 本地缓存禁用确认消息。
 * <p>
 * 实例收到 {@link LocalCachedMapDisable} 并完成处理后，向请求方主题发布此确认。
 *
 * @author Nikita Koksharov
 *
 */
@SuppressWarnings("serial")
public class LocalCachedMapDisableAck implements Serializable {

    public LocalCachedMapDisableAck() {
    }
    
}
