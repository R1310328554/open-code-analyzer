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

package org.springframework.aop.support;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.aopalliance.intercept.MethodInvocation;

import org.springframework.aop.IntroductionInfo;
import org.springframework.util.ClassUtils;

/**
 * 支持 {@link org.springframework.aop.IntroductionInfo} 的实现。
 * <p>A允许子类方便地添加给定对象的所有接口，并抑制不应添加的接口。还允许查询所有引入的接口。
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
@SuppressWarnings("serial")
public class IntroductionInfoSupport implements IntroductionInfo, Serializable {

	protected final Set<Class<?>> publishedInterfaces = new LinkedHashSet<>();

	private transient Map<Method, Boolean> rememberedMethods = new ConcurrentHashMap<>(32);


	/**
	 * 抑制指定的接口，该接口可能由于委托实现而被自动检测到。调用此方法可排除内部接口在代理级别可见。如果委托未实现接口，则 <p> 不执行任何操作。
	 * @param ifc 要抑制的接口
	 */
	public void suppressInterface(Class<?> ifc) {
		this.publishedInterfaces.remove(ifc);
	}

	/**
	 * 获取 Interfaces（`Interfaces`）。
	 */
	@Override
	public Class<?>[] getInterfaces() {
		return ClassUtils.toClassArray(this.publishedInterfaces);
	}

	/**
	 * 检查指定接口是否为已发布的介绍接口。
	 * @param ifc 要检查的接口
	 * @return 该界面是本介绍的一部分
	 */
	public boolean implementsInterface(Class<?> ifc) {
		for (Class<?> pubIfc : this.publishedInterfaces) {
			if (ifc.isInterface() && ifc.isAssignableFrom(pubIfc)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 发布给定委托在代理级别实现的所有接口。
	 * @param delegate 委托对象
	 */
	protected void implementInterfacesOnObject(Object delegate) {
		this.publishedInterfaces.addAll(ClassUtils.getAllInterfacesAsSet(delegate));
	}

	/**
	 * 这个方法是在引入的接口上吗？
	 * @param mi 方法调用
	 * @return 调用的方法位于引入的接口上
	 */
	protected final boolean isMethodOnIntroducedInterface(MethodInvocation mi) {
		Boolean rememberedResult = this.rememberedMethods.get(mi.getMethod());
		if (rememberedResult != null) {
			return rememberedResult;
		}
		else {
			// 计算出来并缓存它。
			boolean result = implementsInterface(mi.getMethod().getDeclaringClass());
			this.rememberedMethods.put(mi.getMethod(), result);
			return result;
		}
	}


	//---------------------------------------------------------------------
	// 序列化支持
	//---------------------------------------------------------------------

	/**
	 * 此方法仅用于恢复记录器。我们不会将记录器设置为静态，因为这意味着子类将使用此类的日志类别。
	 */
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		// 依赖默认序列化；只需在反序列化后初始化状态即可。
		ois.defaultReadObject();
		// 初始化瞬态字段。
		this.rememberedMethods = new ConcurrentHashMap<>(32);
	}

}
