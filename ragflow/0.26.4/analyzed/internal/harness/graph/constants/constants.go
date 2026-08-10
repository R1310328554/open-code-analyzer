// constants.go — Harness 图引擎保留键、配置键与 Pregel 命名约定。

// Package constants 定义 LangGraph Go 全局常量。
package constants

// 保留写入键 — 节点/引擎内部通信
const (
	// Input 图输入值
	Input = "__input__"
	// Interrupt 节点触发的动态中断
	Interrupt = "__interrupt__"
	// Resume 中断恢复传入值
	Resume = "__resume__"
	// Error 节点错误
	Error = "__error__"
	// NoWrites 标记节点无写入
	NoWrites = "__no_writes__"
	// Tasks 节点/边返回的 Send 任务
	Tasks = "__pregel_tasks"
	// Return 记录任务返回值
	Return = "__return__"
	// Previous 处理节点 Control 值的隐式分支
	Previous = "__previous__"
)

// 保留缓存命名空间
const (
	// CacheNSWrites 节点写入缓存命名空间
	CacheNSWrites = "__pregel_ns_writes"
)

// RunnableConfig.Configurable 保留键
const (
	// ConfigKeySend Pregel 写入函数（状态/边/保留键）
	ConfigKeySend = "__pregel_send"
	// ConfigKeyRead 返回当前状态副本的读取函数
	ConfigKeyRead = "__pregel_read"
	// ConfigKeyCall 子图/函数调用入口
	ConfigKeyCall = "__pregel_call"
	// ConfigKeyCheckpointer 父图传递给子图的 checkpointer
	ConfigKeyCheckpointer = "__pregel_checkpointer"
	// ConfigKeyStream 流式协议对象
	ConfigKeyStream = "__pregel_stream"
	// ConfigKeyCache 子图可用缓存
	ConfigKeyCache = "__pregel_cache"
	// ConfigKeyResuming 子图是否从先前检查点恢复
	ConfigKeyResuming = "__pregel_resuming"
	// ConfigKeyTaskID 当前任务 ID
	ConfigKeyTaskID = "__pregel_task_id"
	// ConfigKeyThreadID 执行线程 ID
	ConfigKeyThreadID = "thread_id"
	// ConfigKeyCheckpointMap 父图 checkpoint_ns → checkpoint_id 映射
	ConfigKeyCheckpointMap = "checkpoint_map"
	// ConfigKeyCheckpointID 当前检查点 ID
	ConfigKeyCheckpointID = "checkpoint_id"
	// ConfigKeyCheckpointNS 检查点命名空间（根图为空）
	ConfigKeyCheckpointNS = "checkpoint_ns"
	// ConfigKeyNodeFinished 节点完成回调
	ConfigKeyNodeFinished = "__pregel_node_finished"
	// ConfigKeyScratchpad 当前任务临时 scratchpad
	ConfigKeyScratchpad = "__pregel_scratchpad"
	// ConfigKeyRunnerSubmit runner 任务提交函数
	ConfigKeyRunnerSubmit = "__pregel_runner_submit"
	// ConfigKeyDurability 持久化/耐久模式
	ConfigKeyDurability = "__pregel_durability"
	// ConfigKeyRuntime 运行时上下文（store、stream 等）
	ConfigKeyRuntime = "__pregel_runtime"
	// ConfigKeyResumeMap 任务命名空间 → 恢复值映射
	ConfigKeyResumeMap = "__pregel_resume_map"
)

// 其他 Pregel/检查点常量
const (
	// Push Send 触发的 push 式任务
	Push = "__pregel_push"
	// Pull 边触发的 pull 式任务
	Pull = "__pregel_pull"
	// NSSep 检查点命名空间层级分隔符
	NSSep = "|"
	// NSEnd 命名空间与 task_id 分隔符
	NSEnd = ":"
	// Conf RunnableConfig 中 configurable 键名
	Conf = "configurable"
	// NullTaskID 非任务关联写入使用的占位 task_id
	NullTaskID = "00000000-0000-0000-0000-000000000000"
	// Overwrite 覆盖写入标记键
	Overwrite = "__overwrite__"
	// DefaultCheckpointMaxVersions 每线程默认保留检查点版本数
	DefaultCheckpointMaxVersions = 100
	// DefaultCheckpointListLimit 列出检查点默认分页大小
	DefaultCheckpointListLimit = 10
	// DefaultRecursionLimit Pregel 默认最大超步数
	DefaultRecursionLimit = 50
)

// 公开常量 — 图节点与追踪标签
const (
	// TagNoStream 禁用流式输出标签
	TagNoStream = "nostream"
	// TagHidden 在追踪/流式环境中隐藏节点
	TagHidden = "langsmith:hidden"
	// End 图式 Pregel 终止虚拟节点
	End = "__end__"
	// Start 图式 Pregel 起始虚拟节点
	Start = "__start__"
)

// Reserved 所有保留键的快速查找表
var Reserved = map[string]bool{
	TagHidden:              true,
	Input:                  true,
	Interrupt:              true,
	Resume:                 true,
	Error:                  true,
	NoWrites:               true,
	ConfigKeySend:          true,
	ConfigKeyRead:          true,
	ConfigKeyCall:          true,
	ConfigKeyCheckpointer:  true,
	ConfigKeyStream:        true,
	ConfigKeyCache:         true,
	ConfigKeyCheckpointMap: true,
	ConfigKeyResuming:      true,
	ConfigKeyTaskID:        true,
	ConfigKeyCheckpointID:  true,
	ConfigKeyCheckpointNS:  true,
	ConfigKeyNodeFinished:  true,
	ConfigKeyScratchpad:    true,
	ConfigKeyRunnerSubmit:  true,
	ConfigKeyDurability:    true,
	ConfigKeyRuntime:       true,
	ConfigKeyResumeMap:     true,
	Push:                   true,
	Pull:                   true,
	NSSep:                  true,
	NSEnd:                  true,
	Conf:                   true,
}

// IsReserved 判断键是否为引擎保留键
func IsReserved(key string) bool {
	return Reserved[key]
}

// 用户自定义状态键不得与 Reserved 冲突，否则行为未定义。
