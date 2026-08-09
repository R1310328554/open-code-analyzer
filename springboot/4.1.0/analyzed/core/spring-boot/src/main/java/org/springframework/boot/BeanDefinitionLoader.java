/*
 * Copyright 2012-present the original author or authors.
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

package org.springframework.boot;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import groovy.lang.Closure;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.groovy.GroovyBeanDefinitionReader;
import org.springframework.beans.factory.support.AbstractBeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.filter.AbstractTypeHierarchyTraversingFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * 从底层源（包括 XML 和 JavaConfig）加载 Bean 定义。
 * <p>
 * 作为 {@link AnnotatedBeanDefinitionReader}、{@link XmlBeanDefinitionReader}
 * 和 {@link ClassPathBeanDefinitionScanner} 的简单门面。支持的源类型见
 * {@link SpringApplication}。
 *
 * @author Phillip Webb
 * @author Vladislav Kisel
 * @author Sebastien Deleuze
 * @see #setBeanNameGenerator(BeanNameGenerator)
 */
class BeanDefinitionLoader {

	private static final Pattern GROOVY_CLOSURE_PATTERN = Pattern.compile(".*\\$_.*closure.*");

	private final Object[] sources;

	private final AnnotatedBeanDefinitionReader annotatedReader;

	private final AbstractBeanDefinitionReader xmlReader;

	private final @Nullable BeanDefinitionReader groovyReader;

	private final ClassPathBeanDefinitionScanner scanner;

	private @Nullable ResourceLoader resourceLoader;

	/**
	 * 创建新的 {@link BeanDefinitionLoader}，将 Bean 加载到指定的
	 * {@link BeanDefinitionRegistry} 中。
	 *
	 * @param registry 将包含已加载 Bean 的 Bean 定义注册表
	 * @param sources Bean 源
	 */
	BeanDefinitionLoader(BeanDefinitionRegistry registry, Object... sources) {
		Assert.notNull(registry, "'registry' must not be null");
		Assert.notEmpty(sources, "'sources' must not be empty");
		this.sources = sources;
		this.annotatedReader = new AnnotatedBeanDefinitionReader(registry);
		this.xmlReader = new XmlBeanDefinitionReader(registry);
		this.groovyReader = isGroovyPresent() ? new GroovyBeanDefinitionReader(registry) : null;
		this.scanner = new ClassPathBeanDefinitionScanner(registry);
		this.scanner.addExcludeFilter(new ClassExcludeFilter(sources));
	}

	/**
	 * 设置底层读取器和扫描器使用的 Bean 名称生成器。
	 *
	 * @param beanNameGenerator Bean 名称生成器
	 */
	void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
		this.annotatedReader.setBeanNameGenerator(beanNameGenerator);
		this.scanner.setBeanNameGenerator(beanNameGenerator);
		this.xmlReader.setBeanNameGenerator(beanNameGenerator);
	}

	/**
	 * 设置底层读取器和扫描器使用的资源加载器。
	 *
	 * @param resourceLoader 资源加载器
	 */
	void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
		this.scanner.setResourceLoader(resourceLoader);
		this.xmlReader.setResourceLoader(resourceLoader);
	}

	/**
	 * 设置底层读取器和扫描器使用的环境。
	 *
	 * @param environment 环境
	 */
	void setEnvironment(ConfigurableEnvironment environment) {
		this.annotatedReader.setEnvironment(environment);
		this.scanner.setEnvironment(environment);
		this.xmlReader.setEnvironment(environment);
	}

	/**
	 * 将所有源加载到读取器中。
	 */
	void load() {
		for (Object source : this.sources) {
			load(source);
		}
	}

	private void load(Object source) {
		Assert.notNull(source, "'source' must not be null");
		if (source instanceof Class<?> type) {
			load(type);
			return;
		}
		if (source instanceof Resource resource) {
			load(resource);
			return;
		}
		if (source instanceof Package pack) {
			load(pack);
			return;
		}
		if (source instanceof CharSequence sequence) {
			load(sequence);
			return;
		}
		throw new IllegalArgumentException("Invalid source type " + source.getClass());
	}

	private void load(Class<?> source) {
		if (this.groovyReader != null && GroovyBeanDefinitionSource.class.isAssignableFrom(source)) {
			// Any GroovyLoaders added in beans{} DSL can contribute beans here
			GroovyBeanDefinitionSource loader = BeanUtils.instantiateClass(source, GroovyBeanDefinitionSource.class);
			((GroovyBeanDefinitionReader) this.groovyReader).beans(loader.getBeans());
		}
		if (isEligible(source)) {
			this.annotatedReader.register(source);
		}
	}

	private void load(Resource source) {
		String filename = source.getFilename();
		Assert.state(filename != null, "Source has no filename");
		if (filename.endsWith(".groovy")) {
			if (this.groovyReader == null) {
				throw new BeanDefinitionStoreException("Cannot load Groovy beans without Groovy on classpath");
			}
			this.groovyReader.loadBeanDefinitions(source);
		}
		else {
			this.xmlReader.loadBeanDefinitions(source);
		}
	}

	private void load(Package source) {
		this.scanner.scan(source.getName());
	}

	private void load(CharSequence source) {
		String resolvedSource = this.scanner.getEnvironment().resolvePlaceholders(source.toString());
		// Attempt as a Class
		try {
			load(ClassUtils.forName(resolvedSource, null));
			return;
		}
		catch (IllegalArgumentException | ClassNotFoundException ex) {
			// swallow exception and continue
		}
		// Attempt as Resources
		if (loadAsResources(resolvedSource)) {
			return;
		}
		// Attempt as package
		Package packageResource = findPackage(resolvedSource);
		if (packageResource != null) {
			load(packageResource);
			return;
		}
		throw new IllegalArgumentException("Invalid source '" + resolvedSource + "'");
	}

	private boolean loadAsResources(String resolvedSource) {
		boolean foundCandidate = false;
		Resource[] resources = findResources(resolvedSource);
		for (Resource resource : resources) {
			if (isLoadCandidate(resource)) {
				foundCandidate = true;
				load(resource);
			}
		}
		return foundCandidate;
	}

	private boolean isGroovyPresent() {
		return ClassUtils.isPresent("groovy.lang.MetaClass", null);
	}

	private Resource[] findResources(String source) {
		ResourceLoader loader = (this.resourceLoader != null) ? this.resourceLoader
				: new PathMatchingResourcePatternResolver();
		try {
			if (loader instanceof ResourcePatternResolver resolver) {
				return resolver.getResources(source);
			}
			return new Resource[] { loader.getResource(source) };
		}
		catch (IOException ex) {
			throw new IllegalStateException("Error reading source '" + source + "'");
		}
	}

	private boolean isLoadCandidate(@Nullable Resource resource) {
		if (resource == null || !resource.exists()) {
			return false;
		}
		if (resource instanceof ClassPathResource classPathResource) {
			// A simple package without a '.' may accidentally get loaded as an XML
			// document if we're not careful. The result of getInputStream() will be
			// a file list of the package content. We double-check here that it's not
			// actually a package.
			String path = classPathResource.getPath();
			if (path.indexOf('.') == -1) {
				try {
					return getClass().getClassLoader().getDefinedPackage(path) == null;
				}
				catch (Exception ex) {
					// Ignore
				}
			}
		}
		return true;
	}

	private @Nullable Package findPackage(CharSequence source) {
		Package pkg = getClass().getClassLoader().getDefinedPackage(source.toString());
		if (pkg != null) {
			return pkg;
		}
		try {
			// Attempt to find a class in this package
			ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(getClass().getClassLoader());
			Resource[] resources = resolver
				.getResources(ClassUtils.convertClassNameToResourcePath(source.toString()) + "/*.class");
			for (Resource resource : resources) {
				String filename = resource.getFilename();
				Assert.state(filename != null, "No filename available");
				String className = StringUtils.stripFilenameExtension(filename);
				load(Class.forName(source + "." + className));
				break;
			}
		}
		catch (Exception ex) {
			// swallow exception and continue
		}
		return getClass().getClassLoader().getDefinedPackage(source.toString());
	}

	/**
	 * 检查 Bean 是否有资格注册。
	 *
	 * @param type 候选 Bean 类型
	 * @return 若给定 Bean 类型有资格注册（非 Groovy 闭包且非匿名类）则返回 {@code true}
	 */
	private boolean isEligible(Class<?> type) {
		return !(type.isAnonymousClass() || isGroovyClosure(type) || hasNoConstructors(type));
	}

	private boolean isGroovyClosure(Class<?> type) {
		return GROOVY_CLOSURE_PATTERN.matcher(type.getName()).matches();
	}

	private boolean hasNoConstructors(Class<?> type) {
		Constructor<?>[] constructors = type.getDeclaredConstructors();
		return ObjectUtils.isEmpty(constructors);
	}

	/**
	 * 简单的 {@link TypeFilter}，确保扫描期间不会意外重新添加
	 * 已指定的 {@link Class} 源。
	 */
	private static class ClassExcludeFilter extends AbstractTypeHierarchyTraversingFilter {

		private final Set<String> classNames = new HashSet<>();

		ClassExcludeFilter(Object... sources) {
			super(false, false);
			for (Object source : sources) {
				if (source instanceof Class<?> classSource) {
					this.classNames.add(classSource.getName());
				}
			}
		}

		@Override
		protected boolean matchClassName(String className) {
			return this.classNames.contains(className);
		}

	}

	/**
	 * 在 Groovy 中定义的 Bean 定义源。
	 */
	@FunctionalInterface
	protected interface GroovyBeanDefinitionSource {

		Closure<?> getBeans();

	}

}
