package com.alibaba.arthas.nat.agent.registry.impl;

import com.alibaba.arthas.nat.agent.common.constants.NativeAgentConstants;
import com.alibaba.arthas.nat.agent.registry.NativeAgentRegistry;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.Lease;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.PutResponse;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.etcd.jetcd.options.PutOption;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 基于 etcd 的 Native Agent 注册实现，通过租约（lease）维持节点存活。
 *
 * @description: Etcd native agent register implements NativeAgentRegistry
 * @author：flzjkl
 * @date: 2024-09-13 7:54
 */
public class EtcdNativeAgentRegistry implements NativeAgentRegistry {

    private static final Logger logger = LoggerFactory.getLogger(EtcdNativeAgentRegistry.class);
    /** KV 读写操作的超时时间（秒） */
    private final int TIME_OUT_SECONDS = 5;
    /** 连接 etcd 的超时时间（秒） */
    private static final int CONNECTION_TIME_OUT_SECONDS = 5;
    /** 租约 TTL（秒），到期后需 keepAlive 续期 */
    private final int LEASE_SECONDS = 20;

    /** 用于等待 etcd 连接就绪的闭锁 */
    private static CountDownLatch latch = new CountDownLatch(1);

    @Override
    public void registerNativeAgent(String address, String k, String v) {
        // 创建 etcd 客户端并验证连通性
        Client client = null;
        client = Client.builder().endpoints("http://" + address).connectTimeout(Duration.ofSeconds(CONNECTION_TIME_OUT_SECONDS)).build();
        KV kvClient = client.getKVClient();
        CompletableFuture<GetResponse> future = kvClient.get(ByteSequence.from("anything", StandardCharsets.UTF_8));
        future.thenAcceptAsync(res -> latch.countDown());
        try {
            if (!latch.await(CONNECTION_TIME_OUT_SECONDS, TimeUnit.SECONDS)) {
                logger.error("Connect time out");
                throw new RuntimeException("Connect time out");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // 申请租约，使注册节点随 Agent 存活而自动续期
        Lease leaseClient = null;
        LeaseGrantResponse leaseGrantResponse = null;
        try {
            leaseClient = client.getLeaseClient();
            leaseGrantResponse = leaseClient.grant(LEASE_SECONDS).get();
        } catch (Exception e) {
            logger.error("Create lease failed");
            throw new RuntimeException(e);
        }
        long leaseId = leaseGrantResponse.getID();
        // 后台持续续租，防止 key 因 TTL 到期被删除
        leaseClient.keepAlive(leaseId, new StreamObserver<LeaseKeepAliveResponse>() {
            @Override
            public void onNext(LeaseKeepAliveResponse response) {
                // logger.info("lease renewal success, lease id: " + response.getID());
            }

            @Override
            public void onError(Throwable t) {
                logger.error("keep alive error: " + t.getMessage());
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
            }
        });

        // 同步写入 Native Agent 的 IP 与端口信息，并绑定租约
        try {
            ByteSequence key = ByteSequence.from(NativeAgentConstants.NATIVE_AGENT_KEY + "/" + k, StandardCharsets.UTF_8);
            ByteSequence value = ByteSequence.from(v, StandardCharsets.UTF_8);
            PutResponse putResponse = kvClient.put(key, value, PutOption.newBuilder().withLeaseId(leaseId).build()).get(TIME_OUT_SECONDS, TimeUnit.SECONDS);
            logger.info("put response {}",putResponse.toString());
        } catch (Exception e) {
            logger.error("Register native agent failed");
            throw new RuntimeException(e);
        }
    }

}
