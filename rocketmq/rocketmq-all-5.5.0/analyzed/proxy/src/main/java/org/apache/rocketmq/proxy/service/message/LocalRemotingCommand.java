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
package org.apache.rocketmq.proxy.service.message;

import java.util.HashMap;
import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.protocol.LanguageCode;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;

/**
 * 本地 Remoting 命令封装：便捷创建带自定义头的请求命令。
 */
public class LocalRemotingCommand extends RemotingCommand {

    /**
     * 创建本地 Remoting 请求命令。
     *
     * @param code 请求码
     * @param customHeader 自定义请求头
     * @param language 语言标识
     */
    public static LocalRemotingCommand createRequestCommand(int code, CommandCustomHeader customHeader, String language) {
        LocalRemotingCommand cmd = new LocalRemotingCommand();
        cmd.setCode(code);
        cmd.setLanguage(LanguageCode.getCode(language));
        cmd.writeCustomHeader(customHeader);
        // 初始化扩展字段并序列化自定义头
        cmd.setExtFields(new HashMap<>());
        setCmdVersion(cmd);
        // 将自定义头编码到网络格式
        cmd.makeCustomHeaderToNet();
        return cmd;
    }
}
