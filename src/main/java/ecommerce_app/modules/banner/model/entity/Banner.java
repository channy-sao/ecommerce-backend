package ecommerce_app.modules.banner.model.entity;

import ecommerce_app.constant.enums.BannerType;
import ecommerce_app.infrastructure.model.entity.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
    name = "banners",
    indexes = {
      @Index(name = "idx_banner_active", columnList = "active"),
      @Index(name = "idx_banner_display_order", columnList = "display_order")
    })
@SQLDelete(sql = "UPDATE banners SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class Banner extends SoftDeletableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(length = 150)
  private String title;

  @Column(nullable = false, length = 500)
  private String imageUrl;

  @Column(length = 500)
  private String redirectUrl;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private BannerType type; // PRODUCT, CATEGORY, URL

  @Column(nullable = false)
  private Boolean active = true;

  @Column(name = "display_order")
  private Integer displayOrder = 0;

  private LocalDateTime startAt;

  private LocalDateTime endAt;
}
