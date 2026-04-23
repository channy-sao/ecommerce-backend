package ecommerce_app.service.impl;

import ecommerce_app.constant.enums.StockMovementType;
import ecommerce_app.dto.request.ProductVariantRequest;
import ecommerce_app.dto.request.StockAdjustmentRequest;
import ecommerce_app.dto.response.ProductVariantResponse;
import ecommerce_app.entity.Product;
import ecommerce_app.entity.ProductAttributeValue;
import ecommerce_app.entity.ProductVariant;
import ecommerce_app.entity.VariantStockMovement;
import ecommerce_app.exception.BadRequestException;
import ecommerce_app.exception.ResourceNotFoundException;
import ecommerce_app.mapper.ProductVariantMapper;
import ecommerce_app.repository.ProductAttributeValueRepository;
import ecommerce_app.repository.ProductRepository;
import ecommerce_app.repository.ProductVariantRepository;
import ecommerce_app.repository.VariantStockMovementRepository;
import ecommerce_app.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;
    private final ProductAttributeValueRepository attributeValueRepository;
    private final VariantStockMovementRepository movementRepository;
    private final ProductVariantMapper variantMapper;

    // ── Create variants ──────────────────────────────────────────────
    @Override
    @Transactional
    public List<ProductVariantResponse> createVariants(Long productId, List<ProductVariantRequest> requests) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        List<ProductVariant> saved = requests.stream().map(req -> {
            if (variantRepository.existsBySku(req.getSku())) {
                throw new BadRequestException("SKU already exists: " + req.getSku());
            }

            List<ProductAttributeValue> attrValues = attributeValueRepository
                    .findAllActiveByIds(req.getAttributeValueIds());

            if (attrValues.size() != req.getAttributeValueIds().size()) {
                throw new BadRequestException("One or more attribute value IDs are invalid or inactive");
            }

            ProductVariant variant = variantMapper.toEntity(req);
            variant.setProduct(product);
            variant.setAttributeValues(attrValues);

            ProductVariant savedVariant = variantRepository.save(variant);

            if (req.getStockQuantity() != null && req.getStockQuantity() > 0) {
                recordMovement(savedVariant, StockMovementType.IN,
                        req.getStockQuantity(), 0, req.getStockQuantity(),
                        null, "INITIAL", "Initial stock on variant creation");
            }

            return savedVariant;
        }).toList();

        product.setHasVariants(true);
        productRepository.save(product);

        log.info("Created {} variants for product id={}", saved.size(), productId);
        return variantMapper.toResponseList(saved);
    }

    // ── Update variant ───────────────────────────────────────────────
    @Override
    @Transactional
    public ProductVariantResponse updateVariant(Long variantId, ProductVariantRequest request) {
        ProductVariant variant = findVariantOrThrow(variantId);

        // Check SKU uniqueness excluding self
        if (variantRepository.existsBySkuAndIdNot(request.getSku(), variantId)) {
            throw new BadRequestException("SKU already exists: " + request.getSku());
        }

        // Validate and fetch new attribute values
        List<ProductAttributeValue> attrValues = attributeValueRepository
                .findAllActiveByIds(request.getAttributeValueIds());

        if (attrValues.size() != request.getAttributeValueIds().size()) {
            throw new BadRequestException("One or more attribute value IDs are invalid or inactive");
        }

        variantMapper.updateEntity(variant, request);
        variant.setAttributeValues(attrValues);

        ProductVariant updated = variantRepository.save(variant);
        log.info("Updated variant id={}", variantId);
        return variantMapper.toResponse(updated);
    }

    // ── Get variant by id ────────────────────────────────────────────
    @Override
    public ProductVariantResponse getVariantById(Long variantId) {
        return variantMapper.toResponse(findVariantOrThrow(variantId));
    }

    // ── Get variants by product ──────────────────────────────────────
    @Override
    public List<ProductVariantResponse> getVariantsByProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found: " + productId);
        }
        return variantMapper.toResponseList(
                variantRepository.findByProductIdAndIsActiveTrue(productId)
        );
    }

    // ── Adjust stock ─────────────────────────────────────────────────
    @Override
    @Transactional
    public ProductVariantResponse adjustStock(StockAdjustmentRequest request) {
        ProductVariant variant = findVariantOrThrow(request.getVariantId());

        int before = variant.getStockQuantity();
        int after = switch (request.getMovementType()) {
            case IN, RETURN -> before + request.getQuantity();
            case OUT -> {
                if (before < request.getQuantity()) {
                    throw new BadRequestException(
                            "Insufficient stock for variant id=" + variant.getId() +
                                    ". Available: " + before + ", Requested: " + request.getQuantity()
                    );
                }
                yield before - request.getQuantity();
            }
            case ADJUSTMENT -> {
                if (request.getQuantity() < 0) {
                    throw new BadRequestException("Adjustment quantity cannot be negative");
                }
                yield request.getQuantity(); // set absolute value
            }
        };

        variant.setStockQuantity(after);
        variantRepository.save(variant);

        recordMovement(variant, request.getMovementType(), request.getQuantity(),
                before, after, request.getReferenceId(),
                request.getReferenceType(), request.getNote());

        log.info("Stock adjusted variant id={} type={} qty={} {} -> {}",
                variant.getId(), request.getMovementType(),
                request.getQuantity(), before, after);

        return variantMapper.toResponse(variant);
    }

    // ── Deduct stock on order ────────────────────────────────────────
    @Override
    @Transactional
    public void deductStockForOrder(Long variantId, int quantity, Long orderId) {
        adjustStock(StockAdjustmentRequest.builder()
                .variantId(variantId)
                .movementType(StockMovementType.OUT)
                .quantity(quantity)
                .referenceId(orderId)
                .referenceType("ORDER")
                .note("Deducted on order #" + orderId)
                .build());
    }

    // ── Restore stock on order cancel ────────────────────────────────
    @Override
    @Transactional
    public void restoreStockForOrder(Long variantId, int quantity, Long orderId) {
        adjustStock(StockAdjustmentRequest.builder()
                .variantId(variantId)
                .movementType(StockMovementType.RETURN)
                .quantity(quantity)
                .referenceId(orderId)
                .referenceType("ORDER_CANCEL")
                .note("Restored on order cancel #" + orderId)
                .build());
    }

    // ── Get stock history ────────────────────────────────────────────
    @Override
    public List<VariantStockMovement> getStockHistory(Long variantId) {
        if (!variantRepository.existsById(variantId)) {
            throw new ResourceNotFoundException("Variant not found: " + variantId);
        }
        return movementRepository.findByVariantIdOrderByCreatedAtDesc(variantId);
    }

    // ── Soft delete variant ──────────────────────────────────────────
    @Override
    @Transactional
    public void deleteVariant(Long variantId) {
        ProductVariant variant = findVariantOrThrow(variantId);
        variant.setIsActive(false);
        variantRepository.save(variant);

        // If all variants are inactive, reset hasVariants flag on product
        Long productId = variant.getProduct().getId();
        boolean anyActive = variantRepository.findByProductIdAndIsActiveTrue(productId).isEmpty();
        if (anyActive) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
            product.setHasVariants(false);
            productRepository.save(product);
            log.info("All variants inactive — reset hasVariants=false for product id={}", productId);
        }

        log.info("Soft deleted variant id={}", variantId);
    }

    // ── Private helpers ──────────────────────────────────────────────
    private ProductVariant findVariantOrThrow(Long variantId) {
        return variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found: " + variantId));
    }

    private void recordMovement(ProductVariant variant, StockMovementType type,
                                int qty, int before, int after,
                                Long refId, String refType, String note) {
        movementRepository.save(VariantStockMovement.builder()
                .variant(variant)
                .movementType(type)
                .quantity(qty)
                .quantityBefore(before)
                .quantityAfter(after)
                .referenceId(refId)
                .referenceType(refType)
                .note(note)
                .build());
    }
}