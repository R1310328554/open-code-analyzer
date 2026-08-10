package syslogparser

// Syslog 流解析封装：基于 go-syslog 库，自动识别非透明帧（'<' 开头）
// 与八位计数（数字开头）两种 RFC5424/RFC3164 传输格式。

import (
	"bufio"
	"fmt"
	"io"

	"github.com/leodido/go-syslog/v4"
	"github.com/leodido/go-syslog/v4/nontransparent"
	"github.com/leodido/go-syslog/v4/octetcounting"
)

// 从 Reader 逐条解析 syslog 消息并通过 callback 回调；遇 EOF 或不可恢复错误时返回。
// ParseStream parses a rfc5424 syslog stream from the given Reader, calling
// the callback function with the parsed messages. The parser automatically
// detects octet counting.
// The function returns on EOF or unrecoverable errors.
func ParseStream(isRFC3164Message bool, r io.Reader, callback func(res *syslog.Result), maxMessageLength int) error {
	buf := bufio.NewReaderSize(r, 1<<10)

	b, err := buf.ReadByte()
	if err != nil {
		return err
	}
	_ = buf.UnreadByte()

// 首字节 '<' 表示非透明（non-transparent）分帧，选用对应 RFC 解析器。
	if b == '<' {
		if isRFC3164Message {
			nontransparent.NewParserRFC3164(syslog.WithListener(callback), syslog.WithMaxMessageLength(maxMessageLength), syslog.WithBestEffort()).Parse(buf)
		} else {
			nontransparent.NewParser(syslog.WithListener(callback), syslog.WithMaxMessageLength(maxMessageLength), syslog.WithBestEffort()).Parse(buf)
		}
// 首字节为数字时使用八位计数（octet-counting）分帧解析器。
	} else if b >= '0' && b <= '9' {
		if isRFC3164Message {
			octetcounting.NewParserRFC3164(syslog.WithListener(callback), syslog.WithMaxMessageLength(maxMessageLength), syslog.WithBestEffort()).Parse(buf)
		} else {
			octetcounting.NewParser(syslog.WithListener(callback), syslog.WithMaxMessageLength(maxMessageLength), syslog.WithBestEffort()).Parse(buf)
		}
	} else {
		return fmt.Errorf("invalid or unsupported framing. first byte: '%s'", string(b))
	}

	return nil
}
