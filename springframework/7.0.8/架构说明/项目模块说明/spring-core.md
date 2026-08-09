# spring-core

> 核心工具与底层抽象

## 模块职责

整个框架的地基：资源抽象（Resource）、类型转换、反射/MethodHandle 工具、编解码、环境属性占位符、任务调度基础等。几乎所有模块都依赖它。

## 关键包

- `org.springframework.core`
- `org.springframework.core.io`
- `org.springframework.core.env`
- `org.springframework.core.convert`
- `org.springframework.util`

## 弯弯绕绕（建议精读）

- GenericTypeResolver / ResolvableType：泛型擦除下的类型解析弯弯绕绕最多
- SimpleAliasRegistry / ConcurrentReferenceHashMap：别名与缓存并发细节
- SpringProperties / NativeDetector：运行时能力探测与特性开关

## 规模

- 路径: `spring-core`
- 文件数: 1157
- 代码文件数: 1120
- 语言分布: gradle=1, java=1085, properties=4, kotlin=34, xml=14
