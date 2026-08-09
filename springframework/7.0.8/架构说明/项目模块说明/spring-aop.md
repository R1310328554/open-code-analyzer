# spring-aop

> 面向切面编程

## 模块职责

Proxy / Advisor / Advice / Pointcut 抽象，以及 JDK/CGLIB 代理基础设施。

## 关键包

- `org.springframework.aop`
- `org.springframework.aop.framework`
- `org.springframework.aop.support`

## 弯弯绕绕（建议精读）

- JdkDynamicAopProxy vs CglibAopProxy 选择与拦截链
- ExposeInvocationInterceptor 与自调用（self-invocation）失效问题

## 规模

- 路径: `spring-aop`
- 文件数: 307
- 代码文件数: 287
- 语言分布: gradle=1, java=283, xml=13, kotlin=3
