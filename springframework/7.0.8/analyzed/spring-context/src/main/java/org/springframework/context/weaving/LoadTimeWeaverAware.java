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

package org.springframework.context.weaving;

import org.springframework.beans.factory.Aware;
import org.springframework.instrument.classloading.LoadTimeWeaver;

/**
 * 希望接收应用上下文默认 {@link LoadTimeWeaver} 通知的对象应实现的接口。
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 2.5
 * @see org.springframework.context.ConfigurableApplicationContext#LOAD_TIME_WEAVER_BEAN_NAME
 */
public interface LoadTimeWeaverAware extends Aware {

	/**
	 * 设置所在 {@link org.springframework.context.ApplicationContext ApplicationContext}
	 * 的 {@link LoadTimeWeaver}。
	 * <p>在普通 Bean 属性注入完成之后、初始化回调（如
	 * {@link org.springframework.beans.factory.InitializingBean InitializingBean} 的
	 * {@link org.springframework.beans.factory.InitializingBean#afterPropertiesSet() afterPropertiesSet()}
	 * 或自定义 init 方法）之前调用；且在
	 * {@link org.springframework.context.ApplicationContextAware ApplicationContextAware} 的
	 * {@link org.springframework.context.ApplicationContextAware#setApplicationContext setApplicationContext(..)} 之后调用。
	 * <p><b>注意：</b>仅当应用上下文中实际存在 {@code LoadTimeWeaver} 时才会调用本方法。
	 * 若不存在，则不会调用，由实现类自行决定是否激活织入依赖。
	 * @param loadTimeWeaver {@code LoadTimeWeaver} 实例（永不为 {@code null}）
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext
	 */
	void setLoadTimeWeaver(LoadTimeWeaver loadTimeWeaver);

}
