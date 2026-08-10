package cassandra

// authenticator 提供 CustomPasswordAuthenticator，在 Cassandra 握手时限制可接受的认证器名称列表，同时复用标准用户名/密码挑战-响应格式。

import (
	"fmt"

	gocql "github.com/apache/cassandra-gocql-driver/v2"
)

// CustomPasswordAuthenticator provides the default behaviour for Username/Password authentication with
// Cassandra while allowing users to specify a non-default Authenticator to accept.
// CustomPasswordAuthenticator 白名单 ApprovedAuthenticators，非列表内认证器将被拒绝。
type CustomPasswordAuthenticator struct {
	ApprovedAuthenticators []string
	Username               string
	Password               string
}

func (p CustomPasswordAuthenticator) approve(authenticator string) bool {
	for _, s := range p.ApprovedAuthenticators {
		if authenticator == s {
			return true
		}
	}
	return false
}

// Challenge verifies the name of the authenticator and formats the provided username and password
// into a response
// Challenge 校验 authenticator 名后将 username\0password 编码为响应字节。
func (p CustomPasswordAuthenticator) Challenge(req []byte) ([]byte, gocql.Authenticator, error) {
	if !p.approve(string(req)) {
		return nil, nil, fmt.Errorf("unexpected authenticator %q", req)
	}
	resp := make([]byte, 2+len(p.Username)+len(p.Password))
	resp[0] = 0
	copy(resp[1:], p.Username)
	resp[len(p.Username)+1] = 0
	copy(resp[2+len(p.Username):], p.Password)
	return resp, nil, nil
}

// Success returns nil by default, identical to the default PasswordAuthenticator
// Success 认证成功后无额外处理，行为与默认 PasswordAuthenticator 一致。
func (p CustomPasswordAuthenticator) Success(_ []byte) error {
	return nil
}
// approve 线性扫描 ApprovedAuthenticators，匹配 server 下发的认证器名称。
