package com.tuiop.markethub.carts.cart;

import com.tuiop.markethub.carts.cart.item.CartItem;
import com.tuiop.markethub.carts.cart.item.CartItemRepository;
import com.tuiop.markethub.carts.dto.AddToCartRequest;
import com.tuiop.markethub.carts.dto.CartItemResponse;
import com.tuiop.markethub.carts.dto.CartResponse;
import com.tuiop.markethub.carts.exceptions.UserAlreadyOwnsCartException;
import com.tuiop.markethub.carts.mapper.CartItemMapper;
import com.tuiop.markethub.carts.mapper.CartMapper;
import com.tuiop.markethub.categories.exceptions.InactiveCategoryException;
import com.tuiop.markethub.common.exceptions.ResourceNotFoundException;
import com.tuiop.markethub.merchants.exceptions.InactiveMerchantException;
import com.tuiop.markethub.merchants.exceptions.MerchantNotVerifiedException;
import com.tuiop.markethub.products.Product;
import com.tuiop.markethub.products.ProductRepository;
import com.tuiop.markethub.products.exceptions.InactiveProductException;
import com.tuiop.markethub.products.exceptions.InsufficientStockException;
import com.tuiop.markethub.security.user.CustomUserDetails;
import com.tuiop.markethub.users.User;
import com.tuiop.markethub.users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;


@Service
@Slf4j
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class CartService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;

    private Cart createMyCart(CustomUserDetails principal) {

        User authenticatedUser = userRepository.findById(principal.getUserId()).orElseThrow(() ->
                new ResourceNotFoundException(User.class, principal.getUserId()));


        if (cartRepository.existsByUserId(principal.getUserId())) {
            throw new UserAlreadyOwnsCartException();
        }

        Cart newCart = Cart.builder()
                .user(authenticatedUser)
                .build();

        Cart savedCart = cartRepository.save(newCart);


        log.info("Created cart with id={} and the userId={} ", savedCart.getId(), savedCart.getUser().getId());


        return savedCart;
    }

    @Transactional(readOnly = true)
    public CartResponse getMyCart(CustomUserDetails principal) {
        User authenticatedUser = userRepository.findById(principal.getUserId()).orElseThrow(() ->
                new ResourceNotFoundException(User.class, principal.getUserId()));

        Cart myCart = cartRepository.findDetailedByUserId(authenticatedUser.getId()).orElseThrow(() -> new ResourceNotFoundException(Cart.class, "user.id", authenticatedUser.getId()));

        return cartMapper.toCartResponse(myCart);
    }


    // public method for controller
    @Transactional
    public CartItemResponse addProductToMyCart(AddToCartRequest request, CustomUserDetails principal) {

        return cartItemMapper.toCartItemResponse(addProductToCart(request.productId(), request.quantity(), principal));
    }


    // private method not for controller
    private CartItem addProductToCart(UUID productId, Integer quantity, CustomUserDetails principal) {


        User authenticatedUser = userRepository.findById(principal.getUserId()).orElseThrow(() ->
                new ResourceNotFoundException(User.class, principal.getUserId()));


        Product product = productRepository.findWithMerchantAndCategoryById(productId).orElseThrow(() -> new ResourceNotFoundException(Product.class, productId));

        if (!product.getCategory().getActive()) {
            throw new InactiveCategoryException(product.getCategory().getName());
        }

        if (!product.getMerchant().getActive()) {
            throw new InactiveMerchantException(product.getMerchant().getShopName());
        }

        if (!product.getActive()) {
            throw new InactiveProductException(product.getName());
        }

        if (!product.getMerchant().getVerified()) {
            throw new MerchantNotVerifiedException();
        }

        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException("Quantity can't be negative");
        }

        if (quantity > product.getStockQuantity()) {
            throw new InsufficientStockException(product.getName(), quantity, product.getStockQuantity());
        }
        Cart myCart = cartRepository.findDetailedByUserId(authenticatedUser.getId()).orElseGet(() -> createMyCart(principal));

        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductIdForUpdate(myCart.getId(), productId);

        //check if cart already contains order item of this product
        if (existingItem.isPresent()) {


            int requestedQuantity = Math.addExact(existingItem.get().getQuantity(), quantity);
            int availableQuantity = product.getStockQuantity();

            if (requestedQuantity > availableQuantity) {
                throw new InsufficientStockException(product.getName(), requestedQuantity, availableQuantity);
            }

            existingItem.get().increaseQuantity(quantity);
            return existingItem.get();
        }

        CartItem cartItem = CartItem.builder()
                .cart(myCart)
                .product(product)
                .quantity(quantity)
                .build();


        CartItem savedCartItem = cartItemRepository.save(cartItem);


        log.info("Added cart item id={} to the cart id={}", savedCartItem.getId(), myCart.getId());

        return cartItem;
    }


}
