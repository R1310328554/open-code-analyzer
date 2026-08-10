// Package evals 提供 agentcore 智能体评测框架。
//
// Modeled after deepagents/libs/evals, it enables running real-LLM agent
// evaluations with trajectory scoring, success assertions, and report generation.
//
// Core concepts:
//
//   - EvalCase: a single test case (query, expected behavior, scorers)
//   - TrajectoryScorer: builder for soft expectations + hard success checks
//   - RunEval: executes the agent against a case, captures trajectory
//   - EvalReport: aggregates multiple case results into a summary
//
// Usage in go test:
//
//	func TestMyAgent(t *testing.T) {
//	    evals.RunT(t, &evals.EvalConfig{
//	        Model: myModel,
//	        Agent: myAgent,
//	        Cases: []evals.EvalCase{
//	            {
//	                Name:  "write_hello_py",
//	                Query: "Create a hello.py file",
//	                Scorers: []evals.Scorer{
//	                    evals.FinalTextContains("Hello, World!"),
//	                    evals.FileContentEquals("hello.py", `print("Hello, World!")`),
//	                },
//	            },
//	        },
//	    })
//	}
package evals

// evals.go — agentcore 智能体评测框架：真实 LLM 运行、轨迹采集、断言打分与报告生成。

// evals.go — agentcore 智能体评测框架：真实 LLM 运行、轨迹采集、断言打分与报告生成。

import (
	"context"
	"fmt"
	"os"
	"path/filepath"
	"strings"
	"sync"
	"time"

	"ragflow/internal/harness/core"
	"ragflow/internal/harness/core/schema"
)

// ========================================================================
// Core types
// ========================================================================

// EvalCase 定义单个评测用例（查询、Agent、Scorers）。
type EvalCase struct {
	// Name identifies this case in reports.
	Name string

	// Query is the user input to the agent.
	Query string

	// Agent overrides cfg.Agent for this specific case.
	// When nil, cfg.Agent is used (or a default agent created from cfg.Model).
	Agent core.Agent

	// Scorers are checked against the agent's output.
	// All scorers must pass for the case to succeed.
	Scorers []Scorer

	// Tags for categorising results.
	Tags []string

	// WorkDir is the temporary working directory for this case.
	// When set, file-related scorers use this as the base.
	WorkDir string
}

// Scorer 对智能体输出做断言，失败返回 error。
// Returns nil on success, or an error describing the failure.
type Scorer func(ctx context.Context, result *EvalResult) error

// EvalResult 记录单次运行的消息轨迹、事件与耗时。
type EvalResult struct {
	Case     EvalCase
	Messages []*schema.Message // full conversation trajectory
	Events   []*core.AgentEvent
	Duration time.Duration
	Err      error             // agent execution error (if any)
	Snapshot map[string]string // file snapshots after execution
}

// CaseReport 单用例评测结果摘要。
type CaseReport struct {
	CaseName string
	Passed   bool
	Duration time.Duration
	Failures []Failure
	Tags     []string
}

// Failure 描述一条断言失败详情。
type Failure struct {
	ScorerName string
	Message    string
}

// EvalReport 聚合多用例通过率与总耗时。
type EvalReport struct {
	Cases    []CaseReport
	Total    int
	Passed   int
	Failed   int
	Duration time.Duration
}

// EvalConfig 配置模型、Agent、并发、超时与报告目录。
type EvalConfig struct {
	// Model is the chat model for the agent. Required.
	Model core.Model[*schema.Message]

	// Agent is the agent to evaluate. If nil, a default ReAct agent is created.
	Agent core.Agent

	// Cases to evaluate.
	Cases []EvalCase

	// MaxConcurrency limits parallel case execution. 0 = unlimited.
	MaxConcurrency int

	// Timeout per case. 0 = no timeout.
	Timeout time.Duration

	// ReportDir is where JSON/HTML reports are written.
	// When empty, no files are written.
	ReportDir string

	// LLMJudgeModel is used for LLM-as-judge scorers.
	// When nil, LLMJudge scorers fall back to string matching.
	LLMJudgeModel core.Model[*schema.Message]
}

// ========================================================================
// RunT — single-function entry point for go test
// ========================================================================

// RunT 在 go test 中运行全部用例并输出 PASS/FAIL。
// It's the primary entry point for use in go test functions.
//
//go:generate echo "RunT is designed for use with go test"
func RunT(t testingT, cfg *EvalConfig) {
	if cfg == nil {
		t.Fatal("evals: EvalConfig is nil")
		return
	}

	report := Run(context.Background(), cfg)

	for _, c := range report.Cases {
		if !c.Passed {
			t.Errorf("[FAIL] %s (%v):", c.CaseName, c.Duration)
			for _, f := range c.Failures {
				t.Errorf("  %s: %s", f.ScorerName, f.Message)
			}
		} else {
			t.Logf("[PASS] %s (%v)", c.CaseName, c.Duration)
		}
	}

	t.Logf("Eval summary: %d/%d passed (%v)", report.Passed, report.Total, report.Duration)

	if cfg.ReportDir != "" {
		if err := writeReport(cfg.ReportDir, report); err != nil {
			t.Logf("evals: write report: %v", err)
		}
	}
}

// testingT 测试入口所需的最小 testing.T 子集。
type testingT interface {
	Fatal(args ...any)
	Errorf(format string, args ...any)
	Logf(format string, args ...any)
}

// ========================================================================
// Run — execute all eval cases
// ========================================================================

// Run 执行全部用例并返回 EvalReport。
func Run(ctx context.Context, cfg *EvalConfig) *EvalReport {
	start := time.Now()
	report := &EvalReport{}

	if cfg.MaxConcurrency <= 1 {
		// Sequential execution.
		for _, c := range cfg.Cases {
			cr := runCase(ctx, cfg, c)
			report.Cases = append(report.Cases, cr)
			if cr.Passed {
				report.Passed++
			} else {
				report.Failed++
			}
		}
	} else {
		// Parallel execution with bounded concurrency.
		var mu sync.Mutex
		var wg sync.WaitGroup
		sem := make(chan struct{}, cfg.MaxConcurrency)

		for _, c := range cfg.Cases {
			wg.Add(1)
			sem <- struct{}{}
			go func(case_ EvalCase) {
				defer wg.Done()
				defer func() { <-sem }()
				cr := runCase(ctx, cfg, case_)
				mu.Lock()
				report.Cases = append(report.Cases, cr)
				if cr.Passed {
					report.Passed++
				} else {
					report.Failed++
				}
				mu.Unlock()
			}(c)
		}
		wg.Wait()
	}

	report.Total = report.Passed + report.Failed
	report.Duration = time.Since(start)
	return report
}

func runCase(ctx context.Context, cfg *EvalConfig, c EvalCase) CaseReport {
	cr := CaseReport{CaseName: c.Name, Tags: c.Tags}
	start := time.Now()

	// Use case-specific agent, or shared agent, or create a default.
	agent := c.Agent
	if agent == nil {
		agent = cfg.Agent
	}
	if agent == nil {
		agent = core.NewReActAgent(&core.ReActConfig[*schema.Message]{
			Model: cfg.Model,
		}).WithName("eval_agent")
	}

	// Run the agent.
	runner := core.NewTypedRunner(core.RunnerConfig[*schema.Message]{Agent: agent})
	iter := runner.Run(ctx, []*schema.Message{schema.UserMessage(c.Query)})

	result := &EvalResult{
		Case:     c,
		Snapshot: takeSnapshot(c.WorkDir),
	}

	for {
		ev, ok := iter.Next()
		if !ok {
			break
		}
		result.Events = append(result.Events, ev)
		if ev.Err != nil {
			result.Err = ev.Err
			break
		}
		if ev.Output != nil && ev.Output.MessageOutput != nil && !ev.Output.MessageOutput.IsStreaming && ev.Output.MessageOutput.Message != nil {
			result.Messages = append(result.Messages, ev.Output.MessageOutput.Message)
		}
	}

	result.Duration = time.Since(start)
	cr.Duration = result.Duration

	// Run scorers.
	for _, scorer := range c.Scorers {
		if err := scorer(ctx, result); err != nil {
			cr.Failures = append(cr.Failures, Failure{
				ScorerName: scorerName(scorer),
				Message:    err.Error(),
			})
		}
	}

	cr.Passed = len(cr.Failures) == 0
	return cr
}

// ========================================================================
// Built-in Scorers
// ========================================================================

// FinalTextContains 断言最终助手输出包含指定子串。
// contains the given substring (case-insensitive).
func FinalTextContains(substr string) Scorer {
	return func(ctx context.Context, r *EvalResult) error {
		text := lastAssistantText(r)
		if text == "" {
			return fmt.Errorf("final text is empty, expected to contain %q", substr)
		}
		if !containsFold(text, substr) {
			return fmt.Errorf("final text does not contain %q:\n%s", substr, truncate(text, 500))
		}
		return nil
	}
}

// FinalTextExcludes 断言最终输出不包含禁止子串。
// does NOT contain the given substring.
func FinalTextExcludes(substr string) Scorer {
	return func(ctx context.Context, r *EvalResult) error {
		text := lastAssistantText(r)
		if containsFold(text, substr) {
			return fmt.Errorf("final text contains forbidden %q:\n%s", substr, truncate(text, 500))
		}
		return nil
	}
}

// AgentError 断言智能体运行无错误。
func AgentError() Scorer {
	return func(ctx context.Context, r *EvalResult) error {
		if r.Err != nil {
			return fmt.Errorf("agent error: %w", r.Err)
		}
		return nil
	}
}

// AgentErrorContains 断言智能体错误信息包含指定文本。
// the given substring.
func AgentErrorContains(substr string) Scorer {
	return func(ctx context.Context, r *EvalResult) error {
		if r.Err == nil {
			return fmt.Errorf("expected agent error containing %q, got none", substr)
		}
		if !containsFold(r.Err.Error(), substr) {
			return fmt.Errorf("agent error %q does not contain %q", r.Err.Error(), substr)
		}
		return nil
	}
}

// FileContentEquals 断言文件内容与期望值完全一致。
func FileContentEquals(path, expectedContent string) Scorer {
	return func(ctx context.Context, r *EvalResult) error {
		data, err := os.ReadFile(path)
		if err != nil {
			return fmt.Errorf("read %s: %w", path, err)
		}
		got := string(data)
		if got != expectedContent {
			return fmt.Errorf("file %s content mismatch:\nexpected:\n%s\n\ngot:\n%s", path, expectedContent, truncate(got, 500))
		}
		return nil
	}
}

// FileContains 断言文件内容包含子串。
func FileContains(path, substr string) Scorer {
	return func(ctx context.Context, r *EvalResult) error {
		data, err := os.ReadFile(path)
		if err != nil {
			return fmt.Errorf("read %s: %w", path, err)
		}
		if !containsFold(string(data), substr) {
			return fmt.Errorf("file %s does not contain %q", path, substr)
		}
		return nil
	}
}

// ToolCalled 断言智能体调用了指定工具。
func ToolCalled(toolName string) Scorer {
	return func(ctx context.Context, r *EvalResult) error {
		for _, msg := range r.Messages {
			for _, tc := range msg.ToolCalls {
				if tc.Function.Name == toolName {
					return nil
				}
			}
		}
		return fmt.Errorf("agent did not call tool %q", toolName)
	}
}

// Steps 断言助手消息步数不少于 minSteps。
func Steps(minSteps int) Scorer {
	return func(ctx context.Context, r *EvalResult) error {
		steps := countAssistantMessages(r.Messages)
		if steps < minSteps {
			return fmt.Errorf("expected at least %d agent steps, got %d", minSteps, steps)
		}
		return nil
	}
}

// LLMJudge 使用裁判 LLM 按自然语言标准评判输出。
// output against the given criteria.
//
// The judge model receives a structured prompt with the original query, the
// agent's final output, and the evaluation instruction. It must respond with
// PASS or FAIL.
//
// When judgeModel is nil, the scorer returns an error asking to configure one.
func LLMJudge(judgeModel core.Model[*schema.Message], instruction string) Scorer {
	return func(ctx context.Context, r *EvalResult) error {
		text := lastAssistantText(r)
		if text == "" {
			return fmt.Errorf("no assistant output to judge")
		}
		if judgeModel == nil {
			return fmt.Errorf("LLM judge model is nil. Set evals.LLMJudgeModel or pass a model.\nInstruction: %s", instruction)
		}
		return judgeOutput(ctx, judgeModel, r.Case.Query, text, instruction)
	}
}

// judgeOutput 向裁判模型发送结构化提示并解析 PASS/FAIL。
func judgeOutput(ctx context.Context, model core.Model[*schema.Message], query, output, instruction string) error {
	prompt := fmt.Sprintf(judgePromptTemplate, query, output, instruction)
	judgeMsgs := []*schema.Message{
		schema.UserMessage(prompt),
	}

	result, err := model.Generate(ctx, judgeMsgs)
	if err != nil {
		return fmt.Errorf("LLM judge error: %w", err)
	}
	if result == nil {
		return fmt.Errorf("LLM judge returned nil response")
	}

	response := strings.TrimSpace(result.Content)
	if strings.HasPrefix(response, "PASS") {
		return nil
	}
	return fmt.Errorf("LLM judge: %s", response)
}

const judgePromptTemplate = `You are evaluating an AI coding assistant's output.

### User Query
%s

### Assistant Output
%s

### Evaluation Criteria
%s

Determine if the assistant's output satisfies the criteria.

Reply with EXACTLY one line:
- If the criteria are met: PASS
- If the criteria are NOT met: FAIL: <reason>`

// ========================================================================
// Helpers
// ========================================================================

// lastAssistantText 取轨迹中最后一条 assistant 消息文本
func lastAssistantText(r *EvalResult) string {
	for i := len(r.Messages) - 1; i >= 0; i-- {
		if r.Messages[i].Role == schema.RoleAssistant {
			return r.Messages[i].Content
		}
	}
	return ""
}

func countAssistantMessages(msgs []*schema.Message) int {
	count := 0
	for _, m := range msgs {
		if m.Role == schema.RoleAssistant {
			count++
		}
	}
	return count
}

func containsFold(s, substr string) bool {
	s, substr = strings.ToLower(s), strings.ToLower(substr)
	return strings.Contains(s, substr)
}

func truncate(s string, max int) string {
	if len(s) <= max {
		return s
	}
	return s[:max] + "..."
}

func scorerName(s Scorer) string {
	return fmt.Sprintf("%T", s)
}

func takeSnapshot(workDir string) map[string]string {
	if workDir == "" {
		return nil
	}
	snap := make(map[string]string)
	filepath.Walk(workDir, func(path string, info os.FileInfo, err error) error {
		if err != nil || info.IsDir() {
			return nil
		}
		data, _ := os.ReadFile(path)
		snap[path] = string(data)
		return nil
	})
	return snap
}

// writeReport 写入 summary.json 与 results.csv
func writeReport(dir string, report *EvalReport) error {
	os.MkdirAll(dir, 0755)

	// Write summary JSON.
	summary := fmt.Sprintf(`{"total":%d,"passed":%d,"failed":%d,"duration":"%s"}`,
		report.Total, report.Passed, report.Failed, report.Duration)
	if err := os.WriteFile(filepath.Join(dir, "summary.json"), []byte(summary), 0644); err != nil {
		return err
	}

	// Write per-case results.
	var buf strings.Builder
	buf.WriteString("Case,Duration,Passed,Failures\n")
	for _, c := range report.Cases {
		failures := ""
		if len(c.Failures) > 0 {
			var msgs []string
			for _, f := range c.Failures {
				msgs = append(msgs, f.ScorerName+": "+f.Message)
			}
			failures = strings.Join(msgs, "; ")
		}
		buf.WriteString(fmt.Sprintf("%s,%v,%v,%s\n", c.CaseName, c.Duration, c.Passed, failures))
	}
	return os.WriteFile(filepath.Join(dir, "results.csv"), []byte(buf.String()), 0644)
}

// 支持顺序/并发执行；未指定 Agent 时自动创建默认 ReActAgent。
