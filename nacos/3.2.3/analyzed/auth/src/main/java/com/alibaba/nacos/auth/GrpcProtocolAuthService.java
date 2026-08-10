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

import com.alibaba.nacos.api.common.ApiType;
import com.alibaba.nacos.api.remote.request.Request;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.auth.config.NacosAuthConfig;
import com.alibaba.nacos.auth.context.GrpcIdentityContextBuilder;
import com.alibaba.nacos.auth.parser.grpc.AbstractGrpcResourceParser;
import com.alibaba.nacos.auth.parser.grpc.AiGrpcResourceParser;
import com.alibaba.nacos.auth.parser.grpc.ConfigGrpcResourceParser;
import com.alibaba.nacos.auth.parser.grpc.NamingGrpcResourceParser;
import com.alibaba.nacos.auth.serveridentity.ServerIdentity;
import com.alibaba.nacos.auth.serveridentity.ServerIdentityResult;
import com.alibaba.nacos.auth.util.Loggers;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.auth.api.IdentityContext;
import com.alibaba.nacos.plugin.auth.api.Resource;
import com.alibaba.nacos.plugin.auth.constant.SignType;

import java.util.HashMap;
import java.util.Map;

/**
 * gRPC 协议鉴权服务实现。
 *
 * <p>负责解析 gRPC 请求中的资源与身份上下文，并支持集群内部 API 的服务端身份校验。</p>
 *
 * @author xiweng.yy
 */
public class GrpcProtocolAuthService extends AbstractProtocolAuthService<Request> {
    
    /** 按 {@link SignType} 索引的 gRPC 资源解析器映射。 */
    private final Map<String, AbstractGrpcResourceParser> resourceParserMap;
    
    /** gRPC 身份上下文构建器。 */
    private final GrpcIdentityContextBuilder identityContextBuilder;
    
    /** 构造 gRPC 鉴权服务并初始化解析器与身份构建器。 */
    public GrpcProtocolAuthService(NacosAuthConfig authConfig) {
        super(authConfig);
        resourceParserMap = new HashMap<>(2);
        identityContextBuilder = new GrpcIdentityContextBuilder(authConfig);
    }
    
    /** 注册命名、配置与 AI 模块的 gRPC 资源解析器。 */
    @Override
    public void initialize() {
        super.initialize();
        resourceParserMap.put(SignType.NAMING, new NamingGrpcResourceParser());
        resourceParserMap.put(SignType.CONFIG, new ConfigGrpcResourceParser());
        resourceParserMap.put(SignType.AI, new AiGrpcResourceParser());
    }
    
    /** 从 gRPC 请求与 {@link Secured} 注解解析鉴权资源。 */
    @Override
    public Resource parseResource(Request request, Secured secured) {
        if (StringUtils.isNotBlank(secured.resource())) {
            return parseSpecifiedResource(secured);
        }
        String type = secured.signType();
        AbstractGrpcResourceParser parser = resourceParserMap.get(type);
        if (parser == null) {
            Loggers.AUTH.warn("Can't find Grpc request resourceParser for type {}", type);
            return useSpecifiedParserToParse(secured, request);
        }
        return parser.parse(request, secured);
    }
    
    /** 从 gRPC 请求头构建 {@link IdentityContext}。 */
    @Override
    public IdentityContext parseIdentity(Request request) {
        return identityContextBuilder.build(request);
    }
    
    /** 仅对 {@link ApiType#INNER_API} 执行服务端身份校验。 */
    @Override
    public ServerIdentityResult checkServerIdentity(Request request, Secured secured) {
        if (ApiType.INNER_API != secured.apiType()) {
            return ServerIdentityResult.noMatched();
        }
        return super.checkServerIdentity(request, secured);
    }
    
    /** 从 gRPC 请求头读取服务端身份 key 与 value。 */
    @Override
    protected ServerIdentity parseServerIdentity(Request request) {
        String serverIdentityKey = authConfig.getServerIdentityKey();
        String serverIdentity = request.getHeader(serverIdentityKey);
        return new ServerIdentity(serverIdentityKey, serverIdentity);
    }
}
