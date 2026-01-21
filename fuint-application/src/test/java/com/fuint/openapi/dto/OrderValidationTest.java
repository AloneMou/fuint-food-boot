package com.fuint.openapi.dto;

import com.fuint.openapi.dto.request.OrderCreateReqDTO;
import com.fuint.openapi.dto.request.OrderGoodsItemDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OrderValidationTest {

    private static Validator validator;

    @BeforeAll
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testOrderCreateReqDTOValidation() {
        OrderCreateReqDTO req = OrderCreateReqDTO.builder().build();

        Set<ConstraintViolation<OrderCreateReqDTO>> violations = validator.validate(req);
        assertFalse(violations.isEmpty(), "Should have violations for empty object");
    }

    @Test
    public void testValidOrderCreateReqDTO() {
        OrderGoodsItemDTO item = new OrderGoodsItemDTO();
        item.setGoodsId(1);
        item.setQuantity(2);
        item.setSkuId(10);
        item.setUnitPrice(new BigDecimal("10.00"));

        OrderCreateReqDTO req = OrderCreateReqDTO.builder()
                .userId(1)
                .storeId(100)
                .preTotalAmount(new BigDecimal("20.00"))
                .items(Collections.singletonList(item))
                .build();

        Set<ConstraintViolation<OrderCreateReqDTO>> violations = validator.validate(req);
        assertTrue(violations.isEmpty(), "Should be valid: " + violations);
    }
}
