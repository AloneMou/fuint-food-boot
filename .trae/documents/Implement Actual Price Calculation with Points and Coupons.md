I have modified `OpenGoodsServiceImpl.java` and `CGoodsListRespVO.java` to include the calculation of usable points, usable coupons, and the actual price after deduction.

**Changes:**

1. **`CGoodsListRespVO.java`**:

   * Added `pointDeduction` (BigDecimal): Amount deducted by points.

   * Added `couponDeduction` (BigDecimal): Amount deducted by coupons.

   * Added `realPrice` (BigDecimal): The actual price to be paid after all deductions.

2. **`OpenGoodsServiceImpl.java`**:

   * Injected `SettingService`, `UserCouponService`, `CouponService`, and `MtCouponGoodsMapper`.

   * Updated `getGoodsList` to pre-fetch:

     * User information (for point balance).

     * Point settings (for exchange rate and usage permission).

     * User's unused coupons (including checking which goods they apply to).

   * Updated `buildGoodsVO` to calculate:

     * **Member Discount**: Already existed (affects `dynamicPrice`).

     * **Coupon Deduction**: Iterates through available coupons, checks applicability (goods ID, price threshold), and finds the maximum deduction amount.

     * **Point Deduction**: Calculates maximum point usage based on remaining price and exchange rate.

     * **Real Price**: `dynamicPrice - couponDeduction - pointDeduction`.

   * This logic is applied for single-specification goods, consistent with the existing `dynamicPrice` calculation.

**Code References:**

* [CGoodsListRespVO.java](d:\Project\Aite\Foot-Fuint-Backend-master\fuint-application\src\main\java\com\fuint\openapi\v1\goods\product\vo\response\CGoodsListRespVO.java)

* [OpenGoodsServiceImpl.java](d:\Project\Aite\Foot-Fuint-Backend-master\fuint-application\src\main\java\com\fuint\openapi\service\impl\OpenGoodsServiceImpl.java)

