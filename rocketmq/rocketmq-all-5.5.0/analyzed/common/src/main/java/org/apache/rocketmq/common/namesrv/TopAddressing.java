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
package org.apache.rocketmq.common.namesrv;


/**
 * 顶层 NameServer 地址解析 SPI：从 WS/云配置等来源获取 NS 地址。
 */
public interface TopAddressing {

    /** 获取当前 NameServer 地址字符串。 */
    String fetchNSAddr();

    /** 注册 NameServer 地址变更回调。 */
    void registerChangeCallBack(NameServerUpdateCallback changeCallBack);
}
