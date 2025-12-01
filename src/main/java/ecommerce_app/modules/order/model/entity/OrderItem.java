package ecommerce_app.modules.order.model.entity;

import ecommerce_app.modules.cart.model.entity.Cart;
import ecommerce_app.modules.product.model.entity.Product;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "order_items")
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

  @Column(nullable = false, name = "quantity")
  private Integer quantity;

  @Column(nullable = false, name = "subtotal")
  private BigDecimal subtotal;

  @ManyToOne(
      fetch = FetchType.LAZY,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE},
      targetEntity = Cart.class,
      optional = false)
  @JoinColumn(name = "cart_id", nullable = false, referencedColumnName = "id")
  private Cart cart;
}
