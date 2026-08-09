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

package org.springframework.beans.factory.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCreationNotAllowedException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.core.SimpleAliasRegistry;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 共享 Bean 实例的通用注册表，实现 {@link org.springframework.beans.factory.config.SingletonBeanRegistry}。
 * 允许注册供所有调用方共享的单例实例，并按 Bean 名称获取。
 *
 * <p>同时支持注册 {@link org.springframework.beans.factory.DisposableBean} 实例
 * （它们未必与已注册的单例一一对应），以便在注册表关闭时销毁。
 * 还可登记 Bean 之间的依赖关系，从而保证正确的销毁顺序。
 *
 * <p>本类主要作为 {@link org.springframework.beans.factory.BeanFactory} 实现的基类，
 * 抽出单例 Bean 实例的通用管理逻辑。注意
 * {@link org.springframework.beans.factory.config.ConfigurableBeanFactory}
 * 接口本身也扩展了 {@link SingletonBeanRegistry}。
 *
 * <p>与 {@link AbstractBeanFactory}、{@link DefaultListableBeanFactory}（二者继承本类）不同，
 * 本类既不假设存在 BeanDefinition 概念，也不绑定具体的实例创建流程。
 * 也可以作为嵌套助手类，供外部委托调用。
 *
 * <p>核心机制简述：
 * <ul>
 * <li><b>三级缓存</b>：{@code singletonObjects}（成品）→ {@code earlySingletonObjects}（早期引用）→
 * {@code singletonFactories}（早期暴露工厂），用于解决单例循环依赖；</li>
 * <li><b>单例锁</b>：{@code singletonLock} 串行化创建路径，6.2 起配合宽松创建（lenient creation）；</li>
 * <li><b>依赖销毁</b>：通过 {@code dependentBeanMap} 先销毁依赖方，再销毁被依赖方。</li>
 * </ul>
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see #registerSingleton
 * @see #registerDisposableBean
 * @see org.springframework.beans.factory.DisposableBean
 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory
 */
public class DefaultSingletonBeanRegistry extends SimpleAliasRegistry implements SingletonBeanRegistry {

	/** 创建过程中被压制异常的最大保留条数。 */
	private static final int SUPPRESSED_EXCEPTIONS_LIMIT = 100;


	/** 单例创建共用锁，串行化「拿锁创建 / 早期引用提升」等关键路径。 */
	final Lock singletonLock = new ReentrantLock();

	/** 一级缓存：已完成创建的单例，beanName → 实例。 */
	private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

	/** 三级缓存：创建期登记的 ObjectFactory，用于循环依赖时按需产出早期引用。 */
	private final Map<String, ObjectFactory<?>> singletonFactories = new ConcurrentHashMap<>(16);

	/** 单例创建/注册完成后的自定义回调，beanName → Consumer。 */
	private final Map<String, Consumer<Object>> singletonCallbacks = new ConcurrentHashMap<>(16);

	/** 二级缓存：从工厂取出的早期单例引用，beanName → 尚未完全初始化的实例。 */
	private final Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>(16);

	/** 已注册单例名称集合，按注册顺序保存（LinkedHashSet）。 */
	private final Set<String> registeredSingletons = Collections.synchronizedSet(new LinkedHashSet<>(256));

	/** 当前正在创建中的单例 Bean 名称集合。 */
	private final Set<String> singletonsCurrentlyInCreation = ConcurrentHashMap.newKeySet(16);

	/** 被排除在「创建中检查」之外的 Bean 名称（例如某些内部场景）。 */
	private final Set<String> inCreationCheckExclusions = ConcurrentHashMap.newKeySet(16);

	/** 宽松创建（lenient creation）跟踪专用锁。 */
	private final Lock lenientCreationLock = new ReentrantLock();

	/** 宽松创建完成时的条件变量，用于唤醒等待方。 */
	private final Condition lenientCreationFinished = this.lenientCreationLock.newCondition();

	/** 当前处于宽松创建中的单例 Bean 名称。 */
	private final Set<String> singletonsInLenientCreation = new HashSet<>();

	/** 等待链：某个创建线程正在等待另一个宽松创建线程，用于检测互等死锁。 */
	private final Map<Thread, Thread> lenientWaitingThreads = new HashMap<>();

	/** 当前正在创建的 Bean → 实际创建线程。 */
	private final Map<String, Thread> currentCreationThreads = new ConcurrentHashMap<>();

	/** 是否正处于 {@link #destroySingletons} 销毁流程中。 */
	private volatile boolean singletonsCurrentlyInDestruction = false;

	/** 创建过程中被压制的异常集合，最终会挂到顶层 BeanCreationException 上。 */
	private @Nullable Set<Exception> suppressedExceptions;

	/** 可销毁 Bean：beanName → DisposableBean 实例。 */
	private final Map<String, DisposableBean> disposableBeans = new LinkedHashMap<>();

	/** 包含关系：外层 Bean → 它所包含的内嵌 Bean 名称集合。 */
	private final Map<String, Set<String>> containedBeanMap = new ConcurrentHashMap<>(16);

	/** 依赖方映射：被依赖 Bean → 依赖它的 Bean 名称集合（销毁时这些依赖方要先销毁）。 */
	private final Map<String, Set<String>> dependentBeanMap = new ConcurrentHashMap<>(64);

	/** 依赖关系反向映射：某个 Bean → 它所依赖的 Bean 名称集合。 */
	private final Map<String, Set<String>> dependenciesForBeanMap = new ConcurrentHashMap<>(64);


	@Override
	public void registerSingleton(String beanName, Object singletonObject) throws IllegalStateException {
		Assert.notNull(beanName, "Bean name must not be null");
		Assert.notNull(singletonObject, "Singleton object must not be null");
		this.singletonLock.lock();
		try {
			addSingleton(beanName, singletonObject);
		}
		finally {
			this.singletonLock.unlock();
		}
	}

	/**
	 * 将给定单例对象加入注册表。
	 * <p>供新注册/新创建的单例暴露时调用。
	 * @param beanName the name of the bean
	 * @param singletonObject the singleton object
	 */
	protected void addSingleton(String beanName, Object singletonObject) {
		Object oldObject = this.singletonObjects.putIfAbsent(beanName, singletonObject);
		if (oldObject != null) {
			throw new IllegalStateException("Could not register object [" + singletonObject +
					"] under bean name '" + beanName + "': there is already object [" + oldObject + "] bound");
		}
		// 成品已入一级缓存，清理二、三级缓存中的同名条目
		this.singletonFactories.remove(beanName);
		this.earlySingletonObjects.remove(beanName);
		this.registeredSingletons.add(beanName);

		Consumer<Object> callback = this.singletonCallbacks.get(beanName);
		if (callback != null) {
			callback.accept(singletonObject);
		}
	}

	/**
	 * 为指定单例登记一个 ObjectFactory，必要时用它构建实例。
	 * <p>用于早期暴露：例如在属性填充完成前就把工厂放进三级缓存，以便解开循环依赖。
	 * @param beanName the name of the bean
	 * @param singletonFactory the factory for the singleton object
	 */
	protected void addSingletonFactory(String beanName, ObjectFactory<?> singletonFactory) {
		Assert.notNull(singletonFactory, "Singleton factory must not be null");
		// 写入三级缓存；若二级已有早期对象则清掉，保证后续从工厂重新取
		this.singletonFactories.put(beanName, singletonFactory);
		this.earlySingletonObjects.remove(beanName);
		this.registeredSingletons.add(beanName);
	}

	@Override
	public void addSingletonCallback(String beanName, Consumer<Object> singletonConsumer) {
		this.singletonCallbacks.put(beanName, singletonConsumer);
	}

	@Override
	public @Nullable Object getSingleton(String beanName) {
		return getSingleton(beanName, true);
	}

	/**
	 * 返回以给定名称注册的（原始）单例对象。
	 * <p>会检查已实例化的单例，也允许拿到「正在创建中」的早期引用（用于解决循环依赖）。
	 * @param beanName the name of the bean to look for
	 * @param allowEarlyReference whether early references should be created or not
	 * @return the registered singleton object, or {@code null} if none found
	 */
	protected @Nullable Object getSingleton(String beanName, boolean allowEarlyReference) {
		// 先无锁查一级缓存（成品单例），命中则直接返回
		Object singletonObject = this.singletonObjects.get(beanName);
		// 一级没有，且该 Bean 正在创建中 → 才考虑二级/三级（循环依赖场景）
		if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
			// 查二级缓存：是否已有早期引用
			singletonObject = this.earlySingletonObjects.get(beanName);
			if (singletonObject == null && allowEarlyReference) {
				// 需要从三级工厂提升时，必须抢到单例锁；抢不到说明不是原创建线程在持锁推进，避免误推早期引用
				if (!this.singletonLock.tryLock()) {
					// 非创建线程不在此处做早期单例推断
					return null;
				}
				try {
					// 持锁后双重检查：一级 → 二级 → 三级工厂
					singletonObject = this.singletonObjects.get(beanName);
					if (singletonObject == null) {
						singletonObject = this.earlySingletonObjects.get(beanName);
						if (singletonObject == null) {
							ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
							if (singletonFactory != null) {
								// 调用工厂（常为 getEarlyBeanReference），得到早期对象
								singletonObject = singletonFactory.getObject();
								// 工厂仍在说明期间没被人搬走，则提升到二级并移除三级
								if (this.singletonFactories.remove(beanName) != null) {
									this.earlySingletonObjects.put(beanName, singletonObject);
								}
								else {
									// 工厂已被移除：可能已成品入一级，或创建失败被清理，以一级为准
									singletonObject = this.singletonObjects.get(beanName);
								}
							}
						}
					}
				}
				finally {
					this.singletonLock.unlock();
				}
			}
		}
		return singletonObject;
	}

	/**
	 * 返回以给定名称注册的（原始）单例对象；若尚未注册则通过工厂创建并登记。
	 * @param beanName the name of the bean
	 * @param singletonFactory the ObjectFactory to lazily create the singleton
	 * with, if necessary
	 * @return the registered singleton object
	 */
	@SuppressWarnings("NullAway") // Dataflow analysis limitation
	public Object getSingleton(String beanName, ObjectFactory<?> singletonFactory) {
		Assert.notNull(beanName, "Bean name must not be null");

		Thread currentThread = Thread.currentThread();
		// null=传统行为（总想拿满锁）；true=允许持锁但也接受宽松回退；false=明确不持锁，走宽松创建
		Boolean lockFlag = isCurrentThreadAllowedToHoldSingletonLock();
		boolean acquireLock = !Boolean.FALSE.equals(lockFlag);
		boolean locked = (acquireLock && this.singletonLock.tryLock());

		try {
			Object singletonObject = this.singletonObjects.get(beanName);
			if (singletonObject == null) {
				// 想拿锁却没拿到：按 lockFlag 分支处理
				if (acquireLock && !locked) {
					if (Boolean.TRUE.equals(lockFlag)) {
						// 另一线程正卡在工厂回调里。6.2 起：在单例锁外创建本 Bean（仍保证线程安全暴露，但依赖其它 Bean 时有碰撞风险）
						this.lenientCreationLock.lock();
						try {
							if (logger.isInfoEnabled()) {
								Set<String> lockedBeans = new HashSet<>(this.singletonsCurrentlyInCreation);
								lockedBeans.removeAll(this.singletonsInLenientCreation);
								logger.info("Obtaining singleton bean '" + beanName + "' in thread \"" +
										currentThread.getName() + "\" while other thread holds singleton " +
										"lock for other beans " + lockedBeans);
							}
							this.singletonsInLenientCreation.add(beanName);
						}
						finally {
							this.lenientCreationLock.unlock();
						}
					}
					else {
						// 无明确加锁指示（非协调引导阶段），且锁被其它创建方法占着 → 阻塞等待
						this.singletonLock.lock();
						locked = true;
						// 等待期间可能已有别的线程创建完成
						singletonObject = this.singletonObjects.get(beanName);
						if (singletonObject != null) {
							return singletonObject;
						}
					}
				}

				// 工厂正在销毁单例时禁止再创建
				if (this.singletonsCurrentlyInDestruction) {
					throw new BeanCreationNotAllowedException(beanName,
							"Singleton bean creation not allowed while singletons of this factory are in destruction " +
							"(Do not request a bean from a BeanFactory in a destroy method implementation!)");
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Creating shared instance of singleton bean '" + beanName + "'");
				}

				try {
					// 标记「正在创建」；若已在集合中则抛 BeanCurrentlyInCreationException
					beforeSingletonCreation(beanName);
				}
				catch (BeanCurrentlyInCreationException ex) {
					// 可能是另一线程在宽松创建同一 Bean：在条件变量上等待其完成
					this.lenientCreationLock.lock();
					try {
						while ((singletonObject = this.singletonObjects.get(beanName)) == null) {
							Thread otherThread = this.currentCreationThreads.get(beanName);
							// 本线程就是创建者，或形成等待环 → 真正的循环创建，原样抛出
							if (otherThread != null && (otherThread == currentThread ||
									checkDependentWaitingThreads(otherThread, currentThread))) {
								throw ex;
							}
							// 已不在宽松创建集合中，跳出等待逻辑
							if (!this.singletonsInLenientCreation.contains(beanName)) {
								break;
							}
							if (otherThread != null) {
								this.lenientWaitingThreads.put(currentThread, otherThread);
							}
							try {
								this.lenientCreationFinished.await();
							}
							catch (InterruptedException ie) {
								currentThread.interrupt();
							}
							finally {
								if (otherThread != null) {
									this.lenientWaitingThreads.remove(currentThread);
								}
							}
						}
					}
					finally {
						this.lenientCreationLock.unlock();
					}
					if (singletonObject != null) {
						return singletonObject;
					}
					if (locked) {
						throw ex;
					}
					// 未持锁时再迟到加锁，等待目标 Bean 创建结束
					this.singletonLock.lock();
					locked = true;
					singletonObject = this.singletonObjects.get(beanName);
					if (singletonObject != null) {
						return singletonObject;
					}
					beforeSingletonCreation(beanName);
				}

				boolean newSingleton = false;
				// 仅在持锁且尚未有压制异常集合时，为本轮创建开账本
				boolean recordSuppressedExceptions = (locked && this.suppressedExceptions == null);
				if (recordSuppressedExceptions) {
					this.suppressedExceptions = new LinkedHashSet<>();
				}
				try {
					// 宽松路径下，调用工厂前再查一次是否已被别人放进一级缓存
					singletonObject = this.singletonObjects.get(beanName);
					if (singletonObject == null) {
						this.currentCreationThreads.put(beanName, currentThread);
						try {
							// 真正执行创建回调（通常是 createBean）
							singletonObject = singletonFactory.getObject();
						}
						finally {
							this.currentCreationThreads.remove(beanName);
						}
						newSingleton = true;
					}
				}
				catch (IllegalStateException ex) {
					// 创建过程中单例可能已被隐式注册；有则沿用，无则继续抛
					singletonObject = this.singletonObjects.get(beanName);
					if (singletonObject == null) {
						throw ex;
					}
				}
				catch (BeanCreationException ex) {
					if (recordSuppressedExceptions) {
						for (Exception suppressedException : this.suppressedExceptions) {
							ex.addRelatedCause(suppressedException);
						}
					}
					throw ex;
				}
				finally {
					if (recordSuppressedExceptions) {
						this.suppressedExceptions = null;
					}
					afterSingletonCreation(beanName);
				}

				if (newSingleton) {
					try {
						addSingleton(beanName, singletonObject);
					}
					catch (IllegalStateException ex) {
						// 宽松创建下允许「同一实例已被隐式放入」；实例不同才失败
						Object object = this.singletonObjects.get(beanName);
						if (singletonObject != object) {
							throw ex;
						}
					}
				}
			}
			return singletonObject;
		}
		finally {
			if (locked) {
				this.singletonLock.unlock();
			}
			// 清理宽松创建标记并唤醒所有等待者
			this.lenientCreationLock.lock();
			try {
				this.singletonsInLenientCreation.remove(beanName);
				this.lenientWaitingThreads.entrySet().removeIf(
						entry -> entry.getValue() == currentThread);
				this.lenientCreationFinished.signalAll();
			}
			finally {
				this.lenientCreationLock.unlock();
			}
		}
	}

	/** 沿宽松等待链检查 candidateThread 是否已在 waitingThread 的等待路径上（防死锁）。 */
	private boolean checkDependentWaitingThreads(Thread waitingThread, Thread candidateThread) {
		Thread threadToCheck = waitingThread;
		while ((threadToCheck = this.lenientWaitingThreads.get(threadToCheck)) != null) {
			if (threadToCheck == candidateThread) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断当前线程是否允许持有单例锁。
	 * <p>默认返回 {@code null}，表示所有线程都按传统方式强制拿满锁。
	 * {@link DefaultListableBeanFactory} 会覆盖此方法，区分预实例化阶段的线程：
	 * 主线程为 {@code true}，托管后台线程为 {@code false}，未托管线程则依配置而定。
	 * @return {@code true} if the current thread is explicitly allowed to hold the
	 * lock but also accepts lenient fallback behavior, {@code false} if it is
	 * explicitly not allowed to hold the lock and therefore forced to use lenient
	 * fallback behavior, or {@code null} if there is no specific indication
	 * (traditional behavior: forced to always hold a full lock)
	 * @since 6.2
	 */
	protected @Nullable Boolean isCurrentThreadAllowedToHoldSingletonLock() {
		return null;
	}

	/**
	 * 登记单例创建期间被压制的异常（例如临时性循环引用解决失败）。
	 * <p>默认实现把异常保存在本注册表的压制异常集合中，最多 100 条，
	 * 最终作为相关原因附加到顶层 {@link BeanCreationException}。
	 * @param ex the Exception to register
	 * @see BeanCreationException#getRelatedCauses()
	 */
	protected void onSuppressedException(Exception ex) {
		if (this.suppressedExceptions != null && this.suppressedExceptions.size() < SUPPRESSED_EXCEPTIONS_LIMIT) {
			this.suppressedExceptions.add(ex);
		}
	}

	/**
	 * 从单例注册表移除指定名称的 Bean：正常销毁，或早期暴露后创建失败时的清理。
	 * @param beanName the name of the bean
	 */
	protected void removeSingleton(String beanName) {
		this.singletonObjects.remove(beanName);
		this.singletonFactories.remove(beanName);
		this.earlySingletonObjects.remove(beanName);
		this.registeredSingletons.remove(beanName);
	}

	@Override
	public boolean containsSingleton(String beanName) {
		return this.singletonObjects.containsKey(beanName);
	}

	@Override
	public String[] getSingletonNames() {
		return StringUtils.toStringArray(this.registeredSingletons);
	}

	@Override
	public int getSingletonCount() {
		return this.registeredSingletons.size();
	}


	public void setCurrentlyInCreation(String beanName, boolean inCreation) {
		Assert.notNull(beanName, "Bean name must not be null");
		if (!inCreation) {
			this.inCreationCheckExclusions.add(beanName);
		}
		else {
			this.inCreationCheckExclusions.remove(beanName);
		}
	}

	public boolean isCurrentlyInCreation(String beanName) {
		Assert.notNull(beanName, "Bean name must not be null");
		return (!this.inCreationCheckExclusions.contains(beanName) && isActuallyInCreation(beanName));
	}

	protected boolean isActuallyInCreation(String beanName) {
		return isSingletonCurrentlyInCreation(beanName);
	}

	/**
	 * 判断指定单例 Bean 是否正在整个工厂范围内创建。
	 * @param beanName the name of the bean
	 */
	public boolean isSingletonCurrentlyInCreation(@Nullable String beanName) {
		return this.singletonsCurrentlyInCreation.contains(beanName);
	}

	/**
	 * 单例创建前回调。
	 * <p>默认实现把该单例登记为「正在创建」。
	 * @param beanName the name of the singleton about to be created
	 * @see #isSingletonCurrentlyInCreation
	 */
	protected void beforeSingletonCreation(String beanName) {
		if (!this.inCreationCheckExclusions.contains(beanName) && !this.singletonsCurrentlyInCreation.add(beanName)) {
			throw new BeanCurrentlyInCreationException(beanName);
		}
	}

	/**
	 * 单例创建后回调。
	 * <p>默认实现将该单例从「正在创建」集合中移除。
	 * @param beanName the name of the singleton that has been created
	 * @see #isSingletonCurrentlyInCreation
	 */
	protected void afterSingletonCreation(String beanName) {
		if (!this.inCreationCheckExclusions.contains(beanName) && !this.singletonsCurrentlyInCreation.remove(beanName)) {
			throw new IllegalStateException("Singleton '" + beanName + "' isn't currently in creation");
		}
	}


	/**
	 * 把给定 Bean 加入本注册表的可销毁列表。
	 * <p>可销毁 Bean 通常对应已注册单例，名称一致但实例可能不同
	 * （例如为未实现 Spring {@code DisposableBean} 的单例包一层适配器）。
	 * @param beanName the name of the bean
	 * @param bean the bean instance
	 */
	public void registerDisposableBean(String beanName, DisposableBean bean) {
		synchronized (this.disposableBeans) {
			this.disposableBeans.put(beanName, bean);
		}
	}

	/**
	 * 登记两个 Bean 之间的包含关系（例如内嵌 Bean 与外层 Bean）。
	 * <p>同时把外层 Bean 登记为依赖内嵌 Bean，以便销毁顺序正确。
	 * @param containedBeanName the name of the contained (inner) bean
	 * @param containingBeanName the name of the containing (outer) bean
	 * @see #registerDependentBean
	 */
	public void registerContainedBean(String containedBeanName, String containingBeanName) {
		synchronized (this.containedBeanMap) {
			Set<String> containedBeans =
					this.containedBeanMap.computeIfAbsent(containingBeanName, key -> new LinkedHashSet<>(8));
			if (!containedBeans.add(containedBeanName)) {
				return;
			}
		}
		registerDependentBean(containedBeanName, containingBeanName);
	}

	/**
	 * 为给定 Bean 登记一个依赖方：该依赖方必须在给定 Bean 销毁之前先销毁。
	 * @param beanName the name of the bean
	 * @param dependentBeanName the name of the dependent bean
	 */
	public void registerDependentBean(String beanName, String dependentBeanName) {
		String canonicalName = canonicalName(beanName);

		synchronized (this.dependentBeanMap) {
			Set<String> dependentBeans =
					this.dependentBeanMap.computeIfAbsent(canonicalName, key -> new LinkedHashSet<>(8));
			if (!dependentBeans.add(dependentBeanName)) {
				return;
			}
		}

		synchronized (this.dependenciesForBeanMap) {
			Set<String> dependenciesForBean =
					this.dependenciesForBeanMap.computeIfAbsent(dependentBeanName, key -> new LinkedHashSet<>(8));
			dependenciesForBean.add(canonicalName);
		}
	}

	/**
	 * 判断指定依赖方是否已登记为依赖给定 Bean，或其任一传递依赖。
	 * @param beanName the name of the bean to check
	 * @param dependentBeanName the name of the dependent bean
	 * @since 4.0
	 */
	protected boolean isDependent(String beanName, String dependentBeanName) {
		synchronized (this.dependentBeanMap) {
			return isDependent(beanName, dependentBeanName, null);
		}
	}

	private boolean isDependent(String beanName, String dependentBeanName, @Nullable Set<String> alreadySeen) {
		if (alreadySeen != null && alreadySeen.contains(beanName)) {
			return false;
		}
		String canonicalName = canonicalName(beanName);
		Set<String> dependentBeans = this.dependentBeanMap.get(canonicalName);
		if (dependentBeans == null || dependentBeans.isEmpty()) {
			return false;
		}
		if (dependentBeans.contains(dependentBeanName)) {
			return true;
		}
		if (alreadySeen == null) {
			alreadySeen = new HashSet<>();
		}
		alreadySeen.add(beanName);
		for (String transitiveDependency : dependentBeans) {
			if (isDependent(transitiveDependency, dependentBeanName, alreadySeen)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断给定名称是否已有依赖方 Bean 登记。
	 * @param beanName the name of the bean to check
	 */
	protected boolean hasDependentBean(String beanName) {
		return this.dependentBeanMap.containsKey(beanName);
	}

	/**
	 * 返回所有依赖指定 Bean 的 Bean 名称（若有）。
	 * @param beanName the name of the bean
	 * @return the array of dependent bean names, or an empty array if none
	 */
	public String[] getDependentBeans(String beanName) {
		Set<String> dependentBeans = this.dependentBeanMap.get(beanName);
		if (dependentBeans == null) {
			return new String[0];
		}
		synchronized (this.dependentBeanMap) {
			return StringUtils.toStringArray(dependentBeans);
		}
	}

	/**
	 * 返回指定 Bean 所依赖的全部 Bean 名称（若有）。
	 * @param beanName the name of the bean
	 * @return the array of names of beans which the bean depends on,
	 * or an empty array if none
	 */
	public String[] getDependenciesForBean(String beanName) {
		Set<String> dependenciesForBean = this.dependenciesForBeanMap.get(beanName);
		if (dependenciesForBean == null) {
			return new String[0];
		}
		synchronized (this.dependenciesForBeanMap) {
			return StringUtils.toStringArray(dependenciesForBean);
		}
	}

	/**
	 * 销毁本注册表中的全部单例：先按注册逆序销毁 DisposableBean，再清空依赖图与三级缓存。
	 */
	public void destroySingletons() {
		if (logger.isTraceEnabled()) {
			logger.trace("Destroying singletons in " + this);
		}
		// 标记销毁中，阻止 getSingleton(factory) 再创建新单例
		this.singletonsCurrentlyInDestruction = true;

		String[] disposableBeanNames;
		synchronized (this.disposableBeans) {
			disposableBeanNames = StringUtils.toStringArray(this.disposableBeans.keySet());
		}
		// 逆序销毁：后注册的先销毁（依赖方通常更晚注册）
		for (int i = disposableBeanNames.length - 1; i >= 0; i--) {
			destroySingleton(disposableBeanNames[i]);
		}

		// 清空包含/依赖关系图
		this.containedBeanMap.clear();
		this.dependentBeanMap.clear();
		this.dependenciesForBeanMap.clear();

		// 持锁清空三级缓存与注册集合
		this.singletonLock.lock();
		try {
			clearSingletonCache();
		}
		finally {
			this.singletonLock.unlock();
		}
	}

	/**
	 * 清空本注册表中所有缓存的单例实例。
	 * @since 4.3.15
	 */
	protected void clearSingletonCache() {
		this.singletonObjects.clear();
		this.singletonFactories.clear();
		this.earlySingletonObjects.clear();
		this.registeredSingletons.clear();
		this.singletonsCurrentlyInDestruction = false;
	}

	/**
	 * 销毁给定 Bean。若存在对应的 DisposableBean 实例则委托给 {@code destroyBean}。
	 * @param beanName the name of the bean
	 * @see #destroyBean
	 */
	public void destroySingleton(String beanName) {
		// 取出并移除 DisposableBean；destroyBean 会先递归销毁依赖方
		DisposableBean disposableBean;
		synchronized (this.disposableBeans) {
			disposableBean = this.disposableBeans.remove(beanName);
		}
		destroyBean(beanName, disposableBean);

		// destroySingletons() 会在最后统一清缓存；单独销毁时才立刻 removeSingleton
		if (!this.singletonsCurrentlyInDestruction) {
			// 6.2 起：单例实例在销毁步骤之后才移除，便于 on-demand supplier 等迟到读取
			if (this.currentCreationThreads.get(beanName) == Thread.currentThread()) {
				// 本线程创建失败后的本地清理：可能走了无锁宽松创建，故不加单例锁
				removeSingleton(beanName);
			}
			else {
				this.singletonLock.lock();
				try {
					removeSingleton(beanName);
				}
				finally {
					this.singletonLock.unlock();
				}
			}
		}
	}

	/**
	 * 销毁给定 Bean。必须先销毁依赖该 Bean 的其它 Bean，再销毁自身。不应向外抛异常。
	 * @param beanName the name of the bean
	 * @param bean the bean instance to destroy
	 */
	protected void destroyBean(String beanName, @Nullable DisposableBean bean) {
		// 1) 先销毁所有依赖方（dependentBeanMap：beanName → 依赖它的那些 Bean）
		Set<String> dependentBeanNames;
		synchronized (this.dependentBeanMap) {
			// 同步取出并断开，避免并发修改同一集合
			dependentBeanNames = this.dependentBeanMap.remove(beanName);
		}
		if (dependentBeanNames != null) {
			if (logger.isTraceEnabled()) {
				logger.trace("Retrieved dependent beans for bean '" + beanName + "': " + dependentBeanNames);
			}
			for (String dependentBeanName : dependentBeanNames) {
				destroySingleton(dependentBeanName);
			}
		}

		// 2) 再执行自身 destroy
		if (bean != null) {
			try {
				bean.destroy();
			}
			catch (Throwable ex) {
				if (logger.isWarnEnabled()) {
					logger.warn("Destruction of bean with name '" + beanName + "' threw an exception", ex);
				}
			}
		}

		// 3) 销毁其所包含的内嵌 Bean
		Set<String> containedBeans;
		synchronized (this.containedBeanMap) {
			containedBeans = this.containedBeanMap.remove(beanName);
		}
		if (containedBeans != null) {
			for (String containedBeanName : containedBeans) {
				destroySingleton(containedBeanName);
			}
		}

		// 4) 从其它 Bean 的依赖方集合中把自己抹掉
		synchronized (this.dependentBeanMap) {
			for (Iterator<Map.Entry<String, Set<String>>> it = this.dependentBeanMap.entrySet().iterator(); it.hasNext();) {
				Map.Entry<String, Set<String>> entry = it.next();
				Set<String> dependenciesToClean = entry.getValue();
				dependenciesToClean.remove(beanName);
				if (dependenciesToClean.isEmpty()) {
					it.remove();
				}
			}
		}

		// 5) 清除「我依赖谁」的反向记录
		this.dependenciesForBeanMap.remove(beanName);
	}

	@Deprecated(since = "6.2")
	@Override
	public final Object getSingletonMutex() {
		return new Object();
	}

}
