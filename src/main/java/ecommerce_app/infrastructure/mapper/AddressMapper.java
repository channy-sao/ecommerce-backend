package ecommerce_app.infrastructure.mapper;

import ecommerce_app.modules.address.model.dto.AddressResponse;
import ecommerce_app.modules.address.model.entity.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

/**
 * MapStruct mapper responsible for converting {@link Address} entities
 * into {@link AddressResponse} DTOs.
 *
 * <p>This mapper extracts the associated user ID from the {@code Address}
 * entity and maps it to the {@code userId} field in the response.</p>
 */
@Component
@Mapper(componentModel = "spring")
public interface AddressMapper {

    /**
     * Static mapper instance for non-Spring usage.
     *
     * <p>When using Spring dependency injection, prefer injecting
     * {@link AddressMapper} instead of accessing this instance directly.</p>
     */
    AddressMapper INSTANCE = Mappers.getMapper(AddressMapper.class);

    /**
     * Maps an {@link Address} entity to an {@link AddressResponse}.
     *
     * <p>The {@code user.id} field from the entity is mapped to
     * {@code userId} in the response.</p>
     *
     * @param address the address entity to convert
     * @return the mapped address response DTO
     */
    @Mapping(source = "user.id", target = "userId")
    AddressResponse toResponse(Address address);
}
