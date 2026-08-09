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

package org.springframework.cache.config;

import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;

import org.springframework.aop.config.AopNamespaceUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.cache.interceptor.BeanFactoryCacheOperationSourceAdvisor;
import org.springframework.cache.interceptor.CacheInterceptor;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * {@link org.springframework.beans.factory.xml.BeanDefinitionParser} 实现，
 * 便于用户配置启用注解驱动缓存所需的全部基础设施 Bean。
 *
 * <p>默认所有代理均为 JDK 动态代理。若以具体类而非接口注入 Bean，
 * 可能遇到问题；可将 {@code proxy-target-class} 属性设为 {@code true} 以使用 CGLIB 类代理。
 *
 * <p>若类路径存在 JSR-107 API 与 Spring JCache 实现，还会注册处理
 * {@code CacheResult}、{@code CachePut}、{@code CacheRemove}、{@code CacheRemoveAll}
 * 注解所需的基础设施 Bean。
 *
 * @author Costin Leau
 * @author Stephane Nicoll
 * @since 3.1
 */
class AnnotationDrivenCacheBeanDefinitionParser implements BeanDefinitionParser {

	private static final String CACHE_ASPECT_CLASS_NAME =
			"org.springframework.cache.aspectj.AnnotationCacheAspect";

	private static final String JCACHE_ASPECT_CLASS_NAME =
			"org.springframework.cache.aspectj.JCacheCacheAspect";

	/** 类路径是否存在 JSR-107 {@code javax.cache.Cache}。 */
	private static final boolean JSR_107_PRESENT;

	/** 类路径是否存在 Spring JCache 实现。 */
	private static final boolean JCACHE_IMPL_PRESENT;

	static {
		ClassLoader classLoader = AnnotationDrivenCacheBeanDefinitionParser.class.getClassLoader();
		JSR_107_PRESENT = ClassUtils.isPresent("javax.cache.Cache", classLoader);
		JCACHE_IMPL_PRESENT = ClassUtils.isPresent(
				"org.springframework.cache.jcache.interceptor.DefaultJCacheOperationSource", classLoader);
	}


	/**
	 * 解析 {@code <cache:annotation-driven>} 标签。
	 * 必要时向容器 {@link AopNamespaceUtils#registerAutoProxyCreatorIfNecessary 注册 AutoProxyCreator}。
	 */
	@Override
	public @Nullable BeanDefinition parse(Element element, ParserContext parserContext) {
		String mode = element.getAttribute("mode");
		if ("aspectj".equals(mode)) {
			// mode="aspectj"：注册 AspectJ 切面而非 Spring AOP Advisor
			registerCacheAspect(element, parserContext);
		}
		else {
			// mode="proxy"（默认）：注册 Spring AOP Advisor 链
			registerCacheAdvisor(element, parserContext);
		}

		return null;
	}

	private void registerCacheAspect(Element element, ParserContext parserContext) {
		SpringCachingConfigurer.registerCacheAspect(element, parserContext);
		if (JSR_107_PRESENT && JCACHE_IMPL_PRESENT) {
			JCacheCachingConfigurer.registerCacheAspect(element, parserContext);
		}
	}

	private void registerCacheAdvisor(Element element, ParserContext parserContext) {
		AopNamespaceUtils.registerAutoProxyCreatorIfNecessary(parserContext, element);
		SpringCachingConfigurer.registerCacheAdvisor(element, parserContext);
		if (JSR_107_PRESENT && JCACHE_IMPL_PRESENT) {
			JCacheCachingConfigurer.registerCacheAdvisor(element, parserContext);
		}
	}

	/**
	 * 解析缓存解析策略：若声明了 {@code cache-resolver} 属性则注入解析器；
	 * 否则注入 {@code cache-manager}。{@code setBoth} 为 {@code true} 时两者都注入
	 * （JSR-107 场景需要）。
	 */
	private static void parseCacheResolution(Element element, BeanDefinition def, boolean setBoth) {
		String name = element.getAttribute("cache-resolver");
		boolean hasText = StringUtils.hasText(name);
		if (hasText) {
			def.getPropertyValues().add("cacheResolver", new RuntimeBeanReference(name.trim()));
		}
		if (!hasText || setBoth) {
			def.getPropertyValues().add("cacheManager",
					new RuntimeBeanReference(CacheNamespaceHandler.extractCacheManager(element)));
		}
	}

	private static void parseErrorHandler(Element element, BeanDefinition def) {
		String name = element.getAttribute("error-handler");
		if (StringUtils.hasText(name)) {
			def.getPropertyValues().add("errorHandler", new RuntimeBeanReference(name.trim()));
		}
	}


	/**
	 * 配置支持 Spring 缓存注解所需的基础设施 Bean。
	 */
	private static class SpringCachingConfigurer {

		private static void registerCacheAdvisor(Element element, ParserContext parserContext) {
			if (!parserContext.getRegistry().containsBeanDefinition(CacheManagementConfigUtils.CACHE_ADVISOR_BEAN_NAME)) {
				Object eleSource = parserContext.extractSource(element);

				// 创建 CacheOperationSource 定义
				RootBeanDefinition sourceDef = new RootBeanDefinition("org.springframework.cache.annotation.AnnotationCacheOperationSource");
				sourceDef.setSource(eleSource);
				sourceDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
				String sourceName = parserContext.getReaderContext().registerWithGeneratedName(sourceDef);

				// 创建 CacheInterceptor 定义
				RootBeanDefinition interceptorDef = new RootBeanDefinition(CacheInterceptor.class);
				interceptorDef.setSource(eleSource);
				interceptorDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
				parseCacheResolution(element, interceptorDef, false);
				parseErrorHandler(element, interceptorDef);
				CacheNamespaceHandler.parseKeyGenerator(element, interceptorDef);
				interceptorDef.getPropertyValues().add("cacheOperationSources", new RuntimeBeanReference(sourceName));
				String interceptorName = parserContext.getReaderContext().registerWithGeneratedName(interceptorDef);

				// 创建 CacheAdvisor 定义
				RootBeanDefinition advisorDef = new RootBeanDefinition(BeanFactoryCacheOperationSourceAdvisor.class);
				advisorDef.setSource(eleSource);
				advisorDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
				advisorDef.getPropertyValues().add("cacheOperationSource", new RuntimeBeanReference(sourceName));
				advisorDef.getPropertyValues().add("adviceBeanName", interceptorName);
				if (element.hasAttribute("order")) {
					advisorDef.getPropertyValues().add("order", element.getAttribute("order"));
				}
				parserContext.getRegistry().registerBeanDefinition(CacheManagementConfigUtils.CACHE_ADVISOR_BEAN_NAME, advisorDef);

				CompositeComponentDefinition compositeDef = new CompositeComponentDefinition(element.getTagName(), eleSource);
				compositeDef.addNestedComponent(new BeanComponentDefinition(sourceDef, sourceName));
				compositeDef.addNestedComponent(new BeanComponentDefinition(interceptorDef, interceptorName));
				compositeDef.addNestedComponent(new BeanComponentDefinition(advisorDef, CacheManagementConfigUtils.CACHE_ADVISOR_BEAN_NAME));
				parserContext.registerComponent(compositeDef);
			}
		}

		/**
		 * 注册缓存 AspectJ 切面。
		 * <pre class="code">
		 * &lt;bean id="cacheAspect" class="org.springframework.cache.aspectj.AnnotationCacheAspect" factory-method="aspectOf"&gt;
		 *   &lt;property name="cacheManager" ref="cacheManager"/&gt;
		 *   &lt;property name="keyGenerator" ref="keyGenerator"/&gt;
		 * &lt;/bean&gt;
		 * </pre>
		 */
		private static void registerCacheAspect(Element element, ParserContext parserContext) {
			if (!parserContext.getRegistry().containsBeanDefinition(CacheManagementConfigUtils.CACHE_ASPECT_BEAN_NAME)) {
				RootBeanDefinition def = new RootBeanDefinition();
				def.setBeanClassName(CACHE_ASPECT_CLASS_NAME);
				def.setFactoryMethodName("aspectOf");
				parseCacheResolution(element, def, false);
				CacheNamespaceHandler.parseKeyGenerator(element, def);
				parserContext.registerBeanComponent(new BeanComponentDefinition(def, CacheManagementConfigUtils.CACHE_ASPECT_BEAN_NAME));
			}
		}
	}


	/**
	 * 配置支持标准 JSR-107 缓存注解所需的基础设施 Bean。
	 */
	private static class JCacheCachingConfigurer {

		private static void registerCacheAdvisor(Element element, ParserContext parserContext) {
			if (!parserContext.getRegistry().containsBeanDefinition(CacheManagementConfigUtils.JCACHE_ADVISOR_BEAN_NAME)) {
				Object source = parserContext.extractSource(element);

				// 创建 JCache OperationSource 定义
				BeanDefinition sourceDef = createJCacheOperationSourceBeanDefinition(element, source);
				String sourceName = parserContext.getReaderContext().registerWithGeneratedName(sourceDef);

				// 创建 JCacheInterceptor 定义
				RootBeanDefinition interceptorDef =
						new RootBeanDefinition("org.springframework.cache.jcache.interceptor.JCacheInterceptor");
				interceptorDef.setSource(source);
				interceptorDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
				interceptorDef.getPropertyValues().add("cacheOperationSource", new RuntimeBeanReference(sourceName));
				parseErrorHandler(element, interceptorDef);
				String interceptorName = parserContext.getReaderContext().registerWithGeneratedName(interceptorDef);

				// 创建 JCache Advisor 定义
				RootBeanDefinition advisorDef = new RootBeanDefinition(
						"org.springframework.cache.jcache.interceptor.BeanFactoryJCacheOperationSourceAdvisor");
				advisorDef.setSource(source);
				advisorDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
				advisorDef.getPropertyValues().add("cacheOperationSource", new RuntimeBeanReference(sourceName));
				advisorDef.getPropertyValues().add("adviceBeanName", interceptorName);
				if (element.hasAttribute("order")) {
					advisorDef.getPropertyValues().add("order", element.getAttribute("order"));
				}
				parserContext.getRegistry().registerBeanDefinition(CacheManagementConfigUtils.JCACHE_ADVISOR_BEAN_NAME, advisorDef);

				CompositeComponentDefinition compositeDef = new CompositeComponentDefinition(element.getTagName(), source);
				compositeDef.addNestedComponent(new BeanComponentDefinition(sourceDef, sourceName));
				compositeDef.addNestedComponent(new BeanComponentDefinition(interceptorDef, interceptorName));
				compositeDef.addNestedComponent(new BeanComponentDefinition(advisorDef, CacheManagementConfigUtils.JCACHE_ADVISOR_BEAN_NAME));
				parserContext.registerComponent(compositeDef);
			}
		}

		private static void registerCacheAspect(Element element, ParserContext parserContext) {
			if (!parserContext.getRegistry().containsBeanDefinition(CacheManagementConfigUtils.JCACHE_ASPECT_BEAN_NAME)) {
				Object source = parserContext.extractSource(element);

				BeanDefinition cacheOperationSourceDef = createJCacheOperationSourceBeanDefinition(element, source);
				String cacheOperationSourceName = parserContext.getReaderContext().registerWithGeneratedName(cacheOperationSourceDef);

				RootBeanDefinition jcacheAspectDef = new RootBeanDefinition();
				jcacheAspectDef.setBeanClassName(JCACHE_ASPECT_CLASS_NAME);
				jcacheAspectDef.setFactoryMethodName("aspectOf");
				jcacheAspectDef.getPropertyValues().add("cacheOperationSource", new RuntimeBeanReference(cacheOperationSourceName));
				parserContext.getRegistry().registerBeanDefinition(CacheManagementConfigUtils.JCACHE_ASPECT_BEAN_NAME, jcacheAspectDef);

				CompositeComponentDefinition compositeDef = new CompositeComponentDefinition(element.getTagName(), source);
				compositeDef.addNestedComponent(new BeanComponentDefinition(cacheOperationSourceDef, cacheOperationSourceName));
				compositeDef.addNestedComponent(new BeanComponentDefinition(jcacheAspectDef, CacheManagementConfigUtils.JCACHE_ASPECT_BEAN_NAME));
				parserContext.registerComponent(compositeDef);
			}
		}

		private static RootBeanDefinition createJCacheOperationSourceBeanDefinition(Element element, @Nullable Object eleSource) {
			RootBeanDefinition sourceDef =
					new RootBeanDefinition("org.springframework.cache.jcache.interceptor.DefaultJCacheOperationSource");
			sourceDef.setSource(eleSource);
			sourceDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
			// JSR-107 需要同时注入 cacheManager 与 cacheResolver（异常缓存解析器依赖 cacheManager）
			// 命名空间无法单独配置该异常缓存解析器
			parseCacheResolution(element, sourceDef, true);
			CacheNamespaceHandler.parseKeyGenerator(element, sourceDef);
			return sourceDef;
		}
	}

}
