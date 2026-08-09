/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.redisson.quarkus.client.it;

import org.redisson.api.*;
import org.redisson.api.redisnode.RedisNodes;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * Quarkus Redisson 客户端集成测试 REST 资源。
 * <p>暴露 {@link RMap}、{@link RRemoteService}、Redis 节点 ping 与
 * {@link RScheduledExecutorService} 等典型用法的 HTTP 端点，供 IT 用例调用。
 */
@Path("/quarkus-redisson-client")
public class QuarkusRedissonClientResource {

    /** 注入的 Redisson 客户端（由扩展 CDI 生产者提供）。 */
    @Inject
    RedissonClient redisson;

    /** 测试 {@link RMap} 读写：写入键 {@code "1"} 并返回其值。 */
    @GET
    @Path("/map")
    public String map() {
        RMap<String, Integer> m = redisson.getMap("test");
        m.put("1", 2);
        return m.get("1").toString();
    }

    /** 测试 {@link RRemoteService}：注册 {@link RemService} 实现并远程调用。 */
    @GET
    @Path("/remoteService")
    public String remoteService() {
        RRemoteService t = redisson.getRemoteService("test");

        t.register(RemService.class, new RemoteServiceImpl());

        RemService rs = t.get(RemService.class);
        return rs.executeMe();
    }

    /** 对单机 Redis 节点执行 {@code pingAll} 连通性检测。 */
    @GET
    @Path("/pingAll")
    public String pingAll() {
        redisson.getRedisNodes(RedisNodes.SINGLE).pingAll();
        return "OK";
    }

    /** 测试 {@link RScheduledExecutorService}：提交 {@link Task} 并同步等待结果。 */
    @GET
    @Path("/executeTask")
    public String executeTask() throws ExecutionException, InterruptedException {
        RScheduledExecutorService t = redisson.getExecutorService("test");
        t.registerWorkers(WorkerOptions.defaults());

        RExecutorFuture<String> r = t.submit(new Task());
        return r.get();
    }

}
