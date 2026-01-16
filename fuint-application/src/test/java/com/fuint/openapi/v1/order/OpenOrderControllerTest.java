package com.fuint.openapi.v1.order;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fuint.common.dto.OrderDto;
import com.fuint.common.dto.ResCartDto;
import com.fuint.common.dto.UserOrderDto;
import com.fuint.common.enums.*;
import com.fuint.common.param.OrderListParam;
import com.fuint.common.service.*;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.pojo.CommonResult;
import com.fuint.openapi.v1.order.vo.*;
import com.fuint.repository.mapper.MtOrderGoodsMapper;
import com.fuint.repository.mapper.MtUserActionMapper;
import com.fuint.repository.model.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * OpenOrderController 测试类
 * 
 * @author Test
 */
@RunWith(MockitoJUnitRunner.class)
public class OpenOrderControllerTest {

    @InjectMocks
    private OpenOrderController openOrderController;

    @Mock
    private OrderService orderService;

    @Mock
    private MemberService memberService;

    @Mock
    private CartService cartService;

    @Mock
    private GoodsService goodsService;

    @Mock
    private SettingService settingService;

    @Mock
    private RefundService refundService;

    @Mock
    private MtUserActionMapper mtUserActionMapper;

    @Mock
    private MtOrderGoodsMapper mtOrderGoodsMapper;

    private MtUser mockUser;
    private MtOrder mockOrder;
    private Map<String, Object> mockPreCreateResult;

    @Before
    public void setUp() {
        // 初始化模拟用户
        mockUser = new MtUser();
        mockUser.setId(1001);
        mockUser.setName("测试用户");
        mockUser.setMobile("13800138000");
        mockUser.setStatus(StatusEnum.ENABLED.getKey());

        // 初始化模拟订单
        mockOrder = new MtOrder();
        mockOrder.setId(10001);
        mockOrder.setOrderSn("ORDER20260116001");
        mockOrder.setUserId(1001);
        mockOrder.setMerchantId(1);
        mockOrder.setStoreId(1);
        mockOrder.setAmount(new BigDecimal("100.00"));
        mockOrder.setPayAmount(new BigDecimal("90.00"));
        mockOrder.setStatus(OrderStatusEnum.CREATED.getKey());
        mockOrder.setPayStatus(PayStatusEnum.WAIT.getKey());
        mockOrder.setOrderMode(OrderModeEnum.ONESELF.getKey());

        // 初始化预创建结果
        mockPreCreateResult = new HashMap<>();
        mockPreCreateResult.put("totalAmount", new BigDecimal("100.00"));
        mockPreCreateResult.put("discountAmount", new BigDecimal("10.00"));
        mockPreCreateResult.put("pointAmount", new BigDecimal("0.00"));
        mockPreCreateResult.put("deliveryFee", new BigDecimal("0.00"));
        mockPreCreateResult.put("payableAmount", new BigDecimal("90.00"));
        mockPreCreateResult.put("usePoint", 0);
        mockPreCreateResult.put("availablePoint", 100);
        mockPreCreateResult.put("selectedCouponId", 0);
        mockPreCreateResult.put("calculateTime", new Date());
        mockPreCreateResult.put("availableCoupons", new ArrayList<>());
        
        // 模拟商品列表
        List<ResCartDto> goodsList = new ArrayList<>();
        ResCartDto cartDto = new ResCartDto();
        cartDto.setGoodsId(1);
        cartDto.setSkuId(1);
        cartDto.setNum(2);
        
        MtGoods goodsInfo = new MtGoods();
        goodsInfo.setId(1);
        goodsInfo.setName("测试商品");
        goodsInfo.setPrice(new BigDecimal("50.00"));
        goodsInfo.setLinePrice(new BigDecimal("60.00"));
        goodsInfo.setLogo("/images/goods.jpg");
        cartDto.setGoodsInfo(goodsInfo);
        
        goodsList.add(cartDto);
        mockPreCreateResult.put("goodsList", goodsList);
    }

    /**
     * 测试订单预创建 - 成功场景
     */
    @Test
    public void testPreCreateOrder_Success() throws Exception {
        // 准备请求参数
        OrderPreCreateReqVO reqVO = new OrderPreCreateReqVO();
        reqVO.setUserId(1001);
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

        // Mock服务调用
        when(memberService.queryMemberById(1001)).thenReturn(mockUser);
        when(orderService.preCreateOrder(anyInt(), anyInt(), anyList(), anyInt(), anyInt(), anyString(), anyString(), anyInt()))
                .thenReturn(mockPreCreateResult);
        when(settingService.getUploadBasePath()).thenReturn("https://example.com");

        // 执行测试
        CommonResult<OrderPreCreateRespVO> result = openOrderController.preCreateOrder(reqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertNotNull(result.getData());
        
        OrderPreCreateRespVO respVO = result.getData();
        assertEquals(new BigDecimal("100.00"), respVO.getTotalAmount());
        assertEquals(new BigDecimal("90.00"), respVO.getPayableAmount());
        assertEquals(new BigDecimal("10.00"), respVO.getDiscountAmount());
        assertNotNull(respVO.getGoodsList());
        assertEquals(1, respVO.getGoodsList().size());

        // 验证服务调用
        verify(memberService, times(1)).queryMemberById(1001);
        verify(orderService, times(1)).preCreateOrder(anyInt(), anyInt(), anyList(), anyInt(), anyInt(), anyString(), anyString(), anyInt());
    }

    /**
     * 测试订单预创建 - 用户不存在
     */
    @Test
    public void testPreCreateOrder_UserNotFound() throws Exception {
        OrderPreCreateReqVO reqVO = new OrderPreCreateReqVO();
        reqVO.setUserId(9999);
        
        List<OrderGoodsItemVO> items = new ArrayList<>();
        OrderGoodsItemVO item = new OrderGoodsItemVO();
        item.setGoodsId(1);
        item.setQuantity(1);
        items.add(item);
        reqVO.setItems(items);

        // Mock用户不存在
        when(memberService.queryMemberById(9999)).thenReturn(null);

        // 执行测试
        CommonResult<OrderPreCreateRespVO> result = openOrderController.preCreateOrder(reqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(404), result.getCode());
        assertEquals("用户不存在", result.getMsg());
    }

    /**
     * 测试创建订单 - 成功场景
     */
    @Test
    public void testCreateOrder_Success() throws Exception {
        // 准备请求参数
        OrderCreateReqVO reqVO = new OrderCreateReqVO();
        reqVO.setUserId(1001);
        reqVO.setMerchantId(1);
        reqVO.setStoreId(1);
        reqVO.setOrderMode(OrderModeEnum.ONESELF.getKey());
        reqVO.setPlatform("MP-WEIXIN");
        reqVO.setPreTotalAmount(new BigDecimal("90.00"));
        reqVO.setRemark("测试订单");
        reqVO.setType(OrderTypeEnum.GOODS);
        reqVO.setPayType(PayTypeEnum.JSAPI.getKey());
        
        List<OrderGoodsItemVO> items = new ArrayList<>();
        OrderGoodsItemVO item = new OrderGoodsItemVO();
        item.setGoodsId(1);
        item.setSkuId(1);
        item.setQuantity(2);
        items.add(item);
        reqVO.setItems(items);

        // Mock服务调用
        when(memberService.queryMemberById(1001)).thenReturn(mockUser);
        when(orderService.preCreateOrder(anyInt(), anyInt(), anyList(), anyInt(), anyInt(), anyString(), anyString(), anyInt()))
                .thenReturn(mockPreCreateResult);
        when(orderService.saveOrder(any(OrderDto.class))).thenReturn(mockOrder);
        
        UserOrderDto userOrderDto = new UserOrderDto();
        userOrderDto.setId(mockOrder.getId());
        userOrderDto.setOrderSn(mockOrder.getOrderSn());
        when(orderService.getOrderById(mockOrder.getId())).thenReturn(userOrderDto);

        // 执行测试
        CommonResult<UserOrderDto> result = openOrderController.createOrder(reqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertNotNull(result.getData());
        assertEquals(mockOrder.getId(), result.getData().getId());
        assertEquals(mockOrder.getOrderSn(), result.getData().getOrderSn());

        // 验证服务调用
        verify(memberService, times(1)).queryMemberById(1001);
        verify(orderService, times(1)).saveOrder(any(OrderDto.class));
        verify(orderService, times(1)).getOrderById(mockOrder.getId());
    }

    /**
     * 测试创建订单 - 价格不一致
     */
    @Test
    public void testCreateOrder_PriceMismatch() throws Exception {
        OrderCreateReqVO reqVO = new OrderCreateReqVO();
        reqVO.setUserId(1001);
        reqVO.setPreTotalAmount(new BigDecimal("80.00")); // 与预期不符
        
        List<OrderGoodsItemVO> items = new ArrayList<>();
        OrderGoodsItemVO item = new OrderGoodsItemVO();
        item.setGoodsId(1);
        item.setQuantity(2);
        items.add(item);
        reqVO.setItems(items);

        // Mock服务调用
        when(memberService.queryMemberById(1001)).thenReturn(mockUser);
        when(orderService.preCreateOrder(anyInt(), anyInt(), anyList(), anyInt(), anyInt(), anyString(), anyString(), anyInt()))
                .thenReturn(mockPreCreateResult);

        // 执行测试
        CommonResult<UserOrderDto> result = openOrderController.createOrder(reqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(400), result.getCode());
        assertEquals("商品更新,请重新下单", result.getMsg());
    }

    /**
     * 测试取消订单 - 未支付订单
     */
    @Test
    public void testCancelOrder_Unpaid() throws Exception {
        OrderCancelReqVO reqVO = new OrderCancelReqVO();
        reqVO.setOrderId(10001);
        reqVO.setRemark("用户取消");

        // Mock订单状态为未支付
        mockOrder.setPayStatus(PayStatusEnum.WAIT.getKey());
        when(orderService.getOrderInfo(10001)).thenReturn(mockOrder);
        doNothing().when(orderService).cancelOrder(10001, "用户取消");

        // 执行测试
        CommonResult<Boolean> result = openOrderController.cancelOrder(reqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertTrue(result.getData());

        // 验证服务调用
        verify(orderService, times(1)).cancelOrder(10001, "用户取消");
        verify(refundService, never()).doRefund(anyInt(), anyString(), anyString(), any());
    }

    /**
     * 测试取消订单 - 已支付订单（触发退款）
     */
    @Test
    public void testCancelOrder_PaidWithRefund() throws Exception {
        OrderCancelReqVO reqVO = new OrderCancelReqVO();
        reqVO.setOrderId(10001);
        reqVO.setRemark("用户取消");

        // Mock订单状态为已支付
        mockOrder.setPayStatus(PayStatusEnum.SUCCESS.getKey());
        mockOrder.setPayAmount(new BigDecimal("90.00"));
        when(orderService.getOrderInfo(10001)).thenReturn(mockOrder);
        when(refundService.doRefund(anyInt(), anyString(), anyString(), any())).thenReturn(true);
        doNothing().when(orderService).cancelOrder(10001, "用户取消");

        // 执行测试
        CommonResult<Boolean> result = openOrderController.cancelOrder(reqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertTrue(result.getData());

        // 验证服务调用
        verify(refundService, times(1)).doRefund(eq(10001), eq("90.00"), eq("订单取消自动退款"), any());
        verify(orderService, times(1)).cancelOrder(10001, "用户取消");
    }

    /**
     * 测试取消订单 - 订单不存在
     */
    @Test
    public void testCancelOrder_OrderNotFound() throws Exception {
        OrderCancelReqVO reqVO = new OrderCancelReqVO();
        reqVO.setOrderId(99999);
        reqVO.setRemark("用户取消");

        when(orderService.getOrderInfo(99999)).thenReturn(null);

        // 执行测试
        CommonResult<Boolean> result = openOrderController.cancelOrder(reqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(404), result.getCode());
        assertEquals("订单不存在", result.getMsg());
    }

    /**
     * 测试支付订单 - 成功
     */
    @Test
    public void testPayOrder_Success() throws Exception {
        OrderPayReqVO reqVO = new OrderPayReqVO();
        reqVO.setOrderId(10001);
        reqVO.setPayAmount(new BigDecimal("90.00"));

        when(orderService.getOrderInfo(10001)).thenReturn(mockOrder);
        when(orderService.setOrderPayed(10001, new BigDecimal("90.00"))).thenReturn(true);
        when(settingService.querySettingByName(anyInt(), anyString(), anyString())).thenReturn(null);

        // 执行测试
        CommonResult<Boolean> result = openOrderController.payOrder(reqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertTrue(result.getData());

        // 验证服务调用
        verify(orderService, times(1)).setOrderPayed(10001, new BigDecimal("90.00"));
    }

    /**
     * 测试支付订单 - 订单不存在
     */
    @Test
    public void testPayOrder_OrderNotFound() throws Exception {
        OrderPayReqVO reqVO = new OrderPayReqVO();
        reqVO.setOrderId(99999);
        reqVO.setPayAmount(new BigDecimal("90.00"));

        when(orderService.getOrderInfo(99999)).thenReturn(null);

        // 执行测试
        CommonResult<Boolean> result = openOrderController.payOrder(reqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(404), result.getCode());
        assertEquals("订单不存在", result.getMsg());
    }

    /**
     * 测试订单退款 - 成功
     */
    @Test
    public void testRefundOrder_Success() throws Exception {
        OrderRefundReqVO reqVO = new OrderRefundReqVO();
        reqVO.setOrderId(10001);
        reqVO.setAmount(new BigDecimal("90.00"));
        reqVO.setRemark("用户申请退款");

        when(refundService.doRefund(anyInt(), anyString(), anyString(), any())).thenReturn(true);
        when(orderService.getOrderInfo(10001)).thenReturn(mockOrder);
        when(settingService.querySettingByName(anyInt(), anyString(), anyString())).thenReturn(null);

        // 执行测试
        CommonResult<Boolean> result = openOrderController.refundOrder(reqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertTrue(result.getData());

        // 验证服务调用
        verify(refundService, times(1)).doRefund(eq(10001), eq("90.00"), eq("用户申请退款"), any());
    }

    /**
     * 测试获取订单详情 - 成功
     */
    @Test
    public void testGetOrderDetail_Success() throws Exception {
        UserOrderDto userOrderDto = new UserOrderDto();
        userOrderDto.setId(10001);
        userOrderDto.setOrderSn("ORDER20260116001");
        userOrderDto.setUserId(1001);
        
        when(orderService.getOrderById(10001)).thenReturn(userOrderDto);
        when(orderService.list(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());

        // 执行测试
        CommonResult<Map<String, Object>> result = openOrderController.getOrderDetail(10001);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertNotNull(result.getData());
        
        Map<String, Object> data = result.getData();
        assertNotNull(data.get("order"));
        assertEquals(0, data.get("queueCount"));
        assertEquals(0, data.get("estimatedWaitTime"));
    }

    /**
     * 测试获取订单详情 - 订单不存在
     */
    @Test
    public void testGetOrderDetail_NotFound() throws Exception {
        when(orderService.getOrderById(99999)).thenReturn(null);

        // 执行测试
        CommonResult<Map<String, Object>> result = openOrderController.getOrderDetail(99999);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(404), result.getCode());
        assertEquals("订单不存在", result.getMsg());
    }

    /**
     * 测试订单列表查询 - 成功
     */
    @Test
    public void testGetOrderList_Success() throws Exception {
        OrderListReqVO reqVO = new OrderListReqVO();
        reqVO.setUserId(1001);
        reqVO.setPage(1);
        reqVO.setPageSize(10);

        List<UserOrderDto> orderList = new ArrayList<>();
        UserOrderDto dto = new UserOrderDto();
        dto.setId(10001);
        dto.setOrderSn("ORDER20260116001");
        orderList.add(dto);
        
        PageRequest pageRequest = PageRequest.of(0, 10);
        org.springframework.data.domain.Page<UserOrderDto> page = new PageImpl<>(orderList, pageRequest, 1);
        PaginationResponse<UserOrderDto> paginationResponse = new PaginationResponse<>(page, UserOrderDto.class);
        
        when(orderService.getUserOrderList(any(OrderListParam.class))).thenReturn(paginationResponse);

        // 执行测试
        CommonResult<PaginationResponse<UserOrderDto>> result = openOrderController.getOrderList(reqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertNotNull(result.getData());

        // 验证服务调用
        verify(orderService, times(1)).getUserOrderList(any(OrderListParam.class));
    }

    /**
     * 测试订单评价 - 成功
     */
    @Test
    public void testEvaluateOrder_Success() {
        OrderEvaluateReqVO reqVO = new OrderEvaluateReqVO();
        reqVO.setOrderId(10001);
        reqVO.setScore(9);
        reqVO.setComment("服务很好");

        when(orderService.getById(10001)).thenReturn(mockOrder);
        when(mtUserActionMapper.insert(any(MtUserAction.class))).thenReturn(1);

        // 执行测试
        CommonResult<Boolean> result = openOrderController.evaluateOrder(reqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertTrue(result.getData());

        // 验证服务调用
        verify(mtUserActionMapper, times(1)).insert(any(MtUserAction.class));
    }

    /**
     * 测试订单评价拉取 - 成功
     */
    @Test
    public void testGetEvaluations_Success() {
        EvaluationListReqVO reqVO = new EvaluationListReqVO();
        reqVO.setPage(1);
        reqVO.setPageSize(10);
        reqVO.setStartTime("2026-01-01");
        reqVO.setEndTime("2026-01-31");

        List<MtUserAction> actionList = new ArrayList<>();
        MtUserAction action = new MtUserAction();
        action.setId(1);
        action.setAction("NPS_EVALUATION");
        action.setDescription("很好");
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", 10001);
        params.put("score", 9);
        action.setParam(JSON.toJSONString(params));
        actionList.add(action);

        when(mtUserActionMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(actionList);

        // 执行测试
        CommonResult<PaginationResponse<MtUserAction>> result = openOrderController.getEvaluations(reqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertNotNull(result.getData());
    }

    /**
     * 测试订单评价拉取 - 按SKU筛选
     */
    @Test
    public void testGetEvaluations_WithSkuFilter() {
        EvaluationListReqVO reqVO = new EvaluationListReqVO();
        reqVO.setPage(1);
        reqVO.setPageSize(10);
        
        List<Integer> skuIds = new ArrayList<>();
        skuIds.add(1);
        skuIds.add(2);
        reqVO.setSkuIds(skuIds);

        // Mock订单商品数据
        List<MtOrderGoods> orderGoodsList = new ArrayList<>();
        MtOrderGoods orderGoods = new MtOrderGoods();
        orderGoods.setOrderId(10001);
        orderGoods.setSkuId(1);
        orderGoodsList.add(orderGoods);
        
        when(mtOrderGoodsMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(orderGoodsList);
        when(mtUserActionMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());

        // 执行测试
        CommonResult<PaginationResponse<MtUserAction>> result = openOrderController.getEvaluations(reqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
    }
}
