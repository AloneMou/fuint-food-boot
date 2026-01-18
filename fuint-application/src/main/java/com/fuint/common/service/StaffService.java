package com.fuint.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.repository.model.MtStaff;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 店铺员工业务接口
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface StaffService extends IService<MtStaff> {

    /**
     * 员工查询列表
     *
     * @param paginationRequest
     * @return
     */
    PaginationResponse<MtStaff> queryStaffListByPagination(PaginationRequest paginationRequest) throws BusinessCheckException;

    /**
     * 保存员工信息
     *
     * @param reqStaff 员工信息
     * @param operator 操作人
     * @return
     * @throws BusinessCheckException
     */
    MtStaff saveStaff(MtStaff reqStaff, String operator) throws BusinessCheckException;


    /**
     * 新增员工
     *
     * @param reqStaff 员工信息
     * @param operator 操作人
     * @return 创建员工信息
     */
    MtStaff createStaff(MtStaff reqStaff, String operator);

    /**
     * 修改员工信息
     *
     * @param reqStaff 员工信息
     * @param operator 操作人
     * @return 修改员工信息
     */
    MtStaff updateStaff(MtStaff reqStaff, String operator);


    /**
     * 删除员工
     *
     * @param phone    手机号
     * @param operator 操作人
     * @return 删除员工信息
     */
    void deleteStaff(String phone, String operator);

    /**
     * 根据ID获取店铺信息
     *
     * @param id 员工id
     * @throws BusinessCheckException
     */
    MtStaff queryStaffById(Integer id) throws BusinessCheckException;

    /**
     * 审核更改状态(禁用，审核通过)
     *
     * @param id
     * @return
     * @throws BusinessCheckException
     */
    Integer updateAuditedStatus(Integer id, String statusEnum) throws BusinessCheckException;

    /**
     * 根据条件搜索员工
     *
     * @param params 请求参数
     * @return
     */
    List<MtStaff> queryStaffByParams(Map<String, Object> params) throws BusinessCheckException;

    /**
     * 根据手机号获取员工信息
     *
     * @param mobile 手机
     * @return
     * @throws BusinessCheckException
     */
    MtStaff queryStaffByMobile(String mobile) throws BusinessCheckException;

    /**
     * 根据会员ID获取员工信息
     *
     * @param userId 会员ID
     * @return
     * @throws BusinessCheckException
     */
    MtStaff queryStaffByUserId(Integer userId) throws BusinessCheckException;

    /**
     * 根据会员ID列表获取员工信息
     *
     * @param userIds 会员ID列表
     * @return 员工列表
     */
    List<MtStaff> queryStaffListByUserIds(Collection<Integer> userIds);
}
