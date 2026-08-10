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

package com.alibaba.nacos.naming.web;

import com.alibaba.nacos.api.exception.runtime.NacosDeserializationException;
import com.alibaba.nacos.common.utils.InternetAddressUtil;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.core.utils.ReuseHttpServletRequest;
import com.alibaba.nacos.naming.healthcheck.RsInfo;

/**
 * 基于实例 IP:Port 的 Distro 责任标签生成器。
 *
 * <p>从请求参数 ip/port 或旧版 beat JSON 解析 {@link RsInfo}，生成 {@code ip:port} 形式的分区键。</p>
 *
 * @author xiweng.yy
 */
public class DistroIpPortTagGenerator implements DistroTagGenerator {
    
    private static final String PARAMETER_BEAT = "beat";
    
    private static final String PARAMETER_IP = "ip";
    
    private static final String PARAMETER_PORT = "port";
    
    /** 解析请求参数并返回 ip:port 责任标签，缺省端口为 0。 */
    @Override
    public String getResponsibleTag(ReuseHttpServletRequest request) {
        String ip = request.getParameter(PARAMETER_IP);
        String port = request.getParameter(PARAMETER_PORT);
        if (StringUtils.isBlank(ip)) {
            // 兼容旧客户端：ip 为空时从 beat 参数 JSON 解析 IP 与端口
            String beatStr = request.getParameter(PARAMETER_BEAT);
            if (StringUtils.isNotBlank(beatStr)) {
                try {
                    RsInfo rsInfo = JacksonUtils.toObj(beatStr, RsInfo.class);
                    ip = rsInfo.getIp();
                    port = String.valueOf(rsInfo.getPort());
                } catch (NacosDeserializationException ignored) {
                }
            }
        }
        if (StringUtils.isNotBlank(ip)) {
            ip = ip.trim();
        }
        port = StringUtils.isBlank(port) ? "0" : port.trim();
        return ip + InternetAddressUtil.IP_PORT_SPLITER + port;
    }
}
