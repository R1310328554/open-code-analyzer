/** JWT 载荷解码后的最小结构（当前仅关心过期时间 exp） */
export interface DecodedToken {
  exp?: number;
}

/**
 * 解码 JWT 访问令牌的中段（payload），无需验证签名。
 * 适用于客户端侧读取 exp 等声明以判断令牌是否过期。
 */
export function decodeToken(token: string): DecodedToken {
  const [, payload] = token.split(".");

  if (typeof payload !== "string") {
    return {};
  }

  let decoded;

  try {
    decoded = base64UrlDecode(payload);
  } catch (error) {
    throw new Error(
      "Unable to decode token, payload is not a valid Base64URL value.",
      { cause: error },
    );
  }

  try {
    return JSON.parse(decoded);
  } catch (error) {
    throw new Error(
      "Unable to decode token, payload is not a valid JSON value.",
      { cause: error },
    );
  }
}

/** Base64URL 解码：`-`/`_` 还原为 `+`/`/`，并按长度补齐 `=` 填充 */
function base64UrlDecode(input: string): string {
  let output = input.replaceAll("-", "+").replaceAll("_", "/");

  switch (output.length % 4) {
    case 0:
      break;
    case 2:
      output += "==";
      break;
    case 3:
      output += "=";
      break;
    default:
      throw new Error("Input is not of the correct length.");
  }

  try {
    return b64DecodeUnicode(output);
  } catch {
    return atob(output);
  }
}

/** 支持 Unicode 的 Base64 解码（通过 percent-encoding 中转） */
function b64DecodeUnicode(input: string): string {
  return decodeURIComponent(
    atob(input).replace(/(.)/g, (m, p) => {
      let code = p.charCodeAt(0).toString(16).toUpperCase();

      if (code.length < 2) {
        code = "0" + code;
      }

      return "%" + code;
    }),
  );
}
