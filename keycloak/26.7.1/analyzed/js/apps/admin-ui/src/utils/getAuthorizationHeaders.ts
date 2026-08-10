/** 由 access token 构造 HTTP Authorization 请求头。 */
export function getAuthorizationHeaders(
  accessToken?: string,
): Record<string, string> {
  if (!accessToken) {
    return {};
  }

  return { Authorization: `Bearer ${accessToken}` };
}
