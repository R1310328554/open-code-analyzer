/*
 * Copyright 2016 Analytical Graphics, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.keycloak.authentication.authenticators.x509;

import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.ServicesLogger;

import org.jboss.logging.Logger;

import static org.keycloak.authentication.authenticators.util.AuthenticatorUtils.getDisabledByBruteForceEventError;

/**
 * @author <a href="mailto:pnalyvayko@agi.com">Peter Nalyvayko</a>
 * @version $Revision: 1 $
 *
 */
public class X509ClientCertificateAuthenticator extends AbstractX509ClientCertificateAuthenticator {

    /** 日志记录器。 */
    private final static Logger logger = Logger.getLogger(X509ClientCertificateAuthenticator.class);

    @Override
    public void close() {

    }

    @Override
    /** 校验证书链、提取用户身份、查找用户并可选展示身份确认页。 */
    public void authenticate(AuthenticationFlowContext context) {

        try {

            dumpContainerAttributes(context);

            X509Certificate[] certs = getCertificateChain(context);
            if (certs == null || certs.length == 0) {
                // 无客户端证书时跳过本步骤，继续后续认证流程
                logger.debug("[authenticate] x509 client certificate is not available for mutual SSL.");
                context.attempted();
                return;
            }

            saveX509CertificateAuditDataToAuthSession(context, certs[0]);
            recordX509CertificateAuditDataViaContextEvent(context);

            X509AuthenticatorConfigModel config = null;
            if (context.getAuthenticatorConfig() != null && context.getAuthenticatorConfig().getConfig() != null) {
                config = new X509AuthenticatorConfigModel(context.getAuthenticatorConfig());
            }
            if (config == null) {
                logger.warn("[authenticate] x509 Client Certificate Authentication configuration is not available.");
                context.challenge(createInfoResponse(context, "X509 client authentication has not been configured yet"));
                context.attempted();
                return;
            }

            // 校验 X509 客户端证书（信任链、时效、密钥用途、策略与吊销状态）
            try {
                CertificateValidator.CertificateValidatorBuilder builder = certificateValidationParameters(context.getSession(), config);
                CertificateValidator validator = builder.build(certs);
                validator.validateTrust()
                         .validateTimestamps()
                         .validateKeyUsage()
                         .validateExtendedKeyUsage()
                         .validatePolicy()
                         .checkRevocationStatus();
            } catch(Exception e) {
                logger.error(e.getMessage(), e);
                // TODO：使用特定 locale 加载本地化错误消息
                String errorMessage = "Certificate validation's failed.";
                // TODO：form().setErrors 是否足以在登录页展示错误
                context.challenge(createErrorResponse(context, certs[0].getSubjectDN().getName(),
                        errorMessage, "Certificate revoked or incorrect."));
                context.attempted();
                return;
            }

            Object userIdentity = getUserIdentityExtractor(config).extractUserIdentity(certs);
            if (userIdentity == null) {
                context.getEvent().error(Errors.INVALID_USER_CREDENTIALS);
                logger.warnf("[authenticate] Unable to extract user identity from certificate.");
                // TODO use specific locale to load error messages
                String errorMessage = "Unable to extract user identity from specified certificate";
                // TODO is calling form().setErrors enough to show errors on login screen?
                context.challenge(createErrorResponse(context, certs[0].getSubjectDN().getName(), errorMessage));
                context.attempted();
                return;
            }

            UserModel user;
            try {
                context.getEvent().detail(Details.USERNAME, userIdentity.toString());
                context.getAuthenticationSession().setAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME, userIdentity.toString());
                user = getUserIdentityToModelMapper(config).find(context, userIdentity);
            }
            catch(ModelDuplicateException e) {
                ServicesLogger.LOGGER.modelDuplicateException(e);
                String errorMessage = "X509 certificate authentication's failed.";
                // TODO is calling form().setErrors enough to show errors on login screen?
                context.challenge(createErrorResponse(context, certs[0].getSubjectDN().getName(),
                        errorMessage, e.getMessage()));
                context.attempted();
                return;
            }

            if (invalidUser(context, user)) {
                // TODO use specific locale to load error messages
                String errorMessage = "X509 certificate authentication's failed.";
                // TODO is calling form().setErrors enough to show errors on login screen?
                context.challenge(createErrorResponse(context, certs[0].getSubjectDN().getName(),
                        errorMessage, "Invalid user"));
                context.attempted();
                return;
            }

            String bruteForceError = getDisabledByBruteForceEventError(context, user);
            if (bruteForceError != null) {
                context.getEvent().user(user);
                context.getEvent().error(bruteForceError);
                // TODO use specific locale to load error messages
                String errorMessage = "X509 certificate authentication's failed.";
                // TODO is calling form().setErrors enough to show errors on login screen?
                context.challenge(createErrorResponse(context, certs[0].getSubjectDN().getName(),
                        errorMessage, "Invalid user"));
                context.attempted();
                return;
            }

            if (!userEnabled(context, user)) {
                // TODO use specific locale to load error messages
                String errorMessage = "X509 certificate authentication's failed.";
                // TODO is calling form().setErrors enough to show errors on login screen?
                context.challenge(createErrorResponse(context, certs[0].getSubjectDN().getName(),
                        errorMessage, "User is disabled"));
                context.attempted();
                return;
            }
            context.setUser(user);

            // 判断是否展示证书身份确认页
            if (!config.getConfirmationPageDisallowed()) {
                // 须用 forceChallenge 展示确认页，让用户接受证书身份或改用用户名密码登录
                context.forceChallenge(createSuccessResponse(context, certs[0].getSubjectDN().toString()));
                // 暂不标记成功，等待用户在确认页做出选择
            }
            else {
                // 跳过确认页直接登录
                context.success(UserCredentialModel.CLIENT_CERT);
            }
        }
        catch(Exception e) {
            logger.errorf(e, "[authenticate] Exception: %s", e.getMessage());
            context.attempted();
        }
    }

    /** 构建证书认证失败时的 X509 确认/错误页响应。 */
    private Response createErrorResponse(AuthenticationFlowContext context,
                                         String subjectDN,
                                         String errorMessage,
                                         String ... errorParameters) {

        return createResponse(context, subjectDN, false, errorMessage, errorParameters);
    }

    /** 构建证书认证成功时的 X509 确认页响应。 */
    private Response createSuccessResponse(AuthenticationFlowContext context,
                                           String subjectDN) {
        return createResponse(context, subjectDN, true, null, null);
    }

    /** 组装 X509 确认页表单数据与错误消息。 */
    private Response createResponse(AuthenticationFlowContext context,
                                         String subjectDN,
                                         boolean isUserEnabled,
                                         String errorMessage,
                                         Object[] errorParameters) {

        LoginFormsProvider form = context.form();
        if (errorMessage != null && errorMessage.trim().length() > 0) {
            List<FormMessage> errors = new LinkedList<>();

            errors.add(new FormMessage(errorMessage));
            if (errorParameters != null) {

                for (Object errorParameter : errorParameters) {
                    if (errorParameter == null) continue;
                    for (String part : errorParameter.toString().split("\n")) {
                        errors.add(new FormMessage(part));
                    }
                }
            }
            form.setErrors(errors);
        }

        MultivaluedMap<String,String> formData = new MultivaluedHashMap<>();
        formData.add("username", context.getUser() != null ? context.getUser().getUsername() : "unknown user");
        formData.add("subjectDN", subjectDN);
        formData.add("isUserEnabled", String.valueOf(isUserEnabled));

        form.setFormData(formData);

        return form.createX509ConfirmPage();
    }

    /** 调试输出会话容器属性名。 */
    private void dumpContainerAttributes(AuthenticationFlowContext context) {

        Map<String, Object> attributeNames = context.getSession().getAttributes();
        for (String name : attributeNames.keySet()) {
            logger.tracef("[dumpContainerAttributes] \"%s\"", name);
        }
    }

    /** 检查用户是否已启用。 */
    private boolean userEnabled(AuthenticationFlowContext context, UserModel user) {
        if (!user.isEnabled()) {
            context.getEvent().user(user);
            context.getEvent().error(Errors.USER_DISABLED);
            return false;
        }
        return true;
    }

    /** 检查用户是否存在。 */
    private boolean invalidUser(AuthenticationFlowContext context, UserModel user) {
        if (user == null) {
            context.getEvent().error(Errors.USER_NOT_FOUND);
            return true;
        }
        return false;
    }

    @Override
    /** 处理用户在 X509 确认页的提交（确认或取消）。 */
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        if (formData.containsKey("cancel")) {
            context.clearUser();
            context.attempted();
            return;
        }
        if (context.getUser() != null) {
            recordX509CertificateAuditDataViaContextEvent(context);
            context.success(UserCredentialModel.CLIENT_CERT);
            return;
        }
        context.attempted();
    }
}
