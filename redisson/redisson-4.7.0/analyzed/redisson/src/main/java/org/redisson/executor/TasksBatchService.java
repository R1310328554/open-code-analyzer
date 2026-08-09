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
package org.redisson.executor;

import org.redisson.api.RFuture;
import org.redisson.client.codec.Codec;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.command.CommandBatchService;

import java.util.List;

/**
 * 支持批量（pipeline）提交任务的 {@link TasksService} 子类。
 * <p>
 * 内部使用 {@link org.redisson.command.CommandBatchService} 合并多条 Redis 写入，
 * 调用 {@link #executeAdd()} 或 {@link #executeAddAsync()} 一次性刷盘。
 *
 * @author Nikita Koksharov
 *
 */
public class TasksBatchService extends TasksService {

    /** 批量命令执行器，缓存 add 相关的 EVAL 脚本。 */
    private final CommandBatchService batchCommandService;
    
    /** 构造批量任务服务并初始化 CommandBatchService。 */
    public TasksBatchService(Codec codec, String name, CommandAsyncExecutor commandExecutor, String executorId) {
        super(codec, name, commandExecutor, executorId);
        batchCommandService = new CommandBatchService(commandExecutor);
    }
    
    /** 入队操作走批量执行器而非单条 commandExecutor。 */
    @Override
    protected CommandAsyncExecutor getAddCommandExecutor() {
        return batchCommandService;
    }

    /** 同步刷盘批量 add 命令并返回各条入队结果。 */
    public List<Boolean> executeAdd() {
        return (List<Boolean>) batchCommandService.execute().getResponses();
    }
    
    /** 异步刷盘批量 add 命令。 */
    public RFuture<List<Boolean>> executeAddAsync() {
        return (RFuture<List<Boolean>>) (Object) batchCommandService.executeAsync();
    }

    
}
