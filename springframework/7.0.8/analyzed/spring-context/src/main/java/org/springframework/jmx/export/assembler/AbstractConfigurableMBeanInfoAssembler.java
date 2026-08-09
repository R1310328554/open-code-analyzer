
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
