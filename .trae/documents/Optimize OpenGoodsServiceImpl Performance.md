I have identified the performance bottleneck in `OpenGoodsServiceImpl.java`. The current implementation performs database queries inside a loop when processing user coupons (N+1 problem), which causes significant delays when a user has multiple coupons.

### Bottleneck Analysis
- **Repeated Coupon Queries**: `couponService.queryCouponById(uc.getCouponId())` is called for every user coupon.
- **Repeated Goods Queries**: `mtCouponGoodsMapper.getCouponGoods(coupon.getId())` is called for every coupon that has product restrictions.
- **Impact**: If a user has 20 coupons, this results in 20-40 separate database queries sequentially before any price calculation happens.

### Optimization Plan
I will refactor the code to use **Batch Fetching**:

1.  **Batch Fetch Coupons**:
    - Extract all `couponId`s from the user's coupon list.
    - Call `couponService.queryCouponListByIds(List<Integer> ids)` to fetch all coupon details in a single query.
    - Store them in a `Map<Integer, MtCoupon>` for fast lookup.

2.  **Batch Fetch Coupon Goods**:
    - Filter coupons that are restricted to specific goods (`ApplyGoodsEnum.PARK_GOODS`).
    - Fetch all related goods associations for these coupons in a single query using `mtCouponGoodsMapper.selectList` with an `IN` clause.
    - Group the results into a `Map<Integer, Set<Integer>>` (Coupon ID -> Set of Goods IDs).

3.  **Refactor Loop Logic**:
    - Rewrite the coupon processing loop to use these pre-fetched maps instead of making DB calls.

### Expected Result
- Database queries for coupon processing will be reduced from `O(N)` to `O(1)` (constant 2-3 queries regardless of coupon count).
- Response time should drop significantly (likely under 200ms for this part).

### Verification
- I will verify the code changes to ensure all logic (validity checks, effectiveness checks) remains exactly the same, just faster.
