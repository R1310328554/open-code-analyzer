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
 * 由能够基于 {@link AdvisedSupport} 配置对象创建 AOP 代理的工厂实现的接口。
 * <p>代理应遵守以下约定： <ul> <li> 它们应实现配置指示应代理的所有接口。 <li>他们应该实现{@link Advised}接口。 <li> 他们应该实现 equa
 * ls 方法来比较代理接口、建议和目标。 <li> 如果所有顾问和目标都是可序列化的，那么它们应该是可序列化的。 <li> 如果顾问和目标是线程安全的，那么它们应该是线程安全的。
 *  </ul>
 * <p>Proxies 可能允许也可能不允许更改建议。如果他们不允许建议更改（例如，因为配置被冻结），代理应该在尝试更改建议时抛出 {@link AopConfigExcepti
 * on}。
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public interface AopProxyFactory {

	/**
	 * 为给定的 AOP 配置创建 {@link AopProxy}。
	 * @param config AdvisedSupport 对象形式的 AOP 配置
	 * @return 对应的AOP代理
	 * @throws AopConfigException 如果配置无效
	 */
	AopProxy createAopProxy(AdvisedSupport config) throws AopConfigException;

}
