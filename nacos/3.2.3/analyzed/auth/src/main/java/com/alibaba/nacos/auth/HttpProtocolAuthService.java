/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.auth;

import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.auth.config.NacosAuthConfig;
import com.alibaba.nacos.auth.context.HttpIdentityContextBuilder;
import com.alibaba.nacos.auth.parser.http.AbstractHttpResourceParser;
import com.alibaba.nacos.auth.parser.http.AiHttpResourceParser;
import com.alibaba.nacos.auth.parser.http.ConfigHttpResourceParser;
import com.alibaba.nacos.auth.parser.http.NamingHttpResourceParser;
import com.alibaba.nacos.auth.serveridentity.ServerIdentity;
import com.alibaba.nacos.auth.util.Loggers;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.auth.api.IdentityContext;
import com.alibaba.nacos.plugin.auth.api.Resource;
import com.alibaba.nacos.plugin.auth.constant.SignType;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP 协议鉴权服务实现。
 *
 * <p>负责解析 HTTP 请求中的资源与身份上下文，支持命名、配置与 AI 模块的内置解析器。</p>
 *
 * @author xiweng.yy
 */
public class HttpProtocolAuthService extends AbstractProtocolAuthService<HttpServletRequest> {
    
    /** 按 {@link SignType} 索引的 HTTP 资源解析器映射。 */
    private final Map<String, AbstractHttpResourceParser> resourceParserMap;
    
    /** HTTP 身份上下文构建器。 */
    private final HttpIdentityContextBuilder identityContextBuilder;
    
    /** 构造 HTTP 鉴权服务并初始化解析器与身份构建器。 */
    public HttpProtocolAuthService(NacosAuthConfig authConfig) {
        super(authConfig);
        resourceParserMap = new HashMap<>(2);
        identityContextBuilder = new HttpIdentityContextBuilder(authConfig);
    }
    
    /** 注册命名、配置与 AI 模块的 HTTP 资源解析器。 */
    @Override
    public void initialize() {
        super.initialize();
        resourceParserMap.put(SignType.NAMING, new NamingHttpResourceParser());
        resourceParserMap.put(SignType.CONFIG, new ConfigHttpResourceParser());
        resourceParserMap.put(SignType.AI, new AiHttpResourceParser());
    }
    
    /** 从 HTTP 请求与 {@link Secured} 注解解析鉴权资源。 */
    @Override
    public Resource parseResource(HttpServletRequest request, Secured secured) {
        if (StringUtils.isNotBlank(secured.resource())) {
            return parseSpecifiedResource(secured);
        }
        String type = secured.signType();
        if (!resourceParserMap.containsKey(type)) {
            Loggers.AUTH.warn(
                "Can't find Http request resourceParser for type {} use specified resource parser",
                type);
            return useSpecifiedParserToParse(secured, request);
        }
        return resourceParserMap.get(type).parse(request, secured);
    }
    
    /** 从 HTTP 请求头/参数构建 {@link IdentityContext}。 */
    @Override
    public IdentityContext parseIdentity(HttpServletRequest request) {
        return identityContextBuilder.build(request);
    }
    
    /** 从 HTTP 请求头读取服务端身份 key 与 value。 */
    @Override
    protected ServerIdentity parseServerIdentity(HttpServletRequest request) {
        String serverIdentityKey = authConfig.getServerIdentityKey();
        String serverIdentity = request.getHeader(serverIdentityKey);
        return new ServerIdentity(serverIdentityKey, serverIdentity);
    }
}
