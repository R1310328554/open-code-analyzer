package com.alibaba.arthas.nat.agent.proxy.registry.impl;

import com.alibaba.arthas.nat.agent.common.constants.NativeAgentConstants;
import com.alibaba.arthas.nat.agent.proxy.registry.NativeAgentProxyRegistry;
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
 * 基于 etcd 的 {@link NativeAgentProxyRegistry} 实现，通过租约续期保证 Proxy 节点存活。
 *
 * @description: Etcd native agent proxy register implements NativeAgentProxyRegistry
 * @author：flzjkl
 * @date: 2024-10-20 18:54
 */
public class EtcdNativeAgentProxyRegistry implements NativeAgentProxyRegistry {

    private static final Logger logger = LoggerFactory.getLogger(EtcdNativeAgentProxyRegistry.class);

    private final int TIME_OUT_SECONDS = 5;
    private static final int CONNECTION_TIME_OUT_SECONDS = 5;
    private final int LEASE_SECONDS = 20;

    private static CountDownLatch latch = new CountDownLatch(1);

    @Override
    public void register(String address, String k, String v) {
        // 创建 etcd 客户端并探测连通性
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

        // 申请租约并启动 keepAlive 续期
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

        // 同步写入 Proxy 注册键，绑定租约 ID
        try {
            ByteSequence key = ByteSequence.from(NativeAgentConstants.NATIVE_AGENT_PROXY_KEY + "/" + k, StandardCharsets.UTF_8);
            ByteSequence value = ByteSequence.from(v, StandardCharsets.UTF_8);
            PutResponse putResponse = kvClient.put(key, value, PutOption.newBuilder().withLeaseId(leaseId).build()).get(TIME_OUT_SECONDS, TimeUnit.SECONDS);
            logger.info("put response {}", putResponse.toString());
        } catch (Exception e) {
            logger.error("Register native proxy failed");
            throw new RuntimeException(e);
        }
    }
}
