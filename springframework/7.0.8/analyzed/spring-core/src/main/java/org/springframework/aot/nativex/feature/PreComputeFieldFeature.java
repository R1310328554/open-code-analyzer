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

package org.springframework.aot.nativex.feature;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.regex.Pattern;

import org.graalvm.nativeimage.hosted.Feature;

/* ===== [OCA 中文解析] =====
class PreComputeFieldFeature — 意图说明

class `PreComputeFieldFeature`：请结合所属模块与调用方理解其在整体架构中的职责。；源文件: `spring-core/src/main/java/org/springframework/aot/nativex/feature/PreComputeFieldFeature.java`

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * GraalVM {@link Feature} that substitutes boolean field values that match certain patterns
 * with values pre-computed ahead-of-time without causing class build-time initialization.
 *
 * <p>It is possible to pass <pre style="code">-Dspring.native.precompute.log=verbose</pre> as a
 * <pre style="code">native-image</pre> compiler build argument to display detailed logs
 * about pre-computed fields.</p>
 *
 * @author Sebastien Deleuze
 * @author Phillip Webb
 * @since 6.0
 */
class PreComputeFieldFeature implements Feature {

	// [OCA] 字段 `verbose`：类成员状态。
	private static final boolean verbose =
			"verbose".equalsIgnoreCase(System.getProperty("spring.native.precompute.log"));

	// [OCA] 字段 `patterns`：类成员状态。
	private static final Pattern[] patterns = {
			Pattern.compile(Pattern.quote("org.springframework.core.NativeDetector#inNativeImage")),
			Pattern.compile(Pattern.quote("org.springframework.cglib.core.AbstractClassGenerator#inNativeImage")),
			Pattern.compile(Pattern.quote("org.springframework.aot.AotDetector#inNativeImage")),
			Pattern.compile(Pattern.quote("org.springframework.") + ".*#.*Present"),
			Pattern.compile(Pattern.quote("org.springframework.") + ".*#.*PRESENT"),
			Pattern.compile(Pattern.quote("reactor.core.") + ".*#.*Available"),
			Pattern.compile(Pattern.quote("org.apache.commons.logging.LogAdapter") + "#.*Present")
	};

	// [OCA] 字段 `throwawayClassLoader`：类成员状态。
	private final ThrowawayClassLoader throwawayClassLoader = new ThrowawayClassLoader(getClass().getClassLoader());


	@Override
	public void beforeAnalysis(BeforeAnalysisAccess access) {
		access.registerSubtypeReachabilityHandler(this::iterateFields, Object.class);
	}

	// This method is invoked for every type that is reachable.
	/* ===== [OCA 中文解析] =====
方法 iterateFields — 意图与阅读要点

方法 `iterateFields` 复杂度较高（CCN≈13, NLOC≈32）。阅读时建议先抓住主路径，再看分支/异常/缓存等旁路逻辑；关注它在调用链中上下游的契约（入参约束、返回值语义、抛出的异常）。
	===== [OCA 中文解析结束] ===== */
	private void iterateFields(DuringAnalysisAccess access, Class<?> subtype) {
		try {
			for (Field field : subtype.getDeclaredFields()) {
				int modifiers = field.getModifiers();
				if (!Modifier.isStatic(modifiers) || !Modifier.isFinal(modifiers) || field.isEnumConstant() ||
						(field.getType() != boolean.class && field.getType() != Boolean.class)) {
					continue;
				}
				String fieldIdentifier = field.getDeclaringClass().getName() + "#" + field.getName();
				for (Pattern pattern : patterns) {
					if (pattern.matcher(fieldIdentifier).matches()) {
						try {
							Object fieldValue = provideFieldValue(field);
							access.registerFieldValueTransformer(field, (receiver, originalValue) -> fieldValue);
							if (verbose) {
								System.out.println(
										"Field " + fieldIdentifier + " set to " + fieldValue + " at build time");
							}
						}
						catch (Throwable ex) {
							if (verbose) {
								System.out.println("Field " + fieldIdentifier + " will be evaluated at runtime " +
										"due to this error during build time evaluation: " + ex);
							}
						}
					}
				}
			}
		}
		catch (NoClassDefFoundError ex) {
			// Skip classes that have not all their field types in the classpath
		}
	}

	// This method is invoked when the field value is written to the image heap or the field is constant folded.
	private Object provideFieldValue(Field field)
			throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {

		Class<?> throwawayClass = this.throwawayClassLoader.loadClass(field.getDeclaringClass().getName());
		Field throwawayField = throwawayClass.getDeclaredField(field.getName());
		throwawayField.setAccessible(true);
		return throwawayField.get(null);
	}

}
