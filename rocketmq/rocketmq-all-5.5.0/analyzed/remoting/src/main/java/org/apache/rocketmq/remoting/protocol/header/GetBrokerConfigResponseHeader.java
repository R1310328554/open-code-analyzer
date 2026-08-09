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

/**
 * $Id: GetBrokerConfigResponseHeader.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */
package org.apache.rocketmq.remoting.protocol.header;

import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.annotation.CFNotNull;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;

/**
 * 获取 Broker 配置的响应头：返回配置数据版本号字符串。
 */
public class GetBrokerConfigResponseHeader implements CommandCustomHeader {
    /** Broker 配置数据版本号。 */
    @CFNotNull
    private String version;

    @Override
    public void checkFields() throws RemotingCommandException {
    }

    /** 返回配置版本号。 */
    public String getVersion() {
        return version;
    }

    /** 设置配置版本号。 */
    public void setVersion(String version) {
        this.version = version;
    }
}
