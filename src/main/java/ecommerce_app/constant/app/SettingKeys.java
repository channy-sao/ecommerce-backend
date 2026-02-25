package ecommerce_app.constant.app;

public final class SettingKeys {

  private SettingKeys() {}

  // ── Store (8) ─────────────────────────────────────────────────────────────
  public static final String STORE_NAME = "store.name";
  public static final String STORE_EMAIL = "store.email";
  public static final String STORE_PHONE = "store.phone";
  public static final String STORE_ADDRESS = "store.address";
  public static final String STORE_CURRENCY = "store.currency"; // USD | KHR
  public static final String STORE_LOGO_URL = "store.logo_url";
  public static final String STORE_FACEBOOK_URL = "store.facebook_url";
  public static final String STORE_TELEGRAM_URL = "store.telegram_url";
  public static final String STORE_WORKING_HOURS = "store.working_hours"; // e.g. "8:00 AM - 11:30 PM"
  public static final String STORE_LATITUDE = "store.location.latitude";
  public static final String STORE_LONGITUDE = "store.location.longitude";
  public static final String STORE_OPEN_AT = "store.open_at"; // e.g. "8:00 AM"
  public static final String STORE_CLOSE_AT = "store.close_at"; // e.g. "
  public static final String STORE_WEBSITE = "store.website";

  // ── Payment (6) ───────────────────────────────────────────────────────────
  public static final String BAKONG_MERCHANT_ID = "payment.bakong.merchant_id";
  public static final String BAKONG_MERCHANT_NAME = "payment.bakong.merchant_name";
  public static final String BAKONG_CITY = "payment.bakong.city";
  public static final String BAKONG_ENABLED = "payment.bakong.enabled";
  public static final String STRIPE_PUBLISHABLE_KEY = "payment.stripe.publishable_key";
  public static final String STRIPE_ENABLED = "payment.stripe.enabled";

  // ── Order (6) ─────────────────────────────────────────────────────────────
  public static final String ORDER_AUTO_CANCEL_MINUTES = "order.auto_cancel_minutes";
  public static final String ORDER_LOW_STOCK_THRESHOLD = "order.low_stock_threshold";
  public static final String ORDER_MIN_AMOUNT = "order.min_amount";
  public static final String ORDER_MAX_ITEMS = "order.max_items";
  public static final String ORDER_NUMBER_PREFIX = "order.number_prefix";
  public static final String ORDER_ALLOW_GUEST = "order.allow_guest_checkout";

  // ── Shipping (4) ──────────────────────────────────────────────────────────
  public static final String SHIPPING_ENABLED = "shipping.enabled";
  public static final String SHIPPING_FLAT_RATE = "shipping.flat_rate";
  public static final String SHIPPING_FREE_THRESHOLD = "shipping.free_threshold";
  public static final String SHIPPING_LABEL = "shipping.label"; // e.g. "Delivery Fee"

  // ── Tax (2) ───────────────────────────────────────────────────────────────
  public static final String TAX_ENABLED = "tax.enabled";
  public static final String TAX_RATE = "tax.rate"; // percentage e.g. "10"

  // ── Notification (2) ──────────────────────────────────────────────────────
  public static final String NOTIFICATION_TELEGRAM_BOT_TOKEN = "notification.telegram.bot_token";
  public static final String NOTIFICATION_TELEGRAM_CHAT_ID = "notification.telegram.chat_id";

  // ── Media (2) ─────────────────────────────────────────────────────────────
  public static final String MEDIA_BASE_URL = "media.base_url";
  public static final String MEDIA_MAX_FILE_SIZE_MB = "media.max_file_size_mb";
}
