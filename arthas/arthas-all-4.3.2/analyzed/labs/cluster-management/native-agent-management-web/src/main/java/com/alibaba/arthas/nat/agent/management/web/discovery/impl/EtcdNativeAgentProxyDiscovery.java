package com.alibaba.arthas.nat.agent.management.web.discovery.impl;

import com.alibaba.arthas.nat.agent.common.constants.NativeAgentConstants;
import com.alibaba.arthas.nat.agent.management.web.discovery.NativeAgentProxyDiscovery;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于 etcd 的 {@link NativeAgentProxyDiscovery} 实现，按前缀扫描代理注册键。
 *
 * @description: EtcdNativeAgentDiscovery implements NativeAgentDiscovery
 * @author：flzjkl
 * @date: 2024-09-15 9:19
 */
public class EtcdNativeAgentProxyDiscovery implements NativeAgentProxyDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(EtcdNativeAgentProxyDiscovery.class);


    @Override
    public List<String> listNativeAgentProxy(String address) {
        // 创建 etcd KV 客户端
        Client client = null;
        KV kvClient = null;
        List<String> res = null;
        try {
            client = Client.builder().endpoints("http://" + address).build();
            kvClient = client.getKVClient();

            // 按 NATIVE_AGENT_PROXY_KEY 前缀批量查询
            GetResponse getResponse = null;
            try {
                ByteSequence prefix = ByteSequence.from(NativeAgentConstants.NATIVE_AGENT_PROXY_KEY, StandardCharsets.UTF_8);
                GetOption option = GetOption.newBuilder().isPrefix(Boolean.TRUE).build();
                getResponse = kvClient.get(prefix, option).get();
            } catch (Exception e) {
                logger.error("get value failed with prefix" + NativeAgentConstants.NATIVE_AGENT_PROXY_KEY);
                throw new RuntimeException(e);
            }

            // 将键值对中的 value 解析为代理地址列表
            List<KeyValue> kvs = getResponse.getKvs();
            if (kvs == null || kvs.size() == 0) {
                return null;
            }
            res = new ArrayList<>(kvs.size());
            for (KeyValue kv : kvs) {
                String value = kv.getValue().toString(StandardCharsets.UTF_8);
                res.add(value);
            }
        } finally {
            if (kvClient != null) {
                kvClient.close();
            }
            if (client != null) {
                client.close();
            }
        }
        return res;
    }
}
