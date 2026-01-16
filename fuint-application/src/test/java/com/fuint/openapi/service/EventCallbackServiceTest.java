package com.fuint.openapi.service;

import com.fuint.common.enums.SettingTypeEnum;
import com.fuint.common.service.SettingService;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.repository.model.MtOrder;
import com.fuint.repository.model.MtSetting;
import com.fuint.repository.model.MtUserCoupon;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * EventCallbackService 测试类
 * 
 * @author Test
 */
@RunWith(MockitoJUnitRunner.class)
public class EventCallbackServiceTest {

    @InjectMocks
    private EventCallbackService eventCallbackService;

    @Mock
    private SettingService settingService;

    private MtOrder mockOrder;
    private MtUserCoupon mockUserCoupon;
    private MtSetting mockSetting;

    @Before
    public void setUp() {
        // 初始化模拟订单
        mockOrder = new MtOrder();
        mockOrder.setId(10001);
        mockOrder.setOrderSn("ORDER20260116001");
        mockOrder.setUserId(1001);
        mockOrder.setMerchantId(1);
        mockOrder.setStoreId(1);
        mockOrder.setAmount(new BigDecimal("100.00"));
        mockOrder.setPayAmount(new BigDecimal("90.00"));
        mockOrder.setVerifyCode("1234");

        // 初始化模拟用户优惠券
        mockUserCoupon = new MtUserCoupon();
        mockUserCoupon.setId(1);
        mockUserCoupon.setUserId(1001);
        mockUserCoupon.setCouponId(1);
        mockUserCoupon.setCode("COUPON001");
        mockUserCoupon.setMerchantId(1);

        // 初始化模拟设置
        mockSetting = new MtSetting();
        mockSetting.setId(1);
        mockSetting.setValue("https://example.com/callback");
    }

    /**
     * 测试发送订单状态变更回调 - 成功场景
     */
    @Test
    public void testSendOrderStatusChangedCallback_Success() throws BusinessCheckException {
        // Mock服务调用
        when(settingService.querySettingByName(1, SettingTypeEnum.ORDER.getKey(), "callback_url"))
                .thenReturn(mockSetting);

        // 执行测试
        eventCallbackService.sendOrderStatusChangedCallback(mockOrder, "CREATED", "PAID");

        // 验证服务调用
        verify(settingService, times(1)).querySettingByName(1, SettingTypeEnum.ORDER.getKey(), "callback_url");
        
        // 等待异步线程执行（实际测试中可能需要更长的等待时间）
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 测试发送订单状态变更回调 - 订单为null
     */
    @Test
    public void testSendOrderStatusChangedCallback_NullOrder() throws BusinessCheckException {
        // 执行测试
        eventCallbackService.sendOrderStatusChangedCallback(null, "CREATED", "PAID");

        // 验证服务调用 - 不应该被调用
        verify(settingService, never()).querySettingByName(anyInt(), anyString(), anyString());
    }

    /**
     * 测试发送订单状态变更回调 - 未配置回调地址
     */
    @Test
    public void testSendOrderStatusChangedCallback_NoCallbackUrl() throws BusinessCheckException {
        // Mock服务调用 - 未配置回调地址
        when(settingService.querySettingByName(1, SettingTypeEnum.ORDER.getKey(), "callback_url"))
                .thenReturn(null);

        // 执行测试
        eventCallbackService.sendOrderStatusChangedCallback(mockOrder, "CREATED", "PAID");

        // 验证服务调用
        verify(settingService, times(1)).querySettingByName(1, SettingTypeEnum.ORDER.getKey(), "callback_url");
    }

    /**
     * 测试发送订单状态变更回调 - 回调地址为空
     */
    @Test
    public void testSendOrderStatusChangedCallback_EmptyCallbackUrl() throws BusinessCheckException {
        // Mock服务调用 - 回调地址为空
        MtSetting emptySetting = new MtSetting();
        emptySetting.setValue("");
        when(settingService.querySettingByName(1, SettingTypeEnum.ORDER.getKey(), "callback_url"))
                .thenReturn(emptySetting);

        // 执行测试
        eventCallbackService.sendOrderStatusChangedCallback(mockOrder, "CREATED", "PAID");

        // 验证服务调用
        verify(settingService, times(1)).querySettingByName(1, SettingTypeEnum.ORDER.getKey(), "callback_url");
    }

    /**
     * 测试发送订单状态变更回调 - 无旧状态
     */
    @Test
    public void testSendOrderStatusChangedCallback_NoOldStatus() throws BusinessCheckException {
        // Mock服务调用
        when(settingService.querySettingByName(1, SettingTypeEnum.ORDER.getKey(), "callback_url"))
                .thenReturn(mockSetting);

        // 执行测试（不传旧状态）
        eventCallbackService.sendOrderStatusChangedCallback(mockOrder, null, "PAID");

        // 验证服务调用
        verify(settingService, times(1)).querySettingByName(1, SettingTypeEnum.ORDER.getKey(), "callback_url");
    }

    /**
     * 测试发送订单支付状态变更回调 - 成功场景
     */
    @Test
    public void testSendPaymentStatusChangedCallback_Success() throws BusinessCheckException {
        // Mock服务调用
        when(settingService.querySettingByName(1, SettingTypeEnum.ORDER.getKey(), "callback_url"))
                .thenReturn(mockSetting);

        // 执行测试
        eventCallbackService.sendPaymentStatusChangedCallback(mockOrder, "SUCCESS");

        // 验证服务调用
        verify(settingService, times(1)).querySettingByName(1, SettingTypeEnum.ORDER.getKey(), "callback_url");
        
        // 等待异步线程执行
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 测试发送订单支付状态变更回调 - 订单为null
     */
    @Test
    public void testSendPaymentStatusChangedCallback_NullOrder() throws BusinessCheckException {
        // 执行测试
        eventCallbackService.sendPaymentStatusChangedCallback(null, "SUCCESS");

        // 验证服务调用 - 不应该被调用
        verify(settingService, never()).querySettingByName(anyInt(), anyString(), anyString());
    }

    /**
     * 测试发送订单可取餐状态通知回调 - 成功场景
     */
    @Test
    public void testSendOrderReadyCallback_Success() throws BusinessCheckException {
        // Mock服务调用
        when(settingService.querySettingByName(1, SettingTypeEnum.ORDER.getKey(), "callback_url"))
                .thenReturn(mockSetting);

        // 准备可取餐商品列表
        Object items = new Object();

        // 执行测试
        eventCallbackService.sendOrderReadyCallback(mockOrder, items);

        // 验证服务调用
        verify(settingService, times(1)).querySettingByName(1, SettingTypeEnum.ORDER.getKey(), "callback_url");
        
        // 等待异步线程执行
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 测试发送订单可取餐状态通知回调 - 订单为null
     */
    @Test
    public void testSendOrderReadyCallback_NullOrder() throws BusinessCheckException {
        // 执行测试
        eventCallbackService.sendOrderReadyCallback(null, new Object());

        // 验证服务调用 - 不应该被调用
        verify(settingService, never()).querySettingByName(anyInt(), anyString(), anyString());
    }

    /**
     * 测试发送订单可取餐状态通知回调 - 无验证码
     */
    @Test
    public void testSendOrderReadyCallback_NoVerifyCode() throws BusinessCheckException {
        // 设置订单无验证码
        mockOrder.setVerifyCode(null);

        // Mock服务调用
        when(settingService.querySettingByName(1, SettingTypeEnum.ORDER.getKey(), "callback_url"))
                .thenReturn(mockSetting);

        // 执行测试
        eventCallbackService.sendOrderReadyCallback(mockOrder, null);

        // 验证服务调用
        verify(settingService, times(1)).querySettingByName(1, SettingTypeEnum.ORDER.getKey(), "callback_url");
    }

    /**
     * 测试发送用户优惠券事件回调 - 成功场景（领取）
     */
    @Test
    public void testSendCouponEventCallback_Received() throws BusinessCheckException {
        // Mock服务调用
        when(settingService.querySettingByName(1, SettingTypeEnum.ORDER.getKey(), "callback_url"))
                .thenReturn(mockSetting);

        // 执行测试
        eventCallbackService.sendCouponEventCallback(mockUserCoupon, "RECEIVED", null);

        // 验证服务调用
        verify(settingService, times(1)).querySettingByName(1, SettingTypeEnum.ORDER.getKey(), "callback_url");
        
        // 等待异步线程执行
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 测试发送用户优惠券事件回调 - 成功场景（使用）
     */
    @Test
    public void testSendCouponEventCallback_Used() throws BusinessCheckException {
        // Mock服务调用
        when(settingService.querySettingByName(1, SettingTypeEnum.ORDER.getKey(), "callback_url"))
                .thenReturn(mockSetting);

        // 执行测试
        eventCallbackService.sendCouponEventCallback(mockUserCoupon, "USED", "ORDER20260116001");

        // 验证服务调用
        verify(settingService, times(1)).querySettingByName(1, SettingTypeEnum.ORDER.getKey(), "callback_url");
        
        // 等待异步线程执行
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 测试发送用户优惠券事件回调 - 用户优惠券为null
     */
    @Test
    public void testSendCouponEventCallback_NullUserCoupon() throws BusinessCheckException {
        // 执行测试
        eventCallbackService.sendCouponEventCallback(null, "RECEIVED", null);

        // 验证服务调用 - 不应该被调用
        verify(settingService, never()).querySettingByName(anyInt(), anyString(), anyString());
    }

    /**
     * 测试发送用户优惠券事件回调 - 未配置回调地址
     */
    @Test
    public void testSendCouponEventCallback_NoCallbackUrl() throws BusinessCheckException {
        // Mock服务调用 - 未配置回调地址
        when(settingService.querySettingByName(1, SettingTypeEnum.ORDER.getKey(), "callback_url"))
                .thenReturn(null);

        // 执行测试
        eventCallbackService.sendCouponEventCallback(mockUserCoupon, "RECEIVED", null);

        // 验证服务调用
        verify(settingService, times(1)).querySettingByName(1, SettingTypeEnum.ORDER.getKey(), "callback_url");
    }

    /**
     * 测试发送用户优惠券事件回调 - 过期
     */
    @Test
    public void testSendCouponEventCallback_Expired() throws BusinessCheckException {
        // Mock服务调用
        when(settingService.querySettingByName(1, SettingTypeEnum.ORDER.getKey(), "callback_url"))
                .thenReturn(mockSetting);

        // 执行测试
        eventCallbackService.sendCouponEventCallback(mockUserCoupon, "EXPIRED", null);

        // 验证服务调用
        verify(settingService, times(1)).querySettingByName(1, SettingTypeEnum.ORDER.getKey(), "callback_url");
    }

    /**
     * 测试发送用户优惠券事件回调 - 撤销
     */
    @Test
    public void testSendCouponEventCallback_Revoked() throws BusinessCheckException {
        // Mock服务调用
        when(settingService.querySettingByName(1, SettingTypeEnum.ORDER.getKey(), "callback_url"))
                .thenReturn(mockSetting);

        // 执行测试
        eventCallbackService.sendCouponEventCallback(mockUserCoupon, "REVOKED", null);

        // 验证服务调用
        verify(settingService, times(1)).querySettingByName(1, SettingTypeEnum.ORDER.getKey(), "callback_url");
    }
}
