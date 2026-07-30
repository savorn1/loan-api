package com.example.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

// A named, admin-managed role a user can hold in addition to their base
// USER/ADMIN tier (see Role enum on SysUser) — e.g. "LOAN_OFFICER", "COLLECTOR".
// Named RbacRole (not Role) to avoid clashing with the existing Role enum,
// which stays untouched: nothing here changes how ROLE_ADMIN/ROLE_USER
// authorities or the X-User-Role header work.
@Entity
@Table(name = "role", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name"),
        @UniqueConstraint(columnNames = "code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RbacRole extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // Stable machine identifier, e.g. "ADMIN" — separate from the human-facing name.
    @Column(nullable = false)
    private String code;

    // Marks the role new users get by default. Nothing currently auto-assigns
    // it — reference data for a future self-registration/default-role flow.
    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private boolean isDefault = false;

    private String description;

    @ManyToMany
    @JoinTable(name = "role_permission",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"))
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

    @ManyToMany(mappedBy = "roles")
    @Builder.Default
    private Set<SysUser> users = new HashSet<>();
}
