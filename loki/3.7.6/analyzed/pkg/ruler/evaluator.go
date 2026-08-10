package ruler

// evaluator 定义 ruler 规则求值接口与 local/remote 模式配置，remote 模式经 query-frontend 执行 LogQL。

import (
	"context"
	"flag"
	"fmt"
	"strings"
	"time"

	"github.com/grafana/loki/v3/pkg/logqlmodel"
)

// Evaluator.Eval 接收 LogQL 表达式与评估时间，返回 logqlmodel.Result。
// Evaluator is the interface that must be satisfied in order to accept rule evaluations from the Ruler.
type Evaluator interface {
	// Eval evaluates the given rule and returns the result.
	Eval(ctx context.Context, qs string, now time.Time) (*logqlmodel.Result, error)
}

// EvaluationConfig 含 mode（local/remote）、max_jitter 与 QueryFrontend 子配置。
type EvaluationConfig struct {
	Mode      string        `yaml:"mode,omitempty"`
	MaxJitter time.Duration `yaml:"max_jitter"`

	QueryFrontend QueryFrontendConfig `yaml:"query_frontend,omitempty"`
}

// RegisterFlags 注册 ruler.evaluation.* 标志，max_jitter 用于打散并发规则评估。
func (c *EvaluationConfig) RegisterFlags(f *flag.FlagSet) {
	f.StringVar(&c.Mode, "ruler.evaluation.mode", EvalModeLocal, "The evaluation mode for the ruler. Can be either 'local' or 'remote'. If set to 'local', the ruler will evaluate rules locally. If set to 'remote', the ruler will evaluate rules remotely. If unset, the ruler will evaluate rules locally.")
	f.DurationVar(&c.MaxJitter, "ruler.evaluation.max-jitter", 0, "Upper bound of random duration to wait before rule evaluation to avoid contention during concurrent execution of rules. Jitter is calculated consistently for a given rule. Set 0 to disable (default).")
	c.QueryFrontend.RegisterFlags(f)
}

// Validate 拒绝除 local 与 remote 以外的 evaluation mode 值。
func (c *EvaluationConfig) Validate() error {
	if c.Mode != EvalModeLocal && c.Mode != EvalModeRemote {
		return fmt.Errorf("invalid evaluation mode: %s. Acceptable modes are: %s", c.Mode, strings.Join([]string{EvalModeLocal, EvalModeRemote}, ", "))
	}

	return nil
}
// EvalModeLocal 在本进程执行 LogQL；EvalModeRemote 将求值委托 query-frontend 集群。
