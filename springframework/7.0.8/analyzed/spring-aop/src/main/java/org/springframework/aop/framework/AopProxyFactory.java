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
 * 能基于 {@link AdvisedSupport} 配置对象创建 AOP 代理的工厂接口。
 *
 * <p>代理应遵守以下约定：
 * <ul>
 * <li>实现配置指示应代理的所有接口。
 * <li>实现 {@link Advised} 接口。
 * <li>实现 equals 方法以比较被代理接口、advice 与目标。
 * <li>若所有通知器与目标可序列化，则代理也应可序列化。
 * <li>若通知器与目标线程安全，则代理也应线程安全。
 * </ul>
 *
 * <p>代理可能允许或不允许修改 advice。
 * 若不允许（例如配置已冻结），尝试修改 advice 时应抛出 {@link AopConfigException}。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public interface AopProxyFactory {

	/**
	 * 为给定 AOP 配置创建 {@link AopProxy}。
	 * @param config 以 AdvisedSupport 对象形式表示的 AOP 配置
	 * @return 对应的 AOP 代理
	 * @throws AopConfigException 若配置无效
	 */
	AopProxy createAopProxy(AdvisedSupport config) throws AopConfigException;

}
