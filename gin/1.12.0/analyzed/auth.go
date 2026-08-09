// Copyright 2014 Manu Martinez-Almeida. All rights reserved.
// Use of this source code is governed by a MIT style
// license that can be found in the LICENSE file.

package gin

import (
	"crypto/subtle"
	"encoding/base64"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin/internal/bytesconv"
)

// AuthUserKey 是 Basic 认证中存放用户凭证的 Context 键名。
const AuthUserKey = "user"

// AuthProxyUserKey 是代理 Basic 认证中存放 proxy_user 凭证的 Context 键名。
const AuthProxyUserKey = "proxy_user"

// Accounts 定义授权登录的用户名/密码映射表。
type Accounts map[string]string

type authPair struct {
	value string
	user  string
}

type authPairs []authPair

func (a authPairs) searchCredential(authValue string) (string, bool) {
	if authValue == "" {
		return "", false
	}
	for _, pair := range a {
		if subtle.ConstantTimeCompare(bytesconv.StringToBytes(pair.value), bytesconv.StringToBytes(authValue)) == 1 {
			return pair.user, true
		}
	}
	return "", false
}

// BasicAuthForRealm 返回 Basic HTTP Authorization 中间件。
// accounts 的键为用户名、值为密码；realm 为认证域名称。
// 若 realm 为空，默认使用 "Authorization Required"。
// （参见 http://tools.ietf.org/html/rfc2617#section-1.2）
func BasicAuthForRealm(accounts Accounts, realm string) HandlerFunc {
	if realm == "" {
		realm = "Authorization Required"
	}
	realm = "Basic realm=" + strconv.Quote(realm)
	pairs := processAccounts(accounts)
	return func(c *Context) {
		// 在允许的凭证列表中查找用户
		user, found := pairs.searchCredential(c.requestHeader("Authorization"))
		if !found {
			// 凭证不匹配，返回 401 并中止后续处理器链
			c.Header("WWW-Authenticate", realm)
			c.AbortWithStatus(http.StatusUnauthorized)
			return
		}

		// 找到用户凭证，将用户名写入 Context 的 AuthUserKey，
		// 后续可通过 c.MustGet(gin.AuthUserKey) 读取。
		c.Set(AuthUserKey, user)
	}
}

// BasicAuth 返回 Basic HTTP Authorization 中间件。
// accounts 的键为用户名、值为密码。
func BasicAuth(accounts Accounts) HandlerFunc {
	return BasicAuthForRealm(accounts, "")
}

func processAccounts(accounts Accounts) authPairs {
	length := len(accounts)
	assert1(length > 0, "Empty list of authorized credentials")
	pairs := make(authPairs, 0, length)
	for user, password := range accounts {
		assert1(user != "", "User can not be empty")
		value := authorizationHeader(user, password)
		pairs = append(pairs, authPair{
			value: value,
			user:  user,
		})
	}
	return pairs
}

func authorizationHeader(user, password string) string {
	base := user + ":" + password
	return "Basic " + base64.StdEncoding.EncodeToString(bytesconv.StringToBytes(base))
}

// BasicAuthForProxy 返回 Basic HTTP Proxy-Authorization 中间件。
// 若 realm 为空，默认使用 "Proxy Authorization Required"。
func BasicAuthForProxy(accounts Accounts, realm string) HandlerFunc {
	if realm == "" {
		realm = "Proxy Authorization Required"
	}
	realm = "Basic realm=" + strconv.Quote(realm)
	pairs := processAccounts(accounts)
	return func(c *Context) {
		proxyUser, found := pairs.searchCredential(c.requestHeader("Proxy-Authorization"))
		if !found {
			// 凭证不匹配，返回 407 并中止后续处理器链
			c.Header("Proxy-Authenticate", realm)
			c.AbortWithStatus(http.StatusProxyAuthRequired)
			return
		}
		// 找到代理用户凭证，将 proxy_user 写入 Context 的 AuthProxyUserKey，
		// 后续可通过 c.MustGet(gin.AuthProxyUserKey) 读取。
		c.Set(AuthProxyUserKey, proxyUser)
	}
}
