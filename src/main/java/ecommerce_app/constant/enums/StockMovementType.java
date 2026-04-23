package ecommerce_app.constant.enums;

public enum StockMovementType {
    IN,          // stock added (import, return)
    OUT,         // stock removed (order, damage)
    ADJUSTMENT,  // manual correction
    RETURN       // customer return
}