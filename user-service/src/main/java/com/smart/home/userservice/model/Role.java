package com.smart.home.userservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="id")
    @Schema(description = "The database generated role ID")
    private Long roleId;

    @Enumerated(EnumType.STRING)
    @Column(name ="role_name", unique = true)
    @Schema(description = "Role name")
    private RoleType role;

    @Column(name = "description")
    @Schema(description = "Role description")
    private String description;

    public Role(RoleType role) {
        this.role = role;
    }
}
