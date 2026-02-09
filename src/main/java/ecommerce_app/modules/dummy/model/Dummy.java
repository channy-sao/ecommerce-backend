package ecommerce_app.modules.dummy.model;

import ecommerce_app.infrastructure.model.entity.TimeAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "dummies")
public class Dummy extends TimeAuditableEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, name = "dummy_name", length = 100)
  private String name;

  @Column(nullable = false, name = "num_rows")
  private Long numRows = 0L;

  @Column(nullable = false, name = "dummy_description", length = 100)
  private String dummyDescription;

  @Column(nullable = false, name = "is_again")
  private boolean again = false;

}
