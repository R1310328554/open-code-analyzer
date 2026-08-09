/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.tools.command;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.rocketmq.remoting.RPCHook;

/**
 * mqadmin 子命令接口：定义命令名、描述、CLI 选项构建与执行逻辑。
 */
public interface SubCommand {
    /** 子命令名称（mqadmin 第一参数）。 */
    String commandName();

    /** 子命令别名，默认 null。 */
    default String commandAlias() {
        return null;
    }

    /** 子命令简短描述，用于 help 输出。 */
    String commandDesc();

    /** 在公共选项基础上追加本子命令专属 CLI 选项。 */
    Options buildCommandlineOptions(final Options options);

    /** 解析完成后执行子命令逻辑；失败抛 {@link SubCommandException}。 */
    void execute(final CommandLine commandLine, final Options options, RPCHook rpcHook) throws SubCommandException;
}
