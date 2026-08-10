//
//  Copyright 2026 The InfiniFlow Authors. All Rights Reserved.
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
// smtp_config.go — SMTP 邮件配置结构体：映射 service_conf.yaml 中的 mail_* 字段，供忘记密码 OTP 等事务邮件发送使用。

//

package common

// SMTPConfig 对应 conf/service_conf.yaml 的 SMTP 配置块。
// 放在 common 包以避免 utility ↔ server 循环依赖；供忘记密码 OTP 及其他事务邮件路径引用。
type SMTPConfig struct {
	// MailServer SMTP 服务器主机名或 IP。
	MailServer      string `mapstructure:"mail_server"`
	// MailPort SMTP 端口号。
	MailPort        int    `mapstructure:"mail_port"`
	// MailUseSSL 是否启用 SSL 直连。
	MailUseSSL      bool   `mapstructure:"mail_use_ssl"`
	// MailUseTLS 是否启用 STARTTLS 升级。
	MailUseTLS      bool   `mapstructure:"mail_use_tls"`
	// MailUsername SMTP 认证用户名。
	MailUsername    string `mapstructure:"mail_username"`
	// MailPassword SMTP 认证密码。
	MailPassword    string `mapstructure:"mail_password"`
	// MailFromName 发件人显示名称。
	MailFromName    string `mapstructure:"mail_from_name"`
	// MailFromAddress 发件人邮箱地址。
	MailFromAddress string `mapstructure:"mail_from_address"`
	// MailFrontendURL 前端站点 URL，用于邮件内链接跳转。
	MailFrontendURL string `mapstructure:"mail_frontend_url"`
}
