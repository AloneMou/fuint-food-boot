package com.fuint.common.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fuint.common.enums.OrderStatusEnum;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.service.*;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pojo.PageResult;
import com.fuint.openapi.v1.goods.comment.vo.*;
import com.fuint.repository.mapper.MtGoodsCommentImageMapper;
import com.fuint.repository.mapper.MtGoodsCommentMapper;
import com.fuint.repository.mapper.MtOrderGoodsMapper;
import com.fuint.repository.model.*;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品评价服务实现类
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Service
@AllArgsConstructor
public class GoodsCommentServiceImpl extends ServiceImpl<MtGoodsCommentMapper, MtGoodsComment> implements GoodsCommentService {

    private static final Logger log = LoggerFactory.getLogger(GoodsCommentServiceImpl.class);

    private final MtGoodsCommentMapper mtGoodsCommentMapper;
    private final MtGoodsCommentImageMapper mtGoodsCommentImageMapper;
    private final MtOrderGoodsMapper mtOrderGoodsMapper;
    private final OrderService orderService;
    private final GoodsService goodsService;
    private final MemberService memberService;
    private final StoreService storeService;
    private final SettingService settingService;

    @Override
    public PageResult<CommentRespVO> queryCommentPageList(CommentPageReqVO pageReqVO) {
        // 设置默认状态查询（排除已删除的）
        if (StrUtil.isEmpty(pageReqVO.getStatus())) {
            pageReqVO.setStatus(StatusEnum.ENABLED.getKey());
        }
        
        PageResult<MtGoodsComment> pageResult = mtGoodsCommentMapper.selectCommentPage(pageReqVO);
        
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty();
        }
        
        // 转换为响应VO
        List<CommentRespVO> respVOList = pageResult.getList().stream()
                .map(this::convertToRespVO)
                .collect(Collectors.toList());
        
        PageResult<CommentRespVO> result = new PageResult<>();
        result.setList(respVOList);
        result.setTotal(pageResult.getTotal());
        result.setTotalPages(pageResult.getTotalPages());
        result.setCurrentPage(pageResult.getCurrentPage());
        result.setPageSize(pageResult.getPageSize());
        
        return result;
    }

    @Override
    public CommentRespVO queryCommentById(Integer commentId) {
        MtGoodsComment comment = mtGoodsCommentMapper.selectById(commentId);
        if (comment == null || StatusEnum.DISABLE.getKey().equals(comment.getStatus())) {
            return null;
        }
        return convertToRespVO(comment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer createComment(CommentCreateReqVO createReqVO) throws BusinessCheckException {
        // 0. 设置评价类型默认值
        if (createReqVO.getCommentType() == null) {
            createReqVO.setCommentType(1); // 默认商品评价
        }

        // 1. 验证订单是否存在
        MtOrder order = orderService.getOrderInfo(createReqVO.getOrderId());
        if (order == null) {
            throw new BusinessCheckException("订单不存在");
        }
        
        // 2. 验证订单是否属于该用户
        if (!order.getUserId().equals(createReqVO.getUserId())) {
            throw new BusinessCheckException("订单不属于该用户");
        }
        
        // 3. 验证订单是否已完成
        if (!OrderStatusEnum.RECEIVED.getKey().equals(order.getStatus()) 
                && !OrderStatusEnum.DELIVERED.getKey().equals(order.getStatus())) {
            throw new BusinessCheckException("订单未完成，不能评价");
        }
        
        if (createReqVO.getCommentType() == 1) {
            // 商品评价逻辑
            if (createReqVO.getGoodsId() == null) {
                throw new BusinessCheckException("商品ID不能为空");
            }
            // 4. 验证商品是否存在
            MtGoods goods = goodsService.queryGoodsById(createReqVO.getGoodsId());
            if (goods == null) {
                throw new BusinessCheckException("商品不存在");
            }

            // 5. 验证是否已评价
            MtGoodsComment existComment = mtGoodsCommentMapper.selectByOrderAndGoods(
                    createReqVO.getOrderId(), createReqVO.getGoodsId(), createReqVO.getUserId());
            if (existComment != null) {
                throw new BusinessCheckException("该商品已评价，不能重复评价");
            }

            if (createReqVO.getScore() < 1 || createReqVO.getScore() > 5) {
                throw new BusinessCheckException("商品评价评分必须在1-5之间");
            }
        } else if (createReqVO.getCommentType() == 2) {
            // NPS评价逻辑
            // 5. 验证是否已进行NPS评价
            MtGoodsComment existNps = mtGoodsCommentMapper.selectByOrderAndNps(
                    createReqVO.getOrderId(), createReqVO.getUserId());
            if (existNps != null) {
                throw new BusinessCheckException("该订单已进行NPS评价，不能重复评价");
            }

            if (createReqVO.getScore() < 0 || createReqVO.getScore() > 10) {
                throw new BusinessCheckException("NPS评价评分必须在0-10之间");
            }
        } else {
            throw new BusinessCheckException("无效的评价类型");
        }
        
        // 6. 验证图片数量
        if (CollUtil.isNotEmpty(createReqVO.getImages()) && createReqVO.getImages().size() > 9) {
            throw new BusinessCheckException("评价图片数量不能超过9张");
        }
        
        // 7. 创建评价记录
        MtGoodsComment comment = new MtGoodsComment();
        comment.setMerchantId(createReqVO.getMerchantId() != null ? createReqVO.getMerchantId() : order.getMerchantId());
        comment.setStoreId(createReqVO.getStoreId() != null ? createReqVO.getStoreId() : order.getStoreId());
        comment.setOrderId(createReqVO.getOrderId());
        comment.setGoodsId(createReqVO.getGoodsId() != null ? createReqVO.getGoodsId() : 0);
        comment.setSkuId(createReqVO.getSkuId() != null ? createReqVO.getSkuId() : 0);
        comment.setUserId(createReqVO.getUserId());
        comment.setCommentType(createReqVO.getCommentType());
        comment.setScore(createReqVO.getScore());
        comment.setContent(createReqVO.getContent() != null ? createReqVO.getContent() : "");
        comment.setIsAnonymous(StringUtils.isNotEmpty(createReqVO.getIsAnonymous()) ? createReqVO.getIsAnonymous() : "N");
        comment.setIsShow("Y");
        comment.setLikeCount(0);
        comment.setCreateTime(new Date());
        comment.setUpdateTime(new Date());
        comment.setOperator("openapi");
        comment.setStatus(StatusEnum.ENABLED.getKey());
        
        mtGoodsCommentMapper.insert(comment);
        
        // 8. 保存评价图片
        if (CollUtil.isNotEmpty(createReqVO.getImages())) {
            saveCommentImages(comment.getId(), createReqVO.getImages());
        }
        
        log.info("创建评价成功，评价ID：{}", comment.getId());
        return comment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateComment(CommentUpdateReqVO updateReqVO) throws BusinessCheckException {
        // 1. 验证评价是否存在
        MtGoodsComment comment = mtGoodsCommentMapper.selectById(updateReqVO.getId());
        if (comment == null || StatusEnum.DISABLE.getKey().equals(comment.getStatus())) {
            throw new BusinessCheckException("评价不存在");
        }
        
        // 2. 验证是否是评价所属用户
        if (!comment.getUserId().equals(updateReqVO.getUserId())) {
            throw new BusinessCheckException("用户无权操作该评价");
        }
        
        // 3. 验证图片数量
        if (CollUtil.isNotEmpty(updateReqVO.getImages()) && updateReqVO.getImages().size() > 9) {
            throw new BusinessCheckException("评价图片数量不能超过9张");
        }
        
        // 4. 更新评价
        if (updateReqVO.getScore() != null) {
            comment.setScore(updateReqVO.getScore());
        }
        if (updateReqVO.getContent() != null) {
            comment.setContent(updateReqVO.getContent());
        }
        if (StringUtils.isNotEmpty(updateReqVO.getIsAnonymous())) {
            comment.setIsAnonymous(updateReqVO.getIsAnonymous());
        }
        comment.setUpdateTime(new Date());
        comment.setOperator("openapi");
        
        mtGoodsCommentMapper.updateById(comment);
        
        // 5. 更新评价图片（如果提供了新的图片列表）
        if (updateReqVO.getImages() != null) {
            // 删除旧图片
            mtGoodsCommentImageMapper.deleteByCommentId(comment.getId());
            // 保存新图片
            if (CollUtil.isNotEmpty(updateReqVO.getImages())) {
                saveCommentImages(comment.getId(), updateReqVO.getImages());
            }
        }
        
        log.info("更新评价成功，评价ID：{}", comment.getId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteComment(CommentDeleteReqVO deleteReqVO) throws BusinessCheckException {
        // 1. 验证评价是否存在
        MtGoodsComment comment = mtGoodsCommentMapper.selectById(deleteReqVO.getId());
        if (comment == null || StatusEnum.DISABLE.getKey().equals(comment.getStatus())) {
            throw new BusinessCheckException("评价不存在");
        }
        
        // 2. 验证是否是评价所属用户
        if (!comment.getUserId().equals(deleteReqVO.getUserId())) {
            throw new BusinessCheckException("用户无权操作该评价");
        }
        
        // 3. 逻辑删除评价
        comment.setStatus(StatusEnum.DISABLE.getKey());
        comment.setUpdateTime(new Date());
        comment.setOperator("openapi");
        
        mtGoodsCommentMapper.updateById(comment);
        
        // 4. 删除评价图片
        mtGoodsCommentImageMapper.deleteByCommentId(comment.getId());
        
        log.info("删除评价成功，评价ID：{}", comment.getId());
        return true;
    }

    @Override
    public Boolean replyComment(CommentReplyReqVO replyReqVO) throws BusinessCheckException {
        // 1. 验证评价是否存在
        MtGoodsComment comment = mtGoodsCommentMapper.selectById(replyReqVO.getId());
        if (comment == null || StatusEnum.DISABLE.getKey().equals(comment.getStatus())) {
            throw new BusinessCheckException("评价不存在");
        }
        
        // 2. 验证商户权限（如果提供了商户ID）
        if (replyReqVO.getMerchantId() != null && !comment.getMerchantId().equals(replyReqVO.getMerchantId())) {
            throw new BusinessCheckException("商户无权操作该评价");
        }
        
        // 3. 更新回复内容
        comment.setReplyContent(replyReqVO.getReplyContent());
        comment.setReplyTime(new Date());
        comment.setUpdateTime(new Date());
        comment.setOperator("openapi");
        
        mtGoodsCommentMapper.updateById(comment);
        
        log.info("商家回复评价成功，评价ID：{}", comment.getId());
        return true;
    }

    @Override
    public CommentStatisticsVO getCommentStatistics(Integer goodsId) {
        CommentStatisticsVO statisticsVO = new CommentStatisticsVO();
        statisticsVO.setGoodsId(goodsId);
        
        // 获取总评价数
        Integer totalCount = mtGoodsCommentMapper.selectCountByGoodsId(goodsId);
        statisticsVO.setTotalCount(totalCount != null ? totalCount : 0);
        
        // 获取平均评分
        BigDecimal avgScore = mtGoodsCommentMapper.selectAvgScoreByGoodsId(goodsId);
        statisticsVO.setAvgScore(avgScore != null ? avgScore.setScale(1, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        
        // 获取各星级数量
        Integer star5Count = mtGoodsCommentMapper.selectCountByGoodsIdAndScore(goodsId, 5);
        Integer star4Count = mtGoodsCommentMapper.selectCountByGoodsIdAndScore(goodsId, 4);
        Integer star3Count = mtGoodsCommentMapper.selectCountByGoodsIdAndScore(goodsId, 3);
        Integer star2Count = mtGoodsCommentMapper.selectCountByGoodsIdAndScore(goodsId, 2);
        Integer star1Count = mtGoodsCommentMapper.selectCountByGoodsIdAndScore(goodsId, 1);
        
        statisticsVO.setStar5Count(star5Count != null ? star5Count : 0);
        statisticsVO.setStar4Count(star4Count != null ? star4Count : 0);
        statisticsVO.setStar3Count(star3Count != null ? star3Count : 0);
        statisticsVO.setStar2Count(star2Count != null ? star2Count : 0);
        statisticsVO.setStar1Count(star1Count != null ? star1Count : 0);
        
        // 计算好评、中评、差评数
        int goodCount = (star5Count != null ? star5Count : 0) + (star4Count != null ? star4Count : 0);
        int normalCount = star3Count != null ? star3Count : 0;
        int badCount = (star2Count != null ? star2Count : 0) + (star1Count != null ? star1Count : 0);
        
        statisticsVO.setGoodCount(goodCount);
        statisticsVO.setNormalCount(normalCount);
        statisticsVO.setBadCount(badCount);
        
        // 计算好评率
        if (totalCount != null && totalCount > 0) {
            BigDecimal goodRate = new BigDecimal(goodCount)
                    .divide(new BigDecimal(totalCount), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100))
                    .setScale(1, RoundingMode.HALF_UP);
            statisticsVO.setGoodRate(goodRate);
        } else {
            statisticsVO.setGoodRate(BigDecimal.ZERO);
        }
        
        return statisticsVO;
    }

    @Override
    public CommentStatisticsVO getStoreNpsStatistics(Integer storeId) {
        CommentStatisticsVO statisticsVO = new CommentStatisticsVO();
        
        // 获取NPS统计
        java.util.Map<String, Object> npsStats = mtGoodsCommentMapper.selectNpsStatsByStoreId(storeId);
        Long totalCount = (Long) npsStats.get("totalCount");
        Double avgScore = (Double) npsStats.get("avgScore");
        
        statisticsVO.setNpsTotalCount(totalCount != null ? totalCount.intValue() : 0);
        statisticsVO.setNpsAvgScore(avgScore != null ? new BigDecimal(avgScore).setScale(1, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        
        // NPS 分类统计
        Integer promoterCount = mtGoodsCommentMapper.selectCountByStoreIdAndScoreRange(storeId, 9, 10);
        Integer passiveCount = mtGoodsCommentMapper.selectCountByStoreIdAndScoreRange(storeId, 7, 8);
        Integer detractorCount = mtGoodsCommentMapper.selectCountByStoreIdAndScoreRange(storeId, 0, 6);
        
        statisticsVO.setNpsPromoterCount(promoterCount != null ? promoterCount : 0);
        statisticsVO.setNpsPassiveCount(passiveCount != null ? passiveCount : 0);
        statisticsVO.setNpsDetractorCount(detractorCount != null ? detractorCount : 0);
        
        // 计算NPS值: (推荐者% - 贬损者%) * 100
        if (statisticsVO.getNpsTotalCount() > 0) {
            BigDecimal promoterRate = new BigDecimal(statisticsVO.getNpsPromoterCount())
                    .divide(new BigDecimal(statisticsVO.getNpsTotalCount()), 4, RoundingMode.HALF_UP);
            BigDecimal detractorRate = new BigDecimal(statisticsVO.getNpsDetractorCount())
                    .divide(new BigDecimal(statisticsVO.getNpsTotalCount()), 4, RoundingMode.HALF_UP);
            
            BigDecimal npsScore = promoterRate.subtract(detractorRate).multiply(new BigDecimal(100)).setScale(1, RoundingMode.HALF_UP);
            statisticsVO.setNpsScore(npsScore);
        } else {
            statisticsVO.setNpsScore(BigDecimal.ZERO);
        }
        
        return statisticsVO;
    }

    @Override
    public CommentStatisticsVO getMerchantNpsStatistics(Integer merchantId) {
        CommentStatisticsVO statisticsVO = new CommentStatisticsVO();
        
        // 获取NPS统计
        java.util.Map<String, Object> npsStats = mtGoodsCommentMapper.selectNpsStatsByMerchantId(merchantId);
        Long totalCount = (Long) npsStats.get("totalCount");
        Double avgScore = (Double) npsStats.get("avgScore");
        
        statisticsVO.setNpsTotalCount(totalCount != null ? totalCount.intValue() : 0);
        statisticsVO.setNpsAvgScore(avgScore != null ? new BigDecimal(avgScore).setScale(1, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        
        // NPS 分类统计
        Integer promoterCount = mtGoodsCommentMapper.selectCountByMerchantIdAndScoreRange(merchantId, 9, 10);
        Integer passiveCount = mtGoodsCommentMapper.selectCountByMerchantIdAndScoreRange(merchantId, 7, 8);
        Integer detractorCount = mtGoodsCommentMapper.selectCountByMerchantIdAndScoreRange(merchantId, 0, 6);
        
        statisticsVO.setNpsPromoterCount(promoterCount != null ? promoterCount : 0);
        statisticsVO.setNpsPassiveCount(passiveCount != null ? passiveCount : 0);
        statisticsVO.setNpsDetractorCount(detractorCount != null ? detractorCount : 0);
        
        // 计算NPS值
        if (statisticsVO.getNpsTotalCount() > 0) {
            BigDecimal promoterRate = new BigDecimal(statisticsVO.getNpsPromoterCount())
                    .divide(new BigDecimal(statisticsVO.getNpsTotalCount()), 4, RoundingMode.HALF_UP);
            BigDecimal detractorRate = new BigDecimal(statisticsVO.getNpsDetractorCount())
                    .divide(new BigDecimal(statisticsVO.getNpsTotalCount()), 4, RoundingMode.HALF_UP);
            
            BigDecimal npsScore = promoterRate.subtract(detractorRate).multiply(new BigDecimal(100)).setScale(1, RoundingMode.HALF_UP);
            statisticsVO.setNpsScore(npsScore);
        } else {
            statisticsVO.setNpsScore(BigDecimal.ZERO);
        }
        
        return statisticsVO;
    }

    @Override
    public List<CommentRespVO> queryUserCommentList(Integer userId) {
        List<MtGoodsComment> commentList = mtGoodsCommentMapper.selectListByUserId(userId);
        if (CollUtil.isEmpty(commentList)) {
            return new ArrayList<>();
        }
        return commentList.stream()
                .map(this::convertToRespVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentRespVO> queryGoodsCommentList(Integer goodsId) {
        List<MtGoodsComment> commentList = mtGoodsCommentMapper.selectShowListByGoodsId(goodsId);
        if (CollUtil.isEmpty(commentList)) {
            return new ArrayList<>();
        }
        return commentList.stream()
                .map(this::convertToRespVO)
                .collect(Collectors.toList());
    }

    @Override
    public Boolean checkCommentExists(Integer orderId, Integer goodsId, Integer userId) {
        MtGoodsComment comment = mtGoodsCommentMapper.selectByOrderAndGoods(orderId, goodsId, userId);
        return comment != null;
    }

    @Override
    public Boolean likeComment(Integer commentId) {
        MtGoodsComment comment = mtGoodsCommentMapper.selectById(commentId);
        if (comment == null || StatusEnum.DISABLE.getKey().equals(comment.getStatus())) {
            return false;
        }
        
        comment.setLikeCount(comment.getLikeCount() + 1);
        comment.setUpdateTime(new Date());
        mtGoodsCommentMapper.updateById(comment);
        
        return true;
    }

    /**
     * 保存评价图片
     *
     * @param commentId 评价ID
     * @param images    图片URL列表
     */
    private void saveCommentImages(Integer commentId, List<String> images) {
        for (int i = 0; i < images.size(); i++) {
            MtGoodsCommentImage image = new MtGoodsCommentImage();
            image.setCommentId(commentId);
            image.setImageUrl(images.get(i));
            image.setSort(i);
            image.setCreateTime(new Date());
            image.setStatus(StatusEnum.ENABLED.getKey());
            mtGoodsCommentImageMapper.insert(image);
        }
    }

    /**
     * 将实体转换为响应VO
     *
     * @param comment 评价实体
     * @return 响应VO
     */
    private CommentRespVO convertToRespVO(MtGoodsComment comment) {
        CommentRespVO respVO = new CommentRespVO();
        respVO.setId(comment.getId());
        respVO.setMerchantId(comment.getMerchantId());
        respVO.setStoreId(comment.getStoreId());
        respVO.setOrderId(comment.getOrderId());
        respVO.setGoodsId(comment.getGoodsId());
        respVO.setSkuId(comment.getSkuId());
        respVO.setUserId(comment.getUserId());
        respVO.setCommentType(comment.getCommentType());
        respVO.setScore(comment.getScore());
        respVO.setContent(comment.getContent());
        respVO.setReplyContent(comment.getReplyContent());
        respVO.setReplyTime(comment.getReplyTime());
        respVO.setIsAnonymous(comment.getIsAnonymous());
        respVO.setIsShow(comment.getIsShow());
        respVO.setLikeCount(comment.getLikeCount());
        respVO.setCreateTime(comment.getCreateTime());
        respVO.setUpdateTime(comment.getUpdateTime());
        respVO.setStatus(comment.getStatus());
        
        // 获取图片列表
        List<MtGoodsCommentImage> imageList = mtGoodsCommentImageMapper.selectListByCommentId(comment.getId());
        if (CollUtil.isNotEmpty(imageList)) {
            String basePath = settingService.getUploadBasePath();
            List<String> images = imageList.stream()
                    .map(img -> {
                        String url = img.getImageUrl();
                        if (StringUtils.isNotEmpty(url) && !url.startsWith("http")) {
                            return basePath + url;
                        }
                        return url;
                    })
                    .collect(Collectors.toList());
            respVO.setImages(images);
        }
        
        // 获取店铺信息
        try {
            MtStore store = storeService.queryStoreById(comment.getStoreId());
            if (store != null) {
                respVO.setStoreName(store.getName());
            }
        } catch (Exception e) {
            log.warn("获取店铺信息失败，storeId={}", comment.getStoreId(), e);
        }
        
        // 获取订单信息
        try {
            MtOrder order = orderService.getOrderInfo(comment.getOrderId());
            if (order != null) {
                respVO.setOrderSn(order.getOrderSn());
            }
        } catch (Exception e) {
            log.warn("获取订单信息失败，orderId={}", comment.getOrderId(), e);
        }
        
        // 获取商品信息
        try {
            MtGoods goods = goodsService.queryGoodsById(comment.getGoodsId());
            if (goods != null) {
                respVO.setGoodsName(goods.getName());
                String logo = goods.getLogo();
                if (StringUtils.isNotEmpty(logo) && !logo.startsWith("http")) {
                    logo = settingService.getUploadBasePath() + logo;
                }
                respVO.setGoodsImage(logo);
            }
        } catch (Exception e) {
            log.warn("获取商品信息失败，goodsId={}", comment.getGoodsId(), e);
        }
        
        // 获取用户信息（如果不是匿名评价）
        if (!"Y".equals(comment.getIsAnonymous())) {
            try {
                MtUser user = memberService.queryMemberById(comment.getUserId());
                if (user != null) {
                    respVO.setUserName(user.getName());
                    String avatar = user.getAvatar();
                    if (StringUtils.isNotEmpty(avatar) && !avatar.startsWith("http")) {
                        avatar = settingService.getUploadBasePath() + avatar;
                    }
                    respVO.setUserAvatar(avatar);
                }
            } catch (Exception e) {
                log.warn("获取用户信息失败，userId={}", comment.getUserId(), e);
            }
        } else {
            respVO.setUserName("匿名用户");
            respVO.setUserAvatar("");
        }
        
        return respVO;
    }

}
