package validation

// util/validation 包 NotificationRateLimitMap 为告警通知集成渠道配置每秒速率上限：支持 flag/YAML 解析并校验 integration 名称白名单。

import (
	"encoding/json"
	"fmt"

	"github.com/pkg/errors"

	"github.com/grafana/loki/v3/pkg/util"
)

var allowedIntegrationNames = []string{
	"webhook", "email", "pagerduty", "opsgenie", "wechat", "slack", "victorops", "pushover", "sns",
}

// NotificationRateLimitMap 键为 webhook/slack 等集成名，值为每秒允许通知次数。
type NotificationRateLimitMap map[string]float64

// String 将 map 序列化为 JSON 字符串，供命令行 flag 默认值展示。
// String implements flag.Value
func (m NotificationRateLimitMap) String() string {
	out, err := json.Marshal(map[string]float64(m))
	if err != nil {
		return fmt.Sprintf("failed to marshal: %v", err)
	}
	return string(out)
}

// Set 解析 JSON 字符串并调用 updateMap，非法 integration 名返回错误。
// Set implements flag.Value
func (m NotificationRateLimitMap) Set(s string) error {
	newMap := map[string]float64{}
	return m.updateMap(json.Unmarshal([]byte(s), &newMap), newMap)
}

// UnmarshalYAML 从 limits 配置加载各渠道通知速率，与 flag 共用 updateMap 校验。
// UnmarshalYAML implements yaml.Unmarshaler.
func (m NotificationRateLimitMap) UnmarshalYAML(unmarshal func(interface{}) error) error {
	newMap := map[string]float64{}
	return m.updateMap(unmarshal(newMap), newMap)
}

func (m NotificationRateLimitMap) updateMap(unmarshalErr error, newMap map[string]float64) error {
	if unmarshalErr != nil {
		return unmarshalErr
	}

	for k, v := range newMap {
		if !util.StringsContain(allowedIntegrationNames, k) {
			return errors.Errorf("unknown integration name: %s", k)
		}
		m[k] = v
	}
	return nil
}

// MarshalYAML implements yaml.Marshaler.
func (m NotificationRateLimitMap) MarshalYAML() (interface{}, error) {
	return map[string]float64(m), nil
}
// allowedIntegrationNames 限定合法渠道，防止配置拼写错误导致静默丢弃限额。
