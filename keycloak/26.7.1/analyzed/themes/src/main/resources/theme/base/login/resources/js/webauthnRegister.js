// WebAuthn 凭证注册：创建公钥凭证并将 attestation 等数据提交服务端
import { base64url } from "rfc4648";

// 注册入口：组装 PublicKeyCredentialCreationOptions 并调用 credentials.create
export async function registerByWebAuthn(input) {

    // 检查浏览器是否支持 WebAuthn
    if (!window.PublicKeyCredential) {
        returnFailure(input.errmsg);
        return;
    }

    const publicKey = {
        challenge: base64url.parse(input.challenge, {loose: true}),
        rp: {id: input.rpId, name: input.rpEntityName},
        user: {
            id: base64url.parse(input.userid, {loose: true}),
            name: input.username,
            displayName: input.username
        },
        pubKeyCredParams: getPubKeyCredParams(input.signatureAlgorithms),
    };

    if (input.attestationConveyancePreference !== 'not specified') {
        publicKey.attestation = input.attestationConveyancePreference;
    }

    const authenticatorSelection = {};
    let isAuthenticatorSelectionSpecified = false;

    if (input.authenticatorAttachment !== 'not specified') {
        authenticatorSelection.authenticatorAttachment = input.authenticatorAttachment;
        isAuthenticatorSelectionSpecified = true;
    }

    if (input.residentKey && input.residentKey !== 'not specified') {
        // residentKey 为当前规范字段；requireResidentKey 已弃用但仍为旧客户端设置
        authenticatorSelection.residentKey = input.residentKey;
        authenticatorSelection.requireResidentKey = input.residentKey === 'required';
        isAuthenticatorSelectionSpecified = true;
    } else if (input.requireResidentKey !== 'not specified') {
        // 未指定 residentKey 时回退到已弃用的 requireResidentKey 选项
        if (input.requireResidentKey === 'Yes') {
            authenticatorSelection.residentKey = 'required';
            authenticatorSelection.requireResidentKey = true;
        } else {
            authenticatorSelection.residentKey = 'discouraged';
            authenticatorSelection.requireResidentKey = false;
        }
        isAuthenticatorSelectionSpecified = true;
    }

    if (input.userVerificationRequirement !== 'not specified') {
        authenticatorSelection.userVerification = input.userVerificationRequirement;
        isAuthenticatorSelectionSpecified = true;
    }

    if (isAuthenticatorSelectionSpecified) {
        publicKey.authenticatorSelection = authenticatorSelection;
    }

    if (input.createTimeout !== 0) {
        publicKey.timeout = input.createTimeout * 1000;
    }

    const excludeCredentials = getExcludeCredentials(input.excludeCredentialIds);
    if (excludeCredentials.length > 0) {
        publicKey.excludeCredentials = excludeCredentials;
    }

    try {
        const result = await doRegister(publicKey);
        returnSuccess(result, input.initLabel, input.initLabelPrompt);
    } catch (error) {
        returnFailure(error);
    }
}

// 调用浏览器 API 创建凭证
// 调用浏览器 API 创建凭证
function doRegister(publicKey) {
    return navigator.credentials.create({publicKey});
}

// 将签名算法列表转换为 pubKeyCredParams；空列表时默认 ES256 (-7)
// 将签名算法列表转换为 pubKeyCredParams；空列表时默认 ES256 (-7)
function getPubKeyCredParams(signatureAlgorithmsList) {
    const pubKeyCredParams = [];
    if (signatureAlgorithmsList.length === 0) {
        pubKeyCredParams.push({type: "public-key", alg: -7});
        return pubKeyCredParams;
    }

    for (const entry of signatureAlgorithmsList) {
        pubKeyCredParams.push({
            type: "public-key",
            alg: entry
        });
    }

    return pubKeyCredParams;
}

// 解析逗号分隔的凭证 ID 列表为 excludeCredentials
// 解析逗号分隔的凭证 ID 列表为 excludeCredentials
function getExcludeCredentials(excludeCredentialIds) {
    const excludeCredentials = [];
    if (excludeCredentialIds === "") {
        return excludeCredentials;
    }

    for (const entry of excludeCredentialIds.split(',')) {
        excludeCredentials.push({
            type: "public-key",
            id: base64url.parse(entry, {loose: true})
        });
    }

    return excludeCredentials;
}

// 将 authenticator transports 数组转为逗号分隔字符串
// 将 authenticator transports 数组转为逗号分隔字符串
function getTransportsAsString(transportsList) {
    if (!Array.isArray(transportsList)) {
        return "";
    }

    return transportsList.join();
}

// 回填注册成功字段、提示用户输入凭证标签并提交 register 表单
// 回填注册成功字段、提示用户输入凭证标签并提交 register 表单
function returnSuccess(result, initLabel, initLabelPrompt) {
    document.getElementById("clientDataJSON").value = base64url.stringify(new Uint8Array(result.response.clientDataJSON), {pad: false});
    document.getElementById("attestationObject").value = base64url.stringify(new Uint8Array(result.response.attestationObject), {pad: false});
    document.getElementById("publicKeyCredentialId").value = base64url.stringify(new Uint8Array(result.rawId), {pad: false});

    if (typeof result.response.getTransports === "function") {
        const transports = result.response.getTransports();
        if (transports) {
            document.getElementById("transports").value = getTransportsAsString(transports);
        }
    } else {
        console.log("Your browser is not able to recognize supported transport media for the authenticator.");
    }

    if (result.authenticatorAttachment) {
        document.getElementById("authenticatorAttachment").value = result.authenticatorAttachment;
    }

    let labelResult = window.prompt(initLabelPrompt, initLabel);
    if (labelResult === null) {
        labelResult = initLabel;
    }
    document.getElementById("authenticatorLabel").value = labelResult;

    document.getElementById("register").requestSubmit();
}

// 将错误写入隐藏字段并提交 register 表单
// 将错误写入隐藏字段并提交 register 表单
function returnFailure(err) {
    document.getElementById("error").value = err;
    document.getElementById("register").requestSubmit();
}
