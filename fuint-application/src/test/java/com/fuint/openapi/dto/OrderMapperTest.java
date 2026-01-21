package com.fuint.openapi.dto;

import com.fuint.openapi.dto.assembler.OrderMapper;
import com.fuint.openapi.dto.request.OrderCreateReqDTO;
import com.fuint.openapi.dto.request.OrderGoodsItemDTO;
import com.fuint.openapi.v1.order.vo.OrderCreateReqVO;
import com.fuint.openapi.v1.order.vo.OrderGoodsItemVO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.Collections;

public class OrderMapperTest {

    private final OrderMapper mapper = Mappers.getMapper(OrderMapper.class);

    @Test
    public void testToOrderCreateReqVO() {
        OrderCreateReqDTO dto = new OrderCreateReqDTO();
        dto.setUserId(1001);
        dto.setStoreId(2001);
        dto.setPreTotalAmount(new BigDecimal("99.99"));
        dto.setRemark("Test Remark");

        OrderGoodsItemDTO itemDTO = new OrderGoodsItemDTO();
        itemDTO.setGoodsId(500);
        itemDTO.setQuantity(2);
        dto.setItems(Collections.singletonList(itemDTO));

        OrderCreateReqVO vo = mapper.toOrderCreateReqVO(dto);

        Assertions.assertNotNull(vo);
        Assertions.assertEquals(dto.getUserId(), vo.getUserId());
        Assertions.assertEquals(dto.getStoreId(), vo.getStoreId());
        Assertions.assertEquals(dto.getPreTotalAmount(), vo.getPreTotalAmount());
        Assertions.assertEquals(dto.getRemark(), vo.getRemark());
        Assertions.assertEquals(1, vo.getItems().size());
        
        OrderGoodsItemVO itemVO = vo.getItems().get(0);
        Assertions.assertEquals(itemDTO.getGoodsId(), itemVO.getGoodsId());
        Assertions.assertEquals(itemDTO.getQuantity(), itemVO.getQuantity());
    }
}
