package com.fuint.repository.mapper;

import com.fuint.common.mybatis.query.LambdaQueryWrapperX;
import com.fuint.repository.base.BaseMapperX;
import com.fuint.repository.model.MtGoodsCommentImage;

import java.util.List;

/**
 * 商品评价图片 Mapper 接口
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface MtGoodsCommentImageMapper extends BaseMapperX<MtGoodsCommentImage> {

    /**
     * 根据评价ID查询图片列表
     *
     * @param commentId 评价ID
     * @return 图片列表
     */
    default List<MtGoodsCommentImage> selectListByCommentId(Integer commentId) {
        return selectList(new LambdaQueryWrapperX<MtGoodsCommentImage>()
                .eq(MtGoodsCommentImage::getCommentId, commentId)
                .ne(MtGoodsCommentImage::getStatus, "D")
                .orderByAsc(MtGoodsCommentImage::getSort)
        );
    }

    /**
     * 删除评价的所有图片（逻辑删除）
     *
     * @param commentId 评价ID
     * @return 删除数量
     */
    default int deleteByCommentId(Integer commentId) {
        MtGoodsCommentImage update = new MtGoodsCommentImage();
        update.setStatus("D");
        return update(update, new LambdaQueryWrapperX<MtGoodsCommentImage>()
                .eq(MtGoodsCommentImage::getCommentId, commentId)
        );
    }

}
