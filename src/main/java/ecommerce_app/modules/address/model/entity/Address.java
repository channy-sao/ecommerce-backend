package ecommerce_app.modules.address.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ecommerce_app.modules.user.model.entity.User;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(
    name = "addresses",
    indexes = {
      @Index(name = "idx_address_user", columnList = "user_id"),
      @Index(name = "idx_address_user_default", columnList = "user_id, is_default")
    })
@ToString(exclude = "user") // Exclude relationship
public class Address {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "street", nullable = false, length = 100)
  private String street;

  @Column(name = "city", nullable = false, length = 100)
  private String city;

  @Column(name = "state", length = 100)
  private String state;

  @Column(name = "zip", length = 50)
  private String zip;

  @Column(name = "country", nullable = false, length = 100)
  private String country;

  @Column(name = "latitude", nullable = false)
  private double latitude;

  @Column(name = "longitude", nullable = false)
  private double longitude;

  @Column(name = "is_default", nullable = false)
  private boolean isDefault;

  @Column(name = "line1", length = 50)
  private String line1;

  @Column(name = "line2", length = 50)
  private String line2;

  @Column(name = "postal_code", length = 100)
  private String postalCode;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
  @JsonIgnore
  private User user;
}
