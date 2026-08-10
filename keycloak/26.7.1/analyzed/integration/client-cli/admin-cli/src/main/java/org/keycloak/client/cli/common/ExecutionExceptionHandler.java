/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.client.cli.common;

import picocli.CommandLine;
import picocli.CommandLine.ParseResult;

/**
 * Picocli 命令执行阶段的异常处理器。
 * <p>
 * 委托 {@link ShortErrorMessageHandler} 输出简短错误信息；若 {@link Globals#dumpTrace} 为 true 则额外打印堆栈。
 */
public final class ExecutionExceptionHandler implements CommandLine.IExecutionExceptionHandler {

    /** 处理运行时异常并返回 Picocli 规定的退出码。 */
    @Override
    public int handleExecutionException(Exception cause, CommandLine cmd, ParseResult parseResult) {
        int exitCode = ShortErrorMessageHandler.shortErrorMessage(cause, cmd);
        if (Globals.dumpTrace) {
            cause.printStackTrace();
        }
        return exitCode;
    }

}
