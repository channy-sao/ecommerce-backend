package ecommerce_app.service.impl;

import ecommerce_app.constant.enums.OrderStatus;
import ecommerce_app.constant.enums.PaymentMethod;
import ecommerce_app.constant.enums.PaymentStatus;
import ecommerce_app.exception.ResourceNotFoundException;
import ecommerce_app.mapper.OrderMapper;
import ecommerce_app.mapper.OrderStatusHistoryMapper;
import ecommerce_app.dto.response.OrderDetailResponse;
import ecommerce_app.dto.response.OrderResponse;
import ecommerce_app.dto.response.OrderStatusHistoryResponse;
import ecommerce_app.entity.Order;
import ecommerce_app.entity.OrderStatusHistory;
import ecommerce_app.repository.OrderRepository;
import ecommerce_app.repository.OrderStatusHistoryRepository;
import ecommerce_app.service.AdminManageOrderService;
import ecommerce_app.specification.OrderSpecification;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminManageOrderServiceImpl implements AdminManageOrderService {
  private final OrderRepository orderRepository;
  private final OrderStatusHistoryRepository orderStatusHistoryRepository;
  private final OrderStatusHistoryMapper orderStatusHistoryMapper;
  private final OrderMapper orderMapper;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

    OrderStatus currentStatus = order.getOrderStatus();

    validateTransition(currentStatus, newStatus);

    // 🔥 Update order
    order.setOrderStatus(newStatus);

    // 🔥 Save history
    OrderStatusHistory history =
        OrderStatusHistory.builder().order(order).status(newStatus).build();

    orderStatusHistoryRepository.save(history);
  }

  @Transactional(readOnly = true)
  @Override
  public Page<OrderResponse> adminGetOrders(
      OrderStatus orderStatus,
      PaymentStatus paymentStatus,
      LocalDate fromDate,
      LocalDate toDate,
      boolean isPaged,
      int page,
      int pageSize,
      String sortBy,
      Sort.Direction sortDirection) {
    Specification<Order> specification =
        OrderSpecification.filter(orderStatus, paymentStatus, fromDate, toDate);
    Sort sort = Sort.by(sortDirection, sortBy);
    if (!isPaged) {
      final List<Order> productList = orderRepository.findAll(specification, sort);
      final List<OrderResponse> productResponseList =
          productList.stream().map(orderMapper::toSimpleResponse).toList();
      return new PageImpl<>(productResponseList);
    }
    // page start from 0
    final var pageable = PageRequest.of(page - 1, pageSize, sort);
    final Page<Order> productPage = orderRepository.findAll(specification, pageable);
    return productPage.map(orderMapper::toSimpleResponse);
  }

  @Transactional(readOnly = true)
  @Override
  public OrderDetailResponse getOrderDetailForAdmin(Long orderId) {
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Order not found with ID: " + orderId));

    // Map order entity → detail DTO
    OrderDetailResponse response = OrderMapper.toDetailResponse(order);

    // Load status history (sorted by createdAt asc)
    List<OrderStatusHistoryResponse> histories =
        orderStatusHistoryRepository.findByOrderIdOrderByCreatedAtAsc(orderId).stream()
            .map(orderStatusHistoryMapper::toResponse)
            .toList();

    response.setStatusHistories(histories);
    return response;
  }

  @Transactional(readOnly = true)
  @Override
  public Page<OrderResponse> getOrdersReadyForCollection(PaymentMethod paymentMethod, int page, int size) {
    PageRequest pageRequest =
            PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "orderDate"));

    List<PaymentMethod> methods;
    if (paymentMethod != null) {
      methods = List.of(paymentMethod);
    } else {
      // Default to both COD and CASH_IN_SHOP
      methods = List.of(PaymentMethod.COD, PaymentMethod.CASH_IN_SHOP, PaymentMethod.CASH);
    }

    Page<Order> orders =
            orderRepository.findByPaymentMethodInAndPaymentStatusAndOrderStatusNot(
                    methods, PaymentStatus.PENDING, OrderStatus.CANCELLED, pageRequest);
    return orders.map(orderMapper::toSimpleResponse);
  }

  private void validateTransition(OrderStatus from, OrderStatus to) {
    if (from == OrderStatus.COMPLETED) {
      throw new IllegalStateException("Completed order cannot be changed");
    }
  }
}
