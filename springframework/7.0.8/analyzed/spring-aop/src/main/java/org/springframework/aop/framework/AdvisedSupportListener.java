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

/**
 * 注册在 {@link ProxyCreatorSupport} 对象上的监听器，
 * 可在激活及 advice 变更时接收回调。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see ProxyCreatorSupport#addListener
 */
public interface AdvisedSupportListener {

	/**
	 * 创建第一个代理时调用。
	 * @param advised AdvisedSupport 对象
	 */
	void activated(AdvisedSupport advised);

	/**
	 * 代理创建后 advice 发生变更时调用。
	 * @param advised AdvisedSupport 对象
	 */
	void adviceChanged(AdvisedSupport advised);

}
