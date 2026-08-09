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

package org.springframework.beans.factory.config;

import java.beans.PropertyEditor;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.core.Ordered;
import org.springframework.util.ClassUtils;

/**
 * {@link BeanFactoryPostProcessor} 实现，便于注册自定义
 * {@link PropertyEditor 属性编辑器}。
 *
 * <p>若要注册 {@link PropertyEditor} 实例，推荐做法是使用自定义
 * {@link PropertyEditorRegistrar} 实现，在指定的
 * {@link org.springframework.beans.PropertyEditorRegistry 注册表}上注册所需的编辑器实例。
 * 每个 PropertyEditorRegistrar 可注册任意数量的自定义编辑器。
 *
 * <pre class="code">
 * &lt;bean id="customEditorConfigurer" class="org.springframework.beans.factory.config.CustomEditorConfigurer"&gt;
 *   &lt;property name="propertyEditorRegistrars"&gt;
 *     &lt;list&gt;
 *       &lt;bean class="mypackage.MyCustomDateEditorRegistrar"/&gt;
 *       &lt;bean class="mypackage.MyObjectEditorRegistrar"/&gt;
 *     &lt;/list&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;
 * </pre>
 *
 * <p>
 * 通过 {@code customEditors} 属性注册 {@link PropertyEditor} <em>类</em> 也完全可行。
 * Spring 会在每次编辑尝试时为其创建新实例：
 *
 * <pre class="code">
 * &lt;bean id="customEditorConfigurer" class="org.springframework.beans.factory.config.CustomEditorConfigurer"&gt;
 *   &lt;property name="customEditors"&gt;
 *     &lt;map&gt;
 *       &lt;entry key="java.util.Date" value="mypackage.MyCustomDateEditor"/&gt;
 *       &lt;entry key="mypackage.MyObject" value="mypackage.MyObjectEditor"/&gt;
 *     &lt;/map&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;
 * </pre>
 *
 * <p>
 * 注意，不应通过 {@code customEditors} 属性注册 {@link PropertyEditor} Bean 实例，
 * 因为 {@link PropertyEditor 属性编辑器}是有状态的，实例在每次编辑尝试时都需要同步。
 * 若需控制 {@link PropertyEditor 属性编辑器}的实例化过程，请使用
 * {@link PropertyEditorRegistrar} 进行注册。
 *
 * <p>
 * 还支持 "java.lang.String[]" 风格的数组类名以及基本类型类名（例如 "boolean"）。
 * 实际类名解析委托给 {@link ClassUtils}。
 *
 * <p><b>注意：</b>通过本配置器注册的自定义属性编辑器<i>不</i>适用于数据绑定。
 * 数据绑定的自定义编辑器需在 {@link org.springframework.validation.DataBinder} 上注册：
 * 可使用公共基类，或委托给公共的 PropertyEditorRegistrar 实现以复用编辑器注册逻辑。
 *
 * @author Juergen Hoeller
 * @since 27.02.2004
 * @see java.beans.PropertyEditor
 * @see org.springframework.beans.PropertyEditorRegistrar
 * @see ConfigurableBeanFactory#addPropertyEditorRegistrar
 * @see ConfigurableBeanFactory#registerCustomEditor
 * @see org.springframework.validation.DataBinder#registerCustomEditor
 */
public class CustomEditorConfigurer implements BeanFactoryPostProcessor, Ordered {

	/** 日志记录器。 */
	protected final Log logger = LogFactory.getLog(getClass());

	/** 执行顺序，默认为最低优先级（与非 Ordered 相同）。 */
	private int order = Ordered.LOWEST_PRECEDENCE;  // default: same as non-Ordered

	/** 属性编辑器注册器数组。 */
	private PropertyEditorRegistrar @Nullable [] propertyEditorRegistrars;

	/** 自定义编辑器映射（类型 → 编辑器类）。 */
	private @Nullable Map<Class<?>, Class<? extends PropertyEditor>> customEditors;


	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	/**
	 * 指定要应用于当前应用上下文中已定义 Bean 的
	 * {@link PropertyEditorRegistrar PropertyEditorRegistrars}。
	 * <p>可与 {@link org.springframework.validation.DataBinder DataBinders} 等共享
	 * {@code PropertyEditorRegistrars}。此外，可避免对自定义编辑器进行同步：
	 * {@code PropertyEditorRegistrar} 在每次 Bean 创建尝试时都会创建新的编辑器实例。
	 * @see ConfigurableListableBeanFactory#addPropertyEditorRegistrar
	 */
	public void setPropertyEditorRegistrars(PropertyEditorRegistrar[] propertyEditorRegistrars) {
		this.propertyEditorRegistrars = propertyEditorRegistrars;
	}

	/**
	 * 通过 {@link Map} 指定要注册的自定义编辑器，以所需类型的类名作为键，
	 * 关联 {@link PropertyEditor} 的类名作为值。
	 * @see ConfigurableListableBeanFactory#registerCustomEditor
	 */
	public void setCustomEditors(Map<Class<?>, Class<? extends PropertyEditor>> customEditors) {
		this.customEditors = customEditors;
	}


	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		// 注册属性编辑器注册器
		if (this.propertyEditorRegistrars != null) {
			for (PropertyEditorRegistrar propertyEditorRegistrar : this.propertyEditorRegistrars) {
				beanFactory.addPropertyEditorRegistrar(propertyEditorRegistrar);
			}
		}
		// 注册自定义编辑器类
		if (this.customEditors != null) {
			this.customEditors.forEach(beanFactory::registerCustomEditor);
		}
	}

}
