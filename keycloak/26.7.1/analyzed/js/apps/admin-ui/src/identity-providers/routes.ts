import type { AppRouteObject } from "../routes";
import { IdentityProviderRoute } from "./routes/IdentityProvider";
import { IdentityProviderKeycloakOidcRoute } from "./routes/IdentityProviderKeycloakOidc";
import { IdentityProviderOidcRoute } from "./routes/IdentityProviderOidc";
import { IdentityProviderSamlRoute } from "./routes/IdentityProviderSaml";
import { IdentityProviderSpiffeRoute } from "./routes/IdentityProviderSpiffe";
import { IdentityProviderKubernetesRoute } from "./routes/IdentityProviderKubernetes";
import { IdentityProviderDefaultTrustRoute } from "./routes/IdentityProviderDefaultTrust";
import { IdentityProvidersRoute } from "./routes/IdentityProviders";
import { IdentityProviderAddMapperRoute } from "./routes/AddMapper";
import { IdentityProviderEditMapperRoute } from "./routes/EditMapper";
import { IdentityProviderCreateRoute } from "./routes/IdentityProviderCreate";
import { IdentityProviderOAuth2Route } from "./routes/IdentityProviderOAuth2";
import { IdentityProviderJWTAuthorizationGrantRoute } from "./routes/IdentityProviderJWTAuthorizationGrant";

/** 身份提供方模块路由：列表、各协议详情（OIDC/SAML/OAuth2 等）、映射器与创建向导。 */
const routes: AppRouteObject[] = [
  IdentityProviderAddMapperRoute,
  IdentityProviderEditMapperRoute,
  IdentityProvidersRoute,
  IdentityProviderOidcRoute,
  IdentityProviderSamlRoute,
  IdentityProviderSpiffeRoute,
  IdentityProviderJWTAuthorizationGrantRoute,
  IdentityProviderKubernetesRoute,
  IdentityProviderDefaultTrustRoute,
  IdentityProviderKeycloakOidcRoute,
  IdentityProviderCreateRoute,
  IdentityProviderRoute,
  IdentityProviderOAuth2Route,
];

export default routes;
