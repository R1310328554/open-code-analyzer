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

package org.springframework.aop.framework;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aopalliance.aop.Advice;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.Advisor;
import org.springframework.aop.DynamicIntroductionAdvice;
import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.IntroductionInfo;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.TargetSource;
import org.springframework.aop.support.DefaultIntroductionAdvisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.target.EmptyTargetSource;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

/**
 * AOP 代理配置管理器的基类。
 * <p> 这些本身不是 AOP 代理，但此类的子类通常是直接获取 AOP 代理实例的工厂。
 * <p>该类释放了子类的建议和顾问的管理工作，但实际上并不实现由子类提供的代理创建方法。
 * <p>该类是可序列化的；子类不需要。
 * <p>该类用于保存代理的快照。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @see org.springframework.aop.framework.AopProxy
 */
public class AdvisedSupport extends ProxyConfig implements Advised {

	/**
	 */
	private static final long serialVersionUID = 2651364800145442165L;

	private static final Advisor[] EMPTY_ADVISOR_ARRAY = new Advisor[0];


	/**
	 * 没有目标时的规范 TargetSource，并且行为由顾问提供。
	 */
	public static final TargetSource EMPTY_TARGET_SOURCE = EmptyTargetSource.INSTANCE;


	/**
	 */
	@SuppressWarnings("serial")
	TargetSource targetSource = EMPTY_TARGET_SOURCE;

	/**
	 */
	private boolean preFiltered = false;

	/**
	 */
	@SuppressWarnings("serial")
	private AdvisorChainFactory advisorChainFactory = DefaultAdvisorChainFactory.INSTANCE;

	/**
	 * 由代理实现的接口。保存在List中，保持注册顺序，以指定接口顺序创建JDK代理。
	 */
	@SuppressWarnings("serial")
	private List<Class<?>> interfaces = new ArrayList<>();

	/**
	 * 顾问名单。如果添加了建议，则在添加到此列表之前，它将被包装在顾问中。
	 */
	@SuppressWarnings("serial")
	private List<Advisor> advisors = new ArrayList<>();

	/**
	 * 最小 {@link AdvisorKeyEntry} 实例的列表，在减少时分配给 {@link #advisors} 字段。
	 * @since 6.0.10
	 * @see #reduceToAdvisorKey
	 */
	@SuppressWarnings("serial")
	private List<Advisor> advisorKey = this.advisors;

	/**
	 */
	private transient @Nullable Map<MethodCacheKey, List<Object>> methodCache;

	/**
	 */
	private transient volatile @Nullable List<Object> cachedInterceptors;

	/**
	 * {@link AopProxy} 实现存储元数据的可选字段。由 {@link JdkDynamicAopProxy} 使用。
	 * @since 6.1.3
	 * @see JdkDynamicAopProxy#JdkDynamicAopProxy(AdvisedSupport)
	 */
	transient volatile @Nullable Object proxyMetadataCache;


	/**
	 * 用作 JavaBean 的无参数构造函数。
	 */
	public AdvisedSupport() {
	}

	/**
	 * 使用给定参数创建 {@code AdvisedSupport} 实例。
	 * @param interfaces 代理接口
	 */
	public AdvisedSupport(Class<?>... interfaces) {
		setInterfaces(interfaces);
	}


	/**
	 * 将给定对象设置为目标。 <p>将为该对象创建一个SingletonTargetSource。
	 * @see #setTargetSource
	 * @see org.springframework.aop.target.SingletonTargetSource
	 */
	public void setTarget(Object target) {
		setTargetSource(new SingletonTargetSource(target));
	}

	/**
	 * 设置 Target Source（`TargetSource`）。
	 */
	@Override
	public void setTargetSource(@Nullable TargetSource targetSource) {
		this.targetSource = (targetSource != null ? targetSource : EMPTY_TARGET_SOURCE);
	}

	/**
	 * 获取 Target Source（`TargetSource`）。
	 */
	@Override
	public TargetSource getTargetSource() {
		return this.targetSource;
	}

	/**
	 * 设置要代理的目标类，指示代理应该可转换为给定的类。 <p> 在内部，将使用给定目标类的 {@link org.springframework.aop.target.EmptyT
	 * argetSource}。所需的代理类型将根据代理的实际创建来确定。 <p>这是设置“targetSource”或“target”的替代方案，适用于我们想要基于目标类（可以是接
	 * 口或具体类）的代理，而没有完全可用的 TargetSource 的情况。
	 * @see #setTargetSource
	 * @see #setTarget
	 */
	public void setTargetClass(@Nullable Class<?> targetClass) {
		this.targetSource = EmptyTargetSource.forClass(targetClass);
	}

	/**
	 * 获取 Target Class（`TargetClass`）。
	 */
	@Override
	public @Nullable Class<?> getTargetClass() {
		return this.targetSource.getTargetClass();
	}

	/**
	 * 设置 Pre Filtered（`PreFiltered`）。
	 */
	@Override
	public void setPreFiltered(boolean preFiltered) {
		this.preFiltered = preFiltered;
	}

	/**
	 * 判断是否 Pre Filtered。
	 */
	@Override
	public boolean isPreFiltered() {
		return this.preFiltered;
	}

	/**
	 * 设置要使用的顾问链工厂。 <p>Default 是 {@link DefaultAdvisorChainFactory}。
	 */
	public void setAdvisorChainFactory(AdvisorChainFactory advisorChainFactory) {
		Assert.notNull(advisorChainFactory, "AdvisorChainFactory must not be null");
		this.advisorChainFactory = advisorChainFactory;
	}

	/**
	 * 返回顾问链工厂以供使用（绝不是 {@code null}）。
	 */
	public AdvisorChainFactory getAdvisorChainFactory() {
		return this.advisorChainFactory;
	}


	/**
	 * 设置要代理的接口。
	 */
	public void setInterfaces(Class<?>... interfaces) {
		Assert.notNull(interfaces, "Interfaces must not be null");
		this.interfaces.clear();
		for (Class<?> ifc : interfaces) {
			addInterface(ifc);
		}
	}

	/**
	 * 添加新的代理接口。
	 * @param ifc 代理的附加接口
	 */
	public void addInterface(Class<?> ifc) {
		Assert.notNull(ifc, "Interface must not be null");
		if (!ifc.isInterface()) {
			throw new IllegalArgumentException("[" + ifc.getName() + "] is not an interface");
		}
		if (!this.interfaces.contains(ifc)) {
			this.interfaces.add(ifc);
			adviceChanged();
		}
	}

	/**
	 * 删除代理接口。如果给定的接口未被代理，<p> 不执行任何操作。
	 * @param ifc 要从代理中删除的接口
	 * @return true} 如果接口被删除； {@code false} 如果未找到接口，因此无法删除
	 */
	public boolean removeInterface(Class<?> ifc) {
		return this.interfaces.remove(ifc);
	}

	/**
	 * 获取 Proxied Interfaces（`ProxiedInterfaces`）。
	 */
	@Override
	public Class<?>[] getProxiedInterfaces() {
		return ClassUtils.toClassArray(this.interfaces);
	}

	/**
	 * 判断是否 Interface Proxied。
	 */
	@Override
	public boolean isInterfaceProxied(Class<?> ifc) {
		for (Class<?> proxyIntf : this.interfaces) {
			if (ifc.isAssignableFrom(proxyIntf)) {
				return true;
			}
		}
		return false;
	}

	boolean hasUserSuppliedInterfaces() {
		for (Class<?> ifc : this.interfaces) {
			if (!SpringProxy.class.isAssignableFrom(ifc) && !isAdvisorIntroducedInterface(ifc)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断是否 Advisor Introduced Interface。
	 */
	private boolean isAdvisorIntroducedInterface(Class<?> ifc) {
		for (Advisor advisor : this.advisors) {
			if (advisor instanceof IntroductionAdvisor introductionAdvisor) {
				for (Class<?> introducedInterface : introductionAdvisor.getInterfaces()) {
					if (introducedInterface == ifc) {
						return true;
					}
				}
			}
		}
		return false;
	}


	/**
	 * 获取 Advisors（`Advisors`）。
	 */
	@Override
	public final Advisor[] getAdvisors() {
		return this.advisors.toArray(EMPTY_ADVISOR_ARRAY);
	}

	/**
	 * 获取 Advisor Count（`AdvisorCount`）。
	 */
	@Override
	public int getAdvisorCount() {
		return this.advisors.size();
	}

	/**
	 * 添加：Advisor（方法 `addAdvisor`）。
	 */
	@Override
	public void addAdvisor(Advisor advisor) {
		int pos = this.advisors.size();
		addAdvisor(pos, advisor);
	}

	/**
	 * 添加：Advisor（方法 `addAdvisor`）。
	 */
	@Override
	public void addAdvisor(int pos, Advisor advisor) throws AopConfigException {
		if (advisor instanceof IntroductionAdvisor introductionAdvisor) {
			validateIntroductionAdvisor(introductionAdvisor);
		}
		addAdvisorInternal(pos, advisor);
	}

	/**
	 * 移除：Advisor（方法 `removeAdvisor`）。
	 */
	@Override
	public boolean removeAdvisor(Advisor advisor) {
		int index = indexOf(advisor);
		if (index == -1) {
			return false;
		}
		else {
			removeAdvisor(index);
			return true;
		}
	}

	/**
	 * 移除：Advisor（方法 `removeAdvisor`）。
	 */
	@Override
	public void removeAdvisor(int index) throws AopConfigException {
		if (isFrozen()) {
			throw new AopConfigException("Cannot remove Advisor: Configuration is frozen.");
		}
		if (index < 0 || index > this.advisors.size() - 1) {
			throw new AopConfigException("Advisor index " + index + " is out of bounds: " +
					"This configuration only has " + this.advisors.size() + " advisors.");
		}

		Advisor advisor = this.advisors.remove(index);
		if (advisor instanceof IntroductionAdvisor introductionAdvisor) {
			// 我们需要删除介绍界面。
			for (Class<?> ifc : introductionAdvisor.getInterfaces()) {
				removeInterface(ifc);
			}
		}

		adviceChanged();
	}

	/**
	 * 方法 `indexOf`：完成本类中与「index Of」相关的职责。
	 */
	@Override
	public int indexOf(Advisor advisor) {
		Assert.notNull(advisor, "Advisor must not be null");
		return this.advisors.indexOf(advisor);
	}

	/**
	 * 方法 `replaceAdvisor`：完成本类中与「replace Advisor」相关的职责。
	 */
	@Override
	public boolean replaceAdvisor(Advisor a, Advisor b) throws AopConfigException {
		Assert.notNull(a, "Advisor a must not be null");
		Assert.notNull(b, "Advisor b must not be null");
		int index = indexOf(a);
		if (index == -1) {
			return false;
		}
		removeAdvisor(index);
		addAdvisor(index, b);
		return true;
	}

	/**
	 * 将所有给定的顾问程序添加到此代理配置中。
	 * @param advisors 顾问进行注册
	 */
	public void addAdvisors(Advisor... advisors) {
		addAdvisors(Arrays.asList(advisors));
	}

	/**
	 * 将所有给定的顾问程序添加到此代理配置中。
	 * @param advisors 顾问进行注册
	 */
	public void addAdvisors(Collection<Advisor> advisors) {
		if (isFrozen()) {
			throw new AopConfigException("Cannot add advisor: Configuration is frozen.");
		}
		if (!CollectionUtils.isEmpty(advisors)) {
			for (Advisor advisor : advisors) {
				if (advisor instanceof IntroductionAdvisor introductionAdvisor) {
					validateIntroductionAdvisor(introductionAdvisor);
				}
				Assert.notNull(advisor, "Advisor must not be null");
				this.advisors.add(advisor);
			}
			adviceChanged();
		}
	}

	/**
	 * 校验：Introduction Advisor（方法 `validateIntroductionAdvisor`）。
	 */
	private void validateIntroductionAdvisor(IntroductionAdvisor advisor) {
		advisor.validateInterfaces();
		// 如果顾问通过了验证，我们就可以进行更改。
		for (Class<?> ifc : advisor.getInterfaces()) {
			addInterface(ifc);
		}
	}

	/**
	 * 添加：Advisor Internal（方法 `addAdvisorInternal`）。
	 */
	private void addAdvisorInternal(int pos, Advisor advisor) throws AopConfigException {
		Assert.notNull(advisor, "Advisor must not be null");
		if (isFrozen()) {
			throw new AopConfigException("Cannot add advisor: Configuration is frozen.");
		}
		if (pos > this.advisors.size()) {
			throw new IllegalArgumentException(
					"Illegal position " + pos + " in advisor list with size " + this.advisors.size());
		}
		this.advisors.add(pos, advisor);
		adviceChanged();
	}

	/**
	 * 允许对 {@link List} 或 {@link Advisor Advisors} 进行不受控制的访问。 <p> 请谨慎使用，并在进行任何修改时记住 {@link
	 * #adviceChanged() fire advice changed events}。
	 */
	protected final List<Advisor> getAdvisorsInternal() {
		return this.advisors;
	}

	/**
	 * 添加：Advice（方法 `addAdvice`）。
	 */
	@Override
	public void addAdvice(Advice advice) throws AopConfigException {
		int pos = this.advisors.size();
		addAdvice(pos, advice);
	}

	/**
	 * 除非建议实现了IntroductionInfo，否则无法以这种方式添加介绍。
	 */
	@Override
	public void addAdvice(int pos, Advice advice) throws AopConfigException {
		Assert.notNull(advice, "Advice must not be null");
		if (advice instanceof IntroductionInfo introductionInfo) {
			// 我们不需要这种介绍的IntroductionAdvisor：
			// 它是完全自我描述的。
			addAdvisor(pos, new DefaultIntroductionAdvisor(advice, introductionInfo));
		}
		else if (advice instanceof DynamicIntroductionAdvice) {
			// 我们需要一个IntroductionAdvisor来进行这种介绍。
			throw new AopConfigException("DynamicIntroductionAdvice may only be added as part of IntroductionAdvisor");
		}
		else {
			addAdvisor(pos, new DefaultPointcutAdvisor(advice));
		}
	}

	/**
	 * 移除：Advice（方法 `removeAdvice`）。
	 */
	@Override
	public boolean removeAdvice(Advice advice) throws AopConfigException {
		int index = indexOf(advice);
		if (index == -1) {
			return false;
		}
		else {
			removeAdvisor(index);
			return true;
		}
	}

	/**
	 * 方法 `indexOf`：完成本类中与「index Of」相关的职责。
	 */
	@Override
	public int indexOf(Advice advice) {
		Assert.notNull(advice, "Advice must not be null");
		for (int i = 0; i < this.advisors.size(); i++) {
			Advisor advisor = this.advisors.get(i);
			if (advisor.getAdvice() == advice) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 给定的建议是否包含在该代理配置中的任何顾问中？
	 * @param advice 检查包含的建议
	 * @return 包含此建议实例
	 */
	public boolean adviceIncluded(@Nullable Advice advice) {
		if (advice != null) {
			for (Advisor advisor : this.advisors) {
				if (advisor.getAdvice() == advice) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 计算给定类别的建议。
	 * @param adviceClass 要检查的建议类
	 * @return 该类或子类的拦截器的数量
	 */
	public int countAdvicesOfType(@Nullable Class<?> adviceClass) {
		int count = 0;
		if (adviceClass != null) {
			for (Advisor advisor : this.advisors) {
				if (adviceClass.isInstance(advisor.getAdvice())) {
					count++;
				}
			}
		}
		return count;
	}


	/**
	 * 根据此配置确定给定方法的 {@link org.aopalliance.intercept.MethodInterceptor} 对象列表。
	 * @param method 代理方法
	 * @param targetClass 目标类别
	 * @return MethodInterceptors 列表（也可能包括 InterceptorAndDynamicMethodMatchers）
	 */
	public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Method method, @Nullable Class<?> targetClass) {
		List<Object> cachedInterceptors;
		if (this.methodCache != null) {
			// 用于特定于方法的切入点的特定于方法的缓存
			MethodCacheKey cacheKey = new MethodCacheKey(method);
			cachedInterceptors = this.methodCache.get(cacheKey);
			if (cachedInterceptors == null) {
				cachedInterceptors = this.advisorChainFactory.getInterceptorsAndDynamicInterceptionAdvice(
						this, method, targetClass);
				this.methodCache.put(cacheKey, cachedInterceptors);
			}
		}
		else {
			// 共享缓存，因为没有特定于方法的顾问程序（见下文）。
			cachedInterceptors = this.cachedInterceptors;
			if (cachedInterceptors == null) {
				cachedInterceptors = this.advisorChainFactory.getInterceptorsAndDynamicInterceptionAdvice(
						this, method, targetClass);
				this.cachedInterceptors = cachedInterceptors;
			}
		}
		return cachedInterceptors;
	}

	/**
	 * 当建议更改时调用。
	 */
	protected void adviceChanged() {
		this.methodCache = null;
		this.cachedInterceptors = null;
		this.proxyMetadataCache = null;

		// 必要时初始化方法缓存；否则，
		// cachedInterceptors 将被共享（见上文）。
		for (Advisor advisor : this.advisors) {
			if (advisor instanceof PointcutAdvisor) {
				this.methodCache = new ConcurrentHashMap<>();
				break;
			}
		}
	}

	/**
	 * 在由无参数构造函数创建的新实例上调用此方法，以从给定对象创建配置的独立副本。
	 * @param other 要从中复制配置的 AdvisedSupport 对象
	 */
	protected void copyConfigurationFrom(AdvisedSupport other) {
		copyConfigurationFrom(other, other.targetSource, new ArrayList<>(other.advisors));
	}

	/**
	 * 从给定的 {@link AdvisedSupport} 对象复制 AOP 配置，但允许替换新的 {@link TargetSource} 和给定的拦截器链。
	 * @param other 用于获取代理配置的 {@code AdvisedSupport} 对象
	 * @param targetSource 新的目标源
	 * @param advisors 连锁顾问
	 */
	protected void copyConfigurationFrom(AdvisedSupport other, TargetSource targetSource, List<Advisor> advisors) {
		copyFrom(other);
		this.targetSource = targetSource;
		this.advisorChainFactory = other.advisorChainFactory;
		this.interfaces = new ArrayList<>(other.interfaces);
		for (Advisor advisor : advisors) {
			if (advisor instanceof IntroductionAdvisor introductionAdvisor) {
				validateIntroductionAdvisor(introductionAdvisor);
			}
			Assert.notNull(advisor, "Advisor must not be null");
			this.advisors.add(advisor);
		}
		adviceChanged();
	}

	/**
	 * 构建此 {@link AdvisedSupport} 的仅配置副本，替换 {@link TargetSource}。
	 */
	AdvisedSupport getConfigurationOnlyCopy() {
		AdvisedSupport copy = new AdvisedSupport();
		copy.copyFrom(this);
		copy.targetSource = EmptyTargetSource.forClass(getTargetClass(), getTargetSource().isStatic());
		copy.preFiltered = this.preFiltered;
		copy.advisorChainFactory = this.advisorChainFactory;
		copy.interfaces = new ArrayList<>(this.interfaces);
		copy.advisors = new ArrayList<>(this.advisors);
		copy.advisorKey = new ArrayList<>(this.advisors.size());
		for (Advisor advisor : this.advisors) {
			copy.advisorKey.add(new AdvisorKeyEntry(advisor));
		}
		copy.methodCache = this.methodCache;
		copy.cachedInterceptors = this.cachedInterceptors;
		copy.proxyMetadataCache = this.proxyMetadataCache;
		return copy;
	}

	void reduceToAdvisorKey() {
		this.advisors = this.advisorKey;
		this.methodCache = null;
		this.cachedInterceptors = null;
		this.proxyMetadataCache = null;
	}

	Object getAdvisorKey() {
		return this.advisorKey;
	}


	/**
	 * 方法 `toProxyConfigString`：完成本类中与「to Proxy Config String」相关的职责。
	 */
	@Override
	public String toProxyConfigString() {
		return toString();
	}

	/**
	 * 用于调试/诊断用途。
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getName());
		sb.append(": ").append(this.interfaces.size()).append(" interfaces ");
		sb.append(ClassUtils.classNamesToString(this.interfaces)).append("; ");
		sb.append(this.advisors.size()).append(" advisors ");
		sb.append(this.advisors).append("; ");
		sb.append("targetSource [").append(this.targetSource).append("]; ");
		sb.append(super.toString());
		return sb.toString();
	}


	//---------------------------------------------------------------------
	// 序列化支持
	//---------------------------------------------------------------------

	/**
	 * 方法 `readObject`：完成本类中与「read Object」相关的职责。
	 */
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		// 依赖默认序列化；只需在反序列化后初始化状态即可。
		ois.defaultReadObject();

		// 如有必要，初始化方法缓存。
		adviceChanged();
	}


	/**
	 * 围绕方法的简单包装类。用作缓存方法时的键，以进行有效的 equals 和 hashCode 比较。
	 */
	private static final class MethodCacheKey implements Comparable<MethodCacheKey> {

		private final Method method;

		private final int hashCode;

		public MethodCacheKey(Method method) {
			this.method = method;
			this.hashCode = method.hashCode();
		}

		@Override
		public boolean equals(@Nullable Object other) {
			return (this == other || (other instanceof MethodCacheKey that &&
					(this.method == that.method || this.method.equals(that.method))));
		}

		@Override
		public int hashCode() {
			return this.hashCode;
		}

		@Override
		public String toString() {
			return this.method.toString();
		}

		@Override
		public int compareTo(MethodCacheKey other) {
			int result = this.method.getName().compareTo(other.method.getName());
			if (result == 0) {
				result = this.method.toString().compareTo(other.method.toString());
			}
			return result;
		}
	}


	/**
	 * {@link Advisor} 实例的存根，仅用于关键目的，允许与建议类和切入点进行有效的 equals 和 hashCode 比较。
	 * @since 6.0.10
	 * @see #getConfigurationOnlyCopy()
	 * @see #getAdvisorKey()
	 */
	private static final class AdvisorKeyEntry implements Advisor {

		private final Class<?> adviceType;

		private final @Nullable String classFilterKey;

		private final @Nullable String methodMatcherKey;

		public AdvisorKeyEntry(Advisor advisor) {
			this.adviceType = advisor.getAdvice().getClass();
			if (advisor instanceof PointcutAdvisor pointcutAdvisor) {
				Pointcut pointcut = pointcutAdvisor.getPointcut();
				this.classFilterKey = pointcut.getClassFilter().toString();
				this.methodMatcherKey = pointcut.getMethodMatcher().toString();
			}
			else {
				this.classFilterKey = null;
				this.methodMatcherKey = null;
			}
		}

		@Override
		public Advice getAdvice() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean equals(@Nullable Object other) {
			return (this == other || (other instanceof AdvisorKeyEntry that &&
					this.adviceType == that.adviceType &&
					ObjectUtils.nullSafeEquals(this.classFilterKey, that.classFilterKey) &&
					ObjectUtils.nullSafeEquals(this.methodMatcherKey, that.methodMatcherKey)));
		}

		@Override
		public int hashCode() {
			return this.adviceType.hashCode();
		}
	}

}
