package ecommerce_app.modules.banner.model.entity;

import ecommerce_app.infrastructure.model.entity.TimeAuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "banners",
    indexes = {
      @Index(name = "idx_banner_active", columnList = "is_active"),
      @Index(name = "idx_banner_display_order", columnList = "display_order"),
      @Index(name = "idx_banner_start_date", columnList = "start_date"),
      @Index(name = "idx_banner_end_date", columnList = "end_date")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Banner extends TimeAuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(length = 500)
  private String description;

  @Column(name = "image", nullable = false, length = 500)
  private String image;

  @Column(name = "link_url", length = 500)
  private String linkUrl; // Where banner links to (product, category, external)

  @Column(name = "link_type", length = 50)
  private String linkType; // PRODUCT, CATEGORY, EXTERNAL, NONE

  @Column(name = "link_id")
  private Long linkId; // ID of product/category if applicable

  @Column(name = "is_active", nullable = false)
  @Builder.Default
  private Boolean isActive = true;

  @Column(name = "display_order", nullable = false)
  @Builder.Default
  private Integer displayOrder = 0;

  @Column(name = "start_date")
  private LocalDateTime startDate;

  @Column(name = "end_date")
  private LocalDateTime endDate;

  @Column(length = 50)
  private String position; // HOME_CAROUSEL, CATEGORY_TOP, etc.

  @Column(name = "background_color", length = 20)
  private String backgroundColor; // Hex color for mobile app
}
