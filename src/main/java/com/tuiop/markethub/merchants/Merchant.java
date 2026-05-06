package com.tuiop.markethub.merchants;

import com.tuiop.markethub.users.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "merchants",
        indexes = {
                @Index(name = "idx_merchants_user_id", columnList = "user_id"),
                @Index(name = "idx_merchants_shop_name", columnList = "shop_name")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "shop_name", nullable = false, unique = true)
    private String shopName;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private Boolean verified;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void prePersist() {

        createdAt = Instant.now();
        updatedAt = Instant.now();

        if (verified == null) {
            verified = false;
        }

        if (active == null) {
            active = true;
        }


    }

    @PreUpdate
    protected void preUpdate() {
        updatedAt = Instant.now();
    }

    public void disable() {
        this.active = false;
    }

    public void verify() {
        this.verified = true;
    }


    public void enable() {
        this.active = true;
    }

}