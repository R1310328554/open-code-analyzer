// 会话轮询间隔（毫秒）
const SESSION_POLLING_INTERVAL = 2000;
// 认证会话哈希校验延迟（毫秒）
const AUTH_SESSION_TIMEOUT_MILLISECS = 1000;
// 页面加载时的 KEYCLOAK_SESSION Cookie 快照
const initialSession = getSession();
const forms = Array.from(document.forms);
let timeout;

// 表单提交时停止会话轮询，避免意外重定向（Safari 对 beforeunload 支持不完整）
forms.forEach((form) =>
  form.addEventListener("submit", () => stopSessionPolling()),
);

// 页面卸载时停止轮询，防止离开页面后仍触发重定向
globalThis.addEventListener("beforeunload", () => stopSessionPolling());

/**
 * 轮询检测是否在其他标签页/窗口建立了新会话，若检测到则跳转到指定 URL。
 * @param {string} redirectUrl - 检测到新会话时的重定向地址
 */
export function startSessionPolling(redirectUrl) {
  if (initialSession) {
    // 页面初始已有会话，无需轮询
    return;
  }

  const session = getSession();

  if (!session) {
    // 尚未检测到新会话，延迟后继续轮询
    timeout = setTimeout(
      () => startSessionPolling(redirectUrl),
      SESSION_POLLING_INTERVAL,
    );
  } else {
    // 检测到新会话，跳转并停止轮询
    location.href = redirectUrl;
    stopSessionPolling();
  }
}

/** 清除会话轮询定时器。 */
function stopSessionPolling() {
  if (timeout) {
    clearTimeout(timeout);
    timeout = undefined;
  }
}

// 延迟比对页面与 Cookie 中的 KC_AUTH_SESSION_HASH，不一致则刷新页面
export function checkAuthSession(pageAuthSessionHash) {
  setTimeout(() => {
    const cookieAuthSessionHash = getKcAuthSessionHash();
    if (
      cookieAuthSessionHash &&
      cookieAuthSessionHash !== pageAuthSessionHash
    ) {
      location.reload();
    }
  }, AUTH_SESSION_TIMEOUT_MILLISECS);
}

// 读取 KC_AUTH_SESSION_HASH Cookie 值
function getKcAuthSessionHash() {
  return getCookieByName("KC_AUTH_SESSION_HASH");
}

// 读取 KEYCLOAK_SESSION Cookie 值
function getSession() {
  return getCookieByName("KEYCLOAK_SESSION");
}

// 按名称解析 document.cookie，去除引号包裹的值
function getCookieByName(name) {
  for (const cookie of document.cookie.split(";")) {
    const [key, value] = cookie.split("=").map((value) => value.trim());
    if (key === name) {
      return value.startsWith('"') && value.endsWith('"')
        ? value.slice(1, -1)
        : value;
    }
  }
  return null;
}
