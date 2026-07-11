package com.fahim1837.auth_gateway.user;

import java.util.UUID;
import java.time.OffsetDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Entity
@Table(name = "user", schema = "public")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String firstName;

    private String lastName;

    @NotEmpty
    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @NotEmpty
    @Column(unique = true, nullable = false)
    private String username;

    private String password;

    @CreationTimestamp
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    @CreatedBy
    @ManyToOne
    @JoinColumn(name = "created_by", updatable = false)
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "updated_by", updatable = false)
    @LastModifiedBy
    private User updatedBy;

    public boolean checkPassword(String password) {
        var encoder = new BCryptPasswordEncoder();
        boolean isPasswordOkay = encoder.matches(password, this.password);
        return isPasswordOkay;
    }

    public void setPassword(String password) {
        var encoder = new BCryptPasswordEncoder(12);
        this.password = encoder.encode(password);
    }
}
