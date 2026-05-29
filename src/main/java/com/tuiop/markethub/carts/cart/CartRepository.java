package com.tuiop.markethub.carts.cart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {

    @Query("""
    select distinct c
    from Cart c
    join fetch c.user
    left join fetch c.cartItems ci
    left join fetch ci.product p
    where c.user.id = :id
    """)
    Optional<Cart> findDetailedByUserId(@Param("id") UUID id);

    boolean existsByUserId(UUID id);

    Optional<Cart> findByUserId(UUID userId);
}
