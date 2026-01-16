package com.fuint.openapi.v1.order;

import com.alibaba.fastjson.JSON;
import com.fuint.common.enums.OrderModeEnum;
import com.fuint.common.enums.OrderTypeEnum;
import com.fuint.common.enums.PayTypeEnum;
import com.fuint.openapi.v1.order.vo.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * OpenOrderController 集成测试类
 * 需要启动完整的Spring Boot上下文和数据库连接
 * 
 * 使用前请确保：
 * 1. 数据库服务已启动
 * 2. application-test.yaml配置正确
 * 3. 测试数据已准备
 * 
 * @author Test
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class OpenOrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 测试订单预创建接口
     */
    @Test
    public void testPreCreateOrderAPI() throws Exception {
        OrderPreCreateReqVO reqVO = new OrderPreCreateReqVO();
        reqVO.setUserId(1);
        reqVO.setMerchantId(1);
        reqVO.setStoreId(1);
        reqVO.setOrderMode(OrderModeEnum.ONESELF.getKey());
        reqVO.setPlatform("MP-WEIXIN");
        
        List<OrderGoodsItemVO> items = new ArrayList<>();
        OrderGoodsItemVO item = new OrderGoodsItemVO();
        item.setGoodsId(1);
        item.setSkuId(1);
        item.setQuantity(2);
        items.add(item);
        reqVO.setItems(items);

        String requestBody = JSON.toJSONString(reqVO);

        MvcResult result = mockMvc.perform(post("/api/v1/order/pre-create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.totalAmount").exists())
                .andExpect(jsonPath("$.data.payableAmount").exists())
                .andReturn();

        System.out.println("预创建订单响应: " + result.getResponse().getContentAsString());
    }

    /**
     * 测试创建订单接口
     */
    @Test
    public void testCreateOrderAPI() throws Exception {
        OrderCreateReqVO reqVO = new OrderCreateReqVO();
        reqVO.setUserId(1);
        reqVO.setMerchantId(1);
        reqVO.setStoreId(1);
        reqVO.setOrderMode(OrderModeEnum.ONESELF.getKey());
        reqVO.setPlatform("MP-WEIXIN");
        reqVO.setPreTotalAmount(new BigDecimal("100.00"));
        reqVO.setRemark("集成测试订单");
        reqVO.setType(OrderTypeEnum.GOODS);
        reqVO.setPayType(PayTypeEnum.JSAPI.getKey());
        
        List<OrderGoodsItemVO> items = new ArrayList<>();
        OrderGoodsItemVO item = new OrderGoodsItemVO();
        item.setGoodsId(1);
        item.setSkuId(1);
        item.setQuantity(1);
        items.add(item);
        reqVO.setItems(items);

        String requestBody = JSON.toJSONString(reqVO);

        MvcResult result = mockMvc.perform(post("/api/v1/order/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn();

        System.out.println("创建订单响应: " + result.getResponse().getContentAsString());
    }

    /**
     * 测试订单详情接口
     */
    @Test
    public void testGetOrderDetailAPI() throws Exception {
        Integer orderId = 1; // 使用实际存在的订单ID

        mockMvc.perform(get("/api/v1/order/detail/{id}", orderId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn();
    }

    /**
     * 测试订单列表接口
     */
    @Test
    public void testGetOrderListAPI() throws Exception {
        mockMvc.perform(get("/api/v1/order/list")
                .param("userId", "1")
                .param("page", "1")
                .param("pageSize", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andReturn();
    }

    /**
     * 测试取消订单接口
     */
    @Test
    public void testCancelOrderAPI() throws Exception {
        OrderCancelReqVO reqVO = new OrderCancelReqVO();
        reqVO.setOrderId(1); // 使用实际存在的订单ID
        reqVO.setRemark("集成测试取消");

        String requestBody = JSON.toJSONString(reqVO);

        mockMvc.perform(post("/api/v1/order/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn();
    }

    /**
     * 测试支付订单接口
     */
    @Test
    public void testPayOrderAPI() throws Exception {
        OrderPayReqVO reqVO = new OrderPayReqVO();
        reqVO.setOrderId(1); // 使用实际存在的订单ID
        reqVO.setPayAmount(new BigDecimal("100.00"));

        String requestBody = JSON.toJSONString(reqVO);

        mockMvc.perform(post("/api/v1/order/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn();
    }

    /**
     * 测试订单退款接口
     */
    @Test
    public void testRefundOrderAPI() throws Exception {
        OrderRefundReqVO reqVO = new OrderRefundReqVO();
        reqVO.setOrderId(1); // 使用实际存在的订单ID
        reqVO.setAmount(new BigDecimal("100.00"));
        reqVO.setRemark("集成测试退款");

        String requestBody = JSON.toJSONString(reqVO);

        mockMvc.perform(post("/api/v1/order/refund")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn();
    }

    /**
     * 测试订单评价接口
     */
    @Test
    public void testEvaluateOrderAPI() throws Exception {
        OrderEvaluateReqVO reqVO = new OrderEvaluateReqVO();
        reqVO.setOrderId(1); // 使用实际存在的订单ID
        reqVO.setScore(9);
        reqVO.setComment("服务非常好，很满意");

        String requestBody = JSON.toJSONString(reqVO);

        mockMvc.perform(post("/api/v1/order/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true))
                .andReturn();
    }

    /**
     * 测试订单评价拉取接口
     */
    @Test
    public void testGetEvaluationsAPI() throws Exception {
        mockMvc.perform(get("/api/v1/order/evaluations")
                .param("page", "1")
                .param("pageSize", "10")
                .param("startTime", "2026-01-01")
                .param("endTime", "2026-01-31"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andReturn();
    }
}
