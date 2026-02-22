package ecommerce_app.service.strategy; // ecommerce_app/modules/promotion/factory/ecommerce_app.service.strategy.PromotionStrategyFactory.java

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PromotionStrategyFactory {

  private final Map<String, PromotionStrategy> strategies;

  public PromotionStrategy getStrategy(String strategyType) {
    PromotionStrategy strategy = strategies.get(strategyType);
    if (strategy == null) {
      throw new IllegalArgumentException("Unsupported promotion strategy: " + strategyType);
    }
    return strategy;
  }

  public boolean supports(String strategyType) {
    return strategies.containsKey(strategyType);
  }
}
