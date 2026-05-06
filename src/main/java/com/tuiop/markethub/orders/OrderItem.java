package com.tuiop.markethub.orders;


import com.tuiop.markethub.merchants.Merchant;
import com.tuiop.markethub.products.Product;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "order_items",
        indexes = {
                @Index(name = "idx_order_items_order_id", columnList = "order_id"),
                @Index(name = "idx_order_items_product_id", columnList = "product_id"),
                @Index(name = "idx_order_items_merchant_id", columnList = "merchant_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(name = "product_name_snapshot", nullable = false)
    private String productNameSnapshot;

    @Column(name = "merchant_name_snapshot", nullable = false)
    private String merchantNameSnapshot;

    @Column(name = "price_snapshot_cents", nullable = false)
    private Long priceSnapshotCents;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "total_price_snapshot_cents", nullable = false)
    private Long totalPriceSnapshotCents;

    @PrePersist
    protected void prePersist() {
        if (totalPriceSnapshotCents == null && priceSnapshotCents != null && quantity != null) {
            totalPriceSnapshotCents = Math.multiplyExact(priceSnapshotCents, quantity);
        }
    }

    public static OrderItem fromProduct(Product product, Integer quantity){

        if(quantity <= 0) throw new IllegalArgumentException("Quantity of products must be positive ");

        return OrderItem.builder()
                .product(product)
                .merchant(product.getMerchant())
                .productNameSnapshot(product.getName())
                .merchantNameSnapshot(product.getMerchant().getShopName())
                .priceSnapshotCents(product.getPriceCents())
                .quantity(quantity)
                .totalPriceSnapshotCents(Math.multiplyExact(quantity, product.getPriceCents()))
                .build();
    }

    public void assignOrder(Order order) {
        this.order = order;
    }
}