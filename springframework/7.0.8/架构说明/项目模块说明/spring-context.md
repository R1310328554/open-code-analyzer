# spring-context

> 应用上下文与企业级特性

## 模块职责

ApplicationContext 在 BeanFactory 之上增加：国际化、事件、资源模式解析、注解配置（@Configuration/@ComponentScan）、Environment、JMX 等。

## 关键包

- `org.springframework.context`
- `org.springframework.context.annotation`
- `org.springframework.context.support`
- `org.springframework.context.event`

## 弯弯绕绕（建议精读）

- ConfigurationClassPostProcessor：@Configuration 的解析/增强/导入
- AnnotationConfigApplicationContext 刷新流程 refresh()
- 事件广播与 @EventListener 适配

## 规模

- 路径: `spring-context`
- 文件数: 1619
- 代码文件数: 1283
- 语言分布: gradle=1, kotlin=26, java=1244, xml=278, properties=26, groovy=12
