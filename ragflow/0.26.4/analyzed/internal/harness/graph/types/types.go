// types.go — 核心类型：StreamMode、RetryPolicy、Command、Send 与图接口。

// Package types 定义 LangGraph Go 核心类型与图构建接口。
package types

import (
	"context"
	"fmt"
	"math/rand"
	"time"
)

// NodeTriggerMode 控制节点触发条件（any/all 前驱）。
type NodeTriggerMode string

const (
	// NodeTriggerAnyPredecessor 默认 BSP：任一前驱完成即触发，支持环。
	// 适用于循环/超步图。
	NodeTriggerAnyPredecessor NodeTriggerMode = "any"

	// NodeTriggerAllPredecessor DAG 模式：全部前驱完成才触发，不支持环。
	// 适用于 fan-in 汇聚节点。
	// fan-in 场景应使用 all 模式。
	NodeTriggerAllPredecessor NodeTriggerMode = "all"
)

// StreamMode 定义 Stream 输出粒度与内容。
type StreamMode string

const (
	// StreamModeValues 每步输出完整状态（含中断）。
	StreamModeValues StreamMode = "values"
	// StreamModeUpdates 仅输出节点/任务增量更新。
	StreamModeUpdates StreamMode = "updates"
	// StreamModeCustom 节点内 StreamWriter 自定义数据。
	StreamModeCustom StreamMode = "custom"
	// StreamModeMessages LLM token 级流式与元数据。
	StreamModeMessages StreamMode = "messages"
	// StreamModeCheckpoints 检查点创建事件。
	StreamModeCheckpoints StreamMode = "checkpoints"
	// StreamModeTasks 任务起止与结果/错误事件。
	StreamModeTasks StreamMode = "tasks"
	// StreamModeDebug 调试组合（checkpoints + tasks）。
	StreamModeDebug StreamMode = "debug"
)

// Durability 检查点写入耐久模式。
type Durability string

const (
	// DurabilitySync 下一步前同步持久化。
	DurabilitySync Durability = "sync"
	// DurabilityAsync 下一步执行时异步持久化。
	DurabilityAsync Durability = "async"
	// DurabilityExit 仅在图退出时持久化。
	DurabilityExit Durability = "exit"
)

// All 特殊值 "*"，表示所有节点均可中断。
const All = "*"

// RetryPolicy 节点/模型调用重试策略。
type RetryPolicy struct {
	// InitialInterval is the amount of time that must elapse before the first retry occurs.
	InitialInterval time.Duration
	// BackoffFactor is the multiplier by which the interval increases after each retry.
	BackoffFactor float64
	// MaxInterval is the maximum amount of time that may elapse between retries.
	MaxInterval time.Duration
	// MaxAttempts is the maximum number of attempts to make before giving up, including the first.
	MaxAttempts int
	// Jitter indicates whether to add random jitter to the interval between retries.
	Jitter bool
	// RetryOn is a function that returns true for exceptions that should trigger a retry.
	RetryOn func(error) bool
}

// DefaultRetryPolicy 默认 3 次、指数退避与 jitter。
func DefaultRetryPolicy() RetryPolicy {
	return RetryPolicy{
		InitialInterval: 500 * time.Millisecond,
		BackoffFactor:   2.0,
		MaxInterval:     128 * time.Second,
		MaxAttempts:     3,
		Jitter:          true,
		RetryOn:         DefaultRetryOn,
	}
}

// CalculateBackoff 计算第 attempt 次退避时长（共享 Pregel 与 agent 重试）。
// (1-indexed). It applies the BackoffFactor, caps at MaxInterval, and optionally adds jitter.
// This is the shared backoff calculation used by both Pregel graph-node retries and
// agent-level model-call retries.
func (p *RetryPolicy) CalculateBackoff(attempt int) time.Duration {
	backoff := time.Duration(float64(p.InitialInterval) * powFloat(p.BackoffFactor, attempt-1))
	if backoff > p.MaxInterval {
		backoff = p.MaxInterval
	}
	if p.Jitter {
		// Subtract up to 50% to spread retry bursts.
		jitter := time.Duration(float64(backoff) * 0.5 * randFloat())
		backoff = backoff - jitter
		if backoff < 0 {
			backoff = 0
		}
	}
	return backoff
}

// powFloat computes base^exp for float base (small int exponents).
func powFloat(base float64, exp int) float64 {
	result := 1.0
	for i := 0; i < exp; i++ {
		result *= base
	}
	return result
}

// randFloat returns a random float in [0,1).
func randFloat() float64 {
	return rand.Float64()
}

// DefaultRetryOn is the default retry condition function.
func DefaultRetryOn(err error) bool {
	return true
}

// CachePolicy 节点结果缓存策略。
type CachePolicy struct {
	// KeyFunc generates a cache key from the node's input.
	KeyFunc func(context.Context, interface{}) string
	// TTL is the time to live for the cache entry in seconds.
	// If nil, the entry never expires.
	TTL *time.Duration
}

// DefaultCacheKey generates a default cache key.
func DefaultCacheKey(ctx context.Context, input interface{}) string {
	return fmt.Sprintf("%v", input)
}

// Interrupt 节点中断信息（value + id）。
type Interrupt struct {
	// Value is the value associated with the interrupt.
	Value interface{}
	// ID is the ID of the interrupt. Can be used to resume the interrupt directly.
	ID string
}

// NewInterrupt creates a new Interrupt with the given value and ID.
func NewInterrupt(value interface{}, id string) *Interrupt {
	return &Interrupt{
		Value: value,
		ID:    id,
	}
}

// StateUpdate 手动状态更新描述。
type StateUpdate struct {
	Values map[string]interface{}
	AsNode string
	TaskID string
}

// PregelTask Pregel 任务快照。
type PregelTask struct {
	ID         string
	Name       string
	Path       []interface{}
	Error      error
	Interrupts []*Interrupt
	State      interface{} // RunnableConfig or StateSnapshot
	Result     interface{}
}

// CacheKey 任务缓存键（NS + Key + TTL）。
type CacheKey struct {
	// Namespace for the cache entry.
	NS []string
	// Key for the cache entry.
	Key string
	// TTL is the time to live for the cache entry in seconds.
	TTL *time.Duration
}

// PregelExecutableTask 可执行 Pregel 任务（含 Writers/Subgraphs）。
type PregelExecutableTask struct {
	Name        string
	Input       interface{}
	Proc        interface{} // Runnable
	Writes      [][2]interface{}
	Config      map[string]interface{}
	Triggers    []string
	RetryPolicy []RetryPolicy
	CacheKey    *CacheKey
	ID          string
	Path        []interface{}
	Writers     []interface{}
	Subgraphs   []interface{} // PregelProtocol
}

// StateSnapshot is a snapshot of the state of the graph at the beginning of a step.
type StateSnapshot struct {
	// Values are the current values of channels.
	Values interface{}
	// Next is the name of the node to execute in each task for this step.
	Next []string
	// Config used to fetch this snapshot.
	Config map[string]interface{}
	// Metadata associated with this snapshot.
	Metadata interface{}
	// CreatedAt is the timestamp of snapshot creation.
	CreatedAt string
	// ParentConfig is the config used to fetch the parent snapshot, if any.
	ParentConfig map[string]interface{}
	// Tasks to execute in this step. If already attempted, may contain an error.
	Tasks []*PregelTask
	// Interrupts that occurred in this step that are pending resolution.
	Interrupts []*Interrupt
}

// Send represents a message or packet to send to a specific node in the graph.
type Send struct {
	// Node is the name of the target node to send the message to.
	Node string
	// Arg is the state or message to send to the target node.
	Arg interface{}
}

// NewSend creates a new Send instance.
func NewSend(node string, arg interface{}) *Send {
	return &Send{
		Node: node,
		Arg:  arg,
	}
}

// Command represents one or more commands to update the graph's state and send messages to nodes.
type Command struct {
	// Graph is the graph to send the command to.
	// Supported values are:
	//   - nil/empty: the current graph
	//   - "__parent__": closest parent graph
	Graph string
	// Update to apply to the graph's state.
	Update interface{}
	// Resume value to resume execution with.
	Resume interface{}
	// Goto can be:
	//   - Name of the node to navigate to next
	//   - Sequence of node names to navigate to next
	//   - Send object
	//   - Sequence of Send objects
	Goto interface{}
}

// Parent is the constant for the parent graph.
const Parent = "__parent__"

// NewCommand creates a new Command.
func NewCommand() *Command {
	return &Command{}
}

// WithGraph sets the graph for the command.
func (c *Command) WithGraph(graph string) *Command {
	c.Graph = graph
	return c
}

// WithUpdate sets the update for the command.
func (c *Command) WithUpdate(update interface{}) *Command {
	c.Update = update
	return c
}

// WithResume sets the resume value for the command.
func (c *Command) WithResume(resume interface{}) *Command {
	c.Resume = resume
	return c
}

// WithGoto sets the goto value for the command.
func (c *Command) WithGoto(gotoVal interface{}) *Command {
	c.Goto = gotoVal
	return c
}

// UpdateAsTuples converts the update to tuples.
func (c *Command) UpdateAsTuples() [][2]interface{} {
	if c.Update == nil {
		return nil
	}

	switch v := c.Update.(type) {
	case map[string]interface{}:
		result := make([][2]interface{}, 0, len(v))
		for key, val := range v {
			result = append(result, [2]interface{}{key, val})
		}
		return result
	default:
		return [][2]interface{}{{"__root__", v}}
	}
}

// StreamWriter is a function that accepts a single argument and writes it to the output stream.
type StreamWriter func(interface{})

// Checkpointer represents the type of checkpointer to use for a subgraph.
type Checkpointer interface {
	// IsCheckpointer marks this as a checkpointer type.
	IsCheckpointer()
}

// CheckpointerBool is a boolean checkpointer type.
type CheckpointerBool bool

func (c CheckpointerBool) IsCheckpointer() {}

// RunnableConfig is defined in config.go

// Overwrite wraps a value to bypass a reducer and write directly to a channel.
type Overwrite struct {
	Value interface{}
}

// NewOverwrite creates a new Overwrite.
func NewOverwrite(value interface{}) *Overwrite {
	return &Overwrite{Value: value}
}

// ReducerFunc reduces multiple values into one.
type ReducerFunc func(current, update interface{}) interface{}

// NodeFunc is the signature of a node function.
type NodeFunc func(context.Context, interface{}) (interface{}, error)

// EdgeFunc is the signature of an edge/condition function.
type EdgeFunc func(context.Context, interface{}) (interface{}, error)

// ============================================================
// Graph type definitions (shared by graph/graph and pregel)
// ============================================================

// Node represents a node in a StateGraph.
type Node struct {
	Name         string
	Function     NodeFunc
	Triggers     []string
	Writes       []string
	RetryPolicy  *RetryPolicy
	Tags         []string
	Metadata     map[string]interface{}
	FieldMapping []FieldMapping
}

// Edge is a directed connection between two nodes.
type Edge struct {
	From string
	To   string
}

// FieldMapping specifies how a field from a source node's output is mapped
// to a target node's input.
type FieldMapping struct {
	From string
	To   string
}

// DataEdge is a directed data-flow connection with field-level mapping.
type DataEdge struct {
	From    string
	To      string
	Mapping []FieldMapping
}

// ConditionalEdge routes to different nodes based on a condition.
type ConditionalEdge struct {
	From      string
	Condition EdgeFunc
	Mapping   map[string]string
}

// Branch provides a higher-level conditional edge.
type Branch struct {
	From      string
	Condition EdgeFunc
	Then      func(interface{}) []string
}

// NodeOptions contains options for adding a node.
type NodeOptions struct {
	RetryPolicy  *RetryPolicy
	Tags         []string
	Metadata     map[string]interface{}
	Triggers     []string
	Writes       []string
	FieldMapping []FieldMapping
	StatePre     NodeFunc
	StatePost    NodeFunc
}

// StateGraph is the interface for graph building and inspection.
type StateGraph interface {
	AddNode(name string, fn NodeFunc) *Node
	AddNodeWithOptions(name string, fn NodeFunc, opts NodeOptions) *Node
	AddEdge(from, to string) error
	AddConditionalEdges(from string, condition EdgeFunc, mapping map[string]string) error
	AddBranch(from string, condition EdgeFunc, then func(interface{}) []string) error
	AddDataEdge(from, to string, mappings ...FieldMapping) error
	AddChannel(name string, channel interface{}) // channel must be channels.Channel
	SetReducer(channelName string, reducer ReducerFunc)
	AddChannelWithReducer(name string, channel interface{}, reducer ReducerFunc)
	SetEntryPoint(node string) error
	SetFinishPoint(node string) error
	WithInputSchema(schema interface{}) StateGraph
	WithOutputSchema(schema interface{}) StateGraph
	SetNodeTriggerMode(mode NodeTriggerMode)
	Compile(opts ...interface{}) (CompiledGraph, error)
	Validate() error

	GetChannels() map[string]interface{}
	GetEntryPoint() string
	GetNode(name string) (*Node, bool)
	GetEdges() []*Edge
	GetConditionalEdges() []*ConditionalEdge
	GetBranches() []*Branch
	GetNodes() map[string]*Node
	GetNodeTriggerMode() NodeTriggerMode
	GetDataEdges() []*DataEdge

	// GetStateSchema returns the raw state schema (struct, map, etc.)
	GetStateSchema() interface{}
}

// CompiledGraph is the interface for executing a compiled graph.
type CompiledGraph interface {
	Invoke(ctx context.Context, input interface{}, config ...*RunnableConfig) (interface{}, error)
	Stream(ctx context.Context, input interface{}, mode StreamMode, config ...*RunnableConfig) (<-chan interface{}, <-chan error)
	GetGraph() StateGraph
	GetCheckpointer() interface{} // cast to checkpoint.BaseCheckpointer
	GetInterrupts() map[string]bool
	GetInterruptsAfter() map[string]bool
	GetRecursionLimit() int
	IsDebug() bool
}

// PregelRunFunc is the pluggable pregel execution function.
// Set by pregel.init() via SetPregelRunFunc.
var PregelRunFunc func(ctx context.Context, cg CompiledGraph, input interface{}, config *RunnableConfig, streamMode StreamMode) (interface{}, error)

// SetPregelRunFunc sets the pregel execution function for compiled graphs.
func SetPregelRunFunc(fn func(ctx context.Context, cg CompiledGraph, input interface{}, config *RunnableConfig, streamMode StreamMode) (interface{}, error)) {
	PregelRunFunc = fn
}

// StateGraph/CompiledGraph 接口定义图构建与 Invoke/Stream 契约。
