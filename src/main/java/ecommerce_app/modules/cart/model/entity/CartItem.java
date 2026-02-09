package ecommerce_app.modules.cart.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

@Table(
    name = "cart_items",
    indexes = {
      @Index(name = "idx_cart_item_cart", columnList = "cart_id"),
      @Index(name = "idx_cart_item_cart_product", columnList = "cart_id, product_id", unique = true)
    })
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(
      fetch = FetchType.LAZY,
      optional = false,
      targetEntity = Product.class,
      cascade = {CascadeType.MERGE, CascadeType.PERSIST})
  @JoinColumn(name = "product_id", referencedColumnName = "id", nullable = false)
  private Product product;

  @ManyToOne(
      fetch = FetchType.LAZY,
      optional = false,
      targetEntity = Cart.class,
      cascade = {CascadeType.MERGE, CascadeType.PERSIST})
  @JoinColumn(name = "cart_id", referencedColumnName = "id", nullable = false)
  @JsonIgnore
  private Cart cart;

  @Column(name = "quantity", nullable = false)
  private int quantity;

  @Column(nullable = false, name = "price")
  private BigDecimal price;

  public void increment() {
    quantity++;
  }

  public void decrement() {

    if (quantity > 0) quantity--;
  }
}
