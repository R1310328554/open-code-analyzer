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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.Interceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.Advisor;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.adapter.AdvisorAdapterRegistry;
import org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry;
import org.springframework.aop.framework.adapter.UnknownAdviceTypeException;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * {@link org.springframework.beans.factory.FactoryBean} 实现，基于 Spring {@link
 * org.springframework.beans.factory.BeanFactory} 中的 bean 构建 AOP 代理。
 * <p>{@link org.aopalliance.intercept.MethodInterceptor MethodInterceptors} 和 {@link
 * org.springframework.aop.Advisor Advisors} 由当前 bean 工厂中的 bean
 * 名称列表来标识，通过“interceptorNames”属性指定。列表中的最后一个条目可以是目标 bean 的名称或 {@link
 * org.springframework.aop.TargetSource}；但是，通常最好使用“targetName”/“target”/“targetSource”属性。
 * <p>可在工厂级别添加全局 Interceptor 与 Advisor。指定的在拦截器列表中展开，其中列表中包含“xxx*”条目，将给定的前缀与 bean 名称相匹配 –例如，“global*
 * ”将匹配“globalBean1”和“globalBean2”；而“*”将匹配所有定义的拦截器。如果匹配的拦截器实现了 {@link org.springframework.c
 * ore.Ordered} 接口，则按返回的 order 值排序应用匹配的 Interceptor。
 * <p> 在给定代理接口时创建 JDK 代理，如果没有给定代理接口，则为实际目标类创建 CGLIB 代理。请注意，后者仅在目标类没有最终方法时才有效，因为将在运行时创建动态子类。
 * <p> 可以将从该工厂获取的代理转换为 {@link Advised}，或者获取 ProxyFactoryBean 引用并以编程方式操作它。这不适用于现有的独立原型参考。然而，
 * 它适用于随后从工厂获得的原型。对拦截的更改将立即对单例（包括现有引用）起作用。但是，要更改接口或目标，需要从工厂获取新实例。这意味着从工厂获得的单例实例不具有相同的对象标识。但
 * 是，它们确实具有相同的拦截器和目标，并且更改任何引用都会更改所有对象。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #setInterceptorNames
 * @see #setProxyInterfaces
 * @see org.aopalliance.intercept.MethodInterceptor
 * @see org.springframework.aop.Advisor
 * @see Advised
 */
@SuppressWarnings("serial")
public class ProxyFactoryBean extends ProxyCreatorSupport
		implements FactoryBean<Object>, BeanClassLoaderAware, BeanFactoryAware {

	/**
	 * 拦截器列表中的值中的此后缀表示扩展全局变量。
	 */
	public static final String GLOBAL_SUFFIX = "*";


	/**
	 * 获取 Log（`Log`）。
	 */
	private static final Log logger = LogFactory.getLog(ProxyFactoryBean.class);

	/** 名称相关状态（`interceptorNames`）。 */
	private String @Nullable [] interceptorNames;

	/** 名称相关状态（`targetName`）。 */
	private @Nullable String targetName;

	/** `true`：该类的成员状态。 */
	private boolean autodetectInterfaces = true;

	/** `true`：该类的成员状态。 */
	private boolean singleton = true;

	/**
	 * 获取 Instance（`Instance`）。
	 */
	private AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

	/** `false`：该类的成员状态。 */
	private boolean freezeProxy = false;

	/**
	 * 获取 Default Class Loader（`DefaultClassLoader`）。
	 */
	private transient @Nullable ClassLoader proxyClassLoader = ClassUtils.getDefaultClassLoader();

	/** `false`：该类的成员状态。 */
	private transient boolean classLoaderConfigured = false;

	/** 底层 BeanFactory 引用。 */
	private transient @Nullable BeanFactory beanFactory;

	/**
	 */
	private boolean advisorChainInitialized = false;

	/**
	 */
	private @Nullable Object singletonInstance;


	/**
	 * 设置我们正在代理的接口的名称。如果没有给出接口，则会创建实际类的 CGLIB。 <p>这本质上等同于“setInterfaces”方法，但镜像TransactionProxyF
	 * actoryBean的“setProxyInterfaces”。
	 * @see #setInterfaces
	 * @see AbstractSingletonProxyFactoryBean#setProxyInterfaces
	 */
	public void setProxyInterfaces(Class<?>[] proxyInterfaces) throws ClassNotFoundException {
		setInterfaces(proxyInterfaces);
	}

	/**
	 * 设置 Advice/Advisor bean 名称列表。必须始终将其设置为在 Bean 工厂中使用该工厂 Bean。
	 * <p>引用的bean应该是Interceptor、Advisor或Advice类型。列表中的最后一个条目可以是工厂中任何bean的名称。如果它既不是 Advice 也不是
	 * Advisor，则会添加一个新的 SingletonTargetSource
	 * 来包装它。如果设置了“target”、“targetSource”或“targetName”属性，则不能使用此类目标
	 * bean，在这种情况下，“interceptorNames”数组必须仅包含 Advice/Advisor bean 名称。
	 * <p><b>NOTE：在“interceptorNames”列表中指定目标 bean 作为最终名称已被弃用，并将在未来的 Spring 版本中删除。</b> 请改用
	 * {@link #setTargetName "targetName"} 属性。
	 * @see org.aopalliance.intercept.MethodInterceptor
	 * @see org.springframework.aop.Advisor
	 * @see org.aopalliance.aop.Advice
	 * @see org.springframework.aop.target.SingletonTargetSource
	 */
	public void setInterceptorNames(String... interceptorNames) {
		this.interceptorNames = interceptorNames;
	}

	/**
	 * 设置目标 bean 的名称。这是在“interceptorNames”数组末尾指定目标名称的替代方法。
	 * <p>您还可以分别通过“target”/“targetSource”属性直接指定目标对象或TargetSource对象。
	 * @see #setInterceptorNames(String[])
	 * @see #setTarget(Object)
	 * @see #setTargetSource(org.springframework.aop.TargetSource)
	 */
	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	/**
	 * 如果未指定，设置是否自动检测代理接口。 <p>默认为“true”。如果未指定接口，则关闭此标志可为完整目标类创建 CGLIB 代理。
	 * @see #setProxyTargetClass
	 */
	public void setAutodetectInterfaces(boolean autodetectInterfaces) {
		this.autodetectInterfaces = autodetectInterfaces;
	}

	/**
	 * 设置单例属性的值。控制此工厂是否应该始终返回相同的代理实例（这意味着相同的目标），或者是否应该返回新的原型实例，这意味着目标和拦截器也可能是新实例（如果它们是从原型 bean 
	 * 定义获取的）。这允许对对象图中的独立性/唯一性进行精细控制。
	 */
	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

	/**
	 * 指定要使用的 AdvisorAdapterRegistry。默认是全局 AdvisorAdapterRegistry。
	 * @see org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry
	 */
	public void setAdvisorAdapterRegistry(AdvisorAdapterRegistry advisorAdapterRegistry) {
		this.advisorAdapterRegistry = advisorAdapterRegistry;
	}

	/**
	 * 设置 Frozen（`Frozen`）。
	 */
	@Override
	public void setFrozen(boolean frozen) {
		this.freezeProxy = frozen;
	}

	/**
	 * 设置ClassLoader来生成代理类。<p>Default是bean的ClassLoader，即包含BeanFactory用来加载所有bean类的ClassLoader。对于
	 * 特定代理，可以在此处覆盖此设置。
	 */
	public void setProxyClassLoader(@Nullable ClassLoader classLoader) {
		this.proxyClassLoader = classLoader;
		this.classLoaderConfigured = (classLoader != null);
	}

	/**
	 * 设置 Bean Class Loader（`BeanClassLoader`）。
	 */
	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		if (!this.classLoaderConfigured) {
			this.proxyClassLoader = classLoader;
		}
	}

	/**
	 * 设置 Bean Factory（`BeanFactory`）。
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
		checkInterceptorNames();
	}


	/**
	 * 返回代理。当客户端从该工厂 bean 获取 bean 时调用。创建要由此工厂返回的 AOP 代理的实例。该实例将被缓存为单例，并在每次调用 {@code getObject()
	 * } 时创建代理。
	 * @return 反映该工厂当前状态的新 AOP 代理
	 */
	@Override
	public @Nullable Object getObject() throws BeansException {
		initializeAdvisorChain();
		if (isSingleton()) {
			return getSingletonInstance();
		}
		else {
			if (this.targetName == null) {
				logger.info("Using non-singleton proxies with singleton targets is often undesirable. " +
						"Enable prototype proxies by setting the 'targetName' property.");
			}
			return newPrototypeInstance();
		}
	}

	/**
	 * 返回代理的类型。将检查单例实例（如果已创建），否则回退到代理接口（如果只有一个实例）、目标 bean 类型或 TargetSource 的目标类。
	 * @see org.springframework.aop.framework.AopProxy#getProxyClass
	 */
	@Override
	public @Nullable Class<?> getObjectType() {
		synchronized (this) {
			if (this.singletonInstance != null) {
				return this.singletonInstance.getClass();
			}
		}
		try {
			// 这可能不完整，因为它可能会错过引入的接口
			// 来自将通过 setInterceptorNames 延迟检索的 Advisor。
			return createAopProxy().getProxyClass(this.proxyClassLoader);
		}
		catch (AopConfigException ex) {
			if (getTargetClass() == null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Failed to determine early proxy class: " + ex.getMessage());
				}
				return null;
			}
			else {
				throw ex;
			}
		}
	}

	/**
	 * 判断是否 Singleton。
	 */
	@Override
	public boolean isSingleton() {
		return this.singleton;
	}


	/**
	 * 返回此类代理对象的单例实例，如果尚未创建则延迟创建它。
	 * @return 共享单例代理
	 */
	private synchronized Object getSingletonInstance() {
		if (this.singletonInstance == null) {
			this.targetSource = freshTargetSource();
			if (this.autodetectInterfaces && getProxiedInterfaces().length == 0 && !isProxyTargetClass()) {
				// 依靠 AOP 基础设施来告诉我们要代理哪些接口。
				Class<?> targetClass = getTargetClass();
				if (targetClass == null) {
					throw new FactoryBeanNotInitializedException("Cannot determine target class for proxy");
				}
				setInterfaces(ClassUtils.getAllInterfacesForClass(targetClass, this.proxyClassLoader));
			}
			// 初始化共享单例实例。
			super.setFrozen(this.freezeProxy);
			this.singletonInstance = getProxy(createAopProxy());
		}
		return this.singletonInstance;
	}

	/**
	 * 创建此类创建的代理对象的新原型实例，由独立的 AdvisedSupport 配置支持。
	 * @return 完全独立的代理，我们可以单独操纵其建议
	 */
	private synchronized Object newPrototypeInstance() {
		// 对于原型，我们需要提供代理
		// 配置的独立实例。
		// 在这种情况下，没有代理将拥有该对象配置的实例，
		// 但会有一个独立的副本。
		ProxyCreatorSupport copy = new ProxyCreatorSupport(getAopProxyFactory());

		// 该副本需要新的顾问链和新的 TargetSource。
		TargetSource targetSource = freshTargetSource();
		copy.copyConfigurationFrom(this, targetSource, freshAdvisorChain());
		if (this.autodetectInterfaces && getProxiedInterfaces().length == 0 && !isProxyTargetClass()) {
			// 依靠 AOP 基础设施来告诉我们要代理哪些接口。
			Class<?> targetClass = targetSource.getTargetClass();
			if (targetClass != null) {
				copy.setInterfaces(ClassUtils.getAllInterfacesForClass(targetClass, this.proxyClassLoader));
			}
		}
		copy.setFrozen(this.freezeProxy);

		return getProxy(copy.createAopProxy());
	}

	/**
	 * 返回要公开的代理对象。 <p> 默认实现使用 {@code getProxy} 调用工厂的 bean 类加载器。可以重写以指定自定义类加载器。
	 * @param aopProxy 准备好的 AopProxy 实例从中获取代理
	 * @return 要公开的代理对象
	 * @see AopProxy#getProxy(ClassLoader)
	 */
	protected Object getProxy(AopProxy aopProxy) {
		return aopProxy.getProxy(this.proxyClassLoader);
	}

	/**
	 * 检查拦截器名称列表是否包含目标名称作为最终元素。如果找到，则从列表中删除最终名称并将其设置为 targetName。
	 */
	private void checkInterceptorNames() {
		if (!ObjectUtils.isEmpty(this.interceptorNames)) {
			String finalName = this.interceptorNames[this.interceptorNames.length - 1];
			if (this.targetName == null && this.targetSource == EMPTY_TARGET_SOURCE) {
				// 链中的最后一个名称可能是 Advisor/Advice 或 target/TargetSource。
				// 不幸的是我们不知道；我们必须查看 bean 的类型。
				if (!finalName.endsWith(GLOBAL_SUFFIX) && !isNamedBeanAnAdvisorOrAdvice(finalName)) {
					// 目标不是拦截器。
					this.targetName = finalName;
					if (logger.isDebugEnabled()) {
						logger.debug("Bean with name '" + finalName + "' concluding interceptor chain " +
								"is not an advisor class: treating it as a target or TargetSource");
					}
					this.interceptorNames = Arrays.copyOf(this.interceptorNames, this.interceptorNames.length - 1);
				}
			}
		}
	}

	/**
	 * 查看 bean 工厂元数据来确定这个 bean 名称（它包含拦截器名称列表）是否是一个 Advisor 或 Advice，或者可能是一个目标。
	 * @param beanName 要检查的 bean 名称
	 * @return true} 如果是顾问或建议
	 */
	private boolean isNamedBeanAnAdvisorOrAdvice(String beanName) {
		Assert.state(this.beanFactory != null, "No BeanFactory set");
		Class<?> namedBeanClass = this.beanFactory.getType(beanName);
		if (namedBeanClass != null) {
			return (Advisor.class.isAssignableFrom(namedBeanClass) || Advice.class.isAssignableFrom(namedBeanClass));
		}
		// 如果我们无法判断，请将其视为目标 bean。
		if (logger.isDebugEnabled()) {
			logger.debug("Could not determine type of bean with name '" + beanName +
					"' - assuming it is neither an Advisor nor an Advice");
		}
		return false;
	}

	/**
	 * 创建顾问（拦截器）链。每次添加新的原型实例时，源自 BeanFactory 的 Advisor 都会刷新。通过工厂 API 以编程方式添加的拦截器不受此类更改的影响。
	 */
	private synchronized void initializeAdvisorChain() throws AopConfigException, BeansException {
		if (!this.advisorChainInitialized && !ObjectUtils.isEmpty(this.interceptorNames)) {
			if (this.beanFactory == null) {
				throw new IllegalStateException("No BeanFactory available anymore (probably due to serialization) " +
						"- cannot resolve interceptor names " + Arrays.toString(this.interceptorNames));
			}

			// 除非我们使用属性指定 targetSource，否则全局变量不能是最后一个...
			if (this.interceptorNames[this.interceptorNames.length - 1].endsWith(GLOBAL_SUFFIX) &&
					this.targetName == null && this.targetSource == EMPTY_TARGET_SOURCE) {
				throw new AopConfigException("Target required after globals");
			}

			// 从 bean 名称具体化拦截器链。
			for (String name : this.interceptorNames) {
				if (name.endsWith(GLOBAL_SUFFIX)) {
					if (!(this.beanFactory instanceof ListableBeanFactory lbf)) {
						throw new AopConfigException(
								"Can only use global advisors or interceptors with a ListableBeanFactory");
					}
					addGlobalAdvisors(lbf, name.substring(0, name.length() - GLOBAL_SUFFIX.length()));
				}

				else {
					// 如果我们到达这里，我们需要添加一个命名拦截器。
					// 我们必须检查它是单例还是原型。
					Object advice;
					if (this.singleton || this.beanFactory.isSingleton(name)) {
						// 将真正的顾问/建议添加到链中。
						advice = this.beanFactory.getBean(name);
					}
					else {
						// 这是一个原型建议或顾问：用原型替换。
						// 避免仅仅为了顾问链初始化而不必要地创建原型 bean。
						advice = new PrototypePlaceholderAdvisor(name);
					}
					addAdvisorOnChainCreation(advice);
				}
			}

			this.advisorChainInitialized = true;
		}
	}


	/**
	 * 返回一个独立的顾问链。每次返回新的原型实例时，我们都需要执行此操作，以返回原型顾问和建议的不同实例。
	 */
	private List<Advisor> freshAdvisorChain() {
		Advisor[] advisors = getAdvisors();
		List<Advisor> freshAdvisors = new ArrayList<>(advisors.length);
		for (Advisor advisor : advisors) {
			if (advisor instanceof PrototypePlaceholderAdvisor ppa) {
				if (logger.isDebugEnabled()) {
					logger.debug("Refreshing bean named '" + ppa.getBeanName() + "'");
				}
				// 将占位符替换为通过 getBean 查找得到的新原型实例
				if (this.beanFactory == null) {
					throw new IllegalStateException("No BeanFactory available anymore (probably due to " +
							"serialization) - cannot resolve prototype advisor '" + ppa.getBeanName() + "'");
				}
				Object bean = this.beanFactory.getBean(ppa.getBeanName());
				Advisor refreshedAdvisor = namedBeanToAdvisor(bean);
				freshAdvisors.add(refreshedAdvisor);
			}
			else {
				// 添加共享实例。
				freshAdvisors.add(advisor);
			}
		}
		return freshAdvisors;
	}

	/**
	 * 添加所有全局拦截器和切入点。
	 */
	private void addGlobalAdvisors(ListableBeanFactory beanFactory, String prefix) {
		String[] globalAdvisorNames =
				BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, Advisor.class);
		String[] globalInterceptorNames =
				BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, Interceptor.class);
		if (globalAdvisorNames.length > 0 || globalInterceptorNames.length > 0) {
			List<Object> beans = new ArrayList<>(globalAdvisorNames.length + globalInterceptorNames.length);
			for (String name : globalAdvisorNames) {
				if (name.startsWith(prefix)) {
					beans.add(beanFactory.getBean(name));
				}
			}
			for (String name : globalInterceptorNames) {
				if (name.startsWith(prefix)) {
					beans.add(beanFactory.getBean(name));
				}
			}
			AnnotationAwareOrderComparator.sort(beans);
			for (Object bean : beans) {
				addAdvisorOnChainCreation(bean);
			}
		}
	}

	/**
	 * 创建建议链时调用。 <p>将给定的建议、顾问或对象添加到拦截器列表中。由于这三种可能性，我们无法更强烈地键入签名。
	 * @param next 忠告、顾问或目标对象
	 */
	private void addAdvisorOnChainCreation(Object next) {
		// 如有必要，我们需要转换为顾问，以便我们的源参考
		// 与我们从超类拦截器中找到的内容相匹配。
		addAdvisor(namedBeanToAdvisor(next));
	}

	/**
	 * 返回创建代理时使用的 TargetSource。如果在InterceptorNames 列表末尾未指定目标，则TargetSource 将是此类的TargetSource
	 * 成员。否则，我们将获取目标 bean 并将其包装在 TargetSource 中（如果需要）。
	 */
	private TargetSource freshTargetSource() {
		if (this.targetName == null) {
			// 不刷新目标：“interceptorNames”中未指定 bean 名称
			return this.targetSource;
		}
		else {
			if (this.beanFactory == null) {
				throw new IllegalStateException("No BeanFactory available anymore (probably due to serialization) " +
						"- cannot resolve target with name '" + this.targetName + "'");
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Refreshing target with name '" + this.targetName + "'");
			}
			Object target = this.beanFactory.getBean(this.targetName);
			return (target instanceof TargetSource targetSource ? targetSource : new SingletonTargetSource(target));
		}
	}

	/**
	 * 将以下来自对 InterceptorNames 数组中的名称调用 getBean() 的对象转换为 Advisor 或 TargetSource。
	 */
	private Advisor namedBeanToAdvisor(Object next) {
		try {
			return this.advisorAdapterRegistry.wrap(next);
		}
		catch (UnknownAdviceTypeException ex) {
			// 我们希望这是一位顾问或建议，
			// 但事实并非如此。这是一个配置错误。
			throw new AopConfigException("Unknown advisor type " + next.getClass() +
					"; can only include Advisor or Advice type beans in interceptorNames chain " +
					"except for last entry which may also be target instance or TargetSource", ex);
		}
	}

	/**
	 * 在建议更改时清除并重新缓存单例。
	 */
	@Override
	protected void adviceChanged() {
		super.adviceChanged();
		if (this.singleton) {
			logger.debug("Advice has changed; re-caching singleton instance");
			synchronized (this) {
				this.singletonInstance = null;
			}
		}
	}


	//---------------------------------------------------------------------
	// 序列化支持
	//---------------------------------------------------------------------

	/**
	 * 执行 readObject 相关逻辑。
	 */
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		// 依赖默认序列化；只需在反序列化后初始化状态即可。
		ois.defaultReadObject();

		// 初始化瞬态字段。
		this.proxyClassLoader = ClassUtils.getDefaultClassLoader();
	}


	/**
	 * 在拦截器链中使用，我们需要在创建代理时用原型替换 bean。
	 */
	private static class PrototypePlaceholderAdvisor implements Advisor, Serializable {

		private final String beanName;

		private final String message;

		public PrototypePlaceholderAdvisor(String beanName) {
			this.beanName = beanName;
			this.message = "Placeholder for prototype Advisor/Advice with bean name '" + beanName + "'";
		}

		public String getBeanName() {
			return this.beanName;
		}

		@Override
		public Advice getAdvice() {
			throw new UnsupportedOperationException("Cannot invoke methods: " + this.message);
		}

		@Override
		public String toString() {
			return this.message;
		}
	}

}
