package com.fuint.openapi.v1.goods.comment;

import cn.iocoder.yudao.framework.ratelimiter.core.annotation.RateLimiter;
import cn.iocoder.yudao.framework.ratelimiter.core.keyresolver.impl.ClientIpRateLimiterKeyResolver;
import cn.iocoder.yudao.framework.signature.core.annotation.ApiSignature;
import com.fuint.common.service.GoodsCommentService;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pojo.CommonResult;
import com.fuint.framework.pojo.PageResult;
import com.fuint.framework.web.BaseController;
import com.fuint.openapi.v1.goods.comment.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static com.fuint.openapi.enums.CommentErrorCodeConstants.*;

/**
 * OpenAPI商品评价相关接口
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Validated
@Api(tags = "OpenApi-商品评价相关接口")
@RestController
@RequestMapping(value = "/api/v1/goods/comment")
public class OpenCommentController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(OpenCommentController.class);

    @Resource
    private GoodsCommentService goodsCommentService;

    /**
     * 分页查询评价列表
     *
     * @param pageReqVO 查询条件
     * @return 分页结果
     */
    @ApiOperation(value = "分页查询评价列表", notes = "支持按商品、订单、用户、评分等条件筛选")
    @GetMapping(value = "/page")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<PageResult<CommentRespVO>> getCommentPage(@Valid CommentPageReqVO pageReqVO) {
        PageResult<CommentRespVO> pageResult = goodsCommentService.queryCommentPageList(pageReqVO);
        return CommonResult.success(pageResult);
    }

    /**
     * 获取评价详情
     *
     * @param id 评价ID
     * @return 评价详情
     */
    @ApiOperation(value = "获取评价详情", notes = "根据ID获取评价详细信息")
    @GetMapping(value = "/detail/{id}")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<CommentRespVO> getCommentDetail(
            @ApiParam(value = "评价ID", required = true, example = "1")
            @PathVariable("id") Integer id) {
        CommentRespVO respVO = goodsCommentService.queryCommentById(id);
        if (respVO == null) {
            return CommonResult.error(COMMENT_NOT_FOUND);
        }
        return CommonResult.success(respVO);
    }

    /**
     * 提交评价
     *
     * @param createReqVO 创建请求
     * @return 评价ID
     */
    @ApiOperation(value = "提交评价", notes = "用户提交评价，支持商品评价(1-5分)和订单NPS评价(0-10分)")
    @PostMapping(value = "/create")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<Integer> createComment(@Valid @RequestBody CommentCreateReqVO createReqVO) {
        try {
            Integer commentId = goodsCommentService.createComment(createReqVO);
            return CommonResult.success(commentId);
        } catch (BusinessCheckException e) {
            log.warn("创建评价失败：{}", e.getMessage());
            return CommonResult.error(500, e.getMessage());
        }
    }

    /**
     * 修改评价
     *
     * @param updateReqVO 更新请求
     * @return 操作结果
     */
    @ApiOperation(value = "修改评价", notes = "用户修改自己的评价")
    @PutMapping(value = "/update")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<Boolean> updateComment(@Valid @RequestBody CommentUpdateReqVO updateReqVO) {
        try {
            Boolean result = goodsCommentService.updateComment(updateReqVO);
            return CommonResult.success(result);
        } catch (BusinessCheckException e) {
            log.warn("更新评价失败：{}", e.getMessage());
            return CommonResult.error(500, e.getMessage());
        }
    }

    /**
     * 删除评价
     *
     * @param deleteReqVO 删除请求
     * @return 操作结果
     */
    @ApiOperation(value = "删除评价", notes = "用户删除自己的评价")
    @DeleteMapping(value = "/delete")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<Boolean> deleteComment(@Valid @RequestBody CommentDeleteReqVO deleteReqVO) {
        try {
            Boolean result = goodsCommentService.deleteComment(deleteReqVO);
            return CommonResult.success(result);
        } catch (BusinessCheckException e) {
            log.warn("删除评价失败：{}", e.getMessage());
            return CommonResult.error(500, e.getMessage());
        }
    }

    /**
     * 商家回复评价
     *
     * @param replyReqVO 回复请求
     * @return 操作结果
     */
    @ApiOperation(value = "商家回复评价", notes = "商家回复用户的评价")
    @PostMapping(value = "/reply")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<Boolean> replyComment(@Valid @RequestBody CommentReplyReqVO replyReqVO) {
        try {
            Boolean result = goodsCommentService.replyComment(replyReqVO);
            return CommonResult.success(result);
        } catch (BusinessCheckException e) {
            log.warn("回复评价失败：{}", e.getMessage());
            return CommonResult.error(500, e.getMessage());
        }
    }

    /**
     * 获取商品评价统计信息
     *
     * @param goodsId 商品ID
     * @return 统计信息
     */
    @ApiOperation(value = "获取商品评价统计", notes = "获取商品的评价统计信息，包括平均分、各星级数量等")
    @GetMapping(value = "/statistics/{goodsId}")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<CommentStatisticsVO> getCommentStatistics(
            @ApiParam(value = "商品ID", required = true, example = "1")
            @PathVariable("goodsId") Integer goodsId) {
        CommentStatisticsVO statisticsVO = goodsCommentService.getCommentStatistics(goodsId);
        return CommonResult.success(statisticsVO);
    }

    /**
     * 获取店铺NPS统计信息
     *
     * @param storeId 店铺ID
     * @return 统计信息
     */
    @ApiOperation(value = "获取店铺NPS统计", notes = "获取店铺的NPS评价统计信息")
    @GetMapping(value = "/statistics/nps/store/{storeId}")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<CommentStatisticsVO> getStoreNpsStatistics(
            @ApiParam(value = "店铺ID", required = true, example = "1")
            @PathVariable("storeId") Integer storeId) {
        CommentStatisticsVO statisticsVO = goodsCommentService.getStoreNpsStatistics(storeId);
        return CommonResult.success(statisticsVO);
    }

    /**
     * 获取商户NPS统计信息
     *
     * @param merchantId 商户ID
     * @return 统计信息
     */
    @ApiOperation(value = "获取商户NPS统计", notes = "获取商户的NPS评价统计信息")
    @GetMapping(value = "/statistics/nps/merchant/{merchantId}")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<CommentStatisticsVO> getMerchantNpsStatistics(
            @ApiParam(value = "商户ID", required = true, example = "1")
            @PathVariable("merchantId") Integer merchantId) {
        CommentStatisticsVO statisticsVO = goodsCommentService.getMerchantNpsStatistics(merchantId);
        return CommonResult.success(statisticsVO);
    }

    /**
     * 查询用户的评价列表
     *
     * @param userId 用户ID
     * @return 评价列表
     */
    @ApiOperation(value = "查询用户评价列表", notes = "查询指定用户的所有评价")
    @GetMapping(value = "/user/{userId}")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<List<CommentRespVO>> getUserCommentList(
            @ApiParam(value = "用户ID", required = true, example = "1")
            @PathVariable("userId") Integer userId) {
        List<CommentRespVO> commentList = goodsCommentService.queryUserCommentList(userId);
        return CommonResult.success(commentList);
    }

    /**
     * 查询商品的评价列表
     *
     * @param goodsId 商品ID
     * @return 评价列表
     */
    @ApiOperation(value = "查询商品评价列表", notes = "查询指定商品的所有评价")
    @GetMapping(value = "/goods/{goodsId}")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<List<CommentRespVO>> getGoodsCommentList(
            @ApiParam(value = "商品ID", required = true, example = "1")
            @PathVariable("goodsId") Integer goodsId) {
        List<CommentRespVO> commentList = goodsCommentService.queryGoodsCommentList(goodsId);
        return CommonResult.success(commentList);
    }

    /**
     * 检查是否已评价
     *
     * @param orderId 订单ID
     * @param goodsId 商品ID
     * @param userId  用户ID
     * @return 是否已评价
     */
    @ApiOperation(value = "检查是否已评价", notes = "检查指定订单的商品是否已被用户评价")
    @GetMapping(value = "/check")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<Boolean> checkCommentExists(
            @ApiParam(value = "订单ID", required = true, example = "1")
            @RequestParam("orderId") Integer orderId,
            @ApiParam(value = "商品ID", required = true, example = "1")
            @RequestParam("goodsId") Integer goodsId,
            @ApiParam(value = "用户ID", required = true, example = "1")
            @RequestParam("userId") Integer userId) {
        Boolean exists = goodsCommentService.checkCommentExists(orderId, goodsId, userId);
        return CommonResult.success(exists);
    }

    /**
     * 点赞评价
     *
     * @param id 评价ID
     * @return 操作结果
     */
    @ApiOperation(value = "点赞评价", notes = "为评价点赞")
    @PostMapping(value = "/like/{id}")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<Boolean> likeComment(
            @ApiParam(value = "评价ID", required = true, example = "1")
            @PathVariable("id") Integer id) {
        Boolean result = goodsCommentService.likeComment(id);
        if (!result) {
            return CommonResult.error(COMMENT_NOT_FOUND);
        }
        return CommonResult.success(result);
    }

}
