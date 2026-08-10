/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

import com.alibaba.nacos.common.constant.HttpHeaderConsts;
import com.alibaba.nacos.common.utils.HttpMethod;
import com.alibaba.nacos.common.utils.InternetAddressUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.core.context.RequestContextHolder;
import com.alibaba.nacos.core.utils.WebUtils;
import com.alibaba.nacos.naming.core.v2.client.ClientAttributes;
import com.alibaba.nacos.naming.core.v2.client.impl.IpPortBasedClient;
import com.alibaba.nacos.naming.core.v2.client.manager.ClientManager;
import com.alibaba.nacos.naming.misc.Loggers;
import com.alibaba.nacos.naming.misc.UtilsAndCommons;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;

/**
 * 1.x 客户端属性采集过滤器。
 *
 * <p>在实例注册与心跳请求中收集版本、应用名、客户端 IP 等属性，写入 {@link RequestContextHolder} 或更新 {@link IpPortBasedClient}。</p>
 *
 * @author hujun
 */
public class ClientAttributesFilter implements Filter {
    
    private static final String BEAT_URI = "/beat";
    
    private static final String IP = "ip";
    
    private static final String PORT = "port";
    
    private static final String ZERO = "0";
    
    @Autowired
    private ClientManager clientManager;
    
    /** 从请求扩展上下文读取当前客户端属性。 */
    public static Optional<ClientAttributes> getCurrentClientAttributes() {
        Object clientAttributes = RequestContextHolder.getContext()
            .getExtensionContext(ClientAttributes.class.getSimpleName());
        if (clientAttributes instanceof ClientAttributes) {
            return Optional.of((ClientAttributes) clientAttributes);
        }
        return Optional.empty();
    }
    
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
        FilterChain filterChain)
        throws IOException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String uri = request.getRequestURI();
        String method = request.getMethod();
        try {
            if (isRegisterInstanceUri(uri, method)) {
                // 注册实例：将客户端属性放入请求扩展上下文
                ClientAttributes attributes = getClientAttributes();
                RequestContextHolder.getContext()
                    .addExtensionContext(ClientAttributes.class.getSimpleName(), attributes);
            } else if (isBeatUri(uri, method)) {
                // 心跳：若客户端尚未记录版本等属性则补写
                String ip = WebUtils.optional(request, IP, StringUtils.EMPTY);
                int port = Integer.parseInt(WebUtils.optional(request, PORT, ZERO));
                String clientId = IpPortBasedClient
                    .getClientId(ip + InternetAddressUtil.IP_PORT_SPLITER + port, true);
                IpPortBasedClient client = (IpPortBasedClient) clientManager.getClient(clientId);
                if (client != null) {
                    ClientAttributes requestClientAttributes = getClientAttributes();
                    // 仅当本地客户端缺少版本属性时才用本次请求属性更新
                    if (canUpdateClientAttributes(client, requestClientAttributes)) {
                        client.setAttributes(requestClientAttributes);
                    }
                }
            }
        } catch (Exception e) {
            Loggers.SRV_LOG.error("handler client attributes error", e);
        }
        try {
            filterChain.doFilter(request, servletResponse);
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
    }
    
    /** 判断是否为 v1/v2 实例心跳 PUT 接口。 */
    private boolean isBeatUri(String uri, String httpMethod) {
        return ((UtilsAndCommons.NACOS_SERVER_CONTEXT + UtilsAndCommons.NACOS_NAMING_CONTEXT
            + UtilsAndCommons.NACOS_NAMING_INSTANCE_CONTEXT + BEAT_URI).equals(uri)
            || (UtilsAndCommons.NACOS_SERVER_CONTEXT
                + UtilsAndCommons.DEFAULT_NACOS_NAMING_CONTEXT_V2
                + UtilsAndCommons.NACOS_NAMING_INSTANCE_CONTEXT + BEAT_URI).equals(uri))
            && HttpMethod.PUT.equals(httpMethod);
    }
    
    /** 判断是否为 v1/v2 实例注册 POST 接口。 */
    private boolean isRegisterInstanceUri(String uri, String httpMethod) {
        return ((UtilsAndCommons.NACOS_SERVER_CONTEXT + UtilsAndCommons.NACOS_NAMING_CONTEXT
            + UtilsAndCommons.NACOS_NAMING_INSTANCE_CONTEXT).equals(uri)
            || (UtilsAndCommons.NACOS_SERVER_CONTEXT
                + UtilsAndCommons.DEFAULT_NACOS_NAMING_CONTEXT_V2
                + UtilsAndCommons.NACOS_NAMING_INSTANCE_CONTEXT).equals(uri))
            && HttpMethod.POST.equals(httpMethod);
    }
    
    /** 请求带版本且客户端尚未持久化版本时才允许更新属性。 */
    private boolean canUpdateClientAttributes(IpPortBasedClient client,
        ClientAttributes requestClientAttributes) {
        if (requestClientAttributes
            .getClientAttribute(HttpHeaderConsts.CLIENT_VERSION_HEADER) == null) {
            return false;
        }
        if (client.getClientAttributes() != null
            && client.getClientAttributes()
                .getClientAttribute(HttpHeaderConsts.CLIENT_VERSION_HEADER) != null) {
            return false;
        }
        return true;
    }
    
    /** 从 BasicContext 组装 User-Agent、App、源 IP 等客户端属性。 */
    private ClientAttributes getClientAttributes() {
        String version = RequestContextHolder.getContext().getBasicContext().getUserAgent();
        String app = RequestContextHolder.getContext().getBasicContext().getApp();
        String clientIp =
            RequestContextHolder.getContext().getBasicContext().getAddressContext().getSourceIp();
        ClientAttributes clientAttributes = new ClientAttributes();
        if (version != null) {
            clientAttributes.addClientAttribute(HttpHeaderConsts.CLIENT_VERSION_HEADER, version);
        }
        if (app != null) {
            clientAttributes.addClientAttribute(HttpHeaderConsts.APP_FILED, app);
        }
        if (clientIp != null) {
            clientAttributes.addClientAttribute(HttpHeaderConsts.CLIENT_IP, clientIp);
        }
        return clientAttributes;
    }
}
