package ecommerce_app.modules.cart.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ecommerce_app.constant.enums.CartStatus;
import ecommerce_app.infrastructure.exception.BadRequestException;
import ecommerce_app.infrastructure.model.entity.TimeAuditableEntity;
import ecommerce_app.modules.order.model.entity.OrderItem;
import ecommerce_app.modules.product.model.entity.Product;
import ecommerce_app.modules.user.model.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Table(
    name = "carts",
    indexes = {@Index(columnList = "uuid", name = "cart_uuid_index")})
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart extends TimeAuditableEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "status", nullable = false, length = 25)
  @Enumerated(EnumType.STRING)
  private CartStatus status;

  @Column(nullable = false, unique = true, name = "uuid")
  private UUID uuid = UUID.randomUUID();

  @OneToMany(
      mappedBy = "cart",
      targetEntity = CartItem.class,
      cascade = CascadeType.ALL,
      fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private List<CartItem> cartItems;

  @OneToMany(
      mappedBy = "cart",
      targetEntity = OrderItem.class,
      cascade = CascadeType.ALL,
      fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private List<OrderItem> orderItems;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  @JsonIgnoreProperties({"cart", "addresses", "orders", "roles"}) // Use @JsonIgnoreProperties
  private User user;

  public void addNewItem(Product product) {
    if (cartItems == null) {
      cartItems = new ArrayList<>();
    }

    boolean exists = cartItems.stream().anyMatch(item -> item.getProduct().equals(product));
    if (exists) throw new BadRequestException("Product is already in cart");
    final CartItem cartItem =
        CartItem.builder()
            .cart(this)
            .product(product)
            .price(product.getPrice())
            .quantity(1)
            .build();
    cartItems.add(cartItem);
  }

  public void removeItemById(Long itemId) {
    cartItems.removeIf(item -> item.getId().equals(itemId)); // Remove by itemId
  }

  // Total price in the cart
  public BigDecimal getTotal() {
    if (cartItems == null || cartItems.isEmpty()) {
      return BigDecimal.ZERO;
    }
    return cartItems.stream()
        .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
