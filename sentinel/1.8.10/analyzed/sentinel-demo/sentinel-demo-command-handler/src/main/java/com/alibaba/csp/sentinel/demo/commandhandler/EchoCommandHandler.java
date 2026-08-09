/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.demo.commandhandler;

import com.alibaba.csp.sentinel.command.CommandHandler;
import com.alibaba.csp.sentinel.command.CommandRequest;
import com.alibaba.csp.sentinel.command.CommandResponse;
import com.alibaba.csp.sentinel.command.annotation.CommandMapping;

/**
 * 自定义 {@link CommandHandler} 注册演示。
 *
 * <ul>
 * <li>1. 实现 {@link CommandHandler} SPI 接口</li>
 * <li>2. 用 {@link CommandMapping} 声明命令名与描述</li>
 * <li>3. 实现 {@code handle} 方法</li>
 * <li>4. 在 {@code resources/META-INF/services/com.alibaba.csp.sentinel.command.CommandHandler} 中注册</li>
 * </ul>
 *
 * @author houyi
 */
@CommandMapping(name = "echo", desc = "echo command for demo")
public class EchoCommandHandler implements CommandHandler<String> {

    /** 读取 name 参数并回显；未传参时提示提交 name。 */
    @Override
    public CommandResponse<String> handle(CommandRequest request) {
        String name = request.getParam("name");
        if (name == null || name.trim().length() == 0) {
            return CommandResponse.ofSuccess("Tell us what's your name by submit a name parameter");
        }
        return CommandResponse.ofSuccess("Hello: " + name);
    }

}
