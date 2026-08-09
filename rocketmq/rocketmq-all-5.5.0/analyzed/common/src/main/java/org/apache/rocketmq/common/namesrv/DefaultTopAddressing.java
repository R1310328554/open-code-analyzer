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

import com.google.common.base.Strings;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Map;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.common.help.FAQUrl;
import org.apache.rocketmq.common.utils.HttpTinyClient;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;

/**
 * 默认 NameServer 地址解析：通过 HTTP 从 WS 域名拉取 NS 地址，并支持 SPI 自定义 {@link TopAddressing}。
 */
public class DefaultTopAddressing implements TopAddressing {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerName.COMMON_LOGGER_NAME);

    /** 缓存或外部设置的 NameServer 地址。 */
    private String nsAddr;
    /** Web 服务地址，用于 HTTP 获取 NameServer 列表。 */
    private String wsAddr;
    /** 单元化部署的单元名。 */
    private String unitName;
    /** 附加 HTTP 查询参数。 */
    private Map<String, String> para;
    /** 通过 ServiceLoader 加载的自定义 TopAddressing 实现列表。 */
    private List<TopAddressing> topAddressingList;

    /** 仅指定 WS 地址构造。 */
    public DefaultTopAddressing(final String wsAddr) {
        this(wsAddr, null);
    }

    /** 指定 WS 地址与单元名构造。 */
    public DefaultTopAddressing(final String wsAddr, final String unitName) {
        this.wsAddr = wsAddr;
        this.unitName = unitName;
        this.topAddressingList = loadCustomTopAddressing();
    }

    /** 指定单元名、查询参数与 WS 地址构造。 */
    public DefaultTopAddressing(final String unitName, final Map<String, String> para, final String wsAddr) {
        this.wsAddr = wsAddr;
        this.unitName = unitName;
        this.para = para;
        this.topAddressingList = loadCustomTopAddressing();
    }

    /** 去除响应字符串首尾空白及首行换行符。 */
    private static String clearNewLine(final String str) {
        String newString = str.trim();
        int index = newString.indexOf("\r");
        if (index != -1) {
            return newString.substring(0, index);
        }

        index = newString.indexOf("\n");
        if (index != -1) {
            return newString.substring(0, index);
        }

        return newString;
    }

    /** 通过 {@link ServiceLoader} 加载首个自定义 TopAddressing 实现。 */
    private List<TopAddressing> loadCustomTopAddressing() {
        ServiceLoader<TopAddressing> serviceLoader = ServiceLoader.load(TopAddressing.class);
        Iterator<TopAddressing> iterator = serviceLoader.iterator();
        List<TopAddressing> topAddressingList = new ArrayList<>();
        if (iterator.hasNext()) {
            topAddressingList.add(iterator.next());
        }
        return topAddressingList;
    }

    @Override
    public final String fetchNSAddr() {
        if (!topAddressingList.isEmpty()) {
            for (TopAddressing topAddressing : topAddressingList) {
                String nsAddress = topAddressing.fetchNSAddr();
                if (!Strings.isNullOrEmpty(nsAddress)) {
                    return nsAddress;
                }
            }
        }
        // 自定义实现均未返回地址时，走默认 HTTP 拉取
        return fetchNSAddr(true, 3000);
    }

    @Override
    public void registerChangeCallBack(NameServerUpdateCallback changeCallBack) {
        if (!topAddressingList.isEmpty()) {
            for (TopAddressing topAddressing : topAddressingList) {
                topAddressing.registerChangeCallBack(changeCallBack);
            }
        }
    }

    /**
     * 通过 HTTP GET 从 wsAddr 拉取 NameServer 地址。
     *
     * @param verbose 失败时是否输出详细日志
     * @param timeoutMills HTTP 超时毫秒数
     * @return 成功返回 NS 地址字符串，失败返回 null
     */
        StringBuilder url = new StringBuilder(this.wsAddr);
        try {
            if (null != para && para.size() > 0) {
                if (!UtilAll.isBlank(this.unitName)) {
                    url.append("-").append(this.unitName).append("?nofix=1&");
                }
                else {
                    url.append("?");
                }
                for (Map.Entry<String, String> entry : this.para.entrySet()) {
                    url.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                }
                url = new StringBuilder(url.substring(0, url.length() - 1));
            }
            else {
                if (!UtilAll.isBlank(this.unitName)) {
                    url.append("-").append(this.unitName).append("?nofix=1");
                }
            }

            HttpTinyClient.HttpResult result = HttpTinyClient.httpGet(url.toString(), null, null, "UTF-8", timeoutMills);
            if (200 == result.code) {
                String responseStr = result.content;
                if (responseStr != null) {
                    return clearNewLine(responseStr);
                } else {
                    LOGGER.error("fetch nameserver address is null");
                }
            } else {
                LOGGER.error("fetch nameserver address failed. statusCode=" + result.code);
            }
        } catch (IOException e) {
            if (verbose) {
                LOGGER.error("fetch name server address exception", e);
            }
        }

        if (verbose) {
            String errorMsg =
                "connect to " + url + " failed, maybe the domain name " + MixAll.getWSAddr() + " not bind in /etc/hosts";
            errorMsg += FAQUrl.suggestTodo(FAQUrl.NAME_SERVER_ADDR_NOT_EXIST_URL);

            LOGGER.warn(errorMsg);
        }
        return null;
    }

    /** 返回当前 nsAddr 字段值。 */
    public String getNsAddr() {
        return nsAddr;
    }

    /** 设置 nsAddr 缓存。 */
    public void setNsAddr(String nsAddr) {
        this.nsAddr = nsAddr;
    }
}
