/**
 * Spring AOP 核心接口，构建于 AOP 联盟 AOP 互操作接口之上。
 * <p>A任何AOP联盟MethodInterceptor都可以在Spring中使用。
 * <br>Spring AOP 还提供： <ul> <li>Introduction 支持 <li>A 切入点抽象，支持“静态”切入点（基于类和方法）和“动态”切入点（也考虑方法
 * 参数）。目前没有用于切入点的 AOP 联盟接口。 <li>A全系列的建议类型，包括周围、之前、返回后和抛出建议。 <li>Extensibility 允许插入任意自定义建议类型
 * ，而无需修改核心框架。 OCAJAVA6文档
 * <p>Spring AOP 可以以编程方式使用或（最好）与 Spring IoC 容器集成。
 */
@NullMarked
package org.springframework.aop;

import org.jspecify.annotations.NullMarked;
