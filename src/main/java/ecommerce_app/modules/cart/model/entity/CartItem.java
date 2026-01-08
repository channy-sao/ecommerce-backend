package ecommerce_app.modules.cart.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import ecommerce_app.constant.enums.CartStatus;
import ecommerce_app.modules.product.model.entity.Product;
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
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;

@Table(name = "cart_items")
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

  @CreatedBy
  @Column(name = "quantity", nullable = false)
  private int quantity;

  @Column(nullable = false, unique = true, name = "price")
  private BigDecimal price;

  @Column(name = "status", nullable = false, length = 25)
  @Enumerated(EnumType.STRING)
  private CartStatus status;


  public void increment() { quantity++; }
  public void decrement() { quantity--; }
}
