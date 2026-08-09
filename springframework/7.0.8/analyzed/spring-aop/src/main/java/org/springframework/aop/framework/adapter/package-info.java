/**
 * SPI 包允许 Spring AOP 框架处理任意通知类型。
 * <p> 如果用户只想使用 <i> 使用 </i> Spring AOP 框架，而不是扩展其功能，则无需关心此包。
 * <p>您可能希望使用这些适配器来包装 Spring 特定的建议，例如 MethodInterceptor 中的 MethodBeforeAdvice，以允许它们在支持 AOP
 * 联盟接口的另一个 AOP 框架中使用。
 * <p> 这些适配器不依赖于任何其他 Spring 框架类来允许此类使用。
 */
@NullMarked
package org.springframework.aop.framework.adapter;

import org.jspecify.annotations.NullMarked;
