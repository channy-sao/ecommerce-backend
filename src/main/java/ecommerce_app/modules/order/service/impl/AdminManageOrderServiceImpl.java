package ecommerce_app.modules.order.service.impl;

import ecommerce_app.constant.enums.OrderStatus;
import ecommerce_app.constant.enums.PaymentStatus;
import ecommerce_app.infrastructure.exception.ResourceNotFoundException;
import ecommerce_app.infrastructure.mapper.OrderMapper;
import ecommerce_app.infrastructure.mapper.OrderStatusHistoryMapper;
import ecommerce_app.modules.order.model.dto.OrderDetailResponse;
import ecommerce_app.modules.order.model.dto.OrderResponse;
import ecommerce_app.modules.order.model.dto.OrderStatusHistoryResponse;
import ecommerce_app.modules.order.model.entity.Order;
import ecommerce_app.modules.order.model.entity.OrderStatusHistory;
import ecommerce_app.modules.order.repository.OrderRepository;
import ecommerce_app.modules.order.repository.OrderStatusHistoryRepository;
import ecommerce_app.modules.order.service.AdminManageOrderService;
import ecommerce_app.modules.order.specification.OrderSpecification;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

  private void validateTransition(OrderStatus from, OrderStatus to) {
    if (from == OrderStatus.COMPLETED) {
      throw new IllegalStateException("Completed order cannot be changed");
    }
  }
}
