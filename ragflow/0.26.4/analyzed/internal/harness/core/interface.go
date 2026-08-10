package core

// interface.go — agentcore 公共类型与 Agent 接口：MessageType 约束、事件/动作、RunStep 序列化与事件构造辅助。


import (
	"bytes"
	"context"
	"encoding/gob"
	"io"

	"ragflow/internal/harness/core/schema"
)

func init() {
	gob.Register(&RunStep{})
}

// MessageType 密封类型约束，限定 *Message 或 *AgenticMessage。
type MessageType interface {
	*schema.Message | *schema.AgenticMessage
}

// ===== 类型别名 =====
type Message = *schema.Message
type MessageStream = *schema.StreamReader[Message]
type AgenticMessage = *schema.AgenticMessage
type AgenticMessageStream = *schema.StreamReader[AgenticMessage]

// ===== Agent 动作 =====

// TransferToAgentAction 转交目标 Agent 名称
type TransferToAgentAction struct {
	DestAgentName string
}

// NewTransferToAgentAction 构造转交动作
func NewTransferToAgentAction(dest string) *AgentAction {
	return &AgentAction{TransferToAgent: &TransferToAgentAction{DestAgentName: dest}}
}

// NewExitAction 构造退出动作
func NewExitAction() *AgentAction {
	return &AgentAction{Exit: true}
}

type BreakLoopAction struct {
	From              string
	Done              bool
	CurrentIterations int
}

// NewBreakLoopAction 构造跳出循环动作
func NewBreakLoopAction(agentName string) *AgentAction {
	return &AgentAction{BreakLoop: &BreakLoopAction{From: agentName}}
}

type AgentAction struct {
	Exit                bool
	Interrupted         *InterruptInfo
	TransferToAgent     *TransferToAgentAction
	BreakLoop           *BreakLoopAction
	CustomizedAction    any
	internalInterrupted *InterruptSignal
}

// ===== 运行步骤 =====

type RunStep struct {
	agentName string
}

func NewRunStep(agentName string) *RunStep { return &RunStep{agentName: agentName} }
func (r *RunStep) String() string          { return r.agentName }
func (r *RunStep) Equals(r1 RunStep) bool  { return r.agentName == r1.agentName }

// GobEncode 实现 gob 编码，供检查点序列化 RunStep。
func (r *RunStep) GobEncode() ([]byte, error) {
	var buf bytes.Buffer
	enc := gob.NewEncoder(&buf)
	if err := enc.Encode(r.agentName); err != nil {
		return nil, err
	}
	return buf.Bytes(), nil
}

// GobDecode 实现 gob 解码，恢复 RunStep。
func (r *RunStep) GobDecode(data []byte) error {
	buf := bytes.NewBuffer(data)
	dec := gob.NewDecoder(buf)
	return dec.Decode(&r.agentName)
}

// ===== 事件 =====

type TypedMessageVariant[M MessageType] struct {
	IsStreaming   bool
	Message       M
	MessageStream *schema.StreamReader[M]
	Role          schema.RoleType
	AgenticRole   schema.AgenticRoleType
	ToolName      string
}

// GetMessage 流式时拼接 MessageStream，否则返回 Message
func (mv *TypedMessageVariant[M]) GetMessage() (M, error) {
	if mv.IsStreaming {
		return concatMessageStream(mv.MessageStream)
	}
	return mv.Message, nil
}

type MessageVariant = TypedMessageVariant[*schema.Message]

type TypedAgentOutput[M MessageType] struct {
	MessageOutput    *TypedMessageVariant[M]
	CustomizedOutput any
}

type AgentOutput = TypedAgentOutput[*schema.Message]

type TypedAgentEvent[M MessageType] struct {
	AgentName string
	RunPath   []RunStep
	Output    *TypedAgentOutput[M]
	Action    *AgentAction
	Err       error
}

type AgentEvent = TypedAgentEvent[*schema.Message]

type TypedAgentInput[M MessageType] struct {
	Messages        []M
	EnableStreaming bool
}

type AgentInput = TypedAgentInput[*schema.Message]

// ===== Agent 接口 =====

type TypedAgent[M MessageType] interface {
	Name(ctx context.Context) string
	Description(ctx context.Context) string
	Run(ctx context.Context, input *TypedAgentInput[M], opts ...RunOption) *AsyncIterator[*TypedAgentEvent[M]]
}

type Agent = TypedAgent[*schema.Message]

type TypedResumableAgent[M MessageType] interface {
	TypedAgent[M]
	Resume(ctx context.Context, info *ResumeInfo, opts ...RunOption) *AsyncIterator[*TypedAgentEvent[M]]
}

type ResumableAgent = TypedResumableAgent[*schema.Message]

// ===== 事件构造 =====

// EventFromMessage 从 Message 构造 AgentEvent
func EventFromMessage(msg Message, msgStream MessageStream, role schema.RoleType, toolName string) *AgentEvent {
	return typedEventFromMessage(msg, msgStream, role, toolName)
}

func typedEventFromMessage[M MessageType](msg M, msgStream *schema.StreamReader[M], role schema.RoleType, toolName string) *TypedAgentEvent[M] {
	return &TypedAgentEvent[M]{
		Output: &TypedAgentOutput[M]{
			MessageOutput: &TypedMessageVariant[M]{
				IsStreaming: msgStream != nil, Message: msg, MessageStream: msgStream,
				Role: role, ToolName: toolName,
			},
		},
	}
}

func typedModelOutputEvent[M MessageType](msg M, msgStream *schema.StreamReader[M]) *TypedAgentEvent[M] {
	var role schema.RoleType
	var agenticRole schema.AgenticRoleType
	var zero M
	if _, ok := any(zero).(*schema.Message); ok {
		role = schema.RoleAssistant
	} else {
		agenticRole = schema.AgenticRoleAssistant
	}
	event := typedEventFromMessage(msg, msgStream, role, "")
	event.Output.MessageOutput.AgenticRole = agenticRole
	return event
}

// EventFromAgenticMessage 从 AgenticMessage 构造事件
func EventFromAgenticMessage(msg AgenticMessage, msgStream AgenticMessageStream, agenticRole schema.AgenticRoleType) *TypedAgentEvent[*schema.AgenticMessage] {
	return &TypedAgentEvent[*schema.AgenticMessage]{
		Output: &TypedAgentOutput[*schema.AgenticMessage]{
			MessageOutput: &TypedMessageVariant[*schema.AgenticMessage]{
				IsStreaming: msgStream != nil, Message: msg, MessageStream: msgStream,
				AgenticRole: agenticRole,
			},
		},
	}
}

// ===== 工具函数 =====

func isNilMessage[M MessageType](msg M) bool {
	var zero M
	return any(msg) == any(zero)
}

// concatMessageStream 将 Message 或 AgenticMessage 流拼接为单条
func concatMessageStream[M MessageType](stream *schema.StreamReader[M]) (M, error) {
	var zero M
	switch s := any(stream).(type) {
	case *schema.StreamReader[*schema.Message]:
		result, err := schema.ConcatMessageStream(s)
		if err != nil {
			return zero, err
		}
		return any(result).(M), nil
	case *schema.StreamReader[*schema.AgenticMessage]:
		defer s.Close()
		var msgs []*schema.AgenticMessage
		for {
			frame, err := s.Recv()
			if err == io.EOF {
				break
			}
			if err != nil {
				return zero, err
			}
			msgs = append(msgs, frame)
		}
		result, err := schema.ConcatAgenticMessages(msgs)
		if err != nil {
			return zero, err
		}
		return any(result).(M), nil
	default:
		panic("unreachable: unknown MessageType")
	}
}

// typedModelOption is a model option with a function.
type typedModelOption[M MessageType] struct {
	f func(o *modelOptions[M])
}

func (o *typedModelOption[M]) applyModel() {}

// modelOptions holds all model call options.
type modelOptions[M MessageType] struct {
	RetryConfig *TypedModelRetryConfig[M]
}

func init() {
	schema.RegisterType("agentcore_run_step", func() any { return &RunStep{} })
	schema.RegisterType("agentcore_event", func() any { return &TypedAgentEvent[*schema.Message]{} })
}

// init 注册 RunStep 与 AgentEvent 的 schema/gob 类型名。
