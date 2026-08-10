// 通行密钥（Passkey）条件式/模态中介自动认证入口
import { doAuthenticate, returnSuccess } from "./webauthnAuthenticate.js";

// sessionStorage 键：记录用户在本认证会话中已关闭模态对话框
const PASSKEY_MODAL_DISMISSED = 'kc_passkey_modal_dismissed';

/**
 * 若存在则返回 KC_AUTH_SESSION_HASH Cookie 值，否则为 undefined。
 */
function getModalDismissedHash() {
    for (const cookie of document.cookie.split(';')) {
        const [key, value] = cookie.trim().split('=');
        if (key === 'KC_AUTH_SESSION_HASH' && value) {
            return value;
        }
    }
    return undefined;
}

/**
 * 页面加载时通行密钥认证的入口。
 *
 * 根据 WebAuthn 无密码策略配置的中介模式（conditional/none/optional/required/silent），
 * 调用一次 navigator.credentials.get()。
 * 模式为 none、浏览器不支持或用户已识别时不会自动尝试，用户仍可通过按钮手动发起。
 *
 * 模态中介（optional/required）下，每个认证会话最多弹出一次对话框；
 * 用户关闭后，后续页面加载（如密码失败后）不再重复弹出。
 */
export async function initAuthenticate(input, availableCallback = () => {}) {
    // 检查浏览器是否支持 WebAuthn
    if (!window.PublicKeyCredential) {
        // 条件式 UI 非必需，静默失败
        return;
    }

    const mediation = input.mediation ?? 'conditional';

    if (input.isUserIdentified || mediation === 'none') {
        availableCallback(false);
        return;
    }

    if (input.authenticatorAttachment === 'platform'
            && !await PublicKeyCredential.isUserVerifyingPlatformAuthenticatorAvailable()) {
        availableCallback(false);
        return;
    }

    // isConditionalMediationAvailable 仅对 conditional（自动填充）中介有意义
    if (mediation === 'conditional') {
        if (typeof PublicKeyCredential.isConditionalMediationAvailable === 'undefined') {
            availableCallback(false);
            return;
        }
        const isAvailable = await PublicKeyCredential.isConditionalMediationAvailable();
        if (!isAvailable) {
            // 条件式 UI 不可用时等同 none 模式
            availableCallback(false);
            return;
        }
        availableCallback(true);
    } else {
        availableCallback(false);
    }

    // 模态中介下，若用户已在本会话关闭过对话框则跳过，避免每次加载都打断用户
    const modalDismissedHash = getModalDismissedHash();
    if ((!modalDismissedHash || modalDismissedHash === sessionStorage.getItem(PASSKEY_MODAL_DISMISSED)) &&
            (mediation === 'optional' || mediation === 'required')) {
        return;
    }

    try {
        const result = await doAuthenticate({
            ...input,
            allowCredentials: [],
            additionalOptions: { mediation },
        });
        if (result) returnSuccess(result);
    } catch (err) {
        // 用户主动关闭模态框时记住状态，同一会话内不再弹出
        if ((mediation === 'optional' || mediation === 'required') &&
                (err?.name === 'NotAllowedError' || err?.name === 'AbortError')) {
            sessionStorage.setItem(PASSKEY_MODAL_DISMISSED, modalDismissedHash);
        }
    }
}
