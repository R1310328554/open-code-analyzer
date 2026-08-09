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
 * 为 {@link org.springframework.aop.IntroductionInfo} 实现提供支持。
 *
 * <p>便于子类从给定对象添加全部接口，
 * 并抑制不应添加的接口。也可查询所有引入接口。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
@SuppressWarnings("serial")
public class IntroductionInfoSupport implements IntroductionInfo, Serializable {

	protected final Set<Class<?>> publishedInterfaces = new LinkedHashSet<>();

	private transient Map<Method, Boolean> rememberedMethods = new ConcurrentHashMap<>(32);


	/**
	 * 抑制指定接口（可能因委托实现而被自动检测）。
	 * 调用本方法可在代理层排除内部接口。
	 * <p>若委托未实现该接口则不执行任何操作。
	 * @param ifc 要抑制的接口
	 */
	public void suppressInterface(Class<?> ifc) {
		this.publishedInterfaces.remove(ifc);
	}

	@Override
	public Class<?>[] getInterfaces() {
		return ClassUtils.toClassArray(this.publishedInterfaces);
	}

	/**
	 * 检查指定接口是否为已发布的引入接口。
	 * @param ifc 待检查的接口
	 * @return 该接口是否属于本引入
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
	 * 在代理层发布给定委托实现的所有接口。
	 * @param delegate 委托对象
	 */
	protected void implementInterfacesOnObject(Object delegate) {
		this.publishedInterfaces.addAll(ClassUtils.getAllInterfacesAsSet(delegate));
	}

	/**
	 * 该方法是否位于引入接口上？
	 * @param mi 方法调用
	 * @return 被调用方法是否位于引入接口上
	 */
	protected final boolean isMethodOnIntroducedInterface(MethodInvocation mi) {
		Boolean rememberedResult = this.rememberedMethods.get(mi.getMethod());
		if (rememberedResult != null) {
			return rememberedResult;
		}
		else {
			// 计算并缓存结果。
			boolean result = implementsInterface(mi.getMethod().getDeclaringClass());
			this.rememberedMethods.put(mi.getMethod(), result);
			return result;
		}
	}


	//---------------------------------------------------------------------
	// 序列化支持
	//---------------------------------------------------------------------

	/**
	 * 实现本方法仅用于恢复 logger。
	 * 不将 logger 设为 static，否则子类会使用本类的日志类别。
	 */
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		// 依赖默认序列化；反序列化后仅初始化状态。
		ois.defaultReadObject();
		// 初始化 transient 字段。
		this.rememberedMethods = new ConcurrentHashMap<>(32);
	}

}
