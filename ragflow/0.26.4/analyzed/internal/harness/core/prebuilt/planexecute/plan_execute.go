// plan_execute.go — Plan-Execute-Replan 模式：Sequential(Planner, Loop(Executor, Replanner)) 分步规划、执行与重规划。
// Package planexecute 实现 Plan-Execute-Replan 智能体模式。
//
// Architecture:
//
//	SequentialAgent(Planner, LoopAgent(Executor, Replanner))
//
// Planner 生成初始分步计划。
// Executor 执行首个未完成步骤。
// Replanner 评估进度，重规划或最终回复。
// The loop repeats until MaxLoopIterations is reached. The respond_tool is configured
// as ReturnDirectly, which causes the replanner sub-agent to return early, but does NOT
// propagate a BreakLoopAction to the outer LoopAgent — loop termination is guaranteed
// only by MaxLoopIterations. For custom termination, provide a RespondTool that emits
// an Exit action or set MaxLoopIterations appropriately.
package planexecute

import (
	"context"
	"encoding/json"
	"fmt"
	"strings"

	"ragflow/internal/harness/core"
	"ragflow/internal/harness/core/schema"
)

// ============================================================
// 会话本地键
// ============================================================

const (
	sessionKeyPlan      = "__planexecute_plan"
	sessionKeyStepsDone = "__planexecute_steps_done"
)

// ============================================================
// Plan 接口与默认实现
// ============================================================

// Plan 结构化分步计划接口。
type Plan interface {
	json.Marshaler
	json.Unmarshaler
	Steps() []string
}

// defaultPlan 默认 Plan 实现。
type defaultPlan struct {
	StepList []string `json:"steps"`
}

func (p *defaultPlan) Steps() []string { return p.StepList }
func (p *defaultPlan) MarshalJSON() ([]byte, error) {
	return json.Marshal(struct {
		Steps []string `json:"steps"`
	}{Steps: p.StepList})
}
func (p *defaultPlan) UnmarshalJSON(data []byte) error {
	var aux struct {
		Steps []string `json:"steps"`
	}
	if err := json.Unmarshal(data, &aux); err != nil {
		return err
	}
	p.StepList = aux.Steps
	return nil
}

// ============================================================
// 配置
// ============================================================

// PlannerConfig 规划器配置。
type PlannerConfig struct {
	Model       core.Model[*schema.Message]
	Instruction string // 覆盖默认 PlannerPrompt
}

// ExecutorConfig 执行器配置。
type ExecutorConfig struct {
	Model       core.Model[*schema.Message]
	Instruction string // overrides default ExecutorPrompt
	Tools       []core.Tool
}

// ReplannerConfig 重规划器配置。
type ReplannerConfig struct {
	Model       core.Model[*schema.Message]
	Instruction string // overrides default ReplannerPrompt
	Tools       []core.Tool
}

// Config PlanExecute 整体配置。
type Config struct {
	Planner           *PlannerConfig
	Executor          *ExecutorConfig
	Replanner         *ReplannerConfig
	Name              string
	MaxLoopIterations int // 默认 10 次
}

// ============================================================
// 工具定义
// ============================================================

const (
	toolPlan    = "plan_tool"
	toolRespond = "respond_tool"
)

// planTool 供规划器输出结构化计划。
// Replanner 也用它更新计划。
var planToolDef = core.NewBaseTool(
	toolPlan,
	`Create or update a step-by-step plan. Args: {"steps":["step1","step2",...]}`,
	func(ctx context.Context, args string) (string, error) {
		var in struct {
			Steps []string `json:"steps"`
		}
		if err := json.Unmarshal([]byte(args), &in); err != nil {
			return "", fmt.Errorf("invalid plan args: %w", err)
		}
		plan := &defaultPlan{StepList: in.Steps}
		if err := core.SetRunLocalValue(ctx, sessionKeyPlan, plan); err != nil {
			return "", err
		}
		// 计划变更时重置已完成步数计数
		if err := core.SetRunLocalValue(ctx, sessionKeyStepsDone, 0); err != nil {
			return "", err
		}
		return fmt.Sprintf("Plan updated with %d steps", len(in.Steps)), nil
	},
)

// respondTool 供 Replanner 发出完成信号。
var respondToolDef = core.NewBaseTool(
	toolRespond,
	`Signal that the task is complete and respond to the user. Args: {"response":"your final answer"}`,
	func(ctx context.Context, args string) (string, error) {
		var in struct {
			Response string `json:"response"`
		}
		if err := json.Unmarshal([]byte(args), &in); err != nil {
			return "", fmt.Errorf("invalid respond args: %w", err)
		}
		return in.Response, nil
	},
)

// ============================================================
// 提示词常量
// ============================================================

const PlannerPrompt = `You are a planner agent. Your job is to create a detailed step-by-step plan to accomplish the user's task.

IMPORTANT RULES:
1. Break the task into clear, actionable steps
2. Each step should be a single, focused action
3. Steps should be in logical order
4. Use the plan_tool to output your plan
5. After creating the plan, transfer to the executor agent

Use the plan_tool with the following JSON format:
{"steps": ["Step 1: ...", "Step 2: ...", ...]}`

const ExecutorPrompt = `You are an executor agent. Execute the first uncompleted step of the plan.

IMPORTANT RULES:
1. The plan and completed steps are available as context
2. Execute ONLY the current step — do not skip ahead
3. Use available tools to accomplish the step
4. When you finish the step, it will be marked as completed
5. After completing the step, transfer to the replanner agent for evaluation

Current objective: {objective}
Current plan: {plan}
Completed steps: {completed_steps}`

const ReplannerPrompt = `You are a replanner agent. Evaluate the progress made and decide whether to continue or respond.

IMPORTANT RULES:
1. Review what was accomplished
2. If more work is needed: use the plan_tool to update the plan, then transfer to the executor
3. If the task is complete: use the respond_tool to provide the final answer
4. Use plan_tool to update the plan when replanning
5. Use respond_tool when the task is done

Available tools:
- plan_tool: Update the plan with new steps (replan)
- respond_tool: Provide the final answer (task complete)

Current objective: {objective}
Current plan: {plan}
Completed steps: {completed_steps}`

// ============================================================
// 子 Agent 名称常量
// ============================================================

const (
	agentNamePlanner   = "planner"
	agentNameExecutor  = "executor"
	agentNameReplanner = "replanner"
	agentNameLoop      = "planexecute_loop"
)

// ============================================================
// New 主构造函数
// ============================================================

// New 创建可恢复 ResumableAgent。
func New(ctx context.Context, cfg *Config) (core.ResumableAgent, error) {
	if cfg == nil {
		cfg = &Config{}
	}
	if cfg.MaxLoopIterations <= 0 {
		cfg.MaxLoopIterations = 10
	}
	if cfg.Name == "" {
		cfg.Name = "plan_execute_agent"
	}

	// 校验各子 Agent 模型配置
	if cfg.Planner == nil || cfg.Planner.Model == nil {
		return nil, fmt.Errorf("planexecute: Planner.Model is required")
	}
	if cfg.Executor == nil || cfg.Executor.Model == nil {
		return nil, fmt.Errorf("planexecute: Executor.Model is required")
	}
	if cfg.Replanner == nil || cfg.Replanner.Model == nil {
		return nil, fmt.Errorf("planexecute: Replanner.Model is required")
	}

	// ---- 创建 Planner ----
	plannerInstruction := cfg.Planner.Instruction
	if plannerInstruction == "" {
		plannerInstruction = PlannerPrompt
	}

	planner := core.NewReActAgent(&core.ReActConfig[*schema.Message]{
		Model:         cfg.Planner.Model,
		Instruction:   plannerInstruction,
		Tools:         []core.Tool{planToolDef},
		MaxIterations: 5,
		GenModelInput: genPlannerInput,
	}).WithName(agentNamePlanner).WithDescription("Generates a step-by-step plan")

	// ---- 创建 Executor ----
	executorInstruction := cfg.Executor.Instruction
	if executorInstruction == "" {
		executorInstruction = ExecutorPrompt
	}

	executorTools := make([]core.Tool, 0, len(cfg.Executor.Tools)+1)
	executorTools = append(executorTools, cfg.Executor.Tools...)

	executor := core.NewReActAgent(&core.ReActConfig[*schema.Message]{
		Model:         cfg.Executor.Model,
		Instruction:   executorInstruction,
		Tools:         executorTools,
		MaxIterations: 15,
		GenModelInput: genExecutorInput,
	}).WithName(agentNameExecutor).WithDescription("Executes the current plan step")

	// ---- 创建 Replanner ----
	replannerInstruction := cfg.Replanner.Instruction
	if replannerInstruction == "" {
		replannerInstruction = ReplannerPrompt
	}

	replannerTools := make([]core.Tool, 0, len(cfg.Replanner.Tools)+2)
	replannerTools = append(replannerTools, cfg.Replanner.Tools...)
	replannerTools = append(replannerTools, planToolDef)
	// respond_tool 标记 ReturnDirectly，使用后 Replanner 直接返回
	returnDirectly := map[string]bool{toolRespond: true}

	replanner := core.NewReActAgent(&core.ReActConfig[*schema.Message]{
		Model:          cfg.Replanner.Model,
		Instruction:    replannerInstruction,
		Tools:          replannerTools,
		ReturnDirectly: returnDirectly,
		MaxIterations:  5,
		GenModelInput:  genReplannerInput,
	}).WithName(agentNameReplanner).WithDescription("Evaluates progress and replans or responds")

	// ---- 组合：Sequential(Planner, Loop(Executor, Replanner)) ----
	// Loop 循环 Executor → Replanner，直至达到 MaxLoopIterations
	loopAgent, err := core.NewLoop(ctx, &core.LoopConfig{
		Name:          agentNameLoop,
		Description:   "Plan-Execute-Replan loop",
		SubAgents:     []core.Agent{executor, replanner},
		MaxIterations: cfg.MaxLoopIterations,
	})
	if err != nil {
		return nil, fmt.Errorf("planexecute: create loop: %w", err)
	}

	// Sequential：Planner → Loop
	seqAgent, err := core.NewSequential(ctx, &core.SequentialConfig{
		Name:        cfg.Name,
		Description: "Plan-Execute-Replan agent",
		SubAgents:   []core.Agent{planner, loopAgent},
	})
	if err != nil {
		return nil, fmt.Errorf("planexecute: create sequential: %w", err)
	}

	return seqAgent, nil
}

// ============================================================
// GenModelInput 输入构建函数
// ============================================================

// genPlannerInput 构建 Planner 模型输入。
func genPlannerInput(ctx context.Context, instruction string, input *core.AgentInput) ([]*schema.Message, error) {
	msgs := make([]*schema.Message, 0, len(input.Messages)+1)
	if instruction != "" {
		msgs = append(msgs, schema.SystemMessage(instruction))
	}
	msgs = append(msgs, input.Messages...)
	return msgs, nil
}

// genContextualInput 将 {objective}/{plan}/{completed_steps} 注入指令。
func genContextualInput(ctx context.Context, instruction string, input *core.AgentInput) ([]*schema.Message, error) {
	planStr := getPlanStr(ctx)
	stepsDone := getStepsDone(ctx)
	objective := getObjective(input.Messages)

	contextStr := strings.NewReplacer(
		"{objective}", objective,
		"{plan}", planStr,
		"{completed_steps}", fmt.Sprintf("%d", stepsDone),
	).Replace(instruction)

	msgs := make([]*schema.Message, 0, len(input.Messages)+1)
	msgs = append(msgs, schema.SystemMessage(contextStr))
	msgs = append(msgs, input.Messages...)
	return msgs, nil
}

// genExecutorInput 委托给 genContextualInput。
func genExecutorInput(ctx context.Context, instruction string, input *core.AgentInput) ([]*schema.Message, error) {
	return genContextualInput(ctx, instruction, input)
}

// genReplannerInput 递增已完成步数后构建输入。
func genReplannerInput(ctx context.Context, instruction string, input *core.AgentInput) ([]*schema.Message, error) {
	// 每次 Replanner 运行表示 Executor 刚完成一步
	// planTool 更新计划时将计数重置为 0
	// 使下一轮计数从零开始。
	currentSteps := getStepsDone(ctx)
	currentSteps++
	_ = core.SetRunLocalValue(ctx, sessionKeyStepsDone, currentSteps)
	return genContextualInput(ctx, instruction, input)
}

// ============================================================
// 辅助函数
// ============================================================

// getPlanStr 从会话读取计划并格式化为编号列表。
func getPlanStr(ctx context.Context) string {
	v, ok, err := core.GetRunLocalValue(ctx, sessionKeyPlan)
	if err != nil || !ok || v == nil {
		return "(no plan yet)"
	}
	p, ok := v.(Plan)
	if !ok {
		return "(plan format error)"
	}
	steps := p.Steps()
	if len(steps) == 0 {
		return "(empty plan)"
	}
	var sb strings.Builder
	for i, s := range steps {
		if i > 0 {
			sb.WriteString("\n")
		}
		sb.WriteString(fmt.Sprintf("%d. %s", i+1, s))
	}
	return sb.String()
}

// getStepsDone 读取已完成步骤数。
func getStepsDone(ctx context.Context) int {
	v, ok, err := core.GetRunLocalValue(ctx, sessionKeyStepsDone)
	if err != nil || !ok {
		return 0
	}
	if n, ok := v.(int); ok {
		return n
	}
	return 0
}

// getObjective 从用户消息提取任务目标。
func getObjective(msgs []*schema.Message) string {
	for _, m := range msgs {
		if m.Role == schema.RoleUser {
			return m.Content
		}
	}
	return ""
}

func init() {
	schema.RegisterName[defaultPlan]("planexecute_default_plan")
}

// respond_tool 的 ReturnDirectly 不会向外层 Loop 传播 BreakLoop；终止依赖 MaxLoopIterations。
