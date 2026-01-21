package ecommerce_app.modules.order.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ecommerce_app.constant.enums.OrderStatus;
import ecommerce_app.constant.enums.PaymentMethod;
import ecommerce_app.constant.enums.PaymentStatus;
import ecommerce_app.constant.enums.ShippingMethod;
import ecommerce_app.infrastructure.model.entity.AuditingEntity;
import ecommerce_app.infrastructure.model.entity.BaseAuditingEntity;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Table(name = "orders")
@Entity
public class Order extends AuditingEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
  @JsonIgnore
  private User user;

  @ManyToOne(
      fetch = FetchType.LAZY,
      optional = false,
      targetEntity = Cart.class,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "cart_id", nullable = false, referencedColumnName = "id")
  @JsonIgnore
  private Cart cart;

  @Column(name = "shipping_address_snapshot", columnDefinition = "TEXT")
  private String shippingAddressSnapshot;

  @Column(name = "shipping_method", nullable = false)
  @Enumerated(EnumType.STRING)
  private ShippingMethod shippingMethod = ShippingMethod.STANDARD;

  @Column(name = "shipping_cost", nullable = false)
  private BigDecimal shippingCost;

  /*
   * Final amount to pay for the entire order after discounts
   * Formula: totalAmount = subtotalAmount − discountAmount
   * Example: subtotalAmount = 500, discountAmount = 50 → totalAmount = 450
   */
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
      fetch = FetchType.LAZY,
      orphanRemoval = true,
      targetEntity = OrderItem.class,
      mappedBy = "order")
  @JsonIgnore
  private List<OrderItem> orderItems = new ArrayList<>();

  // Track if shipping was free due to promotion
  @Column(name = "shipping_discount")
  private BigDecimal shippingDiscount = BigDecimal.ZERO;

  /*
   * All items included in this order
   * Each item has its own subtotal, discount, and totalPrice
   */
  @Column(name = "promotion_code")
  private String promotionCode;

  /*
   * Total discount applied to the order
   * Could include item-level discounts + order-level discount
   * Example: sum of all OrderItem discountAmounts + order-level coupon discount
   */
  @Column(name = "discount_amount")
  private BigDecimal discountAmount = BigDecimal.ZERO;

  /*
   * Sum of all orderItems subtotals BEFORE discounts
   * Formula: subtotalAmount = sum(orderItem.subtotal)
   * Example: orderItems subtotal sum = 500 → subtotalAmount = 500
   */
  @Column(name = "subtotal_amount")
  private BigDecimal subtotalAmount = BigDecimal.ZERO;

  @Column(name = "order_date", nullable = false)
  private LocalDateTime orderDate = LocalDateTime.now();

  @OneToMany(
      mappedBy = "order",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private List<OrderStatusHistory> statusHistories = new ArrayList<>();
}
