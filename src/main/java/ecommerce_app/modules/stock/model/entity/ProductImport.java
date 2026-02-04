package ecommerce_app.modules.stock.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ecommerce_app.infrastructure.model.entity.UserAuditableEntity;
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

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product_imports")
public class ProductImport extends UserAuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @ManyToOne(
      fetch = FetchType.LAZY,
      optional = false,
      targetEntity = Product.class,
      cascade = CascadeType.ALL)
  @JoinColumn(name = "product_id", referencedColumnName = "id")
  @JsonIgnore
  private Product product;

  @Column(nullable = false, name = "quantity")
  private int quantity;

  @Column(nullable = false, name = "unit_price")
  private BigDecimal unitPrice;

  @Column(nullable = false, name = "total_amount")
  private BigDecimal totalAmount;

  @Column(name = "supplier_name")
  private String supplierName;

  @Column(name = "supplier_address")
  private String supplierAddress;

  @Column(name = "supplier phone")
  private String supplierPhone;

  @Column(name = "remark")
  private String remark;
}
