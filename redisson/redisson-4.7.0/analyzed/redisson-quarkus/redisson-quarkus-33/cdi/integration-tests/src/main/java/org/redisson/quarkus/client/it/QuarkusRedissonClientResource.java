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

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.redisson.api.*;
import org.redisson.api.redisnode.RedisNodes;
import org.redisson.client.codec.StringCodec;

import java.util.concurrent.ExecutionException;

/**
 * Quarkus 3.3 Redisson CDI 集成测试 REST 资源。
 * <p>覆盖 {@link RMap}、{@link RRemoteService}、{@link RScheduledExecutorService}
 * 与响应式 {@link RBucketReactive} 等典型 API 的 HTTP 冒烟端点。
 */
@Path("/quarkus-redisson-client")
public class QuarkusRedissonClientResource {

    /** 由 {@link RedissonClientProducer} 提供的 CDI {@link RedissonClient}。 */
    @Inject
    RedissonClient redisson;

    /** 测试 {@link RMap} 基本读写。 */
    @GET
    @Path("/map")
    public String map() {
        RMap<String, Integer> m = redisson.getMap("test");
        m.put("1", 2);
        return m.get("1").toString();
    }

    /** 注册 {@link RemService} 实现并通过 {@link RRemoteService} 远程调用。 */
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

    /** 向 {@link RScheduledExecutorService} 提交 {@link Task} 并同步等待结果。 */
    @GET
    @Path("/executeTask")
    public String executeTask() throws ExecutionException, InterruptedException {
        RScheduledExecutorService t = redisson.getExecutorService("test");
        t.registerWorkers(WorkerOptions.defaults());

        RExecutorFuture<String> r = t.submit(new Task());
        return r.get();
    }

    /** 响应式写入并读取 {@link RBucketReactive}（StringCodec）。 */
    @GET
    @Path("/bucket")
    public Uni<String> getBucket(){
        RBucketReactive<String> bucket = redisson.reactive().getBucket("test-bucket", new StringCodec());
        return Uni.createFrom().future(bucket.set("world").toFuture())
                .flatMap( unused -> Uni.createFrom().future(bucket.get().toFuture()));
    }

    /** 写入后删除响应式 Bucket，返回删除是否成功。 */
    @GET
    @Path("/delBucket")
    public Uni<Boolean> deleteBucket(){
        RBucketReactive<String> bucket = redisson.reactive().getBucket("test-bucket", new StringCodec());
        return Uni.createFrom().future(bucket.set("world").toFuture())
                .flatMap( unused -> Uni.createFrom().future(bucket.delete().toFuture()));
    }

}
