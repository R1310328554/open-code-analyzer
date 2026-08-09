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

package org.springframework.boot.autoconfigure;

import java.util.EventListener;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;

/**
 * 可在 {@code spring.factories} 中注册的监听器，用于接收已导入自动配置的详情。
 * <p>
 * {@link AutoConfigurationImportListener} 可实现以下任意
 * {@link org.springframework.beans.factory.Aware Aware} 接口，
 * 相应方法会在调用
 * {@link #onAutoConfigurationImportEvent(AutoConfigurationImportEvent)} 之前被调用：
 * <ul>
 * <li>{@link EnvironmentAware}</li>
 * <li>{@link BeanFactoryAware}</li>
 * <li>{@link BeanClassLoaderAware}</li>
 * <li>{@link ResourceLoaderAware}</li>
 * </ul>
 *
 * @author Phillip Webb
 * @since 1.5.0
 */
@FunctionalInterface
public interface AutoConfigurationImportListener extends EventListener {

	/**
	 * 处理自动配置导入事件。
	 * @param event 待响应的事件
	 */
	void onAutoConfigurationImportEvent(AutoConfigurationImportEvent event);

}
