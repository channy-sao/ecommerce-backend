package ecommerce_app.api.admin;

import ecommerce_app.constant.enums.OrderStatus;
import ecommerce_app.constant.enums.PaymentStatus;
import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import ecommerce_app.modules.order.model.dto.OrderDetailResponse;
import ecommerce_app.modules.order.model.dto.OrderResponse;
import ecommerce_app.modules.order.service.AdminManageOrderService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Admin Manage Order Controller", description = "Admin Management Order process")
public class AdminManageOrderController {
  private final AdminManageOrderService adminManageOrderService;

  @PatchMapping("/{orderId}/update-status")
  public ResponseEntity<BaseBodyResponse<Void>> updateOrderStatus(
      @PathVariable(name = "orderId", value = "orderId") Long orderId,
      @Parameter(name = "status") @RequestParam(name = "status") OrderStatus newStatus) {
    this.adminManageOrderService.updateOrderStatus(orderId, newStatus);
    return BaseBodyResponse.success("Admin update status successful");
  }

  @GetMapping
  public ResponseEntity<BaseBodyResponse<List<OrderResponse>>> getOrders(
      @RequestParam(value = "orderStatus", required = false) OrderStatus orderStatus,
      @RequestParam(value = "paymentStatus", required = false) PaymentStatus paymentStatus,
      @RequestParam(value = "fromDate", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate fromDate,
      @RequestParam(value = "toDate", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate toDate,
      @RequestParam(value = "isPaged", defaultValue = "true") boolean isPaged,
      @RequestParam(value = "page", defaultValue = "1") int page,
      @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
      @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
      @RequestParam(value = "sortDirection", defaultValue = "DESC") Sort.Direction sortDirection) {
    return BaseBodyResponse.pageSuccess(
        this.adminManageOrderService.adminGetOrders(
            orderStatus,
            paymentStatus,
            fromDate,
            toDate,
            isPaged,
            page,
            pageSize,
            sortBy,
            sortDirection),
        "Orders successful");
  }

  @GetMapping("/{orderId}")
  public ResponseEntity<BaseBodyResponse<OrderDetailResponse>> getOrderDetail(
      @PathVariable(value = "orderId", name = "orderId") Long orderId) {
    return BaseBodyResponse.success(
        this.adminManageOrderService.getOrderDetailForAdmin(orderId),
        "Get Order detail successful");
  }
}
