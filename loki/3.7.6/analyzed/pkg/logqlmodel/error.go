package logqlmodel

// error 定义 LogQL 引擎可识别的哨兵错误与 ParseError/PipelineError/LimitError，支持 errors.Is 分类处理。

import (
	"errors"
	"fmt"

	"github.com/prometheus/prometheus/model/labels"
)

// ErrParse/ErrPipeline/ErrLimit 等哨兵错误供引擎与 API 层区分失败原因。
// Those errors are useful for comparing error returned by the engine.
// e.g. errors.Is(err,logqlmodel.ErrParse) let you know if this is a ast parsing error.
var (
	ErrParse                            = errors.New("failed to parse the log query")
	ErrPipeline                         = errors.New("failed execute pipeline")
	ErrLimit                            = errors.New("limit reached while evaluating the query")
	ErrIntervalLimit                    = errors.New("[interval] value exceeds limit")
	ErrBlocked                          = errors.New("query blocked by policy")
	ErrParseMatchers                    = errors.New("only label matchers are supported")
	ErrUnsupportedSyntaxForInstantQuery = errors.New(
		"log queries are not supported as an instant query type, please change your query to a range query type",
	)
	ErrVariantsDisabled = errors.New(
		"multi variant queries are disabled for this instance",
	)
	ErrorLabel         = "__error__"
	PreserveErrorLabel = "__preserve_error__"
	ErrorDetailsLabel  = "__error_details__"
)

// ParseError 携带行列号，Error() 格式化为人可读的 parse error 消息。
// ParseError is what is returned when we failed to parse.
type ParseError struct {
	msg       string
	line, col int
}

func (p ParseError) Error() string {
	if p.col == 0 && p.line == 0 {
		return fmt.Sprintf("parse error : %s", p.msg)
	}
	return fmt.Sprintf("parse error at line %d, col %d: %s", p.line, p.col, p.msg)
}

// ParseError.Is 使 errors.Is(err, ErrParse) 可识别语法解析失败。
// Is allows to use errors.Is(err,ErrParse) on this error.
func (p ParseError) Is(target error) bool {
	return target == ErrParse
}

func NewParseError(msg string, line, col int) ParseError {
	return ParseError{
		msg:  msg,
		line: line,
		col:  col,
	}
}

func NewStageError(expr string, err error) ParseError {
	return ParseError{
		msg:  fmt.Sprintf(`stage '%s' : %s`, expr, err),
		line: 0,
		col:  0,
	}
}

// PipelineError 表示某条序列 pipeline 执行失败，Error() 提示用 __error__ 标签过滤。
type PipelineError struct {
	metric    labels.Labels
	errorType string
}

func NewPipelineErr(metric labels.Labels) *PipelineError {
	return &PipelineError{
		metric:    metric,
		errorType: metric.Get(ErrorLabel),
	}
}

func (e PipelineError) Error() string {
	return fmt.Sprintf(
		"pipeline error: '%s' for series: '%s'.\n"+
			"Use a label filter to intentionally skip this error. (e.g | __error__!=\"%s\").\n"+
			"To skip all potential errors you can match empty errors.(e.g __error__=\"\")\n"+
			"The label filter can also be specified after unwrap. (e.g | unwrap latency | __error__=\"\" )\n",
		e.errorType, e.metric, e.errorType)
}

// Is allows to use errors.Is(err,ErrPipeline) on this error.
func (e PipelineError) Is(target error) bool {
	return target == ErrPipeline
}

// LimitError 封装序列数或区间超限等限制类错误，Is 匹配 ErrLimit。
type LimitError struct {
	error
}

func NewSeriesLimitError(limit int) *LimitError {
	return &LimitError{
		error: fmt.Errorf("maximum number of series (%d) reached for a single query; consider reducing query cardinality by adding more specific stream selectors, reducing the time range, or aggregating results with functions like sum(), count() or topk()", limit),
	}
}

// Is allows to use errors.Is(err,ErrLimit) on this error.
func (e LimitError) Is(target error) bool {
	return target == ErrLimit
}
// ErrorLabel/__error_details__ 等常量定义 Loki 在指标流上附加的错误标签键名。
