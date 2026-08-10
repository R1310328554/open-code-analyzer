package server

// Promtail Web UI 模板渲染：从 ui.Assets 加载 _base 与页面 HTML，经 Prometheus template 展开。
// 提供 since/pathPrefix 等通用 FuncMap，结果写入 http.ResponseWriter。

import (
	"context"
	"io"
	"net/http"
	"net/url"
	"path"
	template_text "text/template"
	"time"

	"github.com/pkg/errors"
	"github.com/prometheus/common/model"
	"github.com/prometheus/prometheus/template"

	"github.com/grafana/loki/v3/clients/pkg/promtail/server/ui"
)

// 单次页面渲染参数：外部 URL、页面标题、数据与额外模板函数。
// templateOptions is a set of options to render a template.
type templateOptions struct {
	ExternalURL                   *url.URL
	Name, PageTitle, BuildVersion string
	Data                          interface{}
	TemplateFuncs                 template_text.FuncMap
}

// tmplFuncs create a default template function for a given template options
// 默认模板函数：相对时间 since、路径前缀与构建版本字符串。
func (opts templateOptions) tmplFuncs() template_text.FuncMap {
	return template_text.FuncMap{
		"since": func(t time.Time) time.Duration {
			return time.Since(t) / time.Millisecond * time.Millisecond
		},
		"pathPrefix":   func() string { return opts.ExternalURL.Path },
		"pageTitle":    func() string { return opts.PageTitle },
		"buildVersion": func() string { return opts.BuildVersion },
	}
}

// 加载模板、合并 FuncMap、ExpandHTML 后输出；出错返回 500。
// executeTemplate execute a template and write result to the http.ResponseWriter
func executeTemplate(ctx context.Context, w http.ResponseWriter, tmplOpts templateOptions) {
	text, err := getTemplate(tmplOpts.Name)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
	}

	tmpl := template.NewTemplateExpander(
		ctx,
		text,
		tmplOpts.Name,
		tmplOpts.Data,
		model.Now(),
		nil,
		tmplOpts.ExternalURL,
		nil,
	)

	tmpl.Funcs(tmplOpts.tmplFuncs())
	tmpl.Funcs(tmplOpts.TemplateFuncs)

	result, err := tmpl.ExpandHTML(nil)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	_, _ = io.WriteString(w, result)
}

// 拼接 _base.html 与指定页面模板，自 vfs 读取为单一字符串。
func getTemplate(name string) (string, error) {
	var tmpl string

	appendf := func(name string) error {
		f, err := ui.Assets.Open(path.Join("/templates", name))
		if err != nil {
			return err
		}
		defer func() {
			_ = f.Close()
		}()
		b, err := io.ReadAll(f)
		if err != nil {
			return err
		}
		tmpl += string(b)
		return nil
	}

	err := appendf("_base.html")
	if err != nil {
		return "", errors.Wrap(err, "error reading base template")
	}
	err = appendf(name)
	if err != nil {
		return "", errors.Wrapf(err, "error reading page template %s", name)
	}

	return tmpl, nil
}
