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

package org.springframework.instrument.classloading;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 基于 {@link ClassFileTransformer} 的织入器，可对类字节数组依次应用多个转换器。
 * 通常用于类加载器内部。
 *
 * <p>注意：本类刻意保持最少外部依赖，因其会打包进 weaver JAR 并部署到应用服务器。
 *
 * @author Rod Johnson
 * @author Costin Leau
 * @author Juergen Hoeller
 * @since 2.0
 */
public class WeavingTransformer {

	private final @Nullable ClassLoader classLoader;

	private final List<ClassFileTransformer> transformers = new ArrayList<>();


	/**
	 * 为给定类加载器创建新的 WeavingTransformer。
	 * @param classLoader 要为其构建转换器的 ClassLoader
	 */
	public WeavingTransformer(@Nullable ClassLoader classLoader) {
		this.classLoader = classLoader;
	}


	/**
	 * 添加由本织入器应用的类文件转换器。
	 * @param transformer 要注册的类文件转换器
	 */
	public void addTransformer(ClassFileTransformer transformer) {
		Assert.notNull(transformer, "Transformer must not be null");
		this.transformers.add(transformer);
	}


	/**
	 * 对给定类字节定义应用转换。
	 * 本方法始终返回非空字节数组（若未发生转换，内容与原始数组相同）。
	 * @param className 类的全限定名（点分格式，如 some.package.SomeClass）
	 * @param bytes 类字节定义
	 * @return （可能已转换的）类字节定义
	 */
	public byte[] transformIfNecessary(String className, byte[] bytes) {
		String internalName = StringUtils.replace(className, ".", "/");
		return transformIfNecessary(className, internalName, bytes, null);
	}

	/**
	 * 对给定类字节定义应用转换。
	 * 本方法始终返回非空字节数组（若未发生转换，内容与原始数组相同）。
	 * @param className 类的全限定名（点分格式，如 some.package.SomeClass）
	 * @param internalName 类内部名称（斜杠格式，如 some/package/SomeClass）
	 * @param bytes 类字节定义
	 * @param pd 要使用的保护域（可为 {@code null}）
	 * @return （可能已转换的）类字节定义
	 */
	public byte[] transformIfNecessary(String className, String internalName, byte[] bytes, @Nullable ProtectionDomain pd) {
		byte[] result = bytes;
		for (ClassFileTransformer cft : this.transformers) {
			try {
				byte[] transformed = cft.transform(this.classLoader, internalName, null, pd, result);
				if (transformed != null) {
					result = transformed;
				}
			}
			catch (IllegalClassFormatException ex) {
				throw new IllegalStateException("Class file transformation failed", ex);
			}
		}
		return result;
	}

}
