// WebAuthn 认证流程：凭证获取、表单回填与错误提交
import { base64url } from "rfc4648";

// 单例 AbortController，用于取消进行中的 WebAuthn 调用
let abortController = undefined;

// 中止上一次 pending 调用并返回新 signal
export function signal() {
    if (abortController) {
        const abortError = new Error("Cancelling pending WebAuthn call");
        abortError.name = "AbortError";
        abortController.abort(abortError);
    }

    abortController = new AbortController();
    return abortController.signal;
}

// 按钮触发的 WebAuthn 认证入口
export async function authenticateByWebAuthn(input) {
    const allowCredentials = input.isUserIdentified ? getAllowCredentials() : [];
    try {
        const result = await doAuthenticate({ ...input, allowCredentials });
        if (result) returnSuccess(result);
    } catch (error) {
        returnFailure(error);
    }
}

/**
 * 从隐藏表单 authn_select 读取允许的凭证列表。
 * 导出供 passkeysConditionalAuth.js 复用。
 */
export function getAllowCredentials() {
    const allowCredentials = [];
    const authnUse = document.forms['authn_select']?.authn_use_chk;
    if (authnUse !== undefined) {
        if (authnUse.length === undefined) {
            allowCredentials.push({
                id: base64url.parse(authnUse.value, { loose: true }),
                type: 'public-key',
            });
        } else {
            authnUse.forEach((entry) =>
                allowCredentials.push({
                    id: base64url.parse(entry.value, { loose: true }),
                    type: 'public-key',
                }));
        }
    }
    return allowCredentials;
}

/**
 * navigator.credentials.get() 核心封装，供条件式认证复用。
 *
 * input: { challenge, userVerification, rpId, createTimeout, errmsg,
 *           allowCredentials?: PublicKeyCredentialDescriptor[],
 *           additionalOptions?: object  ← 如 { mediation: "conditional" | "optional" | "required" | "silent" } }
 */
export function doAuthenticate(input) {
    if (!window.PublicKeyCredential) {
        returnFailure(input.errmsg);
        return;
    }

    const publicKey = {
        rpId: input.rpId,
        challenge: base64url.parse(input.challenge, { loose: true }),
    };

    if (input.createTimeout !== 0) {
        publicKey.timeout = input.createTimeout * 1000;
    }

    if (input.allowCredentials !== undefined) {
        publicKey.allowCredentials = input.allowCredentials;
    }

    if (input.userVerification !== 'not specified') {
        publicKey.userVerification = input.userVerification;
    }

    return navigator.credentials.get({
        publicKey: publicKey,
        signal: signal(),
        ...input.additionalOptions,
    });
}

// 将认证成功响应写入隐藏字段并提交 webauth 表单
export function returnSuccess(result) {
    document.getElementById("clientDataJSON").value = base64url.stringify(new Uint8Array(result.response.clientDataJSON), { pad: false });
    document.getElementById("authenticatorData").value = base64url.stringify(new Uint8Array(result.response.authenticatorData), { pad: false });
    document.getElementById("signature").value = base64url.stringify(new Uint8Array(result.response.signature), { pad: false });
    document.getElementById("credentialId").value = result.id;
    if (result.response.userHandle) {
        document.getElementById("userHandle").value = base64url.stringify(new Uint8Array(result.response.userHandle), { pad: false });
    }
    const rememberMe = document.getElementById("rememberMe");
    if (rememberMe) {
        const rememberMeInput = document.createElement("input");
        rememberMeInput.type = "hidden";
        rememberMeInput.name = "rememberMe";
        rememberMeInput.value = rememberMe.checked ? "on" : "off";
        document.getElementById("webauth").appendChild(rememberMeInput);
    }
    document.getElementById("webauth").requestSubmit();
}

// 将错误信息写入隐藏字段并提交表单
export function returnFailure(err) {
    document.getElementById("error").value = err;
    document.getElementById("webauth").requestSubmit();
}
