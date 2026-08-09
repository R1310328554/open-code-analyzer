# 多项目中文化总进度

> 方法：逐类/逐文件精读 → 改 `analyzed/` 中文注释。版权/法律声明头保留英文。  
> 队列：`config/project_queue.yaml`  
> 引导：`./scripts/bootstrap_project.sh <alias|url> [modules]`

## 当前状态

| # | 项目 | 状态 | 备注 |
| ---: | --- | --- | --- |
| 1 | spring-projects/spring-framework | **进行中** | 7.0.8；beans ✅；context/tx/aop/jdbc 继续 |
| 2 | spring-projects/spring-boot | 排队 | SF 收尾后 / 可并行 fetch |
| 3 | apache/rocketmq | 排队 | |
| 4 | redisson/redisson | 排队 | |
| 5 | openjdk/jdk | 排队 | 超大仓，分模块 |
| 6 | google/gson | **bootstrap 中** | 小仓优先验证流水线 |
| 7 | alibaba/Sentinel | 排队 | |
| 8 | brettwooldridge/HikariCP | **bootstrap 中** | |
| 9 | LMAX-Exchange/disruptor | **bootstrap 中** | |
| 10 | huggingface/transformers | 排队 | Python |
| 11 | django/django | 排队 | Python |
| 12 | pallets/flask | **bootstrap 中** | Python 小仓 |
| 13 | sqlalchemy/sqlalchemy | 排队 | |
| 14 | pytorch/pytorch | 排队 | 超大仓 |
| 15 | fastapi/fastapi | 排队 | |
| 16 | PaddlePaddle/PaddleOCR | 排队 | |
| 17 | elastic/elasticsearch | 排队 | 超大仓 |
| 18 | ReactiveX/RxJava | 排队 | |
| 19 | alibaba/arthas | 排队 | |
| 20 | netty/netty | 排队 | |
| 21 | alibaba/nacos | 排队 | |
| 22 | keycloak/keycloak | 排队 | |
| 23 | gin-gonic/gin | 排队 | Go |
| 24 | harness/harness | 排队 | Go |
| 25 | ollama/ollama | 排队 | Go |
| 26 | kubernetes/kubernetes | 排队 | 超大仓 |
| 27 | golang/go | 排队 | 超大仓 |
| 28 | infiniflow/ragflow | 排队 | |
| 29 | prometheus/prometheus | 排队 | Go |
| 30 | grafana/loki | 排队 | Go |

## 加速策略

1. **高并行**：每波 8 个 agent × 10 文件，SF 与小仓 bootstrap 重叠  
2. **小仓先跑通**：gson / HikariCP / disruptor / flask 验证多语言队列  
3. **超大仓分模块**：jdk / k8s / pytorch / ES 只先扫核心路径  
4. **队列锁**：`batch.json` 由父编排维护，避免子 agent 回写冲掉领取状态  

## Spring Framework 快照

见 `springframework/7.0.8/架构说明/中文化进度.md`。
