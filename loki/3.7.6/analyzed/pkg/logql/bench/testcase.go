package bench

// testcase 表示一条已解析的 LogQL 基准用例：查询串、时间窗口、方向、步长及 YAML 来源等元数据。

import (
	"fmt"
	"slices"
	"strings"
	"time"

	"github.com/grafana/loki/v3/pkg/logproto"
	"github.com/grafana/loki/v3/pkg/logql/syntax"
)

const (
	kindLog    = "log"
	kindMetric = "metric"
)

// TestCase represents a LogQL test case for benchmarking and testing
// TestCase 由 QueryRegistry.ExpandQuery 生成，供 bench/k6 运行器消费。
type TestCase struct {
	Query     string
	Start     time.Time
	End       time.Time
	Direction logproto.Direction
	Step      time.Duration // Step size for metric queries
	Source    string        // Source location (suite/file.yaml:line)
	QueryDesc string        // Query description from YAML
	Tags      []string
}

// Equal returns true if two TestCases represent the same query execution.
// Equal 比较查询、时间与方向等字段，忽略 QueryDesc 等展示性元数据。
func (c TestCase) Equal(other TestCase) bool {
	return c.Query == other.Query &&
		c.Start.Equal(other.Start) &&
		c.End.Equal(other.End) &&
		c.Step == other.Step &&
		c.Direction == other.Direction &&
		slices.Equal(c.Tags, other.Tags)
}

// Name returns a descriptive name for the test case.
// For log queries, it includes the direction.
// For metric queries (rate, sum), it returns the query with step size.
// Name 为日志类查询附加方向后缀，metric 类仅返回查询表达式本身。
func (c TestCase) Name() string {
	expr, err := syntax.ParseExpr(c.Query)
	if err != nil {
		return fmt.Sprintf("%s [%v]", c.Query, c.Direction)
	}
	if _, ok := expr.(syntax.SampleExpr); ok {
		return c.Query
	}
	return fmt.Sprintf("%s [%v]", c.Query, c.Direction)
}

// Kind returns the kind of the test case based on the query type.
// Kind 通过语法树判断为 log 或 metric，解析失败返回 invalid。
func (c TestCase) Kind() string {
	expr, err := syntax.ParseExpr(c.Query)
	if err != nil {
		return "invalid"
	}
	if _, ok := expr.(syntax.SampleExpr); ok {
		return kindMetric
	}
	return kindLog
}

// Description returns a detailed description of the test case including time range
func (c TestCase) Description() string {
	var b strings.Builder
	if c.Source != "" {
		fmt.Fprintf(&b, "Source: %s\n", c.Source)
	}
	fmt.Fprintf(&b, "Query: %s\n", c.Query)
	fmt.Fprintf(&b, "Time Range: %v to %v\n", c.Start.Format(time.RFC3339), c.End.Format(time.RFC3339))
	if c.Step > 0 {
		fmt.Fprintf(&b, "Step: %v\n", c.Step)
	}
	fmt.Fprintf(&b, "Direction: %v", c.Direction)
	return b.String()
}
// Description 格式化输出 Source、时间范围、Step 与 Direction 供调试与报告。
