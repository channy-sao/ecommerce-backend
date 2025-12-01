package ecommerce_app.infrastructure.mapper;

import ecommerce_app.modules.address.model.dto.AddressResponse;
import ecommerce_app.modules.address.model.entity.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface AddressMapper {
    AddressMapper INSTANCE = Mappers.getMapper(AddressMapper.class);

    @Mapping(source = "user.id", target = "userId")
    AddressResponse toResponse(Address address);
}
