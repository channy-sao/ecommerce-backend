package ecommerce_app.modules.dummy;

import com.github.javafaker.Faker;
import ecommerce_app.constant.enums.AuthProvider;
import ecommerce_app.constant.enums.PaymentMethod;
import ecommerce_app.modules.address.model.entity.Address;
import ecommerce_app.modules.address.repository.AddressRepository;
import ecommerce_app.modules.cart.service.CartService;
import ecommerce_app.modules.category.model.entity.Category;
import ecommerce_app.modules.category.repository.CategoryRepository;
import ecommerce_app.modules.order.model.dto.CheckoutRequest;
import ecommerce_app.modules.order.service.OrderService;
import ecommerce_app.modules.product.model.entity.Product;
import ecommerce_app.modules.product.repository.ProductRepository;
import ecommerce_app.modules.stock.model.dto.ProductImportRequest;
import ecommerce_app.modules.stock.service.ProductImportService;
import ecommerce_app.modules.stock.service.StockService;
import ecommerce_app.modules.user.model.entity.Role;
import ecommerce_app.modules.user.model.entity.User;
import ecommerce_app.modules.user.repository.PermissionRepository;
import ecommerce_app.modules.user.repository.RoleRepository;
import ecommerce_app.modules.user.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class DummyService {
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
  private final Faker faker = new Faker();

  public void dummyRole() {
    log.info("Start dummy role");
    var permissions = permissionRepository.findAll();
    int randomIndex = faker.number().numberBetween(0, permissions.size());
    for (int i = 0; i < 10; i++) {
      Role role = new Role();
      role.setName(faker.job().position()+ i);
      role.setDescription(faker.lorem().fixedString(100));

      role.setPermissions(Set.of(permissions.get(randomIndex)));
      role.setUid(UUID.randomUUID().toString());
      roleRepository.save(role);
    }
    log.info("Finish dummy role");
  }

  public void dummyUser() {
    log.info("Start dummy user");
    final var defaultUserPassword = "password";
    var roles = roleRepository.findAll();
    int randomIndex = faker.number().numberBetween(0, roles.size());
    for (int i = 0; i < 10; i++) {
      var user = new ecommerce_app.modules.user.model.entity.User();
      user.setEmail(faker.internet().emailAddress());
      user.setAuthProvider(AuthProvider.LOCAL);
      user.setEmailVerifiedAt(null);
      user.setIsEmailVerified(false);
      user.setFirstName(faker.name().firstName());
      user.setLastName(faker.name().lastName());
      user.setIsActive(true);
      user.setRememberMe(false);
      user.setPassword(passwordEncoder.encode(defaultUserPassword));
      user.setRoles(Set.of(roles.get(randomIndex)));
      user.setUuid(UUID.randomUUID());
      userRepository.save(user);
    }
    log.info("Finish dummy user");
  }

  public void dummyRoleAndUser() {
    log.info("Start dummy role and user");
    dummyRole();
    dummyUser();
  }

  public void dummyCategory() {
    log.info("Start dummy category");
    for (int i = 0; i < 20; i++) {
      var category = new Category();
      category.setName(faker.commerce().department());
      category.setDescription(faker.lorem().fixedString(100));
      categoryRepository.save(category);
    }
    log.info("Finish dummy category");
  }

  public void dummyProduct() {
    log.info("Start dummy product");
    for (int i = 0; i < 100; i++) {
      var category =
          categoryRepository
              .findAll()
              .get(faker.number().numberBetween(0, categoryRepository.findAll().size()));
      var product = new Product();
      product.setName(faker.commerce().productName());
      product.setDescription(faker.lorem().fixedString(100));
      product.setPrice(BigDecimal.valueOf(faker.number().numberBetween(100, 10000)));
      product.setImage(faker.internet().image());
      product.setCategory(category);
      product.setFavoritesCount(faker.number().numberBetween(0, 1000));
      product.setIsFeature(faker.bool().bool());
      product.setUuid(UUID.randomUUID());
      productRepository.save(product);
    }
    log.info("Finish dummy product");
  }

  public void dummyCategoryAndProduct() {
    log.info("Start dummy category and product");
    dummyCategory();
    dummyProduct();
    log.info("Finish dummy category and product");
  }

  public void dummyAddress() {
    log.info("Start dummy address");
    List<User> users = userRepository.findAll();
    for (User user : users) {
      var address = new Address();
      address.setUser(user);
      address.setLine1(faker.address().streetAddressNumber());
      address.setLine2(faker.address().secondaryAddress());
      address.setStreet(faker.address().streetAddress());
      address.setCity(faker.address().city());
      address.setState(faker.address().state());
      address.setPostalCode(faker.address().zipCode());
      address.setCountry(faker.address().country());
      address.setLatitude(faker.number().randomDouble(0, 0, 180));
      address.setLongitude(faker.number().randomDouble(0, 0, 180));
      address.setDefault(faker.bool().bool());
      address.setZip(faker.address().zipCode());
      addressRepository.save(address);
    }
    log.info("Finish dummy address");
  }

  public void dummyStock() {
    log.info("Start dummy stock");
    final var products = productRepository.findAll();
    for (var product : products) {
      int quantity = faker.number().numberBetween(0, 100);
      var productImportRequest = new ProductImportRequest();
      productImportRequest.setProductId(product.getId());
      productImportRequest.setQuantity(quantity);
      productImportRequest.setUnitPrice(product.getPrice());
      productImportRequest.setRemark("Initial stock import");
      productImportRequest.setSupplierName(faker.company().name());
      productImportRequest.setSupplierPhone(faker.phoneNumber().phoneNumber());
      productImportRequest.setSupplierAddress(faker.address().fullAddress());
      productImportService.importProduct(productImportRequest);
    }
    log.info("Finish dummy stock");
  }

  public void dummyCart() {
    log.info("Start dummy cart");
    List<User> users = userRepository.findAll();
    List<Product> products = productRepository.findAll();
    for (User user : users) {
      int numItems = faker.number().numberBetween(1, 5);
      for (int i = 0; i < numItems; i++) {
        Product product = products.get(faker.number().numberBetween(0, products.size()));
        int quantity = faker.number().numberBetween(1, 10);
        cartService.addNewProductToCart(user.getId(), product.getId());
        for (int q = 1; q <= quantity; q++) {
          cartService.incrementItem(user.getId(), product.getId());
        }
      }
    }
    log.info("Finish dummy cart");
  }

  public void dummyOrder() {
    log.info("Start dummy order");
    List<User> users = userRepository.findAll();
    for (User user : users) {
      final var cart = cartService.getCart(user.getId());
      CheckoutRequest checkoutRequest = new CheckoutRequest();
      checkoutRequest.setCartId(cart.getId());
      checkoutRequest.setShippingAddress(
          addressRepository.getAddressesByUserId(user.getId()).stream()
              .findFirst()
              .map(Address::getId)
              .orElse(null));
      checkoutRequest.setPaymentMethod(PaymentMethod.CASH);
      checkoutRequest.setPromotionCode(null);
      orderService.checkout(checkoutRequest, user.getId());
    }
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

  public void dummyAll() {
    log.info("Start dummy all data");
    dummyRoleAndUser();
    dummyCategoryAndProduct();
    dummyAddress();
    dummyStock();
    dummyCardAndOrder();
    log.info("Finish dummy all data");
  }
}
