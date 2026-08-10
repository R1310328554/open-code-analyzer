package client

// client 包 alerts 子模块封装 Loki Alertmanager 配置 REST API：创建、删除与读取租户级 alertmanager 主配置及模板文件。

import (
	"context"
	"io"

	"github.com/pkg/errors"
	log "github.com/sirupsen/logrus"
	"go.yaml.in/yaml/v3"
)

const alertmanagerAPIPath = "/api/v1/alerts"

type configCompat struct {
	TemplateFiles      map[string]string `yaml:"template_files"`
	AlertmanagerConfig string            `yaml:"alertmanager_config"`
}

// CreateAlertmanagerConfig 以 YAML 封装主配置与模板，POST 到 /api/v1/alerts。
// CreateAlertmanagerConfig creates a new alertmanager config
func (r *LokiClient) CreateAlertmanagerConfig(ctx context.Context, cfg string, templates map[string]string) error {
	payload, err := yaml.Marshal(&configCompat{
		TemplateFiles:      templates,
		AlertmanagerConfig: cfg,
	})
	if err != nil {
		return err
	}

	res, err := r.doRequest(ctx, alertmanagerAPIPath, "POST", payload)
	if err != nil {
		return err
	}

	res.Body.Close()

	return nil
}

// DeleteAlermanagerConfig 对当前租户发起 DELETE，清空远端 Alertmanager 配置。
// DeleteAlermanagerConfig deletes the users alertmanagerconfig
func (r *LokiClient) DeleteAlermanagerConfig(ctx context.Context) error {
	res, err := r.doRequest(ctx, alertmanagerAPIPath, "DELETE", nil)
	if err != nil {
		return err
	}

	res.Body.Close()

	return nil
}

// GetAlertmanagerConfig GET 拉取配置体与模板 map，反序列化失败时记录 debug 日志。
// GetAlertmanagerConfig retrieves a rule group
func (r *LokiClient) GetAlertmanagerConfig(ctx context.Context) (string, map[string]string, error) {
	res, err := r.doRequest(ctx, alertmanagerAPIPath, "GET", nil)
	if err != nil {
		log.Debugln("no alert config present in response")
		return "", nil, err
	}

	defer res.Body.Close()
	body, err := io.ReadAll(res.Body)
	if err != nil {
		return "", nil, err
	}

	compat := configCompat{}
	err = yaml.Unmarshal(body, &compat)
	if err != nil {
		log.WithFields(log.Fields{
			"body": string(body),
		}).Debugln("failed to unmarshal rule group from response")

		return "", nil, errors.Wrap(err, "unable to unmarshal response")
	}

	return compat.AlertmanagerConfig, compat.TemplateFiles, nil
}
// alertmanagerAPIPath 固定为 /api/v1/alerts，与 ruler 规则 API 路径相互独立。
