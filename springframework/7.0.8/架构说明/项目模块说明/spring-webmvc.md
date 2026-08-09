# spring-webmvc

> Servlet 体系 Spring MVC

## 模块职责

DispatcherServlet 前端控制器模型：映射、适配、视图解析、异常处理。

## 关键包

- `org.springframework.web.servlet`

## 弯弯绕绕（建议精读）

- DispatcherServlet#doDispatch 主流程
- RequestMappingHandlerMapping 条件组合（path/method/params/headers/consumes/produces）

## 规模

- 路径: `spring-webmvc`
- 文件数: 731
- 代码文件数: 605
- 语言分布: gradle=1, properties=15, kotlin=8, java=589, xml=49, ruby=1, python=1, javascript=5
