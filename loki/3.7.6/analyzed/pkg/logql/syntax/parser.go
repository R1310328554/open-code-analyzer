package syntax

// parser 封装 goyacc 生成的 syntaxParserImpl，提供 ParseExpr 等公开入口，并在解析后执行 matcher 校验、样本表达式检查与查询长度限制。

import (
	"errors"
	"fmt"
	"strings"
	"sync"

	"github.com/prometheus/prometheus/model/labels"
	promql_parser "github.com/prometheus/prometheus/promql/parser"

	"github.com/grafana/loki/v3/pkg/logqlmodel"
	"github.com/grafana/loki/v3/pkg/util"
)

const (
	EmptyMatchers = "{}"

	errAtleastOneEqualityMatcherRequired = "queries require at least one regexp or equality matcher that does not have an empty-compatible value. For instance, app=~\".*\" does not meet this requirement, but app=~\".+\" will"

	// Prometheus internal data structure panics if given more than this.
	maxStreamLabelsSize = 1<<24 - 1 // 16MB
)

// parserPool 复用 parser 实例（含 lexer 与 yacc 实现），降低热路径分配。
var parserPool = sync.Pool{
	New: func() interface{} {
		p := &parser{
			p:      &syntaxParserImpl{},
			Reader: strings.NewReader(""),
			lexer:  &lexer{},
		}
		return p
	},
}

// (E.Welch) We originally added this limit from fuzz testing and realizing there should be some maximum limit to an allowed query size.
// The original limit was 5120 based on some internet searching and a best estimate of what a reasonable limit would be.
// We have seen use cases with queries containing a lot of filter expressions or long expanded variable names where this limit was too small.
// Apparently the spec does not specify a limit, and more internet searching suggests almost all browsers will handle 100k+ length urls without issue
// Some limit here still seems prudent however, so the new limit is now 128k.
// Also note this is used to allocate the buffer for reading the query string, so there is some memory cost to making this larger.
// maxInputSize 限制查询字符串最大 128KiB，防止 fuzz 与异常大查询耗尽内存。
const maxInputSize = 131072

func init() {
	// Improve the error messages coming out of yacc.
	syntaxErrorVerbose = true
	// uncomment when you need to understand yacc rule tree.
	// exprDebug = 3
	for str, tok := range tokens {
		syntaxToknames[tok-syntaxPrivate+1] = str
	}
}

// parser 组合 yacc 实现、lexer 与 strings.Reader，Parse 后 expr 字段持有 AST 根。
type parser struct {
	p *syntaxParserImpl
	*lexer
	expr Expr
	*strings.Reader
}

func (p *parser) Parse() (Expr, error) {
	p.errs = p.errs[:0]
	p.Scanner.Error = func(_ *Scanner, msg string) {
		p.Error(msg)
	}
	e := p.p.Parse(p)
	if e != 0 || len(p.errs) > 0 {
		return nil, p.errs[0]
	}
	return p.expr, nil
}

// ParseExpr 解析并 validateExpr，确保 matcher 与表达式类型合法。
// ParseExpr parses a string and returns an Expr.
func ParseExpr(input string) (Expr, error) {
	expr, err := ParseExprWithoutValidation(input)
	if err != nil {
		return nil, err
	}
	if err := validateExpr(expr); err != nil {
		return nil, err
	}
	return expr, nil
}

// ParseExprWithoutValidation 跳过语义校验，供内部重写或仅语法分析场景使用。
func ParseExprWithoutValidation(input string) (expr Expr, err error) {
	if len(input) >= maxInputSize {
		return nil, logqlmodel.NewParseError(fmt.Sprintf("input size too long (%d > %d)", len(input), maxInputSize), 0, 0)
	}

	defer func() {
		if r := recover(); r != nil {
			var ok bool
			if err, ok = r.(error); ok {
				if errors.Is(err, logqlmodel.ErrParse) {
					return
				}
				err = logqlmodel.NewParseError(err.Error(), 0, 0)
			}
		}
	}()

	p := parserPool.Get().(*parser)
	defer parserPool.Put(p)

	p.Reset(input)
	p.Init(p.Reader)
	return p.Parse()
}

func MustParseExpr(input string) Expr {
	expr, err := ParseExpr(input)
	if err != nil {
		panic(err)
	}
	return expr
}

func validateExpr(expr Expr) error {
	switch e := expr.(type) {
	case SampleExpr:
		return validateSampleExpr(e)
	case LogSelectorExpr:
		return validateLogSelectorExpression(e)
	case VariantsExpr:
		return validateVariantsExpr(e)
	default:
		return logqlmodel.NewParseError(fmt.Sprintf("unexpected expression type: %v", e), 0, 0)
	}
}

func validateVariantsExpr(e VariantsExpr) error {
	err := validateLogSelectorExpression(e.LogRange().Left)
	if err != nil {
		return err
	}

	for _, variant := range e.Variants() {
		err = validateSampleExpr(variant)
		if err != nil {
			return err
		}
	}

	return nil
}

// validateMatchers 要求至少一个非空兼容的等值/正则 matcher，避免全量扫描。
// validateMatchers checks whether a query would touch all the streams in the query range or uses at least one matcher to select specific streams.
func validateMatchers(matchers []*labels.Matcher) error {
	_, matchers = util.SplitFiltersAndMatchers(matchers)
	if len(matchers) == 0 {
		return logqlmodel.NewParseError(errAtleastOneEqualityMatcherRequired, 0, 0)
	}
	return nil
}

// ParseMatchers 仅接受纯 matcher 表达式 `{app="foo"}`，否则返回 ErrParseMatchers。
// ParseMatchers parses a string and returns labels matchers, if the expression contains
// anything else it will return an error.
func ParseMatchers(input string, validate bool) ([]*labels.Matcher, error) {
	var (
		expr Expr
		err  error
	)

	if validate {
		expr, err = ParseExpr(input)
	} else {
		expr, err = ParseExprWithoutValidation(input)
	}

	if err != nil {
		return nil, err
	}
	matcherExpr, ok := expr.(*MatchersExpr)
	if !ok {
		return nil, logqlmodel.ErrParseMatchers
	}
	return matcherExpr.Mts, nil
}

func MatchersString(xs []*labels.Matcher) string {
	return newMatcherExpr(xs).String()
}

// ParseSampleExpr 解析并断言根节点实现 SampleExpr 接口。
// ParseSampleExpr parses a string and returns the sampleExpr
func ParseSampleExpr(input string) (SampleExpr, error) {
	expr, err := ParseExpr(input)
	if err != nil {
		return nil, err
	}
	sampleExpr, ok := expr.(SampleExpr)
	if !ok {
		return nil, errors.New("only sample expression supported")
	}

	return sampleExpr, nil
}

func validateSampleExpr(expr SampleExpr) error {
	switch e := expr.(type) {
	case *BinOpExpr:
		if e.err != nil {
			return e.err
		}
		if err := validateSampleExpr(e.SampleExpr); err != nil {
			return err
		}
		return validateSampleExpr(e.RHS)
	case *LiteralExpr:
		if e.err != nil {
			return e.err
		}
		return nil
	case *VectorExpr:
		if e.err != nil {
			return e.err
		}
		return nil
	case *VectorAggregationExpr:
		if e.err != nil {
			return e.err
		}
		if e.Operation == OpTypeSort || e.Operation == OpTypeSortDesc {
			if err := validateSortGrouping(e.Grouping); err != nil {
				return err
			}
		}
		return validateSampleExpr(e.Left)
	case *LabelReplaceExpr:
		if e.err != nil {
			return e.err
		}
		return validateSampleExpr(e.Left)
	default:
		selector, err := e.Selector()
		if err != nil {
			return err
		}
		return validateLogSelectorExpression(selector)
	}
}

func validateLogSelectorExpression(expr LogSelectorExpr) error {
	switch e := expr.(type) {
	case *VectorExpr:
		return nil
	default:
		return validateMatchers(e.Matchers())
	}
}

// validateSortGrouping prevent by|without groupings on sort operations.
// This will keep compatibility with promql and allowing sort by (foo) doesn't make much sense anyway when sort orders by value instead of labels.
func validateSortGrouping(grouping *Grouping) error {
	if grouping != nil && len(grouping.Groups) > 0 {
		return logqlmodel.NewParseError("sort and sort_desc doesn't allow grouping by ", 0, 0)
	}
	return nil
}

// ParseLogSelector 解析日志选择器；validate 为 true 时执行完整 validateExpr。
// ParseLogSelector parses a log selector expression `{app="foo"} |= "filter"`
func ParseLogSelector(input string, validate bool) (LogSelectorExpr, error) {
	expr, err := ParseExprWithoutValidation(input)
	if err != nil {
		return nil, err
	}
	logSelector, ok := expr.(LogSelectorExpr)
	if !ok {
		return nil, errors.New("only log selector is supported")
	}
	if validate {
		if err := validateExpr(expr); err != nil {
			return nil, err
		}
	}
	return logSelector, nil
}

// ParseLabels 用 PromQL metric 解析器读取标签串，并 WithoutEmpty 规范化空值标签。
// ParseLabels parses labels from a string using logql parser.
func ParseLabels(lbs string) (labels.Labels, error) {
	if len(lbs) > maxStreamLabelsSize {
		return labels.EmptyLabels(), fmt.Errorf("labels size %d MiB exceeds limit of %d", len(lbs)>>20, maxStreamLabelsSize>>20)
	}
	ls, err := promql_parser.NewParser(promql_parser.Options{}).ParseMetric(lbs)
	if err != nil {
		return labels.EmptyLabels(), err
	}

	// Empty label values are equivalent to absent labels
	// in Prometheus, but they unfortunately alter the
	// Hash values created. This can cause problems in Loki
	// if we can't rely on a set of labels to have a deterministic
	// hash value.
	// Therefore we must normalize early in the write path.
	// See https://github.com/grafana/loki/pull/7355
	// for more information
	return ls.WithoutEmpty(), nil
}
// ParseExprWithoutValidation 用 recover 捕获 yacc panic 并转为 ParseError。
