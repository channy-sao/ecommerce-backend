package ecommerce_app.controller.admin;

import ecommerce_app.constant.enums.OrderStatus;
import ecommerce_app.constant.enums.PaymentMethod;
import ecommerce_app.constant.enums.PaymentStatus;
import ecommerce_app.constant.message.MessageKeyConstant;
import ecommerce_app.dto.response.BaseBodyResponse;
import ecommerce_app.dto.response.OrderDetailResponse;
import ecommerce_app.dto.response.OrderResponse;
import ecommerce_app.mapper.OrderMapper;
import ecommerce_app.repository.OrderRepository;
import ecommerce_app.service.AdminManageOrderService;
import ecommerce_app.util.MessageSourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
  private final MessageSourceService messageSourceService;

  @PreAuthorize("hasAnyAuthority('ORDER_UPDATE', 'ORDER_DELETE', 'ORDER_CANCEL')")
  @PatchMapping("/{orderId}/update-status")
  public ResponseEntity<BaseBodyResponse<Void>> updateOrderStatus(
          @PathVariable Long orderId,
          @Parameter(name = "status") @RequestParam(name = "status") OrderStatus newStatus) {
    this.adminManageOrderService.updateOrderStatus(orderId, newStatus);
    return BaseBodyResponse.success(
        messageSourceService.getMessage(MessageKeyConstant.ORDER_MESSAGE_STATUS_UPDATED));
  }

  @PreAuthorize("hasAnyAuthority('ORDER_READ', 'ORDER_UPDATE', 'ORDER_CANCEL', 'ORDER_DELETE')")
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
        messageSourceService.getMessage(MessageKeyConstant.ORDER_TITLE_LIST));
  }

  @PreAuthorize("hasAnyAuthority('ORDER_READ', 'ORDER_UPDATE', 'ORDER_CANCEL', 'ORDER_DELETE')")
  @GetMapping("/{orderId}")
  public ResponseEntity<BaseBodyResponse<OrderDetailResponse>> getOrderDetail(
          @PathVariable Long orderId) {
    return BaseBodyResponse.success(
        this.adminManageOrderService.getOrderDetailForAdmin(orderId),
        messageSourceService.getMessage(MessageKeyConstant.ORDER_TITLE_DETAIL));
  }

  // Add to OrderController.java
  @GetMapping("/staff/ready-for-collection")
  @Operation(summary = "Get orders ready for cash collection (COD & Cash-in-Shop)")
  public ResponseEntity<BaseBodyResponse<List<OrderResponse>>> getOrdersReadyForCollection(
      @RequestParam(required = false) PaymentMethod paymentMethod,
      @RequestParam(defaultValue = "false") boolean includePaid,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int size) {

    Page<OrderResponse> orders = adminManageOrderService.getOrdersReadyForCollection(
            paymentMethod, page, size, includePaid
    );
    return BaseBodyResponse.pageSuccess(orders, "Success");
  }
}
