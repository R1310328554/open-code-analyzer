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

package com.alibaba.nacos.istio.xds;

import com.alibaba.nacos.istio.api.ApiGenerator;
import com.alibaba.nacos.istio.model.PushRequest;
import com.google.protobuf.Any;
import io.envoyproxy.envoy.service.discovery.v3.Resource;

import java.util.ArrayList;
import java.util.List;

/**
 * 空 XDS 生成器：对未知或未支持的 typeUrl 返回空资源列表。
 *
 * <p>由 {@link com.alibaba.nacos.istio.api.ApiGeneratorFactory} 在无法匹配具体生成器时使用。</p>
 *
 * @author special.fy
 */
public class EmptyXdsGenerator implements ApiGenerator<Any> {
    
    /** 单例实例。 */
    private static volatile EmptyXdsGenerator singleton = null;
    
    /** 获取空 XDS 生成器单例。 */
    public static EmptyXdsGenerator getInstance() {
        if (singleton == null) {
            synchronized (EmptyXdsGenerator.class) {
                if (singleton == null) {
                    singleton = new EmptyXdsGenerator();
                }
            }
        }
        return singleton;
    }
    
    /** 全量推送时返回空列表。 */
    @Override
    public List<Any> generate(PushRequest pushRequest) {
        return new ArrayList<>();
    }
    
    /** 增量推送时返回空列表。 */
    @Override
    public List<Resource> deltaGenerate(PushRequest pushRequest) {
        return new ArrayList<>();
    }
}
