package ecommerce_app.service.strategy;

import ecommerce_app.constant.enums.PaymentGateway;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PaymentStrategyFactory {

  private final Map<PaymentGateway, PaymentGatewayStrategy> strategies;

  public PaymentStrategyFactory(List<PaymentGatewayStrategy> strategyList) {
    this.strategies =
        strategyList.stream().collect(Collectors.toMap(PaymentGatewayStrategy::getGateway, s -> s));
  }

  public PaymentGatewayStrategy get(PaymentGateway gateway) {
    return strategies.get(gateway);
  }
}
