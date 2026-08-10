/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.client.address;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.client.constant.Constants.Address;
import com.alibaba.nacos.client.env.NacosClientProperties;
import com.alibaba.nacos.client.utils.ClientBasicParamUtil;
import com.alibaba.nacos.common.http.client.NacosRestTemplate;
import com.alibaba.nacos.common.utils.InternetAddressUtil;
import com.alibaba.nacos.common.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static com.alibaba.nacos.common.constant.RequestUrlConstants.HTTPS_PREFIX;
import static com.alibaba.nacos.common.constant.RequestUrlConstants.HTTP_PREFIX;

/**
 * Properties server list provider.
 * <p>基于 {@link PropertyKeyConst#SERVER_ADDR} 的固定服务端列表提供者：解析逗号/分号分隔的地址，补全缺省端口，{@link #isFixed()} 恒为 true。</p>
 * 
 * @author totalo
 */
public class PropertiesListProvider extends AbstractServerListProvider {
    
    /** {@link #getServerName()} 前缀，表示静态配置的 server 列表 */
    private static final String FIXED_NAME = "fixed";
    
    /** 初始化后不变的 server 地址列表 */
    private List<String> serverList;
    
    @Override
    public void init(final NacosClientProperties properties,
        final NacosRestTemplate nacosRestTemplate) throws NacosException {
        super.init(properties, nacosRestTemplate);
        serverList = new ArrayList<>();
        String serverAddrsStr = properties.getProperty(PropertyKeyConst.SERVER_ADDR);
        StringTokenizer serverAddrsTokens = new StringTokenizer(serverAddrsStr, ",;");
        while (serverAddrsTokens.hasMoreTokens()) {
            String serverAddr = serverAddrsTokens.nextToken().trim();
            if (serverAddr.startsWith(HTTP_PREFIX) || serverAddr.startsWith(HTTPS_PREFIX)) {
                this.serverList.add(serverAddr);
            } else {
                String[] serverAddrArr = InternetAddressUtil.splitIpPortStr(serverAddr);
                if (serverAddrArr.length == 1) {
                    this.serverList
                        .add(serverAddrArr[0] + InternetAddressUtil.IP_PORT_SPLITER
                            + ClientBasicParamUtil.getDefaultServerPort());
                } else {
                    this.serverList.add(serverAddr);
                }
            }
        }
    }
    
    /** 返回 properties 解析得到的固定列表 */
    @Override
    public List<String> getServerList() {
        return serverList;
    }
    
    /** 由 fixed 前缀、namespace 与 IP 后缀组成唯一服务名 */
    @Override
    public String getServerName() {
        return FIXED_NAME + "-"
            + (StringUtils.isNotBlank(namespace) ? (StringUtils.trim(namespace) + "-")
                : "")
            + ClientBasicParamUtil.getNameSuffixByServerIps(serverList.toArray(new String[0]));
    }
    
    /** 优先级低于 endpoint provider，作为兜底固定地址源 */
    @Override
    public int getOrder() {
        return Address.ADDRESS_SERVER_LIST_PROVIDER_ORDER;
    }
    
    /** 配置了非空 SERVER_ADDR 时匹配 */
    @Override
    public boolean match(final NacosClientProperties properties) {
        return StringUtils.isNotBlank(properties.getProperty(PropertyKeyConst.SERVER_ADDR));
    }
    
    /** 属性配置的地址列表不会后台自动刷新 */
    @Override
    public boolean isFixed() {
        return true;
    }
    
    /** 固定列表无需释放资源，空实现 */
    @Override
    public void shutdown() throws NacosException {
    }
}
