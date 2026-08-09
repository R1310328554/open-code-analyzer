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
package org.apache.rocketmq.common.help;

/**
 * RocketMQ 常见错误对应的 FAQ 文档 URL 及错误信息拼接工具。
 */
public class FAQUrl {

    /** 默认 FAQ 文档地址。 */
    public static final String DEFAULT_FAQ_URL = "https://rocketmq.apache.org/docs/bestPractice/06FAQ";

    /** 申请 Topic 相关 FAQ。 */
    public static final String APPLY_TOPIC_URL = DEFAULT_FAQ_URL;

    /** NameServer 地址不存在 FAQ。 */
    public static final String NAME_SERVER_ADDR_NOT_EXIST_URL = DEFAULT_FAQ_URL;

    /** 消费组名重复 FAQ。 */
    public static final String GROUP_NAME_DUPLICATE_URL = DEFAULT_FAQ_URL;

    /** 客户端参数校验失败 FAQ。 */
    public static final String CLIENT_PARAMETER_CHECK_URL = DEFAULT_FAQ_URL;

    /** 订阅组不存在 FAQ。 */
    public static final String SUBSCRIPTION_GROUP_NOT_EXIST = DEFAULT_FAQ_URL;

    /** 客户端服务不可用 FAQ。 */
    public static final String CLIENT_SERVICE_NOT_OK = DEFAULT_FAQ_URL;

    // FAQ：无此 Topic 路由信息，例如 TopicABC
    /** 无 Topic 路由信息 FAQ。 */
    public static final String NO_TOPIC_ROUTE_INFO = DEFAULT_FAQ_URL;

    /** JSON 加载异常 FAQ。 */
    public static final String LOAD_JSON_EXCEPTION = DEFAULT_FAQ_URL;

    /** 同组不同 Topic 订阅 FAQ。 */
    public static final String SAME_GROUP_DIFFERENT_TOPIC = DEFAULT_FAQ_URL;

    /** 消息队列列表不存在 FAQ。 */
    public static final String MQLIST_NOT_EXIST = DEFAULT_FAQ_URL;

    /** 未预期异常默认 FAQ。 */
    public static final String UNEXPECTED_EXCEPTION_URL = DEFAULT_FAQ_URL;

    /** 发送消息失败 FAQ。 */
    public static final String SEND_MSG_FAILED = DEFAULT_FAQ_URL;

    /** 未知主机异常 FAQ。 */
    public static final String UNKNOWN_HOST_EXCEPTION = DEFAULT_FAQ_URL;

    /** FAQ 提示前缀（英文，拼接在错误信息后）。 */
    private static final String TIP_STRING_BEGIN = "\nSee ";
    /** FAQ 提示后缀。 */
    private static final String TIP_STRING_END = " for further details.";
    /** 附加默认 URL 时的引导语。 */
    private static final String MORE_INFORMATION = "For more information, please visit the url, ";

    /** 在错误信息后追加 "See {url} for further details." 提示。 */
    public static String suggestTodo(final String url) {
        StringBuilder sb = new StringBuilder(TIP_STRING_BEGIN.length() + url.length() + TIP_STRING_END.length());
        sb.append(TIP_STRING_BEGIN);
        sb.append(url);
        sb.append(TIP_STRING_END);
        return sb.toString();
    }

    /** 若错误信息尚未包含 FAQ 提示，则附加默认 FAQ URL。 */
    public static String attachDefaultURL(final String errorMessage) {
        if (errorMessage != null) {
            int index = errorMessage.indexOf(TIP_STRING_BEGIN);
            if (-1 == index) {
                StringBuilder sb = new StringBuilder(errorMessage.length() + UNEXPECTED_EXCEPTION_URL.length() + MORE_INFORMATION.length() + 1);
                sb.append(errorMessage);
                sb.append("\n");
                sb.append(MORE_INFORMATION);
                sb.append(UNEXPECTED_EXCEPTION_URL);
                return sb.toString();
            }
        }

        return errorMessage;
    }
}
