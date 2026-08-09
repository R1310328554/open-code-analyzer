/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.demo.rocketmq;

/** RocketMQ 演示用常量：消费组、Topic 与 NameServer 地址。 */
public final class Constants {

    /** 消费/生产组名。 */
    public static final String TEST_GROUP_NAME = "sentinel-group";
    /** 演示 Topic 名。 */
    public static final String TEST_TOPIC_NAME = "SentinelTopicTest";
    /** 本地 NameServer 地址。 */
    public static final String TEST_NAMESRV_ADDR = "127.0.0.1:9876";

    private Constants() {}
}
