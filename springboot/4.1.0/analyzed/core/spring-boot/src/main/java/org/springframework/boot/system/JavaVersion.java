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

package org.springframework.boot.system;

import java.io.Console;
import java.io.Reader;
import java.text.NumberFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.Future;

import org.jspecify.annotations.Nullable;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.util.ClassUtils;

/**
 * 已知的 Java 版本枚举。
 * 通过检测各版本特有 API 是否存在来推断当前运行时版本。
 *
 * @author Oliver Gierke
 * @author Phillip Webb
 * @author Moritz Halbritter
 * @since 2.0.0
 */
public enum JavaVersion {

	/**
	 * Java 17。
	 * @since 2.5.3
	 */
	SEVENTEEN("17", Console.class, "charset"),

	/**
	 * Java 18。
	 * @since 2.5.11
	 */
	EIGHTEEN("18", Duration.class, "isPositive"),

	/**
	 * Java 19。
	 * @since 2.6.12
	 */
	NINETEEN("19", Future.class, "state"),

	/**
	 * Java 20。
	 * @since 2.7.13
	 */
	TWENTY("20", Class.class, "accessFlags"),

	/**
	 * Java 21。
	 * @since 2.7.16
	 */
	TWENTY_ONE("21", SortedSet.class, "getFirst"),

	/**
	 * Java 22。
	 * @since 3.2.4
	 */
	TWENTY_TWO("22", Console.class, "isTerminal"),

	/**
	 * Java 23。
	 * @since 3.2.9
	 */
	TWENTY_THREE("23", NumberFormat.class, "isStrict"),

	/**
	 * Java 24。
	 * @since 3.4.3
	 */
	TWENTY_FOUR("24", Reader.class, "of", CharSequence.class),

	/**
	 * Java 25。
	 * @since 3.5.7
	 */
	TWENTY_FIVE("25", Reader.class, "readAllLines"),

	/**
	 * Java 26。
	 * @since 4.0.3
	 */
	TWENTY_SIX("26", String.class, "equalsFoldCase", String.class);

	private final String name;

	private final boolean available;

	private final Class<?> versionSpecificClass;

	JavaVersion(String name, Class<?> versionSpecificClass, String versionSpecificMethod, Class<?>... paramTypes) {
		this.name = name;
		this.versionSpecificClass = versionSpecificClass;
		this.available = ClassUtils.hasMethod(versionSpecificClass, versionSpecificMethod, paramTypes);
	}

	@Override
	public String toString() {
		return this.name;
	}

	/**
	 * 返回当前运行时的 {@link JavaVersion}。
	 * 从高版本向低版本依次检测，未匹配时默认 {@link #SEVENTEEN}。
	 *
	 * @return the {@link JavaVersion} Java 版本
	 */
	public static JavaVersion getJavaVersion() {
		List<JavaVersion> candidates = Arrays.asList(JavaVersion.values());
		Collections.reverse(candidates);
		for (JavaVersion candidate : candidates) {
			if (candidate.available) {
				return candidate;
			}
		}
		return SEVENTEEN;
	}

	/**
	 * 判断本版本是否等于或高于给定版本。
	 *
	 * @param version the version to compare 待比较的版本
	 * @return {@code true} if this version is equal to or newer than {@code version} 若本版本不低于给定版本则为 {@code true}
	 */
	public boolean isEqualOrNewerThan(JavaVersion version) {
		return compareTo(version) >= 0;
	}

	/**
	 * 判断本版本是否低于给定版本。
	 *
	 * @param version the version to compare 待比较的版本
	 * @return {@code true} if this version is older than {@code version} 若本版本低于给定版本则为 {@code true}
	 */
	public boolean isOlderThan(JavaVersion version) {
		return compareTo(version) < 0;
	}

	static class Hints implements RuntimeHintsRegistrar {

		@Override
		public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
			for (JavaVersion javaVersion : JavaVersion.values()) {
				hints.reflection().registerType(javaVersion.versionSpecificClass);
			}
		}

	}

}
