package com.meta.accesscontrol.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class Role extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 20, unique = true)
    private String name;

    @Column(length = 255)
    private String description; // Reverted to description

    @ElementCollection(targetClass = Privilege.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "role_privileges", joinColumns = @JoinColumn(name = "role_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "privilege")
    private Set<Privilege> privileges = new HashSet<>();

    public Role(String name) {
        this.name = name;
    }
}