package flagext

// LabelSet 将 Prometheus model.LabelSet 包装为 flag.Value，支持 key=value 与 CSV 多标签格式。

import (
	"bytes"
	"encoding/csv"
	"fmt"
	"strings"

	"github.com/prometheus/common/model"
	"gopkg.in/yaml.v2"

	"github.com/grafana/loki/v3/pkg/util"
)

// LabelSet 内联 model.LabelSet，可用于命令行、YAML 与 JSON 配置中的静态标签。
// LabelSet is a labelSet that can be used as a flag.
type LabelSet struct {
	model.LabelSet `yaml:",inline"`
}

// String 输出 [a=1,b=2] 形式，多标签时用 csv.Writer 正确转义含逗号的值。
// String implements flag.Value
// Format: a=1,b=2
func (v LabelSet) String() string {
	if v.LabelSet == nil {
		return ""
	}
	records := make([]string, 0, len(v.LabelSet)>>1)
	for k, v := range v.LabelSet {
		records = append(records, string(k)+"="+string(v))
	}

	var buf bytes.Buffer
	w := csv.NewWriter(&buf)
	if err := w.Write(records); err != nil {
		panic(err)
	}
	w.Flush()
	return "[" + strings.TrimSpace(buf.String()) + "]"
}

// Set 解析单对 key=value 或 CSV 多对，经 model.LabelSet.Validate 校验标签合法性。
// Set implements flag.Value
func (v *LabelSet) Set(s string) error {
	var ss []string
	n := strings.Count(s, "=")
	switch n {
	case 0:
		return fmt.Errorf("%s must be formatted as key=value", s)
	case 1:
		ss = append(ss, strings.Trim(s, `"`))
	default:
		r := csv.NewReader(strings.NewReader(s))
		var err error
		ss, err = r.Read()
		if err != nil {
			return err
		}
	}

	out := model.LabelSet{}
	for _, pair := range ss {
		kv := strings.SplitN(pair, "=", 2)
		if len(kv) != 2 {
			return fmt.Errorf("%s must be formatted as key=value", pair)
		}
		out[model.LabelName(kv[0])] = model.LabelValue(kv[1])
	}

	if err := out.Validate(); err != nil {
		return err
	}
	v.LabelSet = out
	return nil
}

// UnmarshalYAML 直接反序列化为 model.LabelSet 并赋值给内嵌字段。
// UnmarshalYAML the Unmarshaler interface of the yaml pkg.
func (v *LabelSet) UnmarshalYAML(unmarshal func(interface{}) error) error {
	lbSet := model.LabelSet{}
	err := unmarshal(&lbSet)
	if err != nil {
		return err
	}
	v.LabelSet = lbSet
	return nil
}

// MarshalYAML implements yaml.Marshaller.
func (v LabelSet) MarshalYAML() (interface{}, error) {
	out, err := yaml.Marshal(util.ModelLabelSetToMap(v.LabelSet))
	if err != nil {
		return nil, err
	}
	return string(out), nil
}
// MarshalYAML 经 util.ModelLabelSetToMap 转为 map 再 yaml.Marshal，输出标准 YAML 结构。
