package ecommerce_app.modules.cart.model.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import ecommerce_app.constant.enums.CartStatus;
import ecommerce_app.infrastructure.model.entity.BaseAuditingEntity;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
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
public class Cart extends BaseAuditingEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, name = "total")
  private BigDecimal total;

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

  @OneToOne private User user;

  public void addNewItem(Product product) {
    if (cartItems == null) {
      cartItems = new ArrayList<>();
    }

    boolean exists = cartItems.stream().anyMatch(item -> item.getProduct().equals(product));
    if (exists) throw new IllegalStateException("Already in cart");
    final CartItem cartItem =
        CartItem.builder()
            .cart(this)
            .product(product)
            .price(product.getPrice())
            .status(CartStatus.ACTIVE)
            .quantity(1)
            .build();
    cartItems.add(cartItem);
  }

  public void incrementItem(Product product) {
    CartItem item =
        cartItems.stream()
            .filter(i -> i.getProduct().equals(product))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Not in cart"));
    item.increment();
  }

  public void decrementItem(Product product) {
    Iterator<CartItem> it = cartItems.iterator();
    while (it.hasNext()) {
      CartItem item = it.next();
      if (item.getProduct().equals(product)) {
        item.decrement();
        if (item.getQuantity() <= 0) it.remove();
        return;
      }
    }
  }
}
