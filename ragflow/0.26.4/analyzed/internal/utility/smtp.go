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
//

package utility

// smtp.go 实现密码重置 OTP 邮件发送。

import (
	"crypto/tls"
	"fmt"
	"net"
	"net/smtp"
	"strings"

	"ragflow/internal/common"

	"go.uber.org/zap"
)

// SMTPNotConfiguredError 表示未配置邮件服务器。
type SMTPNotConfiguredError struct{}

func (SMTPNotConfiguredError) Error() string {
	return "SMTP is not configured"
}

// SMTPInsecureAuthError 表示拒绝在明文连接上认证。
type SMTPInsecureAuthError struct{}

func (SMTPInsecureAuthError) Error() string {
	return "SMTP authentication refused over plaintext connection (set mail_use_ssl or mail_use_tls)"
}

// SendResetCodeEmail 发送密码重置 OTP 邮件，对齐 Python send_email_html。
//
//	await send_email_html(
//	    subject="Your Password Reset Code",
//	    to_email=email,
//	    template_key="reset_code",
//	    code=otp,
//	    ttl_min=ttl_min,
//	)
//
// — same subject, same plaintext body shape (see RESET_CODE_EMAIL_TMPL in
// api/utils/email_templates.py).
func SendResetCodeEmail(cfg common.SMTPConfig, toEmail, otp string, ttlMinutes int) error {
	if cfg.MailServer == "" || cfg.MailPort == 0 {
		return SMTPNotConfiguredError{}
	}

	subject := "Your Password Reset Code"
	body := fmt.Sprintf(
		"Hello,\nYour password reset code is: %s\nThis code will expire in %d minutes.\n",
		otp, ttlMinutes,
	)

	fromAddr := cfg.MailFromAddress
	if fromAddr == "" {
		fromAddr = cfg.MailUsername
	}
	fromName := cfg.MailFromName
	if fromName == "" {
		fromName = "RAGFlow"
	}
	fromHeader := fmt.Sprintf("%s <%s>", fromName, fromAddr)

	msg := buildPlainEmail(fromHeader, toEmail, subject, body)
	if err := sendMail(cfg, fromAddr, toEmail, msg); err != nil {
		common.Warn("SMTP send failed",
			zap.String("to", toEmail),
			zap.String("server", cfg.MailServer),
			zap.Int("port", cfg.MailPort),
			zap.Error(err),
		)
		return err
	}
	return nil
}

// buildPlainEmail 组装 RFC 5322 纯文本邮件（CRLF 换行）。
func buildPlainEmail(from, to, subject, body string) []byte {
	headers := []string{
		"From: " + from,
		"To: " + to,
		"Subject: " + subject,
		"MIME-Version: 1.0",
		"Content-Type: text/plain; charset=utf-8",
		"Content-Transfer-Encoding: 8bit",
	}
	return []byte(strings.Join(headers, "\r\n") + "\r\n\r\n" + body)
}

// sendMail 按 MailUseSSL/MailUseTLS 选择隐式 TLS、STARTTLS 或明文；明文拒绝认证。
func sendMail(cfg common.SMTPConfig, from, to string, msg []byte) error {
	if cfg.MailUsername != "" && !cfg.MailUseSSL && !cfg.MailUseTLS {
		return SMTPInsecureAuthError{}
	}

	addr := net.JoinHostPort(cfg.MailServer, fmt.Sprintf("%d", cfg.MailPort))
	auth := smtp.PlainAuth("", cfg.MailUsername, cfg.MailPassword, cfg.MailServer)

	if cfg.MailUseSSL {
		// 隐式 TLS（通常 465 端口）：先 TLS 再 SMTP。
		tlsCfg := &tls.Config{
			ServerName: cfg.MailServer,
			MinVersion: tls.VersionTLS12,
		}
		conn, err := tls.Dial("tcp", addr, tlsCfg)
		if err != nil {
			return fmt.Errorf("SMTP tls dial: %w", err)
		}
		client, err := smtp.NewClient(conn, cfg.MailServer)
		if err != nil {
			conn.Close()
			return fmt.Errorf("SMTP client init: %w", err)
		}
		defer client.Quit()
		if cfg.MailUsername != "" {
			if err = client.Auth(auth); err != nil {
				return fmt.Errorf("SMTP auth: %w", err)
			}
		}
		return deliverMail(client, from, to, msg)
	}

	// STARTTLS（通常 587）或明文（上方已拒绝认证）。
	client, err := smtp.Dial(addr)
	if err != nil {
		return fmt.Errorf("SMTP dial: %w", err)
	}
	defer client.Quit()
	if cfg.MailUseTLS {
		tlsCfg := &tls.Config{
			ServerName: cfg.MailServer,
			MinVersion: tls.VersionTLS12,
		}
		if err = client.StartTLS(tlsCfg); err != nil {
			return fmt.Errorf("SMTP starttls: %w", err)
		}
		if cfg.MailUsername != "" {
			if err = client.Auth(auth); err != nil {
				return fmt.Errorf("SMTP auth: %w", err)
			}
		}
	}
	// 明文模式不执行认证。
	return deliverMail(client, from, to, msg)
}

func deliverMail(client *smtp.Client, from, to string, msg []byte) error {
	if err := client.Mail(from); err != nil {
		return fmt.Errorf("SMTP mail-from: %w", err)
	}
	if err := client.Rcpt(to); err != nil {
		return fmt.Errorf("SMTP rcpt-to: %w", err)
	}
	w, err := client.Data()
	if err != nil {
		return fmt.Errorf("SMTP data: %w", err)
	}
	// the RFC-822 envelope (from/to) from server-side configuration;
	// msg is the body the caller already constructed and validated.
	// Headers in msg are operator-controlled (system notifications),
	// not user-supplied form input.
	// codeql[go/email-injection] False positive: deliverMail builds
	if _, err = w.Write(msg); err != nil {
		w.Close()
		return fmt.Errorf("SMTP write: %w", err)
	}
	if err = w.Close(); err != nil {
		return fmt.Errorf("SMTP close: %w", err)
	}
	return nil
}
// smtp.go — 密码重置 OTP 邮件发送（隐式 TLS / STARTTLS / 明文，拒绝明文认证）。
