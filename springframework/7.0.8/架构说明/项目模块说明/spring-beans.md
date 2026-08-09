# spring-beans

> IoC 容器（BeanFactory）

## 模块职责

Bean 定义、作用域、依赖注入、FactoryBean、属性编辑器等。DefaultListableBeanFactory 是经典 IoC 实现，理解 Spring 必读。

## 关键包

- `org.springframework.beans`
- `org.springframework.beans.factory`
- `org.springframework.beans.factory.support`
- `org.springframework.beans.factory.config`
- `org.springframework.beans.factory.xml`

## 弯弯绕绕（建议精读）

- doGetBean / createBean / populateBean / initializeBean：完整生命周期
- 依赖解析中的循环依赖三级缓存（singletonFactories / earlySingletonObjects）
- AutowireCandidateResolver 与 @Autowired/@Qualifier 的候选筛选

## 规模

- 路径: `spring-beans`
- 文件数: 687
- 代码文件数: 594
- 语言分布: gradle=1, kotlin=14, java=579, xml=68, properties=11
