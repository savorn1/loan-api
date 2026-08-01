package com.example.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Cached in Redis under session:<uuid> (see UserSessionStore) at login/refresh —
// the user's identity plus their flattened RBAC permission list. The JWT itself
// only carries id/username/role/uuid; this is where the (larger, more volatile)
// permission list lives so it can be revoked/updated without reissuing a token.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtUserClaims {

    private Long id;
    private String username;
    private String uuid;
    private String role;
    private List<String> permissions;
    private Long branchId;
    private String branchName;
}
