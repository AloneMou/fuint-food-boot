package com.fuint.openapi.dto.response;

import com.alipay.api.domain.OrderGoodsDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fuint.openapi.dto.api.BaseResponse;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Builder
public class OrderInfoDTO implements BaseResponse {
    @Tolerate
    public OrderInfoDTO() {}

    private Integer id;
    private String orderSn;
    private String status;
    private BigDecimal amount;
    private BigDecimal payAmount;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    
    private List<OrderGoodsDTO> goods;
}
