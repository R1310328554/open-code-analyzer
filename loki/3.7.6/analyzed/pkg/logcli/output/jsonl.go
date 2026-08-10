package output

// JSONLOutput 将每条日志序列化为 JSON Lines，便于脚本与管道工具消费。

import (
	"encoding/json"
	"fmt"
	"io"
	"log"
	"time"

	"github.com/grafana/loki/v3/pkg/loghttp"
)

// JSONLOutput 结构体持有输出流与 LogOutputOptions 配置。
// JSONLOutput prints logs and metadata as JSON Lines, suitable for scripts
type JSONLOutput struct {
	w       io.Writer
	options *LogOutputOptions
}

// FormatAndPrintln 构造含 timestamp、line 及可选 labels 的 JSON 并换行输出。
// Format a log entry as json line
func (o *JSONLOutput) FormatAndPrintln(ts time.Time, lbls loghttp.LabelSet, _ int, line string) {
	entry := map[string]interface{}{
		"timestamp": ts.In(o.options.Timezone),
		"line":      line,
	}

// NoLabels 为真时不写入 labels 字段，减小 JSON 体积。
	// Labels are optional
	if !o.options.NoLabels {
		entry["labels"] = lbls
	}

	out, err := json.Marshal(entry)
	if err != nil {
		log.Fatalf("error marshalling entry: %s", err)
	}

	fmt.Fprintln(o.w, string(out))
}

// WithWriter returns a copy of the LogOutput with the writer set to the given writer
func (o JSONLOutput) WithWriter(w io.Writer) LogOutput {
	return &JSONLOutput{
		w:       w,
		options: o.options,
	}
}
// WithWriter 支持将 JSONL 重定向到任意 Writer，例如临时分片文件。
