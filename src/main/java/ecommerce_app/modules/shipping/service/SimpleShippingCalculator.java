// ecommerce_app/modules/shipping/service/SimpleShippingCalculator.java
package ecommerce_app.modules.shipping.service;

import ecommerce_app.constant.enums.ShippingMethod;
import ecommerce_app.modules.address.model.entity.Address;
import ecommerce_app.modules.cart.model.entity.Cart;
import ecommerce_app.modules.cart.model.entity.CartItem;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SimpleShippingCalculator {

  public BigDecimal calculateShippingCost(ShippingMethod method, Cart cart, Address address) {
    // Simple calculation - can be enhanced later
    BigDecimal baseCost = method.getDefaultCostAsBigDecimal();

    // Add per item cost for STANDARD shipping
    if (method == ShippingMethod.STANDARD) {
      int totalItems = cart.getCartItems().stream().mapToInt(CartItem::getQuantity).sum();
      BigDecimal itemCost = BigDecimal.valueOf(totalItems * 0.50); // $0.50 per item
      baseCost = baseCost.add(itemCost);
    }

    return baseCost;
  }
}
