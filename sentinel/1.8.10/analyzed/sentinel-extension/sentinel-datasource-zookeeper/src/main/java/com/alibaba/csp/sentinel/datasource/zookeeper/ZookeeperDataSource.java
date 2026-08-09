package com.alibaba.csp.sentinel.datasource.zookeeper;

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.apache.curator.framework.AuthInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 只读 {@code DataSource}，以 ZooKeeper 为后端存储 Sentinel 规则。
 * 通过 Curator {@link CuratorCache} 监听节点变更并实时刷新规则。
 *
 * @author guonanjun
 */
public class ZookeeperDataSource<T> extends AbstractDataSource<String, T> {

    private static final int RETRY_TIMES = 3;
    private static final int SLEEP_TIME = 1000;

    private static volatile Map<String, CuratorFramework> zkClientMap = new HashMap<>();
    private static final Object lock = new Object();


    private final ExecutorService pool = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<Runnable>(1), new NamedThreadFactory("sentinel-zookeeper-ds-update", true),
            new ThreadPoolExecutor.DiscardOldestPolicy());

    private CuratorCacheListener listener;
    private final String path;

    private CuratorFramework zkClient = null;
    private CuratorCache nodeCache = null;

    public ZookeeperDataSource(final String serverAddr, final String path, Converter<String, T> parser) {
        super(parser);
        if (StringUtil.isBlank(serverAddr) || StringUtil.isBlank(path)) {
            throw new IllegalArgumentException(String.format("Bad argument: serverAddr=[%s], path=[%s]", serverAddr, path));
        }
        this.path = path;

        init(serverAddr, null);
    }

    /**
     * Nacos 风格构造：{@code groupId/dataId} 映射为 ZK 路径 {@code /groupId/dataId}。
     */
    public ZookeeperDataSource(final String serverAddr, final String groupId, final String dataId,
                               Converter<String, T> parser) {
        super(parser);
        if (StringUtil.isBlank(serverAddr) || StringUtil.isBlank(groupId) || StringUtil.isBlank(dataId)) {
            throw new IllegalArgumentException(String.format("Bad argument: serverAddr=[%s], groupId=[%s], dataId=[%s]", serverAddr, groupId, dataId));
        }
        this.path = getPath(groupId, dataId);

        init(serverAddr, null);
    }

    /**
     * 带 ZK 认证信息的 Nacos 风格构造。
     */
    public ZookeeperDataSource(final String serverAddr, final List<AuthInfo> authInfos, final String groupId, final String dataId,
                               Converter<String, T> parser) {
        super(parser);
        if (StringUtil.isBlank(serverAddr) || StringUtil.isBlank(groupId) || StringUtil.isBlank(dataId)) {
            throw new IllegalArgumentException(String.format("Bad argument: serverAddr=[%s], authInfos=[%s], groupId=[%s], dataId=[%s]", serverAddr, authInfos, groupId, dataId));
        }
        this.path = getPath(groupId, dataId);

        init(serverAddr, authInfos);
    }

    private void init(final String serverAddr, final List<AuthInfo> authInfos) {
        initZookeeperListener(serverAddr, authInfos);
        loadInitialConfig();
    }

    private void loadInitialConfig() {
        try {
            T newValue = loadConfig();
            if (newValue == null) {
                RecordLog.warn("[ZookeeperDataSource] WARN: initial config is null, you may have to check your data source");
            }
            getProperty().updateValue(newValue);
        } catch (Exception ex) {
            RecordLog.warn("[ZookeeperDataSource] Error when loading initial config", ex);
        }
    }

    private void initZookeeperListener(final String serverAddr, final List<AuthInfo> authInfos) {
        try {

            this.listener = CuratorCacheListener.builder().forNodeCache(() -> {
                try {
                    T newValue = loadConfig();
                    RecordLog.info("[ZookeeperDataSource] New property value received for ({}, {}): {}",
                            serverAddr, path, newValue);
                    // 将新配置写入 Sentinel 属性
                    getProperty().updateValue(newValue);
                } catch (Exception ex) {
                    RecordLog.warn("[ZookeeperDataSource] loadConfig exception", ex);
                }
            }).build();

            String zkKey = getZkKey(serverAddr, authInfos);
            if (zkClientMap.containsKey(zkKey)) {
                this.zkClient = zkClientMap.get(zkKey);
            } else {
                synchronized (lock) {
                    if (!zkClientMap.containsKey(zkKey)) {
                        CuratorFramework zc = null;
                        if (authInfos == null || authInfos.size() == 0) {
                            zc = CuratorFrameworkFactory.newClient(serverAddr, new ExponentialBackoffRetry(SLEEP_TIME, RETRY_TIMES));
                        } else {
                            zc = CuratorFrameworkFactory.builder().
                                    connectString(serverAddr).
                                    retryPolicy(new ExponentialBackoffRetry(SLEEP_TIME, RETRY_TIMES)).
                                    authorization(authInfos).
                                    build();
                        }
                        this.zkClient = zc;
                        this.zkClient.start();
                        Map<String, CuratorFramework> newZkClientMap = new HashMap<>(zkClientMap.size());
                        newZkClientMap.putAll(zkClientMap);
                        newZkClientMap.put(zkKey, zc);
                        zkClientMap = newZkClientMap;
                    } else {
                        this.zkClient = zkClientMap.get(zkKey);
                    }
                }
            }

            this.nodeCache = CuratorCache.build(this.zkClient, this.path);
            this.nodeCache.listenable().addListener(this.listener, this.pool);
            this.nodeCache.start();
        } catch (Exception e) {
            RecordLog.warn("[ZookeeperDataSource] Error occurred when initializing Zookeeper data source", e);
            e.printStackTrace();
        }
    }

    @Override
    public String readSource() throws Exception {
        if (this.zkClient == null) {
            throw new IllegalStateException("Zookeeper has not been initialized or error occurred");
        }
        String configInfo = null;
        ChildData childData = nodeCache.get(path).orElse(null);
        if (null != childData && childData.getData() != null) {

            configInfo = new String(childData.getData());
        }
        return configInfo;
    }

    @Override
    public void close() throws Exception {
        if (this.nodeCache != null) {
            this.nodeCache.listenable().removeListener(listener);
            this.nodeCache.close();
        }
        if (this.zkClient != null) {
            this.zkClient.close();
        }
        pool.shutdown();
    }

    private String getPath(String groupId, String dataId) {
        return String.format("/%s/%s", groupId, dataId);
    }

    private String getZkKey(final String serverAddr, final List<AuthInfo> authInfos) {
        if (authInfos == null || authInfos.size() == 0) {
            return serverAddr;
        }
        StringBuilder builder = new StringBuilder(64);
        builder.append(serverAddr).append(getAuthInfosKey(authInfos));
        return builder.toString();
    }

    private String getAuthInfosKey(List<AuthInfo> authInfos) {
        StringBuilder builder = new StringBuilder(32);
        for (AuthInfo authInfo : authInfos) {
            if (authInfo == null) {
                builder.append("{}");
            } else {
                builder.append("{" + "sc=" + authInfo.getScheme() + ",au=" + Arrays.toString(authInfo.getAuth()) + "}");
            }
        }
        return builder.toString();
    }

    protected CuratorFramework getZkClient() {
        return this.zkClient;
    }


}
