package org.keycloak.cache;

/**
 * 本地非集群内存缓存接口，用于优化单节点数据访问。
 * <p>实现由 {@link LocalCacheProvider} 创建，调用方负责 {@link #close()} 释放资源。</p>
 *
 * A {@link LocalCache} should be used when a local, non-clustered, cache is required to optimise data access.
 *
 * @param <K> the type of the cache Keys used for lookup
 * @param <V> the type of the cache Values to be stored
 */
public interface LocalCache<K, V> extends AutoCloseable {

    /**
     * 返回键关联的缓存值，未命中时返回 {@code null}。
     *
     * Returns the value associated with the {@code key}, or {@code null} if there is no
     * cached value for the {@code key}.
     *
     * @param key the key whose associated value is to be returned
     * @return the value associated with the specified key or {@code null} if no value exists
     * @throws NullPointerException if the specified key is null
     */
    V get(K key);

    /**
     * 将值写入缓存，若键已存在则覆盖旧值。
     *
     * Associates the value with the key in this cache.
     * If the cache previously contained a value associated with the key, the old value is replaced by the new value.
     *
     * @param key the key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @throws NullPointerException if the specified key or value is null
     */
    void put(K key, V value);

    /**
     * 使指定键的缓存条目失效。
     *
     * Removes the cached value for the specified {@code key}.
     *
     * @param key the key whose mapping is to be removed from the cache
     * @throws NullPointerException if the specified key is null
     */
    void invalidate(K key);

    /**
     * 关闭缓存并释放关联资源。
     *
     * Closes all resources associated with the cache.
     */
    void close();
}
