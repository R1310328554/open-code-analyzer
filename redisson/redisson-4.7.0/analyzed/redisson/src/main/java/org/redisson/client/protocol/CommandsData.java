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

import org.redisson.misc.LogHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 多条命令组成的队列单元，用于 Pipeline、事务或批量同步。
 * <p>
 * 可标记是否跳过结果、原子执行、进入 MULTI 队列及同步从节点。
 *
 * @author Nikita Koksharov
 *
 */
public class CommandsData implements QueueCommand {

    /** 主命令列表。 */
    private final List<CommandData<?, ?>> commands;
    /** 附加命令（如 MULTI 后的额外操作）。 */
    private final List<CommandData<?, ?>> attachedCommands;
    /** 整批完成的 Promise。 */
    private final CompletableFuture<Void> promise;
    /** 是否丢弃各子命令的独立结果。 */
    private final boolean skipResult;
    /** 是否要求原子性（事务语义）。 */
    private final boolean atomic;
    /** 是否已进入 Redis MULTI 队列。 */
    private final boolean queued;
    /** 执行后是否 WAIT 同步从节点。 */
    private final boolean syncSlaves;

    /** 构造 Pipeline 风格命令批（不跳过结果、非原子）。 */
    public CommandsData(CompletableFuture<Void> promise, List<CommandData<?, ?>> commands, boolean queued, boolean syncSlaves) {
        this(promise, commands, null, false, false, queued, syncSlaves);
    }
    
    public CommandsData(CompletableFuture<Void> promise, List<CommandData<?, ?>> commands, boolean skipResult, boolean atomic, boolean queued, boolean syncSlaves) {
        this(promise, commands, null, skipResult, atomic, queued, syncSlaves);
    }
    
    public CommandsData(CompletableFuture<Void> promise, List<CommandData<?, ?>> commands, List<CommandData<?, ?>> attachedCommands,
            boolean skipResult, boolean atomic, boolean queued, boolean syncSlaves) {
        super();
        this.promise = promise;
        this.commands = commands;
        this.skipResult = skipResult;
        this.atomic = atomic;
        this.attachedCommands = attachedCommands;
        this.queued = queued;
        this.syncSlaves = syncSlaves;
    }

    /** 是否需要在执行后同步从节点复制。 */
    public boolean isSyncSlaves() {
        return syncSlaves;
    }

    public CompletableFuture<Void> getPromise() {
        return promise;
    }

    /** 是否已排队等待 EXEC（事务模式）。 */
    public boolean isQueued() {
        return queued;
    }
    
    /** 是否为原子批量（事务）。 */
    public boolean isAtomic() {
        return atomic;
    }
    
    public boolean isSkipResult() {
        return skipResult;
    }
    
    public List<CommandData<?, ?>> getAttachedCommands() {
        return attachedCommands;
    }
    
    /** 返回主命令列表。 */
    public List<CommandData<?, ?>> getCommands() {
        return commands;
    }

    /** 从子命令中筛出 Pub/Sub 相关操作。 */
    @Override
    public List<CommandData<Object, Object>> getPubSubOperations() {
        List<CommandData<Object, Object>> result = new ArrayList<CommandData<Object, Object>>();
        for (CommandData<?, ?> commandData : commands) {
            if (RedisCommands.PUBSUB_COMMANDS.contains(commandData.getCommand().getName())) {
                result.add((CommandData<Object, Object>) commandData);
            }
        }
        return result;
    }

    @Override
    public boolean tryFailure(Throwable cause) {
        return promise.completeExceptionally(cause);
    }

    @Override
    public String toString() {
        return "CommandsData{" +
                "commands=" + LogHelper.toString(commands) +
                ", promise=" + promise +
                ", skipResult=" + skipResult +
                ", atomic=" + atomic +
                ", queued=" + queued +
                ", syncSlaves=" + syncSlaves +
                '}';
    }

    @Override
    public boolean isExecuted() {
        return promise.isDone();
    }

    @Override
    public boolean isBlockingCommand() {
        return false;
    }

}
