# PR #19 — Security Runtime Hardening and Tenant Isolation Proofs

## Purpose

This slice hardens the JWT runtime introduced in PR #18 without adding a generic scope engine, permission cache, refresh-token subsystem or PostgreSQL RLS.

## Runtime guarantees

- Protected business APIs accept only `EsmpfPrincipal` created from validated JWT plus persisted access.
- A pre-existing generic Spring `Authentication` cannot bypass bearer validation.
- If an Authorization header is present, its bearer token is always parsed and validated.
- Duplicate, oversized, malformed or control-character-bearing Authorization headers are rejected.
- Bearer scheme matching is case-insensitive; credentials remain strict single-token compact JWT values.
- Permission enforcement rejects authenticated principals that are not `EsmpfPrincipal`.
- Mobile-device object authorization is fail-closed for user execution.
- Trusted system execution must be explicit through `SecurityExecutionContext.ExecutionKind.SYSTEM`.

## HTTP semantics

- Missing authentication on protected API: `401`.
- Invalid bearer credentials: `401`.
- Authenticated but untrusted principal: `403`.
- Trusted principal without operation permission: `403`.
- Cross-tenant domain lookup: `404` through tenant-scoped application services.

## Deliberate exclusions

- refresh tokens and token blacklist;
- per-token immediate revocation;
- Redis permission cache;
- generic `scopeType/scopeId` metadata;
- JSON-backed technician authorization;
- service-to-service authentication;
- PostgreSQL RLS.

## Evidence

The PR contains regression tests for:

- persisted principal construction;
- generic pre-authentication rejection;
- bearer overriding foreign authentication;
- duplicate Authorization headers;
- malformed bearer credentials;
- trusted-principal-only permission enforcement;
- fail-closed mobile-device object policy;
- explicit trusted system execution.
