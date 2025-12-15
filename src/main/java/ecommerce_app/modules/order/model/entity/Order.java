package ecommerce_app.modules.order.model.entity;

import ecommerce_app.constant.enums.OrderStatus;
import ecommerce_app.constant.enums.PaymentMethod;
import ecommerce_app.constant.enums.PaymentStatus;
import ecommerce_app.modules.cart.model.entity.Cart;
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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Table(name = "orders")
@Entity
public class Order {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(
      fetch = FetchType.LAZY,
      optional = false,
      targetEntity = User.class,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
  private User user;

  @ManyToOne(
      fetch = FetchType.LAZY,
      optional = false,
      targetEntity = Cart.class,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "cart_id", nullable = false, referencedColumnName = "id")
  private Cart cart;

  @Column(nullable = false, name = "total_amount")
  private BigDecimal totalAmount;

  @Column(nullable = false, length = 30, name = "order_status")
  @Enumerated(EnumType.STRING)
  private OrderStatus orderStatus;

  @Column(nullable = false, length = 30, name = "payment_method")
  @Enumerated(EnumType.STRING)
  private PaymentMethod paymentMethod;

  @Column(nullable = false, length = 30, name = "payment_status")
  @Enumerated(EnumType.STRING)
  private PaymentStatus paymentStatus;

  @OneToMany(
      cascade = CascadeType.ALL,
      fetch = FetchType.EAGER,
      orphanRemoval = true,
      targetEntity = OrderItem.class,
      mappedBy = "order")
  private List<OrderItem> orderItems = new ArrayList<>();
}
