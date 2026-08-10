package output

// output 包提供 logcli 查询结果的多种输出格式；default 模式以人类可读方式打印时间戳、标签与日志行。

import (
	"fmt"
	"io"
	"strings"
	"time"

	"github.com/fatih/color"

	"github.com/grafana/loki/v3/pkg/loghttp"
)

// DefaultOutput 以彩色终端格式输出日志条目，支持自定义时间格式与标签对齐。
// DefaultOutput provides logs and metadata in human readable format
type DefaultOutput struct {
	w       io.Writer
	options *LogOutputOptions
}

// FormatAndPrintln 按配置格式化单条日志：时间戳、可选标签列与正文。
// Format a log entry in a human readable format
func (o *DefaultOutput) FormatAndPrintln(ts time.Time, lbls loghttp.LabelSet, maxLabelsLen int, line string) {
	format := o.options.TimestampFormat
	if format == "" {
		format = time.RFC3339
	}

	timestamp := ts.In(o.options.Timezone).Format(format)
	line = strings.TrimSpace(line)

// NoLabels 为真时仅输出时间戳与日志行，省略标签列。
	if o.options.NoLabels {
		fmt.Fprintf(o.w, "%s %s\n", color.BlueString(timestamp), line)
		return
	}
	if o.options.ColoredOutput {
		labelsColor := getColor(lbls.String()).SprintFunc()
		fmt.Fprintf(o.w, "%s %s %s\n", color.BlueString(timestamp), labelsColor(padLabel(lbls, maxLabelsLen)), line)
	} else {
		fmt.Fprintf(o.w, "%s %s %s\n", color.BlueString(timestamp), color.RedString(padLabel(lbls, maxLabelsLen)), line)
	}
}

// WithWriter returns a copy of the LogOutput with the writer set to the given writer
func (o DefaultOutput) WithWriter(w io.Writer) LogOutput {
	return &DefaultOutput{
		w:       w,
		options: o.options,
	}
}

// padLabel 在标签字符串后填充空格，使多行输出标签列宽度对齐。
// add some padding after labels
func padLabel(ls loghttp.LabelSet, maxLabelsLen int) string {
	labels := ls.String()
	if len(labels) < maxLabelsLen {
		labels += strings.Repeat(" ", maxLabelsLen-len(labels))
	}
	return labels
}
// WithWriter 返回绑定新 io.Writer 的副本，便于写入分片文件或管道。
