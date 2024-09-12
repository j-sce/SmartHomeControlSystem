package com.smart.home.userservice.model;

import com.smart.home.userservice.swagger.DescriptionVariables;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Schema(description = "The database generated user ID")
    @Range(min = 1, max = Long.MAX_VALUE, message = DescriptionVariables.MODEL_ID_RANGE)
    private Long userId;

    @Column(name = "username", nullable = false, unique = true)
    @Schema(description = "Username")
    @NotBlank(message = "Username " + DescriptionVariables.NOT_BLANK)
    @Size(min = 1, max = 50, message = DescriptionVariables.USERNAME_SIZE)
    private String username;

    @Column(name = "password", nullable = false)
    @Schema(description = "Password")
    @NotBlank(message = "Password " + DescriptionVariables.NOT_BLANK)
    @Size(min = 8, max = 100, message = DescriptionVariables.PASSWORD_SIZE)
    private String password;

    @Column(name = "email")
    @Schema(description = "Email")
    @NotBlank(message = "Email must not be null")
    @Email(message = DescriptionVariables.EMAIL_REGEX, regexp = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$")
    private String email;

    @Column(name = "created_at")
    @Schema(description = "Time of user creation")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @Schema(description = "Time of last user update")
    private LocalDateTime updatedAt;

    @Column(name = "role")
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "user_role", joinColumns = {
            @JoinColumn(name = "user_id") }, inverseJoinColumns = {
            @JoinColumn(name = "role_id") })
    @Schema(description = "Roles of user")
    private Set<Role> roles;

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
