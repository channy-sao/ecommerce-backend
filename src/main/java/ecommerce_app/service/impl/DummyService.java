package ecommerce_app.service.impl;

import com.github.javafaker.Faker;
import ecommerce_app.constant.enums.AuthProvider;
import ecommerce_app.constant.enums.BannerLinkType;
import ecommerce_app.constant.enums.PaymentMethod;
import ecommerce_app.constant.enums.PromotionType;
import ecommerce_app.constant.enums.ShippingMethod;
import ecommerce_app.core.io.service.StorageConfig;
import ecommerce_app.entity.Banner;
import ecommerce_app.entity.Promotion;
import ecommerce_app.exception.BadRequestException;
import ecommerce_app.exception.ResourceNotFoundException;
import ecommerce_app.entity.Address;
import ecommerce_app.property.StorageConfigProperty;
import ecommerce_app.repository.AddressRepository;
import ecommerce_app.repository.BannerRepository;
import ecommerce_app.repository.PromotionRepository;
import ecommerce_app.service.CartService;
import ecommerce_app.entity.Category;
import ecommerce_app.repository.CategoryRepository;
import ecommerce_app.entity.Dummy;
import ecommerce_app.repository.DummyRepository;
import ecommerce_app.dto.request.CheckoutRequest;
import ecommerce_app.service.OrderService;
import ecommerce_app.entity.Product;
import ecommerce_app.entity.ProductImage;
import ecommerce_app.repository.ProductRepository;
import ecommerce_app.dto.request.ProductImportRequest;
import ecommerce_app.service.ProductImportService;
import ecommerce_app.entity.Permission;
import ecommerce_app.entity.Role;
import ecommerce_app.entity.User;
import ecommerce_app.repository.PermissionRepository;
import ecommerce_app.repository.RoleRepository;
import ecommerce_app.repository.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import ecommerce_app.util.ImageDownloadUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class DummyService {
  private final DummyRepository dummyRepository;
  private final AddressRepository addressRepository;
  private final CartService cartService;
  private final ProductImportService productImportService;
  private final CategoryRepository categoryRepository;
  private final RoleRepository roleRepository;
  private final UserRepository userRepository;
  private final PermissionRepository permissionRepository;
  private final ProductRepository productRepository;
  private final PasswordEncoder passwordEncoder;
  private final OrderService orderService;
  private final StorageConfig storageConfig;
  private final PromotionRepository promotionRepository;
  private final BannerRepository bannerRepository;
  private final Faker faker = new Faker();

  // dummy 15 rows
  public void dummyRole() {
    log.info("Start dummy role");
    if (dummyRepository.existsByNameAndAgainFalse("Dummy Roles")) {
      log.info("Dummy Roles already exist");
      return;
    }
    var permissions = permissionRepository.findAll();
    for (int i = 0; i < 15; i++) {
      Role role = new Role();
      try {
        role.setName(faker.job().position());
        role.setDescription(faker.lorem().fixedString(100));

        var numOfPermission = faker.number().numberBetween(1, 10);
        Set<Permission> permissionSet = new HashSet<>();
        for (int j = 0; j < numOfPermission; j++) {
          int permissionIndex = faker.number().numberBetween(0, permissions.size() - 1);
          permissionSet.add(permissions.get(permissionIndex));
        }

        role.setPermissions(permissionSet);
        role.setUid(UUID.randomUUID().toString());
        roleRepository.save(role);
      } catch (DataIntegrityViolationException _) {
        // maybe duplicate name
        log.warn("existing role name {} , Now resolved it", role.getName());
        role.setName(faker.job().position() + "-" + faker.job().keySkills());
        roleRepository.save(role);
      }
    }
    Dummy dummy = new Dummy();
    dummy.setName("Dummy Roles");
    dummy.setNumRows(15L);
    dummy.setDummyDescription("Dummy Roles 15 rows");
    dummyRepository.save(dummy);
    log.info("Finish dummy role");
  }

  // dummy 50 rows
  public void dummyUser() {
    log.info("Start dummy user");
    if (dummyRepository.existsByNameAndAgainFalse("Dummy Users")) {
      log.info("Dummy Users already exist");
      return;
    }
    final var defaultUserPassword = "password";
    var roles = roleRepository.findAll();
    for (int i = 0; i < 50; i++) {
      var user = new User();

      try {
        Set<Role> roleSet = new HashSet<>();
        final var numOfRoles = faker.number().numberBetween(0, roles.size() - 1);
        for (int j = 0; j < numOfRoles; j++) {
          roleSet.add(roles.get(faker.number().numberBetween(0, roles.size() - 1)));
        }
        try {
          String imagePath =
              ImageDownloadUtils.downloadAndSave(
                  "https://picsum.photos/300/300", storageConfig.getAvatarPath());

          user.setAvatar(Path.of(imagePath).getFileName().toString());

        } catch (Exception e) {
          log.error("Failed to download image for user {}: {}", user.getEmail(), e.getMessage());
          user.setAvatar(null);
        }
        user.setEmail(faker.internet().emailAddress());
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setEmailVerifiedAt(null);
        user.setIsEmailVerified(false);
        user.setFirstName(faker.name().firstName());
        user.setLastName(faker.name().lastName());
        user.setIsActive(true);
        user.setRememberMe(false);
        user.setPassword(passwordEncoder.encode(defaultUserPassword));
        user.setRoles(roleSet);
        user.setUuid(UUID.randomUUID());
        userRepository.save(user);
      } catch (DataIntegrityViolationException _) {
        log.warn("Existing user name {} already exist , Now use difference", user.getEmail());
        user.setEmail(faker.name().firstName().concat(".").concat(faker.internet().emailAddress()));
        userRepository.save(user);
      }
    }
    Dummy dummy = new Dummy();
    dummy.setName("Dummy Users");
    dummy.setNumRows(50L);
    dummy.setDummyDescription("Dummy Users 50 rows");
    dummyRepository.save(dummy);
    log.info("Finish dummy user");
  }

  public void dummyRoleAndUser() {
    log.info("Start dummy role and user");
    dummyRole();
    dummyUser();
  }

  // dummy 20 rows
  public void dummyCategory() {
    log.info("Start dummy category");
    if (dummyRepository.existsByNameAndAgainFalse("Dummy Categories")) {
      log.info("Dummy Categories already exist");
      return;
    }
    for (int i = 0; i < 20; i++) {
      var category = new Category();
      try {
        category.setName(faker.commerce().department());
        category.setDescription(faker.lorem().fixedString(100));
        category.setIcon("📁");
        category.setDisplayOrder(faker.number().numberBetween(1, 100));
        categoryRepository.save(category);
      } catch (DataIntegrityViolationException _) {
        log.warn("existing Category name {} , Now resolved it", category.getName());
        category.setName(faker.commerce().department().concat("-" + faker.commerce().department()));
        categoryRepository.save(category);
      }
    }
    Dummy dummy = new Dummy();
    dummy.setName("Dummy Categories");
    dummy.setNumRows(20L);
    dummy.setDummyDescription("Dummy Categories 20 rows");
    dummyRepository.save(dummy);
    log.info("Finish dummy category");
  }

  // dummy 250 rows
  public void dummyProduct() {
    log.info("Start dummy product");
    if (dummyRepository.existsByNameAndAgainFalse("Dummy Products")) {
      log.info("Dummy Products already exist");
      return;
    }

    List<Category> categories = categoryRepository.findAll();
    if (categories.isEmpty()) {
      throw new IllegalStateException("No categories found. Run dummyCategory first.");
    }

    for (int i = 0; i < 250; i++) {
      Category category = categories.get(faker.number().numberBetween(0, categories.size() - 1));
      var product = new Product();
      try {
        product.setName(faker.commerce().productName());
        product.setDescription(faker.lorem().fixedString(150));
        product.setPrice(BigDecimal.valueOf(faker.number().numberBetween(100, 2000)));
        product.setCategory(category);
        product.setFavoritesCount(faker.number().numberBetween(0, 2000));
        product.setIsFeature(faker.bool().bool());
        product.setUuid(UUID.randomUUID());

        // CHANGED: build ProductImage list instead of setImage()
        product.setImages(buildDummyImages(product));

        productRepository.save(product);
      } catch (DataIntegrityViolationException ex) {
        log.error(ex.getMessage(), ex);
        product.setName(faker.commerce().productName().concat("-" + faker.commerce().material()));
        product.setDescription(faker.lorem().fixedString(120));
      }
    }

    Dummy dummy = new Dummy();
    dummy.setName("Dummy Products");
    dummy.setNumRows(250L);
    dummy.setDummyDescription("Dummy Products 250 rows");
    dummyRepository.save(dummy);
    log.info("Finish dummy product");
  }

  // Generate 1-3 fake images per product
  private List<ProductImage> buildDummyImages(Product product) {
    int imageCount = faker.number().numberBetween(1, 4); // 1 to 3
    List<ProductImage> images = new ArrayList<>();
    for (int j = 0; j < imageCount; j++) {
      ProductImage img = new ProductImage();
      try {
        String imagePath =
                ImageDownloadUtils.downloadAndSave(
                        "https://picsum.photos/300/300", storageConfig.getProductPath());

        img.setImagePath(Path.of(imagePath).getFileName().toString());

      } catch (Exception e) {
        log.error("Failed to download image for product {}: {}", product.getId(), e.getMessage());
        img.setImagePath(null);
      }
      img.setSortOrder(j); // 0 = primary
      img.setProduct(product);
      images.add(img);
    }
    return images;
  }

  public void dummyCategoryAndProduct() {
    log.info("Start dummy category and product");
    dummyCategory();
    dummyProduct();
    log.info("Finish dummy category and product");
  }

  // dummy default same users and not default same users
  public void dummyAddress() {
    log.info("Start dummy address");
    if (dummyRepository.existsByNameAndAgainFalse("Dummy Addresses")) {
      log.info("Dummy Addresses already exist");
      return;
    }
    List<User> users = userRepository.findAll();
    for (User user : users) {

      // dummy default address of user
      var defaultAddress = new Address();
      defaultAddress.setUser(user);
      defaultAddress.setLine1(faker.address().streetAddressNumber());
      defaultAddress.setLine2(faker.address().secondaryAddress());
      defaultAddress.setStreet(faker.address().streetAddress());
      defaultAddress.setCity(faker.address().city());
      defaultAddress.setState(faker.address().state());
      defaultAddress.setPostalCode(faker.address().zipCode());
      defaultAddress.setCountry(faker.address().country());
      defaultAddress.setLatitude(faker.number().randomDouble(0, 0, 180));
      defaultAddress.setLongitude(faker.number().randomDouble(0, 0, 180));
      defaultAddress.setDefault(true);
      defaultAddress.setZip(faker.address().zipCode());
      addressRepository.save(defaultAddress);

      // dummy one more address for user
      var address = new Address();
      address.setUser(user);
      address.setLine1(faker.address().streetAddressNumber());
      address.setLine2(faker.address().secondaryAddress());
      address.setStreet(faker.address().streetAddress());
      address.setCity(faker.address().city());
      address.setState(faker.address().state());
      address.setPostalCode(faker.address().zipCode());
      address.setCountry(faker.address().country());
      address.setLatitude(faker.number().randomDouble(2, 0, 180));
      address.setLongitude(faker.number().randomDouble(2, 0, 180));
      address.setDefault(false);
      address.setZip(faker.address().zipCode());
      addressRepository.save(address);
    }
    Dummy dummy = new Dummy();
    dummy.setName("Dummy Addresses");
    dummy.setNumRows((long) users.size() * 2);
    dummy.setDummyDescription("Dummy Addresses %s rows".formatted(users.size() * 2));
    dummyRepository.save(dummy);
    log.info("Finish dummy address");
  }

  // dummy with flexible rows specific by products
  public void dummyStock() {
    log.info("Start dummy stock");
    if (dummyRepository.existsByNameAndAgainFalse("Dummy Stocks")) {
      log.info("Dummy Stocks already exist");
      return;
    }
    final var products = productRepository.findAll();
    for (var product : products) {
      // one product can be multiple import
      var numberOfImport = faker.number().numberBetween(1, 10);
      for (int index = 0; index < numberOfImport; index++) {
        int quantity = faker.number().numberBetween(1, 50);
        var productImportRequest = new ProductImportRequest();
        productImportRequest.setProductId(product.getId());
        productImportRequest.setQuantity(quantity);
        int randomPercent = faker.random().nextInt(50, 99);
        productImportRequest.setUnitPrice(
            product
                .getPrice()
                .multiply(
                    new BigDecimal(randomPercent)
                        .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP)));
        productImportRequest.setRemark(faker.lorem().word());
        productImportRequest.setSupplierName(faker.company().name());
        productImportRequest.setSupplierPhone(faker.phoneNumber().phoneNumber());
        productImportRequest.setSupplierAddress(faker.address().fullAddress());
        productImportService.importProduct(productImportRequest);
        log.info("Import product {} {} units successful", product.getName(), quantity);
      }
    }
    Dummy dummy = new Dummy();
    dummy.setName("Dummy Stocks");
    dummy.setNumRows((long) products.size());
    dummy.setAgain(true);
    dummy.setDummyDescription("Dummy Stocks %s rows".formatted(products.size()));
    dummyRepository.save(dummy);
    log.info("Finish dummy stock");
  }

  // same rows as users
  public void dummyCart() {
    log.info("Start dummy cart");
    if (dummyRepository.existsByNameAndAgainFalse("Dummy Carts")) {
      log.info("Dummy Carts already exist");
      return;
    }
    List<User> users = userRepository.findAll();
    List<Product> products = productRepository.findAll();
    for (User user : users) {

      // per cart i want more than one item
      var totalItem = faker.number().numberBetween(1, 10);
      for (var x = 0; x < totalItem; x++) {
        // per item
        try {
          Product product = products.get(faker.number().numberBetween(0, products.size() - 1));
          var cardResponse = cartService.addNewProductToCart(product.getId(), user.getId());
        } catch (BadRequestException e) {
          log.warn(e.getMessage());
        }
      }
    }
    Dummy dummy = new Dummy();
    dummy.setName("Dummy Carts");
    dummy.setNumRows((long) users.size());
    dummy.setAgain(true);
    dummy.setDummyDescription("Dummy Cart %s rows".formatted(users.size()));
    log.info("Finish dummy cart");
  }

  public void dummyOrder() {
    log.info("Start dummy order");
    if (dummyRepository.existsByNameAndAgainFalse("Dummy Orders")) {
      log.info("Dummy Orders already exist");
      return;
    }
    List<User> users = userRepository.findAll();
    for (User user : users) {

      // checkout cart of user
      try {
        final var cart = cartService.getCart(user.getId());
        if (cart == null) {
          continue;
        }
        CheckoutRequest checkoutRequest = new CheckoutRequest();
        checkoutRequest.setCartId(cart.getId());
        checkoutRequest.setShippingMethod(ShippingMethod.STANDARD);
        checkoutRequest.setShippingAddress(
            addressRepository.getAddressesByUserId(user.getId()).stream()
                .filter(Address::isDefault)
                .findFirst()
                .map(Address::getId)
                .orElse(null));
        checkoutRequest.setPaymentMethod(PaymentMethod.CASH);
        checkoutRequest.setPromotionCode(null);
        orderService.checkout(checkoutRequest, user.getId());
      } catch (ResourceNotFoundException e) {
        log.warn(e.getMessage());
      }
    }
    Dummy dummy = new Dummy();
    dummy.setName("Dummy Orders");
    dummy.setNumRows((long) users.size());
    dummy.setAgain(true);
    dummy.setDummyDescription("Dummy Orders %s rows".formatted(users.size()));
    dummyRepository.save(dummy);
    log.info("Finish dummy order");
  }

  public void dummyCardAndOrder() {
    log.info("Start dummy cart and order");
    // 3 time dummy cart and order to create more data for testing
    for (int i = 0; i < 3; i++) {
      dummyCart();
      dummyOrder();
    }
    log.info("Finish dummy cart and order");
  }


  public void dummyPromotion() {
    log.info("Start dummy promotions");

    if (dummyRepository.existsByNameAndAgainFalse("Dummy Promotions")) {
      log.info("Dummy Promotions already exist");
      return;
    }

    var products = productRepository.findAll();

    for (int i = 0; i < 20; i++) {

      Promotion promotion = new Promotion();

      try {

        // 🔹 Basic info
        promotion.setName(faker.commerce().promotionCode());
        promotion.setCode("PROMO-" + faker.number().digits(5));

        // 🔹 Random discount type
        PromotionType type = faker.bool().bool()
                ? PromotionType.PERCENTAGE
                : PromotionType.FIXED_AMOUNT;

        promotion.setDiscountType(type);

        if (type == PromotionType.PERCENTAGE) {
          promotion.setDiscountValue(
                  BigDecimal.valueOf(faker.number().numberBetween(5, 50))
          );
        } else {
          promotion.setDiscountValue(
                  BigDecimal.valueOf(faker.number().numberBetween(5, 200))
          );
        }

        // 🔹 Buy X Get Y (optional)
        if (faker.bool().bool()) {
          promotion.setBuyQuantity(faker.number().numberBetween(1, 5));
          promotion.setGetQuantity(faker.number().numberBetween(1, 3));
        }

        // 🔹 Status
        promotion.setActive(faker.bool().bool());

        // 🔹 Date range
        LocalDateTime start = LocalDateTime.now()
                .minusDays(faker.number().numberBetween(0, 10));

        LocalDateTime end = start.plusDays(
                faker.number().numberBetween(5, 30)
        );

        promotion.setStartAt(start);
        promotion.setEndAt(end);

        // 🔹 Usage limits
        promotion.setMaxUsage(faker.number().numberBetween(10, 200));
        promotion.setMaxUsagePerUser(faker.number().numberBetween(1, 5));

        // 🔹 Minimum purchase
        promotion.setMinPurchaseAmount(
                BigDecimal.valueOf(faker.number().numberBetween(50, 500))
        );

        // 🔹 Assign random products
        if (!products.isEmpty()) {
          int numProducts = faker.number().numberBetween(1, products.size());
          Collections.shuffle(products);
          promotion.setProducts(products.subList(0, numProducts));
        }

        promotionRepository.save(promotion);

      } catch (DataIntegrityViolationException e) {
        log.warn("Duplicate promotion code, regenerating...");
        promotion.setCode("PROMO-" + UUID.randomUUID().toString().substring(0, 8));
        promotionRepository.save(promotion);
      }
    }

    Dummy dummy = new Dummy();
    dummy.setName("Dummy Promotions");
    dummy.setNumRows(20L);
    dummy.setDummyDescription("Dummy Promotions 20 rows");
    dummyRepository.save(dummy);

    log.info("Finish dummy promotions");
  }


  public void dummyBanner() {

    log.info("Start dummy banners");

    if (dummyRepository.existsByNameAndAgainFalse("Dummy Banners")) {
      log.info("Dummy Banners already exist");
      return;
    }

    var products = productRepository.findAll();
    var categories = categoryRepository.findAll();

    for (int i = 0; i < 15; i++) {

      Banner banner = new Banner();

      try {

        // 🔹 Basic Info
        banner.setTitle(faker.commerce().productName());
        banner.setDescription(faker.lorem().sentence(10));

        // 🔹 Download random image (landscape banner size)
        String imagePath = ImageDownloadUtils.downloadAndSave(
                "https://picsum.photos/1200/400",
                storageConfig.getBannerPath()
        );

        // Store only filename
        banner.setImage(Path.of(imagePath).getFileName().toString());

        // 🔹 Link Type
        List<String> linkTypes = List.of("PRODUCT", "CATEGORY", "EXTERNAL", "NONE");
        String linkType = linkTypes.get(faker.number().numberBetween(0, linkTypes.size()));

        banner.setLinkType(BannerLinkType.valueOf(linkType));

        switch (linkType) {

          case "PRODUCT" -> {
            if (!products.isEmpty()) {
              var product = products.get(
                      faker.number().numberBetween(0, products.size())
              );
              banner.setLinkId(product.getId());
            }
          }

          case "CATEGORY" -> {
            if (!categories.isEmpty()) {
              var category = categories.get(
                      faker.number().numberBetween(0, categories.size())
              );
              banner.setLinkId(category.getId());
            }
          }

          case "EXTERNAL" ->
                  banner.setLinkUrl("https://picsum.photos/300/300");

          default -> {
            banner.setLinkId(null);
            banner.setLinkUrl(null);
          }
        }

        // 🔹 Active
        banner.setIsActive(faker.bool().bool());

        // 🔹 Display Order
        banner.setDisplayOrder(i);

        // 🔹 Dates
        LocalDateTime start = LocalDateTime.now()
                .minusDays(faker.number().numberBetween(0, 5));

        LocalDateTime end = start.plusDays(
                faker.number().numberBetween(5, 20)
        );

        banner.setStartDate(start);
        banner.setEndDate(end);

        // 🔹 Position
        List<String> positions = List.of(
                "HOME_CAROUSEL",
                "CATEGORY_TOP",
                "FLASH_SALE",
                "MOBILE_SLIDER"
        );

        banner.setPosition(
                positions.get(faker.number().numberBetween(0, positions.size()))
        );

        // 🔹 Random background color
        banner.setBackgroundColor(
                "#" + faker.color().hex().substring(0, 6)
        );

        bannerRepository.save(banner);

      } catch (Exception e) {
        log.warn("Error creating banner: {}", e.getMessage());
      }
    }

    Dummy dummy = new Dummy();
    dummy.setName("Dummy Banners");
    dummy.setNumRows(15L);
    dummy.setDummyDescription("Dummy Banners 15 rows");
    dummyRepository.save(dummy);

    log.info("Finish dummy banners");
  }





  @Async
  public void dummyAll() {
    log.info("Start dummy all data");
    dummyRoleAndUser();
    dummyCategoryAndProduct();
    dummyPromotion();
    dummyBanner();
    dummyAddress();
    dummyStock();
    dummyCardAndOrder();
    log.info("Finish dummy all data");
  }
}
