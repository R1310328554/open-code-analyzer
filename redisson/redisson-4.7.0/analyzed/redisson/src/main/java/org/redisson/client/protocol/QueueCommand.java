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

import java.util.List;

/**
 * 连接命令队列中的统一抽象：单命令 {@link CommandData} 或多命令 {@link CommandsData}。
 * <p>
 * 队列处理器通过本接口判断阻塞、Pub/Sub 与完成状态。
 *
 * @author Nikita Koksharov
 *
 */
public interface QueueCommand {
    
    /** 返回本队列项涉及的 Pub/Sub 子命令。 */
    List<CommandData<Object, Object>> getPubSubOperations();

    /** 以异常标记命令失败。 */
    boolean tryFailure(Throwable cause);
    
    /** 命令是否已执行完毕。 */
    boolean isExecuted();

    /** 是否为阻塞命令（需独占连接直至超时或数据到达）。 */
    boolean isBlockingCommand();
    
}
