// Copyright 2019 Drone IO, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package main

import (
	spec "github.com/drone/drone/cmd/drone-server/config"
	"github.com/drone/drone/core"
	"github.com/drone/drone/plugin/admission"
	"github.com/drone/drone/plugin/config"
	"github.com/drone/drone/plugin/converter"
	"github.com/drone/drone/plugin/registry"
	"github.com/drone/drone/plugin/secret"
	"github.com/drone/drone/plugin/validator"
	"github.com/drone/drone/plugin/webhook"
	"github.com/drone/go-scm/scm"

	"github.com/google/wire"
)

// wire set for loading plugins.
// pluginSet 定义各类扩展插件（准入、配置、转换等）的 Wire 提供者集合。
var pluginSet = wire.NewSet(
	provideAdmissionPlugin,
	provideConfigPlugin,
	provideConvertPlugin,
	provideRegistryPlugin,
	provideSecretPlugin,
	provideValidatePlugin,
	provideWebhookPlugin,
)

// provideAdmissionPlugin 根据环境配置组合并返回用户准入控制插件。
func provideAdmissionPlugin(client *scm.Client, orgs core.OrganizationService, users core.UserService, config spec.Config) core.AdmissionService {
	return admission.Combine(
		admission.Membership(orgs, config.Users.Filter),
		admission.Open(config.Registration.Closed),
		admission.Nobot(users, config.Users.MinAge),
		admission.External(
			config.Authn.Endpoint,
			config.Authn.Secret,
			config.Authn.SkipVerify,
		),
	)
}

// provideConfigPlugin 根据环境配置返回 YAML 流水线配置解析插件。
func provideConfigPlugin(client *scm.Client, contents core.FileService, conf spec.Config) core.ConfigService {
	return config.Combine(
		config.Memoize(
			config.Global(
				conf.Yaml.Endpoint,
				conf.Yaml.Secret,
				conf.Yaml.SkipVerify,
				conf.Yaml.Timeout,
			),
		),
		config.Repository(contents),
	)
}

// provideConvertPlugin 根据环境配置返回 YAML 流水线转换插件。
func provideConvertPlugin(client *scm.Client, fileService core.FileService, conf spec.Config, templateStore core.TemplateStore) core.ConvertService {
	return converter.Combine(
		conf.Convert.Multi,
		converter.Legacy(false),
		converter.Starlark(
			conf.Starlark.Enabled,
			conf.Starlark.StepLimit,
			conf.Starlark.SizeLimit,
		),
		converter.Jsonnet(
			conf.Jsonnet.Enabled,
			conf.Jsonnet.ImportLimit,
			fileService,
		),
		converter.Template(
			templateStore,
			conf.Starlark.StepLimit,
			conf.Starlark.SizeLimit,
		),
		converter.Memoize(
			converter.Remote(
				conf.Convert.Endpoint,
				conf.Convert.Secret,
				conf.Convert.Extension,
				conf.Convert.SkipVerify,
				conf.Convert.Timeout,
			),
			conf.Convert.CacheSize,
		),
	)
}

// provideRegistryPlugin 根据环境配置返回容器镜像仓库凭证插件。
func provideRegistryPlugin(config spec.Config) core.RegistryService {
	return registry.Combine(
		registry.External(
			config.Secrets.Endpoint,
			config.Secrets.Password,
			config.Secrets.SkipVerify,
		),
		registry.FileSource(
			config.Docker.Config,
		),
		registry.EndpointSource(
			config.Registries.Endpoint,
			config.Registries.Password,
			config.Registries.SkipVerify,
		),
	)
}

// provideSecretPlugin 根据环境配置返回密钥管理外部插件。
func provideSecretPlugin(config spec.Config) core.SecretService {
	return secret.External(
		config.Secrets.Endpoint,
		config.Secrets.Password,
		config.Secrets.SkipVerify,
	)
}

// provideValidatePlugin 根据环境配置返回 YAML 流水线校验插件。
func provideValidatePlugin(conf spec.Config) core.ValidateService {
	return validator.Combine(
		validator.Remote(
			conf.Validate.Endpoint,
			conf.Validate.Secret,
			conf.Validate.SkipVerify,
			conf.Validate.Timeout,
		),
		// THIS FEATURE IS INTERNAL USE ONLY AND SHOULD
		// NOT BE USED OR RELIED UPON IN PRODUCTION.
		validator.Filter(
			nil,
			conf.Repository.Ignore,
		),
	)
}

// provideWebhookPlugin 根据环境配置返回 Webhook 事件转发插件。
func provideWebhookPlugin(config spec.Config, system *core.System) core.WebhookSender {
	return webhook.New(webhook.Config{
		Events:   config.Webhook.Events,
		Endpoint: config.Webhook.Endpoint,
		Secret:   config.Webhook.Secret,
		System:   system,
	})
}
