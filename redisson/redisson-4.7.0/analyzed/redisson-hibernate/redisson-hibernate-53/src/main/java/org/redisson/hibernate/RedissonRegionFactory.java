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
package org.redisson.hibernate;

import org.hibernate.boot.registry.selector.spi.StrategySelector;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.cfg.spi.DomainDataRegionBuildingContext;
import org.hibernate.cache.cfg.spi.DomainDataRegionConfig;
import org.hibernate.cache.spi.CacheKeysFactory;
import org.hibernate.cache.spi.DomainDataRegion;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cache.spi.support.*;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.redisson.Redisson;
import org.redisson.api.RMapCache;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.LongCodec;
import org.redisson.config.Config;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

/**
 * 基于 Redisson 的 Hibernate 二级缓存 {@link RegionFactory} 实现（Hibernate 5.3+）。
 * <p>在 Region 启动时创建并持有独立的 {@link RedissonClient} 实例。
 *
 * @author Nikita Koksharov
 */
public class RedissonRegionFactory extends RegionFactoryTemplate {
    
    private static final long serialVersionUID = 3785315696581773811L;

    /** 查询结果缓存区域的默认配置键后缀。 */
    public static final String QUERY_DEF = "query";
    
    /** 集合缓存区域的默认配置键后缀。 */
    public static final String COLLECTION_DEF = "collection";
    
    /** 实体缓存区域的默认配置键后缀。 */
    public static final String ENTITY_DEF = "entity";
    
    /** 自然 ID 缓存区域的默认配置键后缀。 */
    public static final String NATURAL_ID_DEF = "naturalid";
    
    /** 时间戳缓存区域的默认配置键后缀。 */
    public static final String TIMESTAMPS_DEF = "timestamps";
    
    /** Region 最大条目数配置键后缀。 */
    public static final String MAX_ENTRIES_SUFFIX = ".eviction.max_entries";

    /** Region TTL 配置键后缀。 */
    public static final String TTL_SUFFIX = ".expiration.time_to_live";

    /** Region 最大空闲时间配置键后缀。 */
    public static final String MAX_IDLE_SUFFIX = ".expiration.max_idle_time";
    
    /** Hibernate 属性中 Redisson 相关配置的前缀。 */
    public static final String CONFIG_PREFIX = "hibernate.cache.redisson.";
    
    /** Redisson 配置文件路径对应的 Hibernate 属性键。 */
    public static final String REDISSON_CONFIG_PATH = CONFIG_PREFIX + "config";

    /** 是否在 Redis 不可用时启用本地降级模式的属性键。 */
    public static final String FALLBACK = CONFIG_PREFIX + "fallback";

    RedissonClient redisson;
    private CacheKeysFactory cacheKeysFactory;
    protected boolean fallback;

    /** 返回启动时解析的缓存键工厂。 */
    @Override
    protected CacheKeysFactory getImplicitCacheKeysFactory() {
        return cacheKeysFactory;
    }

    /** 加载 Redisson 配置、初始化客户端并解析 fallback 与缓存键工厂。 */
    @Override
    protected void prepareForUse(SessionFactoryOptions settings, @SuppressWarnings("rawtypes") Map properties) throws CacheException {
        this.redisson = createRedissonClient(properties);

        String fallbackValue = (String) properties.getOrDefault(FALLBACK, "false");
        fallback = Boolean.parseBoolean(fallbackValue);

        StrategySelector selector = settings.getServiceRegistry().getService(StrategySelector.class);
        cacheKeysFactory = selector.resolveDefaultableStrategy(CacheKeysFactory.class, 
                properties.get(Environment.CACHE_KEYS_FACTORY), new RedissonCacheKeysFactory(redisson.getConfig().getCodec()));
    }

    /** 从类路径或指定路径加载 YAML/JSON 配置并创建 {@link RedissonClient}。 */
    protected RedissonClient createRedissonClient(Map properties) {
        Config config = null;
        if (!properties.containsKey(REDISSON_CONFIG_PATH)) {
            config = loadConfig(RedissonRegionFactory.class.getClassLoader(), "redisson.json");
            if (config == null) {
                config = loadConfig(RedissonRegionFactory.class.getClassLoader(), "redisson.yaml");
            }
        } else {
            String configPath = ConfigurationHelper.getString(REDISSON_CONFIG_PATH, properties);
            config = loadConfig(RedissonRegionFactory.class.getClassLoader(), configPath);
            if (config == null) {
                config = loadConfig(configPath);
            }
        }
        
        if (config == null) {
            throw new CacheException("Unable to locate Redisson configuration");
        }

        return Redisson.create(config);
    }
    
    private Config loadConfig(String configPath) {
        try {
            return Config.fromYAML(new File(configPath));
        } catch (Exception e) {
            throw new CacheException("Can't parse default config", e);
        }
    }
    
    private Config loadConfig(ClassLoader classLoader, String fileName) {
        InputStream is = classLoader.getResourceAsStream(fileName);
        if (is != null) {
            try {
                return Config.fromYAML(is);
            } catch (Exception e) {
                throw new CacheException("Can't parse config", e);
            }
        }
        return null;
    }

    /** 关闭 Redisson 客户端并释放连接。 */
    @Override
    protected void releaseFromUse() {
        redisson.shutdown();
    }

    /** 默认启用最小化 put 策略以减少缓存写入。 */
    @Override
    public boolean isMinimalPutsEnabledByDefault() {
        return true;
    }

    /** 默认缓存并发访问策略为 {@link AccessType#TRANSACTIONAL}。 */
    @Override
    public AccessType getDefaultAccessType() {
        return AccessType.TRANSACTIONAL;
    }

    /** 通过 Redis Lua 脚本生成全局递增时间戳；失败且启用 fallback 时使用父类本地递增。 */
    @Override
    public long nextTimestamp() {
        long time = System.currentTimeMillis() << 12;
        try {
            return redisson.getScript(LongCodec.INSTANCE).eval(RScript.Mode.READ_WRITE,
                    "local currentTime = redis.call('get', KEYS[1]);"
                            + "if currentTime == false then "
                            + "redis.call('set', KEYS[1], ARGV[1]); "
                            + "return ARGV[1]; "
                            + "end;"
                            + "local nextValue = math.max(tonumber(ARGV[1]), tonumber(currentTime) + 1); "
                            + "redis.call('set', KEYS[1], nextValue); "
                            + "return nextValue;",
                    RScript.ReturnType.LONG, Arrays.<Object>asList(qualifyName("redisson-hibernate-timestamp")), time);
        } catch (Exception e) {
            if (fallback) {
                return super.nextTimestamp();
            }
            throw e;
        }
    }

    /** 构建域数据（实体/集合/自然 ID）二级缓存 Region。 */
    @Override
    public DomainDataRegion buildDomainDataRegion(
            DomainDataRegionConfig regionConfig,
            DomainDataRegionBuildingContext buildingContext) {
        verifyStarted();
        return new DomainDataRegionImpl(
                regionConfig,
                this,
                createDomainDataStorageAccess( regionConfig, buildingContext ),
                getImplicitCacheKeysFactory(),
                buildingContext
        );
    }

    /** 根据 Region 配置创建 {@link RedissonStorage} 作为底层存储访问层。 */
    @Override
    protected DomainDataStorageAccess createDomainDataStorageAccess(DomainDataRegionConfig regionConfig,
            DomainDataRegionBuildingContext buildingContext) {
        String defaultKey = null;
        if (!regionConfig.getCollectionCaching().isEmpty()) {
            defaultKey = COLLECTION_DEF;
        } else if (!regionConfig.getEntityCaching().isEmpty()) {
            defaultKey = ENTITY_DEF;
        } else if (!regionConfig.getNaturalIdCaching().isEmpty()) {
            defaultKey = NATURAL_ID_DEF;
        } else {
            // 无法从 Region 配置推断缓存类型。
            throw new IllegalArgumentException("Unable to determine entity cache type!");
        }

        RMapCache<Object, Object> mapCache = getCache(qualifyName(regionConfig.getRegionName()), buildingContext.getSessionFactory().getProperties(), defaultKey);
        return new RedissonStorage(regionConfig.getRegionName(), mapCache, ((Redisson)redisson).getServiceManager(), buildingContext.getSessionFactory().getProperties(), defaultKey);
    }

    private String qualifyName(String name) {
        return RegionNameQualifier.INSTANCE.qualify(name, getOptions());
    }

    /** 为查询结果 Region 创建 {@link RedissonStorage} 存储访问。 */
    @Override
    protected StorageAccess createQueryResultsRegionStorageAccess(String regionName,
            SessionFactoryImplementor sessionFactory) {
        RMapCache<Object, Object> mapCache = getCache(qualifyName(regionName), sessionFactory.getProperties(), QUERY_DEF);
        return new RedissonStorage(regionName, mapCache, ((Redisson)redisson).getServiceManager(), sessionFactory.getProperties(), QUERY_DEF);
    }

    /** 为时间戳 Region 创建 {@link RedissonStorage} 存储访问。 */
    @Override
    protected StorageAccess createTimestampsRegionStorageAccess(String regionName,
            SessionFactoryImplementor sessionFactory) {
        RMapCache<Object, Object> mapCache = getCache(qualifyName(regionName), sessionFactory.getProperties(), TIMESTAMPS_DEF);
        return new RedissonStorage(regionName, mapCache, ((Redisson)redisson).getServiceManager(), sessionFactory.getProperties(), TIMESTAMPS_DEF);
    }

    /** 获取指定 Region 名称对应的 {@link RMapCache} 实例。 */
    protected RMapCache<Object, Object> getCache(String cacheName, Map properties, String defaultKey) {
        return redisson.getMapCache(cacheName);
    }
    
}
