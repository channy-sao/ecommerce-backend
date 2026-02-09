package ecommerce_app.modules.order.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ecommerce_app.modules.cart.model.entity.Cart;
import ecommerce_app.modules.product.model.entity.Product;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(
    name = "order_items",
    indexes = {
      @Index(name = "idx_order_item_order", columnList = "order_id"),
      @Index(name = "idx_order_item_product", columnList = "product_id")
    })
public class OrderItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(
      fetch = FetchType.LAZY,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE},
      targetEntity = Order.class,
      optional = false)
  @JoinColumn(name = "order_id", nullable = false, referencedColumnName = "id")
  private Order order;

  @ManyToOne(
      fetch = FetchType.LAZY,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE},
      targetEntity = Product.class,
      optional = false)
  @JoinColumn(name = "product_id", nullable = false, referencedColumnName = "id")
  private Product product;

  // How many units of this product were ordered
  @Column(nullable = false, name = "quantity")
  private Integer quantity;

  /*
   * Price before any discount
   * Formula: subtotal = originalPrice × quantity
   * Example: originalPrice = 50, quantity = 3 → subtotal = 150
   */
  @Column(nullable = false, name = "subtotal")
  private BigDecimal subtotal;

  @ManyToOne(
      fetch = FetchType.LAZY,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE},
      targetEntity = Cart.class,
      optional = false)
  @JoinColumn(name = "cart_id", nullable = false, referencedColumnName = "id")
  @JsonIgnore
  private Cart cart;

  // The applied promotion or coupon code, if any
  @Column(name = "promotion_code")
  private String promotionCode;

  /*
   * How much discount is applied to this item
   * Example: subtotal = 150, discountAmount = 30 → 30 off
   */
  @Column(name = "discount_amount")
  private BigDecimal discountAmount = BigDecimal.ZERO;

  /*
   * Unit price of the product at the time of order
   * Used to calculate subtotal
   * Example: 1 unit = 50
   */
  @Column(name = "original_price")
  private BigDecimal originalPrice;

  /*
   * Final price after discount
   * Formula: totalPrice = subtotal − discountAmount
   * Example: subtotal = 150, discountAmount = 30 → totalPrice = 120
   */
  @Column(name = "total_price")
  private BigDecimal totalPrice;
}
