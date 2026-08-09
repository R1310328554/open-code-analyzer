#!/usr/bin/env python3
from pathlib import Path
import re

ROOT = Path("/workspace/springframework/7.0.8/analyzed/spring-context/src/main/java/org/springframework/jmx/export/assembler")

def write(name, content):
    p = ROOT / name
    p.write_text(content.replace("\\t", "\t"), encoding="utf-8")
    cn = len(re.findall(r"[\u4e00-\u9fff]", p.read_text()))
    print(f"OK cn={cn} {name}")

write("AbstractConfigurableMBeanInfoAssembler.java", r'''
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

package org.springframework.jmx.export.assembler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.modelmbean.ModelMBeanNotificationInfo;

import org.jspecify.annotations.Nullable;

import org.springframework.jmx.export.metadata.JmxMetadataUtils;
import org.springframework.jmx.export.metadata.ManagedNotification;
import org.springframework.util.StringUtils;

/**
 * 支持可配置 JMX 通知行为的 MBeanInfoAssembler 基类。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public abstract class AbstractConfigurableMBeanInfoAssembler extends AbstractReflectiveMBeanInfoAssembler {

	/** 全局默认的通知元数据。 */
	private ModelMBeanNotificationInfo @Nullable [] notificationInfos;

	/** Bean 键到通知元数据数组的映射。 */
	private final Map<String, ModelMBeanNotificationInfo[]> notificationInfoMappings = new HashMap<>();


	/**
	 * 设置全局通知元数据，将 {@code ManagedNotification} 转换为 ModelMBean 通知信息。
	 * @param notificationInfos 通知元数据数组
	 */
	public void setNotificationInfos(ManagedNotification[] notificationInfos) {
		ModelMBeanNotificationInfo[] infos = new ModelMBeanNotificationInfo[notificationInfos.length];
		for (int i = 0; i < notificationInfos.length; i++) {
			ManagedNotification notificationInfo = notificationInfos[i];
			infos[i] = JmxMetadataUtils.convertToModelMBeanNotificationInfo(notificationInfo);
		}
		this.notificationInfos = infos;
	}

	/**
	 * 设置 Bean 键到通知元数据的映射。
	 * 映射值可为单个 {@code ManagedNotification} 或其集合。
	 * @param notificationInfoMappings Bean 键到通知配置的映射
	 */
	public void setNotificationInfoMappings(Map<String, Object> notificationInfoMappings) {
		notificationInfoMappings.forEach((beanKey, result) ->
				this.notificationInfoMappings.put(beanKey, extractNotificationMetadata(result)));
	}


	@Override
	protected ModelMBeanNotificationInfo[] getNotificationInfo(Object managedBean, String beanKey) {
		ModelMBeanNotificationInfo[] result = null;
		if (StringUtils.hasText(beanKey)) {
			result = this.notificationInfoMappings.get(beanKey);
		}
		if (result == null) {
			result = this.notificationInfos;
		}
		return (result != null ? result : new ModelMBeanNotificationInfo[0]);
	}

	/**
	 * 从映射值中提取通知元数据，支持单个 {@code ManagedNotification} 或集合。
	 * @param mapValue 映射中的值
	 * @return ModelMBeanNotificationInfo 数组
	 */
	private ModelMBeanNotificationInfo[] extractNotificationMetadata(Object mapValue) {
		if (mapValue instanceof ManagedNotification mn) {
			return new ModelMBeanNotificationInfo[] {JmxMetadataUtils.convertToModelMBeanNotificationInfo(mn)};
		}
		else if (mapValue instanceof Collection<?> col) {
			List<ModelMBeanNotificationInfo> result = new ArrayList<>();
			for (Object colValue : col) {
				if (!(colValue instanceof ManagedNotification mn)) {
					throw new IllegalArgumentException(
							"Property 'notificationInfoMappings' only accepts ManagedNotifications for Map values");
				}
				result.add(JmxMetadataUtils.convertToModelMBeanNotificationInfo(mn));
			}
			return result.toArray(new ModelMBeanNotificationInfo[0]);
		}
		else {
			throw new IllegalArgumentException(
					"Property 'notificationInfoMappings' only accepts ManagedNotifications for Map values");
		}
	}

}
''')


write("MethodNameBasedMBeanInfoAssembler.java", r'''
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

package org.springframework.jmx.export.assembler;

import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.util.StringUtils;

/**
 * {@code AbstractReflectiveMBeanInfoAssembler} 的子类，允许指定要作为
 * MBean 操作与属性暴露的方法名。JavaBean getter/setter 会自动暴露为 JMX 属性。
 *
 * <p>可通过 {@code managedMethods} 属性提供方法名数组。若有多个 Bean 且各自
 * 需要不同的方法列表，可使用 {@code methodMappings} 将 Bean 键映射到方法名列表。
 *
 * <p>若同时指定 {@code methodMappings} 与 {@code managedMethods}，
 * Spring 会先在映射中查找；若未找到，则使用 {@code managedMethods} 定义的方法名。
 *
 * @author Juergen Hoeller
 * @since 1.2
 * @see #setManagedMethods
 * @see #setMethodMappings
 * @see InterfaceBasedMBeanInfoAssembler
 * @see SimpleReflectiveMBeanInfoAssembler
 * @see MethodExclusionMBeanInfoAssembler
 * @see org.springframework.jmx.export.MBeanExporter
 */
public class MethodNameBasedMBeanInfoAssembler extends AbstractConfigurableMBeanInfoAssembler {

	/** 用于创建管理接口的方法名集合。 */
	private @Nullable Set<String> managedMethods;

	/** Bean 键到方法名集合的映射。 */
	private @Nullable Map<String, Set<String>> methodMappings;


	/**
	 * 设置用于创建管理接口的方法名数组。
	 * <p>若某 Bean 在 {@code methodMappings} 中无对应条目，则使用这些方法名。
	 * @param methodNames 要暴露的方法名
	 * @see #setMethodMappings
	 */
	public void setManagedMethods(String... methodNames) {
		this.managedMethods = Set.of(methodNames);
	}

	/**
	 * 设置 Bean 键到逗号分隔方法名列表的映射。
	 * <p>属性键须与 Bean 键匹配，属性值为方法名列表。查找时 Spring 优先检查这些映射。
	 * @param mappings Bean 键到方法名的映射
	 */
	public void setMethodMappings(Properties mappings) {
		this.methodMappings = new HashMap<>();
		for (Enumeration<?> en = mappings.keys(); en.hasMoreElements();) {
			String beanKey = (String) en.nextElement();
			String[] methodNames = StringUtils.commaDelimitedListToStringArray(mappings.getProperty(beanKey));
			this.methodMappings.put(beanKey, Set.of(methodNames));
		}
	}


	@Override
	protected boolean includeReadAttribute(Method method, String beanKey) {
		return isMatch(method, beanKey);
	}

	@Override
	protected boolean includeWriteAttribute(Method method, String beanKey) {
		return isMatch(method, beanKey);
	}

	@Override
	protected boolean includeOperation(Method method, String beanKey) {
		return isMatch(method, beanKey);
	}

	/**
	 * 判断给定方法名是否在允许暴露的集合中。
	 * 优先查找 {@code methodMappings} 中该 Bean 的条目，否则回退到 {@code managedMethods}。
	 * @param method 待检查的方法
	 * @param beanKey Bean 键
	 * @return 方法名匹配则返回 {@code true}
	 */
	protected boolean isMatch(Method method, String beanKey) {
		if (this.methodMappings != null) {
			Set<String> methodNames = this.methodMappings.get(beanKey);
			if (methodNames != null) {
				return methodNames.contains(method.getName());
			}
		}
		return (this.managedMethods != null && this.managedMethods.contains(method.getName()));
	}

}
''')

write("MethodExclusionMBeanInfoAssembler.java", r'''
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

package org.springframework.jmx.export.assembler;

import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.util.StringUtils;

/**
 * {@code AbstractReflectiveMBeanInfoAssembler} 的子类，允许显式排除某些方法名，
 * 使其不作为 MBean 操作或属性暴露。
 *
 * <p>未显式排除的方法均会暴露给 JMX。JavaBean getter/setter 会自动暴露为 JMX 属性。
 *
 * <p>可通过 {@code ignoredMethods} 属性提供要排除的方法名数组。若有多个 Bean 且各自
 * 需要不同的排除列表，可使用 {@code ignoredMethodMappings} 将 Bean 键映射到方法名列表。
 *
 * <p>若同时指定 {@code ignoredMethodMappings} 与 {@code ignoredMethods}，
 * Spring 会先在映射中查找；若未找到，则使用 {@code ignoredMethods} 定义的方法名。
 *
 * @author Rob Harrop
 * @author Seth Ladd
 * @since 1.2.5
 * @see #setIgnoredMethods
 * @see #setIgnoredMethodMappings
 * @see InterfaceBasedMBeanInfoAssembler
 * @see SimpleReflectiveMBeanInfoAssembler
 * @see MethodNameBasedMBeanInfoAssembler
 * @see org.springframework.jmx.export.MBeanExporter
 */
public class MethodExclusionMBeanInfoAssembler extends AbstractConfigurableMBeanInfoAssembler {

	/** 全局要忽略的方法名集合。 */
	private @Nullable Set<String> ignoredMethods;

	/** Bean 键到要忽略的方法名集合的映射。 */
	private @Nullable Map<String, Set<String>> ignoredMethodMappings;


	/**
	 * 设置创建管理接口时要<b>忽略</b>的方法名数组。
	 * <p>若某 Bean 在 {@code ignoredMethodMappings} 中无对应条目，则使用这些方法名。
	 * @param ignoredMethodNames 要忽略的方法名
	 * @see #setIgnoredMethodMappings(java.util.Properties)
	 */
	public void setIgnoredMethods(String... ignoredMethodNames) {
		this.ignoredMethods = Set.of(ignoredMethodNames);
	}

	/**
	 * 设置 Bean 键到逗号分隔方法名列表的映射。
	 * <p>这些方法名在创建管理接口时会被<b>忽略</b>。
	 * <p>属性键须与 Bean 键匹配，属性值为方法名列表。查找时 Spring 优先检查这些映射。
	 * @param mappings Bean 键到方法名的映射
	 */
	public void setIgnoredMethodMappings(Properties mappings) {
		this.ignoredMethodMappings = new HashMap<>();
		for (Enumeration<?> en = mappings.keys(); en.hasMoreElements();) {
			String beanKey = (String) en.nextElement();
			String[] methodNames = StringUtils.commaDelimitedListToStringArray(mappings.getProperty(beanKey));
			this.ignoredMethodMappings.put(beanKey, Set.of(methodNames));
		}
	}


	@Override
	protected boolean includeReadAttribute(Method method, String beanKey) {
		return isNotIgnored(method, beanKey);
	}

	@Override
	protected boolean includeWriteAttribute(Method method, String beanKey) {
		return isNotIgnored(method, beanKey);
	}

	@Override
	protected boolean includeOperation(Method method, String beanKey) {
		return isNotIgnored(method, beanKey);
	}

	/**
	 * 判断给定方法是否应纳入管理接口，即未被配置为忽略。
	 * @param method 操作方法
	 * @param beanKey {@code MBeanExporter} 的 beans 映射中与该 MBean 关联的键
	 * @return 若方法未被忽略则返回 {@code true}
	 */
	protected boolean isNotIgnored(Method method, String beanKey) {
		if (this.ignoredMethodMappings != null) {
			Set<String> methodNames = this.ignoredMethodMappings.get(beanKey);
			if (methodNames != null) {
				return !methodNames.contains(method.getName());
			}
		}
		if (this.ignoredMethods != null) {
			return !this.ignoredMethods.contains(method.getName());
		}
		return true;
	}

}
''')


write("SimpleReflectiveMBeanInfoAssembler.java", r'''
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

package org.springframework.jmx.export.assembler;

import java.lang.reflect.Method;

/**
 * {@code AbstractReflectiveMBeanInfoAssembler} 的简单子类，
 * 对方法与属性的纳入始终返回 {@code true}，从而将所有 public 方法与属性
 * 分别暴露为 JMX 操作与属性。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2
 */
public class SimpleReflectiveMBeanInfoAssembler extends AbstractConfigurableMBeanInfoAssembler {

	/**
	 * 始终返回 {@code true}。
	 */
	@Override
	protected boolean includeReadAttribute(Method method, String beanKey) {
		return true;
	}

	/**
	 * 始终返回 {@code true}。
	 */
	@Override
	protected boolean includeWriteAttribute(Method method, String beanKey) {
		return true;
	}

	/**
	 * 始终返回 {@code true}。
	 */
	@Override
	protected boolean includeOperation(Method method, String beanKey) {
		return true;
	}

}
''')
