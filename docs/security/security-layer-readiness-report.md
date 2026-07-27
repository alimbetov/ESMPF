# ESMPF security-layer readiness report

## Decision

JWT is intentionally minimal and identifies only the user:

```text
sub = userId
```

`businessId`, roles and permissions are not trusted token claims. After JWT validation the server loads `UserAccount` and resolves current access data server-side.

## Service inventory

The project exposes 13 public application-service contracts:

1. `CustomerService`
2. `CustomerInteractionService`
3. `CatalogService`
4. `EquipmentService`
5. `MaintenanceService`
6. `ServiceManagementService`
7. `ServiceSupportService`
8. `IdentityService`
9. `CommercialService`
10. `DocumentService`
11. `CommunicationService`
12. `PlatformService`
13. `ContentService`

The existing architecture test proves that public service contracts do not accept `tenantId` or `businessId` from client commands and do not expose internal domain entity types.

## JWT contract

Mandatory claims:

- `iss`
- `aud`
- `sub` — internal user UUID
- `iat`
- `nbf`
- `exp`
- `jti`
- `token_type=access`
- `ver=1`

The JWT does not contain `business_id`, roles or permissions. This avoids stale authorization data and prevents token claims from becoming the source of truth for application access.

## Google Identity adaptation

Google is used only to prove an external identity. ESMPF always issues its own JWT after successful Google verification.

```text
Google credential
  -> GoogleIdentityVerifier
  -> verified Google subject and email
  -> existing active ESMPF UserAccount
  -> optional first-login link GOOGLE + subject
  -> ESMPF JWT with sub = internal userId
```

The service foundation intentionally uses a simple pre-provisioned policy:

- unknown Google accounts are rejected;
- automatic user registration is not performed;
- the first login may link an existing active user by verified normalized email;
- subsequent logins resolve the user by stable Google `sub`;
- an already linked user cannot be silently rebound to another Google subject.

Application contracts prepared for future controllers:

- `AuthenticationService.signInWithGoogle(...)`;
- `GoogleIdentityVerifier`;
- `AuthenticationUserGateway`;
- `GoogleSignInCommand`;
- `AuthenticationResponse`.

A future controller only receives the Google Identity Services `credential`, builds `GoogleSignInCommand`, calls `AuthenticationService` and returns `AuthenticationResponse`. It must not decode or trust Google token fields itself.

## Runtime resolution

```text
validated ESMPF JWT
  -> userId
  -> UserAccount lookup
  -> active/status validation
  -> roles and effective permissions resolution
  -> AuthenticatedActor
```

`AuthenticatedActor` is a server-side runtime object containing `userId`, roles and permissions.

## Deliberate boundary

This foundation does not yet:

- create Spring MVC controllers;
- install a servlet bearer-token filter;
- enable `SecurityFilterChain`;
- implement password login;
- provide a concrete Google SDK adapter;
- enforce `@PreAuthorize`;
- auto-register unknown Google users.

The next controller stage should add a concrete Google verifier bean configured with the Google Web Client ID, an `AuthenticationUserGateway` adapter over the identity persistence model, and the HTTP endpoint without changing the application service contract.
