# 多项目中文化总进度

> 逐文件精读改 `analyzed/`。版权头保留英文。清单：`config/project_queue.yaml`

## 加速策略（已启用）

- Spring Framework：8 并行 agent × 10 文件/波
- 小仓并行 bootstrap + 精读：gson / HikariCP / disruptor / flask
- spring-boot 后台 bootstrap 中
- 超大仓（jdk/k8s/pytorch/ES）稍后分模块

## 快照

| 项目 | 版本 | done | pending | 状态 |
| --- | --- | ---: | ---: | --- |
| springframework | 7.0.8 | 478 | 907 | 进行中（高并行） |
| gson | gson-parent-2.14.0 | 0 | 73 | 首波精读中 |
| hikaricp | dev-a4d93f4f8551 | 0 | 24 | 首波精读中 |
| disruptor | 4.0.0 | 0 | 151 | 核心 src/main 精读中 |
| flask | 3.1.3 | 0 | 7 | 首波精读中 |
| spring-boot | - | - | - | bootstrap 中 |
| 其余 24 个项目 | - | - | - | 排队（见 project_queue.yaml） |
