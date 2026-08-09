/**
 * 用于 ApplicationContext 中的 Bean 后处理器，通过自动创建 AOP 代理来简化 AOP 使用，而无需使用 ProxyFactoryBean。
 * <p> 此包中的各种后处理器只需添加到 ApplicationContext（通常在 XML bean 定义文档中）即可自动代理选定的 bean。
 * <p><b>NB</b>：BeanFactory 实现不支持自动代理，因为后处理器 Bean 仅在应用程序上下文中自动检测。后处理器可以在 ConfigurableBeanFa
 * ctory 上显式注册。
 */
@NullMarked
package org.springframework.aop.framework.autoproxy;

import org.jspecify.annotations.NullMarked;
