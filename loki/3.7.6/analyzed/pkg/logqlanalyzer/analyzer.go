package logqlanalyzer

// logqlanalyzer 逐阶段回放 LogQL pipeline：解析选择器、构建 PipelineAnalyzer，记录每 stage 处理前后行内容与标签。

import (
	"fmt"
	"time"

	"github.com/pkg/errors"
	"github.com/prometheus/prometheus/model/labels"
	"github.com/prometheus/prometheus/promql/parser"

	"github.com/grafana/loki/v3/pkg/logql/log"
	"github.com/grafana/loki/v3/pkg/logql/syntax"
)

// logQLAnalyzer 无状态分析器，analyze 对多条日志行批量执行 pipeline 追踪。
type logQLAnalyzer struct {
}

func (a logQLAnalyzer) analyze(query string, logs []string) (*Result, error) {
	expr, err := syntax.ParseLogSelector(query, true)
	if err != nil {
		return nil, errors.Wrap(err, "invalid query")
	}
	streamSelector, stages, err := a.extractExpressionParts(expr)
	if err != nil {
		return nil, errors.Wrap(err, "can not extract parts of expression")
	}
	pipeline, err := expr.Pipeline()
	if err != nil {
		return nil, errors.Wrap(err, "can not create pipeline")
	}
	streamLabels, err := parser.NewParser(parser.Options{}).ParseMetric(streamSelector)
	if err != nil {
		return nil, errors.Wrap(err, "can not parse labels from stream selector")
	}
	analyzer := NewPipelineAnalyzer(pipeline, streamLabels)
	response := &Result{StreamSelector: streamSelector, Stages: stages, Results: make([]LineResult, 0, len(logs))}
	for _, line := range logs {
		analysisRecords := analyzer.AnalyzeLine(line)
		response.Results = append(response.Results, mapAllToLineResult(line, analysisRecords))
	}
	return response, nil
}

// extractExpressionParts 从 PipelineExpr 或 MatchersExpr 拆出选择器与各 stage 文本。
func (a logQLAnalyzer) extractExpressionParts(expr syntax.LogSelectorExpr) (string, []string, error) {
	switch expr := expr.(type) {
	case *syntax.PipelineExpr:
		stages := make([]string, 0, len(expr.MultiStages)+1)
		streamSelector := expr.Left.String()
		for _, stage := range expr.MultiStages {
			stages = append(stages, stage.String())
		}
		return streamSelector, stages, nil
	case *syntax.MatchersExpr:
		return expr.String(), []string{}, nil
	default:
		return "", nil, fmt.Errorf("unsupported type of expression")
	}

}

func mapAllToLineResult(originLine string, analysisRecords []StageAnalysisRecord) LineResult {
	stageRecords := make([]StageRecord, 0, len(analysisRecords))
	for _, record := range analysisRecords {
		if !record.Processed {
			break
		}
		stageRecords = append(stageRecords, StageRecord{
			LineBefore:   record.LineBefore,
			LabelsBefore: mapAllToLabelsResponse(record.LabelsBefore),
			LineAfter:    record.LineAfter,
			LabelsAfter:  mapAllToLabelsResponse(record.LabelsAfter),
			FilteredOut:  record.FilteredOut,
		})
	}
	return LineResult{originLine, stageRecords}
}

func mapAllToLabelsResponse(lbls []labels.Label) []Label {
	result := make([]Label, 0, len(lbls))
	for _, label := range lbls {
		result = append(result, Label{Name: label.Name, Value: label.Value})
	}
	return result
}

// PipelineAnalyzer 对单行日志返回按 stage 顺序的 StageAnalysisRecord 切片。
type PipelineAnalyzer interface {
	AnalyzeLine(line string) []StageAnalysisRecord
}
type noopPipelineAnalyzer struct {
}

func (n noopPipelineAnalyzer) AnalyzeLine(_ string) []StageAnalysisRecord {
	return []StageAnalysisRecord{}
}

type streamPipelineAnalyzer struct {
	origin       log.AnalyzablePipeline
	stagesCount  int
	streamLabels labels.Labels
}

// NewPipelineAnalyzer 若 pipeline 实现 AnalyzablePipeline 则包装为 streamPipelineAnalyzer。
func NewPipelineAnalyzer(origin log.Pipeline, streamLabels labels.Labels) PipelineAnalyzer {
	if o, ok := origin.(log.AnalyzablePipeline); ok {
		stagesCount := len(o.Stages())
		return &streamPipelineAnalyzer{o, stagesCount, streamLabels}
	}
	return &noopPipelineAnalyzer{}
}

func (p streamPipelineAnalyzer) AnalyzeLine(line string) []StageAnalysisRecord {
	stages := p.origin.Stages()
	stageRecorders := make([]log.Stage, 0, len(stages))
	records := make([]StageAnalysisRecord, len(stages))
	for i, stage := range stages {
		stageRecorders = append(stageRecorders, StageAnalysisRecorder{origin: stage,
			records:    records,
			stageIndex: i,
		})
	}
	stream := log.NewStreamPipeline(stageRecorders, p.origin.LabelsBuilder().ForLabels(p.streamLabels, labels.StableHash(p.streamLabels)))
	_, _, _ = stream.ProcessString(time.Now().UnixMilli(), line, labels.EmptyLabels())
	return records
}

// StageAnalysisRecorder 包装真实 Stage，在 Process 中捕获前后行/标签与是否被过滤。
type StageAnalysisRecorder struct {
	log.Stage
	origin     log.Stage
	stageIndex int
	records    []StageAnalysisRecord
}

func (s StageAnalysisRecorder) Process(ts int64, line []byte, lbs *log.LabelsBuilder) ([]byte, bool) {
	lineBefore := string(line)
	labelsBefore := lbs.UnsortedLabels(nil)

	lineResult, ok := s.origin.Process(ts, line, lbs)

	s.records[s.stageIndex] = StageAnalysisRecord{
		Processed:    true,
		LabelsBefore: labelsBefore,
		LineBefore:   lineBefore,
		LabelsAfter:  lbs.UnsortedLabels(nil),
		LineAfter:    string(lineResult),
		FilteredOut:  !ok,
	}
	return lineResult, ok
}
func (s StageAnalysisRecorder) RequiredLabelNames() []string {
	return s.origin.RequiredLabelNames()
}

// StageAnalysisRecord 保存单 stage 的输入输出快照及 FilteredOut 标志。
type StageAnalysisRecord struct {
	Processed    bool
	LineBefore   string
	LabelsBefore []labels.Label
	LineAfter    string
	LabelsAfter  []labels.Label
	FilteredOut  bool
}
// mapAllToLineResult 将已处理 stage 记录转为 HTTP 响应用的 LineResult/StageRecord。
