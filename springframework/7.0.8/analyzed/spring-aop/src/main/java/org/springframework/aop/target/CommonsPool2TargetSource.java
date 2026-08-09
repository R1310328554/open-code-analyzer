/*
 * Copyright 2002-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.aop.target;

import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;

/**
 * 在可配置的 Apache Commons2 Pool 中持有对象的
 * {@link org.springframework.aop.TargetSource} 实现。
 *
 * <p>默认创建 {@code GenericObjectPool} 实例。
 * 子类可通过覆盖 {@code createObjectPool()} 更改使用的 {@code ObjectPool} 类型。
 *
 * <p>提供与 Commons Pool {@code GenericObjectPool} 类对应的诸多配置属性；
 * 构造时传入 {@code GenericObjectPool}。
 * 若创建子类以更改 {@code ObjectPool} 实现类型，
 * 请传入与所选实现相关的配置属性值。
 *
 * <p>显式不镜像 {@code testOnBorrow}、{@code testOnReturn} 与 {@code testWhileIdle}，
 * 因本类使用的 {@code PoolableObjectFactory} 实现未提供有意义的校验。
 * 所有暴露的 Commons Pool 属性均使用对应默认值。
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @author Kazuki Shimizu
 * @since 4.2
 * @see GenericObjectPool
 * @see #createObjectPool()
 * @see #setMaxSize
 * @see #setMaxIdle
 * @see #setMinIdle
 * @see #setMaxWait
 * @see #setTimeBetweenEvictionRunsMillis
 * @see #setMinEvictableIdleTimeMillis
 */
@SuppressWarnings({"rawtypes", "unchecked", "serial", "deprecation"})
public class CommonsPool2TargetSource extends AbstractPoolingTargetSource implements PooledObjectFactory<Object> {

	private int maxIdle = GenericObjectPoolConfig.DEFAULT_MAX_IDLE;

	private int minIdle = GenericObjectPoolConfig.DEFAULT_MIN_IDLE;

	private long maxWait = GenericObjectPoolConfig.DEFAULT_MAX_WAIT_MILLIS;

	private long timeBetweenEvictionRunsMillis = GenericObjectPoolConfig.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS;

	private long minEvictableIdleTimeMillis = GenericObjectPoolConfig.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS;

	private boolean blockWhenExhausted = GenericObjectPoolConfig.DEFAULT_BLOCK_WHEN_EXHAUSTED;

	/**
	 * 用于池化目标对象的 Apache Commons {@code ObjectPool}。
	 */
	private @Nullable ObjectPool pool;


	/**
	 * 以默认设置创建 CommonsPoolTargetSource。
	 * 池默认最大容量为 8。
	 * @see #setMaxSize
	 * @see GenericObjectPoolConfig#setMaxTotal
	 */
	public CommonsPool2TargetSource() {
		setMaxSize(GenericObjectPoolConfig.DEFAULT_MAX_TOTAL);
	}


	/**
	 * 设置池中空闲对象的最大数量。
	 * 默认为 8。
	 * @see GenericObjectPool#setMaxIdle
	 */
	public void setMaxIdle(int maxIdle) {
		this.maxIdle = maxIdle;
	}

	/**
	 * Return the maximum number of idle objects in the pool.
	 */
	public int getMaxIdle() {
		return this.maxIdle;
	}

	/**
	 * Set the minimum number of idle objects in the pool.
	 * Default is 0.
	 * @see GenericObjectPool#setMinIdle
	 */
	public void setMinIdle(int minIdle) {
		this.minIdle = minIdle;
	}

	/**
	 * Return the minimum number of idle objects in the pool.
	 */
	public int getMinIdle() {
		return this.minIdle;
	}

	/**
	 * Set the maximum waiting time for fetching an object from the pool.
	 * Default is -1, waiting forever.
	 * @see GenericObjectPool#setMaxWaitMillis
	 */
	public void setMaxWait(long maxWait) {
		this.maxWait = maxWait;
	}

	/**
	 * Return the maximum waiting time for fetching an object from the pool.
	 */
	public long getMaxWait() {
		return this.maxWait;
	}

	/**
	 * Set the time between eviction runs that check idle objects whether
	 * they have been idle for too long or have become invalid.
	 * Default is -1, not performing any eviction.
	 * @see GenericObjectPool#setTimeBetweenEvictionRunsMillis
	 */
	public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
		this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
	}

	/**
	 * Return the time between eviction runs that check idle objects.
	 */
	public long getTimeBetweenEvictionRunsMillis() {
		return this.timeBetweenEvictionRunsMillis;
	}

	/**
	 * Set the minimum time that an idle object can sit in the pool before
	 * it becomes subject to eviction. Default is 1800000 (30 minutes).
	 * <p>Note that eviction runs need to be performed to take this
	 * setting into effect.
	 * @see #setTimeBetweenEvictionRunsMillis
	 * @see GenericObjectPool#setMinEvictableIdleTimeMillis
	 */
	public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
		this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
	}

	/**
	 * Return the minimum time that an idle object can sit in the pool.
	 */
	public long getMinEvictableIdleTimeMillis() {
		return this.minEvictableIdleTimeMillis;
	}

	/**
	 * Set whether the call should block when the pool is exhausted.
	 */
	public void setBlockWhenExhausted(boolean blockWhenExhausted) {
		this.blockWhenExhausted = blockWhenExhausted;
	}

	/**
	 * Specify if the call should block when the pool is exhausted.
	 */
	public boolean isBlockWhenExhausted() {
		return this.blockWhenExhausted;
	}


	/**
	 * 创建并持有 ObjectPool 实例。
	 * @see #createObjectPool()
	 */
	@Override
	protected final void createPool() {
		logger.debug("正在创建 Commons 对象池");
		this.pool = createObjectPool();
	}

	/**
	 * 子类可覆盖以返回特定 Commons 池。
	 * 应在此将配置属性应用到池上。
	 * <p>默认为具有给定池大小的 GenericObjectPool 实例。
	 * @return 空的 Commons {@code ObjectPool}
	 * @see GenericObjectPool
	 * @see #setMaxSize
	 */
	protected ObjectPool createObjectPool() {
		GenericObjectPoolConfig config = new GenericObjectPoolConfig();
		config.setMaxTotal(getMaxSize());
		config.setMaxIdle(getMaxIdle());
		config.setMinIdle(getMinIdle());
		config.setMaxWaitMillis(getMaxWait());
		config.setTimeBetweenEvictionRunsMillis(getTimeBetweenEvictionRunsMillis());
		config.setMinEvictableIdleTimeMillis(getMinEvictableIdleTimeMillis());
		config.setBlockWhenExhausted(isBlockWhenExhausted());
		return new GenericObjectPool(this, config);
	}


	/**
	 * 从 {@code ObjectPool} 借用对象。
	 */
	@Override
	public Object getTarget() throws Exception {
		Assert.state(this.pool != null, "No Commons ObjectPool available");
		return this.pool.borrowObject();
	}

	/**
	 * 将指定对象归还底层 {@code ObjectPool}。
	 */
	@Override
	public void releaseTarget(Object target) throws Exception {
		if (this.pool != null) {
			this.pool.returnObject(target);
		}
	}

	@Override
	public int getActiveCount() throws UnsupportedOperationException {
		return (this.pool != null ? this.pool.getNumActive() : 0);
	}

	@Override
	public int getIdleCount() throws UnsupportedOperationException {
		return (this.pool != null ? this.pool.getNumIdle() : 0);
	}


	/**
	 * 销毁本对象时关闭底层 {@code ObjectPool}。
	 */
	@Override
	public void destroy() throws Exception {
		if (this.pool != null) {
			logger.debug("正在关闭 Commons ObjectPool");
			this.pool.close();
		}
	}


	//----------------------------------------------------------------------------
	// org.apache.commons.pool2.PooledObjectFactory 接口实现
	//----------------------------------------------------------------------------

	@Override
	public PooledObject<Object> makeObject() throws Exception {
		return new DefaultPooledObject<>(newPrototypeInstance());
	}

	@Override
	public void destroyObject(PooledObject<Object> p) throws Exception {
		destroyPrototypeInstance(p.getObject());
	}

	@Override
	public boolean validateObject(PooledObject<Object> p) {
		return true;
	}

	@Override
	public void activateObject(PooledObject<Object> p) throws Exception {
	}

	@Override
	public void passivateObject(PooledObject<Object> p) throws Exception {
	}

}
