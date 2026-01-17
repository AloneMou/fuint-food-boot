-- 为 mt_coupon 表添加优惠费率和最大优惠金额字段
-- 执行时间: 2026-01-17

-- 添加优惠费率字段（0-100，100表示全免）
ALTER TABLE `mt_coupon` 
ADD COLUMN `DISCOUNT_RATE` INT DEFAULT 0 COMMENT '优惠费率（0-100，100表示全免）' AFTER `AMOUNT`;

-- 添加最大优惠金额字段
ALTER TABLE `mt_coupon` 
ADD COLUMN `MAX_DISCOUNT_AMOUNT` DECIMAL(10,2) DEFAULT 0.00 COMMENT '最大优惠金额' AFTER `DISCOUNT_RATE`;
