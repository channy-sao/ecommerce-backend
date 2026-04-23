package ecommerce_app.mapper;

import ecommerce_app.dto.request.ProductVariantRequest;
import ecommerce_app.dto.response.ProductVariantResponse;
import ecommerce_app.entity.ProductAttributeValue;
import ecommerce_app.entity.ProductVariant;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class ProductVariantMapper {

    // ── Entity → Response ────────────────────────────────────────────
    public ProductVariantResponse toResponse(ProductVariant variant) {
        if (variant == null) return null;

        List<ProductVariantResponse.AttributeValueDto> attrDtos =
            variant.getAttributeValues() == null
                ? Collections.emptyList()
                : variant.getAttributeValues().stream()
                    .map(this::toAttributeValueDto)
                    .toList();

        return ProductVariantResponse.builder()
                .id(variant.getId())
                .sku(variant.getSku())
                .price(variant.getPrice())
                .effectivePrice(variant.getEffectivePrice())
                .stockQuantity(variant.getStockQuantity())
                .lowStockThreshold(variant.getLowStockThreshold())
                .stockStatus(variant.getStockStatus().name())
                .isActive(variant.getIsActive())
                .attributeValues(attrDtos)
                .build();
    }

    public List<ProductVariantResponse> toResponseList(List<ProductVariant> variants) {
        if (variants == null) return Collections.emptyList();
        return variants.stream().map(this::toResponse).toList();
    }

    // ── Request → Entity (used during creation, product set separately) ──
    public ProductVariant toEntity(ProductVariantRequest request) {
        if (request == null) return null;

        return ProductVariant.builder()
                .sku(request.getSku())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0)
                .lowStockThreshold(request.getLowStockThreshold() != null ? request.getLowStockThreshold() : 10)
                .isActive(true)
                .build();
    }

    // ── Apply update from request to existing entity ─────────────────
    public void updateEntity(ProductVariant variant, ProductVariantRequest request) {
        if (variant == null || request == null) return;

        variant.setSku(request.getSku());
        variant.setPrice(request.getPrice());
        variant.setLowStockThreshold(
            request.getLowStockThreshold() != null ? request.getLowStockThreshold() : 10
        );
        // stockQuantity is NOT updated here — use adjustStock() instead
        // isActive is NOT updated here — use deleteVariant() instead
    }

    // ── Private helpers ──────────────────────────────────────────────
    private ProductVariantResponse.AttributeValueDto toAttributeValueDto(ProductAttributeValue value) {
        return ProductVariantResponse.AttributeValueDto.builder()
                .id(value.getId())
                .attribute(value.getDefinition() != null
                    ? value.getDefinition().getDisplayName() != null
                        ? value.getDefinition().getDisplayName()
                        : value.getDefinition().getName()
                    : null)
                .value(value.getValue())
                .build();
    }
}