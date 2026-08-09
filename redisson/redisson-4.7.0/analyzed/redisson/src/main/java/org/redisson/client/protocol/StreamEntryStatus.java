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
package org.redisson.client.protocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Redis Stream 条目操作结果状态码。
 * <p>
 * 用于 {@code XDEL}、{@code XACK} 等命令的逐条回复，标识删除或确认是否成功，
 * 以及是否存在未处理的 pending 引用。
 *
 * @author seakider
 *
 */
public enum StreamEntryStatus {

    /** 操作成功（状态码 1）。 */
    SUCCESS(1),

    /** 消息 ID 不存在（状态码 -1）。 */
    ID_NOT_FOUND(-1),

    /** 条目仍有关联的 pending 引用，无法删除（状态码 2）。 */
    HAS_PENDING_REFERENCES(2);

    private static final Logger log = LoggerFactory.getLogger(StreamEntryStatus.class);
    /** Redis 返回的整型状态码。 */
    private final int status;

    StreamEntryStatus(int status) {
        this.status = status;
    }

    /** 返回底层整型状态码。 */
    public int getStatus() {
        return status;
    }

    /**
     * 根据 Redis 回复中的整型码解析对应枚举常量。
     *
     * @param code Redis 返回的状态码
     * @return 匹配的枚举值，未知码时记录错误并返回 {@code null}
     */
    public static StreamEntryStatus valueOfStatus(int code) {
        for (StreamEntryStatus value : StreamEntryStatus.values()) {
            if (code == value.status)
                return value;
        }

        log.error("unknown status:{}", code);
        return null;
    }
}
