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
package org.redisson.config;

/**
 * Redis 命令名称映射接口，用于在发送前转换命令名。
 * <p>
 * 典型场景：代理层命令重命名、兼容不同 Redis 变种。
 *
 * @author Nikita Koksharov
 *
 */
public interface CommandMapper {

    /**
     * 将原始 Redis 命令名 <code>name</code> 映射为目标命令名。
     *
     * @param name 原始命令名
     * @return 映射后的命令名
     */
    String map(String name);

    /**
     * 返回恒等映射实现（原样返回命令名），作为默认策略。
     *
     * @return CommandMapper 实例
     */
    static CommandMapper direct() {
        return new DefaultCommandMapper();
    }

}
