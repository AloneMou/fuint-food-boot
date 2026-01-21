package com.fuint.openapi.dto.assembler;

import com.fuint.common.dto.UserOrderDto;
import com.fuint.openapi.dto.request.OrderCreateReqDTO;
import com.fuint.openapi.dto.response.OrderInfoDTO;
import com.fuint.openapi.v1.order.vo.OrderCreateReqVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * Order Mapper using MapStruct
 * Replaces BeanUtils and manual conversion.
 */
@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    /**
     * Convert new Request DTO to legacy VO for service compatibility
     * or internal DTO.
     */
    OrderCreateReqVO toCreateReqVO(OrderCreateReqDTO dto);

    /**
     * Convert internal UserOrderDto to new Response DTO
     */
    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "totalAmount", source = "amount")
    OrderInfoDTO toOrderInfoDTO(UserOrderDto userOrderDto);

    List<OrderInfoDTO> toOrderInfoDTOList(List<UserOrderDto> userOrderDtos);
}
