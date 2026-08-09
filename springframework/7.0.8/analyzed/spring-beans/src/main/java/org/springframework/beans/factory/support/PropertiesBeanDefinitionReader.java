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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.CannotLoadBeanClassException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.util.DefaultPropertiesPersister;
import org.springframework.util.PropertiesPersister;
import org.springframework.util.StringUtils;

/**
 * 简单 Properties 格式的 Bean 定义读取器。
 *
 * <p>为 Map/Properties 和 ResourceBundle 提供 Bean 定义注册方法。
 * 通常应用于 DefaultListableBeanFactory。
 *
 * <p><b>示例：</b>
 *
 * <pre class="code">
 * employee.(class)=MyClass       // bean is of class MyClass
 * employee.(abstract)=true       // this bean can't be instantiated directly
 * employee.group=Insurance       // real property
 * employee.usesDialUp=false      // real property (potentially overridden)
 *
 * salesrep.(parent)=employee     // derives from "employee" bean definition
 * salesrep.(lazy-init)=true      // lazily initialize this singleton bean
 * salesrep.manager(ref)=tony     // reference to another bean
 * salesrep.department=Sales      // real property
 *
 * techie.(parent)=employee       // derives from "employee" bean definition
 * techie.(scope)=prototype       // bean is a prototype (not a shared instance)
 * techie.manager(ref)=jeff       // reference to another bean
 * techie.department=Engineering  // real property
 * techie.usesDialUp=true         // real property (overriding parent value)
 *
 * ceo.$0(ref)=secretary          // inject 'secretary' bean as 0th constructor arg
 * ceo.$1=1000000                 // inject value '1000000' at 1st constructor arg
 * </pre>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 26.11.2003
 * @see DefaultListableBeanFactory
 * @deprecated 建议使用 Spring 通用 Bean 定义格式和/或自定义 BeanDefinitionReader 实现
 */
@Deprecated(since = "5.3")
public class PropertiesBeanDefinitionReader extends AbstractBeanDefinitionReader {

	/**
	 * 表示 true 的 T/F 属性值。
	 * 其他任何值均表示 false。大小写敏感。
	 */
	public static final String TRUE_VALUE = "true";

	/**
	 * Bean 名称与属性名称之间的分隔符。
	 * 遵循常规 Java 命名约定。
	 */
	public static final String SEPARATOR = ".";

	/**
	 * 用于区分 {@code owner.(class)=com.myapp.MyClass} 的特殊键。
	 */
	public static final String CLASS_KEY = "(class)";

	/**
	 * 用于区分 {@code owner.(parent)=parentBeanName} 的特殊键。
	 */
	public static final String PARENT_KEY = "(parent)";

	/**
	 * 用于区分 {@code owner.(scope)=prototype} 的特殊键。
	 * 默认为 "true"。
	 */
	public static final String SCOPE_KEY = "(scope)";

	/**
	 * 用于区分 {@code owner.(singleton)=false} 的特殊键。
	 * 默认为 "true"。
	 */
	public static final String SINGLETON_KEY = "(singleton)";

	/**
	 * 用于区分 {@code owner.(abstract)=true} 的特殊键。
	 * 默认为 "false"。
	 */
	public static final String ABSTRACT_KEY = "(abstract)";

	/**
	 * 用于区分 {@code owner.(lazy-init)=true} 的特殊键。
	 * 默认为 "false"。
	 */
	public static final String LAZY_INIT_KEY = "(lazy-init)";

	/**
	 * 引用当前 BeanFactory 中其他 Bean 的属性后缀：
	 * 例如 {@code owner.dog(ref)=fido}。
	 * 引用的是单例还是 prototype 取决于目标 Bean 的定义。
	 */
	public static final String REF_SUFFIX = "(ref)";

	/**
	 * 引用其他 Bean 的值前缀。
	 */
	public static final String REF_PREFIX = "*";

	/**
	 * 表示构造函数参数定义的前缀。
	 */
	public static final String CONSTRUCTOR_ARG_PREFIX = "$";


	/** 默认父 Bean 名称。 */
	private @Nullable String defaultParentBean;

	/** 用于解析 Properties 文件的 PropertiesPersister。 */
	private PropertiesPersister propertiesPersister = DefaultPropertiesPersister.INSTANCE;


	/**
	 * 为给定 Bean 工厂创建新的 PropertiesBeanDefinitionReader。
	 * @param registry 用于加载 Bean 定义的 BeanFactory，以 BeanDefinitionRegistry 形式提供
	 */
	public PropertiesBeanDefinitionReader(BeanDefinitionRegistry registry) {
		super(registry);
	}


	/**
	 * 设置本 Bean 工厂的默认父 Bean。
	 * 若本工厂处理的子 Bean 定义既未提供 parent 也未提供 class 属性，
	 * 则使用此默认值。
	 * <p>例如可用于视图定义文件，为所有视图定义具有默认视图类和公共属性的父 Bean。
	 * 自行定义 parent 或 class 的视图定义仍可覆盖此设置。
	 * <p>严格来说，默认父 Bean 设置不适用于携带 class 的 Bean 定义，
	 * 这是出于向后兼容；但仍符合典型使用场景。
	 */
	public void setDefaultParentBean(@Nullable String defaultParentBean) {
		this.defaultParentBean = defaultParentBean;
	}

	/**
	 * 返回本 Bean 工厂的默认父 Bean。
	 */
	public @Nullable String getDefaultParentBean() {
		return this.defaultParentBean;
	}

	/**
	 * 设置用于解析 Properties 文件的 PropertiesPersister。
	 * 默认为 {@code DefaultPropertiesPersister}。
	 * @see DefaultPropertiesPersister#INSTANCE
	 */
	public void setPropertiesPersister(@Nullable PropertiesPersister propertiesPersister) {
		this.propertiesPersister =
				(propertiesPersister != null ? propertiesPersister : DefaultPropertiesPersister.INSTANCE);
	}

	/**
	 * 返回用于解析 Properties 文件的 PropertiesPersister。
	 */
	public PropertiesPersister getPropertiesPersister() {
		return this.propertiesPersister;
	}


	/**
	 * 从指定 Properties 文件加载 Bean 定义，使用所有属性键（不按前缀过滤）。
	 * @param resource Properties 文件的资源描述符
	 * @return 找到的 Bean 定义数量
	 * @throws BeanDefinitionStoreException 加载或解析出错时
	 * @see #loadBeanDefinitions(org.springframework.core.io.Resource, String)
	 */
	@Override
	public int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException {
		return loadBeanDefinitions(new EncodedResource(resource), null);
	}

	/**
	 * 从指定 Properties 文件加载 Bean 定义。
	 * @param resource Properties 文件的资源描述符
	 * @param prefix 键的前缀过滤器：例如 'beans.'（可为空或 {@code null}）
	 * @return 找到的 Bean 定义数量
	 * @throws BeanDefinitionStoreException 加载或解析出错时
	 */
	public int loadBeanDefinitions(Resource resource, @Nullable String prefix) throws BeanDefinitionStoreException {
		return loadBeanDefinitions(new EncodedResource(resource), prefix);
	}

	/**
	 * 从指定 Properties 文件加载 Bean 定义。
	 * @param encodedResource Properties 文件的资源描述符，可指定解析编码
	 * @return 找到的 Bean 定义数量
	 * @throws BeanDefinitionStoreException 加载或解析出错时
	 */
	public int loadBeanDefinitions(EncodedResource encodedResource) throws BeanDefinitionStoreException {
		return loadBeanDefinitions(encodedResource, null);
	}

	/**
	 * 从指定 Properties 文件加载 Bean 定义。
	 * @param encodedResource Properties 文件的资源描述符，可指定解析编码
	 * @param prefix 键的前缀过滤器：例如 'beans.'（可为空或 {@code null}）
	 * @return 找到的 Bean 定义数量
	 * @throws BeanDefinitionStoreException 加载或解析出错时
	 */
	public int loadBeanDefinitions(EncodedResource encodedResource, @Nullable String prefix)
			throws BeanDefinitionStoreException {

		if (logger.isTraceEnabled()) {
			logger.trace("Loading properties bean definitions from " + encodedResource);
		}

		Properties props = new Properties();
		try {
			try (InputStream is = encodedResource.getResource().getInputStream()) {
				if (encodedResource.getEncoding() != null) {
					getPropertiesPersister().load(props, new InputStreamReader(is, encodedResource.getEncoding()));
				}
				else {
					getPropertiesPersister().load(props, is);
				}
			}

			int count = registerBeanDefinitions(props, prefix, encodedResource.getResource().getDescription());
			if (logger.isDebugEnabled()) {
				logger.debug("Loaded " + count + " bean definitions from " + encodedResource);
			}
			return count;
		}
		catch (IOException ex) {
			throw new BeanDefinitionStoreException("Could not parse properties from " + encodedResource.getResource(), ex);
		}
	}

	/**
	 * 注册 ResourceBundle 中包含的 Bean 定义，使用所有属性键（不按前缀过滤）。
	 * @param rb 待加载的 ResourceBundle
	 * @return 找到的 Bean 定义数量
	 * @throws BeanDefinitionStoreException 加载或解析出错时
	 * @see #registerBeanDefinitions(java.util.ResourceBundle, String)
	 */
	public int registerBeanDefinitions(ResourceBundle rb) throws BeanDefinitionStoreException {
		return registerBeanDefinitions(rb, null);
	}

	/**
	 * 注册 ResourceBundle 中包含的 Bean 定义。
	 * <p>语法与 Map 类似。此方法便于启用标准 Java 国际化支持。
	 * @param rb 待加载的 ResourceBundle
	 * @param prefix 键的前缀过滤器：例如 'beans.'（可为空或 {@code null}）
	 * @return 找到的 Bean 定义数量
	 * @throws BeanDefinitionStoreException 加载或解析出错时
	 */
	public int registerBeanDefinitions(ResourceBundle rb, @Nullable String prefix) throws BeanDefinitionStoreException {
		// 创建 Map 并调用重载方法
		Map<String, Object> map = new HashMap<>();
		Enumeration<String> keys = rb.getKeys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			map.put(key, rb.getObject(key));
		}
		return registerBeanDefinitions(map, prefix);
	}


	/**
	 * 注册 Map 中包含的 Bean 定义，使用所有属性键（不按前缀过滤）。
	 * @param map {@code name} 到 {@code property}（String 或 Object）的映射。
	 * 属性值若来自 Properties 文件等则为字符串。属性名（键）<b>必须</b>为 String。类键必须为 String。
	 * @return 找到的 Bean 定义数量
	 * @throws BeansException 加载或解析出错时
	 * @see #registerBeanDefinitions(java.util.Map, String, String)
	 */
	public int registerBeanDefinitions(Map<?, ?> map) throws BeansException {
		return registerBeanDefinitions(map, null);
	}

	/**
	 * 注册 Map 中包含的 Bean 定义。
	 * 忽略不符合条件的属性。
	 * @param map {@code name} 到 {@code property}（String 或 Object）的映射
	 * @param prefix 键的前缀过滤器：例如 'beans.'（可为空或 {@code null}）
	 * @return 找到的 Bean 定义数量
	 * @throws BeansException 加载或解析出错时
	 */
	public int registerBeanDefinitions(Map<?, ?> map, @Nullable String prefix) throws BeansException {
		return registerBeanDefinitions(map, prefix, "Map " + map);
	}

	/**
	 * 注册 Map 中包含的 Bean 定义。
	 * 忽略不符合条件的属性。
	 * @param map {@code name} 到 {@code property}（String 或 Object）的映射
	 * @param prefix 键的前缀过滤器：例如 'beans.'（可为空或 {@code null}）
	 * @param resourceDescription Map 来源资源的描述（用于日志）
	 * @return 找到的 Bean 定义数量
	 * @throws BeansException 加载或解析出错时
	 * @see #registerBeanDefinitions(Map, String)
	 */
	public int registerBeanDefinitions(Map<?, ?> map, @Nullable String prefix, String resourceDescription)
			throws BeansException {

		if (prefix == null) {
			prefix = "";
		}
		int beanCount = 0;

		for (Object key : map.keySet()) {
			if (!(key instanceof String keyString)) {
				throw new IllegalArgumentException("Illegal key [" + key + "]: only Strings allowed");
			}
			if (keyString.startsWith(prefix)) {
				// 键格式：prefix<name>.property
				String nameAndProperty = keyString.substring(prefix.length());
				// 在属性名之前查找点号，忽略属性键中的点号
				int sepIdx ;
				int propKeyIdx = nameAndProperty.indexOf(PropertyAccessor.PROPERTY_KEY_PREFIX);
				if (propKeyIdx != -1) {
					sepIdx = nameAndProperty.lastIndexOf(SEPARATOR, propKeyIdx);
				}
				else {
					sepIdx = nameAndProperty.lastIndexOf(SEPARATOR);
				}
				if (sepIdx != -1) {
					String beanName = nameAndProperty.substring(0, sepIdx);
					if (logger.isTraceEnabled()) {
						logger.trace("Found bean name '" + beanName + "'");
					}
					if (!getRegistry().containsBeanDefinition(beanName)) {
						// 若尚未注册，则注册该 Bean 定义
						registerBeanDefinition(beanName, map, prefix + beanName, resourceDescription);
						++beanCount;
					}
				}
				else {
					// 忽略：不是有效的 Bean 名称和属性，尽管以所需前缀开头
					if (logger.isDebugEnabled()) {
						logger.debug("Invalid bean name and property [" + nameAndProperty + "]");
					}
				}
			}
		}

		return beanCount;
	}

	/**
	 * 获取给定前缀（将被剥离）下的所有属性值，
	 * 并将它们定义的 Bean 以给定名称注册到工厂。
	 * @param beanName 要定义的 Bean 名称
	 * @param map 包含字符串对的 Map
	 * @param prefix 每个条目的前缀（将被剥离）
	 * @param resourceDescription Map 来源资源的描述（用于日志）
	 * @throws BeansException Bean 定义无法解析或注册时
	 */
	protected void registerBeanDefinition(String beanName, Map<?, ?> map, String prefix, String resourceDescription)
			throws BeansException {

		String className = null;
		String parent = null;
		String scope = BeanDefinition.SCOPE_SINGLETON;
		boolean isAbstract = false;
		boolean lazyInit = false;

		ConstructorArgumentValues cas = new ConstructorArgumentValues();
		MutablePropertyValues pvs = new MutablePropertyValues();

		String prefixWithSep = prefix + SEPARATOR;
		int beginIndex = prefixWithSep.length();

		for (Map.Entry<?, ?> entry : map.entrySet()) {
			String key = ((String) entry.getKey()).strip();
			if (key.startsWith(prefixWithSep)) {
				String property = key.substring(beginIndex);
				if (CLASS_KEY.equals(property)) {
					className = ((String) entry.getValue()).strip();
				}
				else if (PARENT_KEY.equals(property)) {
					parent = ((String) entry.getValue()).strip();
				}
				else if (ABSTRACT_KEY.equals(property)) {
					String val = ((String) entry.getValue()).strip();
					isAbstract = TRUE_VALUE.equals(val);
				}
				else if (SCOPE_KEY.equals(property)) {
					// Spring 2.0 风格
					scope = ((String) entry.getValue()).strip();
				}
				else if (SINGLETON_KEY.equals(property)) {
					// Spring 1.2 风格
					String val = ((String) entry.getValue()).strip();
					scope = (!StringUtils.hasLength(val) || TRUE_VALUE.equals(val) ?
							BeanDefinition.SCOPE_SINGLETON : BeanDefinition.SCOPE_PROTOTYPE);
				}
				else if (LAZY_INIT_KEY.equals(property)) {
					String val = ((String) entry.getValue()).strip();
					lazyInit = TRUE_VALUE.equals(val);
				}
				else if (property.startsWith(CONSTRUCTOR_ARG_PREFIX)) {
					if (property.endsWith(REF_SUFFIX)) {
						int index = Integer.parseInt(property, 1, property.length() - REF_SUFFIX.length(), 10);
						cas.addIndexedArgumentValue(index, new RuntimeBeanReference(entry.getValue().toString()));
					}
					else {
						int index = Integer.parseInt(property, 1, property.length(), 10);
						cas.addIndexedArgumentValue(index, readValue(entry));
					}
				}
				else if (property.endsWith(REF_SUFFIX)) {
					// 非真实属性，而是对另一个 prototype 的引用
					// 提取属性名：property 格式为 dog(ref)
					property = property.substring(0, property.length() - REF_SUFFIX.length());
					String ref = ((String) entry.getValue()).strip();

					// 被引用的 Bean 尚未注册也无妨：运行时会解析引用
					Object val = new RuntimeBeanReference(ref);
					pvs.add(property, val);
				}
				else {
					// 普通 Bean 属性
					pvs.add(property, readValue(entry));
				}
			}
		}

		if (logger.isTraceEnabled()) {
			logger.trace("Registering bean definition for bean name '" + beanName + "' with " + pvs);
		}

		// 若非处理父 Bean 本身且未指定类名，则使用默认父 Bean
		// 后者出于向后兼容必须保留
		if (parent == null && className == null && !beanName.equals(this.defaultParentBean)) {
			parent = this.defaultParentBean;
		}

		try {
			AbstractBeanDefinition bd = BeanDefinitionReaderUtils.createBeanDefinition(
					parent, className, getBeanClassLoader());
			bd.setScope(scope);
			bd.setAbstract(isAbstract);
			bd.setLazyInit(lazyInit);
			bd.setConstructorArgumentValues(cas);
			bd.setPropertyValues(pvs);
			getRegistry().registerBeanDefinition(beanName, bd);
		}
		catch (ClassNotFoundException ex) {
			throw new CannotLoadBeanClassException(resourceDescription, beanName, className, ex);
		}
		catch (LinkageError err) {
			throw new CannotLoadBeanClassException(resourceDescription, beanName, className, err);
		}
	}

	/**
	 * 读取条目的值。对以星号前缀的值正确解释为 Bean 引用。
	 */
	private Object readValue(Map.Entry<?, ?> entry) {
		Object val = entry.getValue();
		if (val instanceof String strVal) {
			// 若以引用前缀开头...
			if (strVal.startsWith(REF_PREFIX)) {
				// 展开引用
				String targetName = strVal.substring(1);
				if (targetName.startsWith(REF_PREFIX)) {
					// 转义前缀 -> 使用普通值
					val = targetName;
				}
				else {
					val = new RuntimeBeanReference(targetName);
				}
			}
		}
		return val;
	}

}
