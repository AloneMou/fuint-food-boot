package com.fuint.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fuint.common.dto.GoodsCateDto;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.pojo.PageResult;
import com.fuint.openapi.v1.goods.cate.vo.MtGoodsCatePageReqVO;
import com.fuint.openapi.v1.goods.cate.vo.MtGoodsCateRespVO;
import com.fuint.repository.model.MtGoodsCate;

import java.util.List;
import java.util.Map;

/**
 * 商品分类业务接口
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface CateService extends IService<MtGoodsCate> {

    /**
     * 分页查询列表
     *
     * @param paginationRequest
     * @return
     */
    PaginationResponse<GoodsCateDto> queryCateListByPagination(PaginationRequest paginationRequest) throws BusinessCheckException;

    /**
     * 添加商品分类
     *
     * @param reqDto 分类参数
     * @return
     * @throws BusinessCheckException
     */
    MtGoodsCate addCate(MtGoodsCate reqDto) throws BusinessCheckException;

    /**
     * 根据ID获取商品分类信息
     *
     * @param id ID
     * @throws BusinessCheckException
     */
    MtGoodsCate queryCateById(Integer id) throws BusinessCheckException;

    /**
     * 根据ID删除
     *
     * @param id       分类ID
     * @param operator 操作人
     * @return
     * @throws BusinessCheckException
     */
    void deleteCate(Integer id, String operator) throws BusinessCheckException;

    /**
     * 更新分类
     *
     * @param reqDto 分类参数
     * @return
     * @throws BusinessCheckException
     */
    MtGoodsCate updateCate(MtGoodsCate reqDto) throws BusinessCheckException;

    /**
     * 根据条件搜索分类
     *
     * @param params 查询参数
     * @return
     */
    List<MtGoodsCate> queryCateListByParams(Map<String, Object> params) throws BusinessCheckException;

    /**
     * 获取分类分页列表
     *
     * @param pageReqVO 查询参数
     * @return 分页结果
     */
    PageResult<MtGoodsCateRespVO> getCatePage(MtGoodsCatePageReqVO pageReqVO);
}
