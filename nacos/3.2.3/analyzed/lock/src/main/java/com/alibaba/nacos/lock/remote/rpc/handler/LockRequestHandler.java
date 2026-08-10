/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.lock.remote.rpc.handler;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.lock.model.LockInstance;
import com.alibaba.nacos.api.lock.remote.LockOperationEnum;
import com.alibaba.nacos.api.lock.remote.request.LockOperationRequest;
import com.alibaba.nacos.api.lock.remote.response.LockOperationResponse;
import com.alibaba.nacos.api.remote.request.RequestMeta;
import com.alibaba.nacos.core.remote.RequestHandler;
import com.alibaba.nacos.lock.exception.NacosLockException;
import com.alibaba.nacos.lock.service.LockOperationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 分布式锁 gRPC 远程请求处理器。
 *
 * <p>接收 {@link com.alibaba.nacos.api.lock.remote.request.LockOperationRequest}，
 * 按 {@link LockOperationEnum} 分发至 {@link LockOperationService} 执行加锁或解锁。</p>
 *
 * @author 985492783@qq.com
 * @description LockRequestHandler
 * @date 2023/6/29 14:00
 */
@Since("3.0.0")
@Component
public class LockRequestHandler
    extends RequestHandler<LockOperationRequest, LockOperationResponse> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(LockRequestHandler.class);
    
    private final LockOperationService lockOperationService;
    
    /**
     * 注入锁操作服务。
     *
     * @param lockOperationService 锁业务服务
     */
    public LockRequestHandler(LockOperationService lockOperationService) {
        this.lockOperationService = lockOperationService;
    }
    
    /** 处理加锁/解锁 gRPC 请求（TODO：鉴权待支持）。 */
    @Override
    public LockOperationResponse handle(LockOperationRequest request, RequestMeta meta)
        throws NacosException {
        Boolean lock = null;
        LOGGER.info("request: {}, instance: {}", request.getLockOperationEnum(),
            request.getLockInstance());
        try {
            if (request.getLockOperationEnum() == LockOperationEnum.ACQUIRE) {
                LockInstance lockInstance = request.getLockInstance();
                lock = lockOperationService.lock(lockInstance);
            } else if (request.getLockOperationEnum() == LockOperationEnum.RELEASE) {
                lock = lockOperationService.unLock(request.getLockInstance());
            } else {
                return LockOperationResponse.fail("There is no Handler of such operations!");
            }
            return LockOperationResponse.success(lock);
        } catch (NacosLockException e) {
            return LockOperationResponse.fail(e.getMessage());
        }
    }
}
