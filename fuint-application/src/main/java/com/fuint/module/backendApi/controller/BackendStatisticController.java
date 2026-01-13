package com.fuint.module.backendApi.controller;

import cn.hutool.core.date.DatePattern;
import com.fuint.common.Constants;
import com.fuint.common.dto.*;
import com.fuint.common.service.GoodsService;
import com.fuint.common.service.MemberService;
import com.fuint.common.service.OrderService;
import com.fuint.common.util.DateUtil;
import com.fuint.common.util.ExcelUtil;
import com.fuint.common.util.TokenUtil;
import com.fuint.common.util.XlsUtil;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.web.BaseController;
import com.fuint.framework.web.ResponseObject;
import com.fuint.repository.bean.GoodsTopBean;
import com.fuint.repository.bean.MemberTopBean;
import com.fuint.repository.model.MtUser;
import com.fuint.repository.vo.request.GoodsStatisticsReqVO;
import com.fuint.repository.vo.request.MemberStatisticsReqVO;
import com.fuint.utils.StringUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

import static com.fuint.common.util.XlsUtil.objectConvertToString;

/**
 * 数据统计控制器
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Api(tags = "管理端-数据统计相关接口")
@RestController
@AllArgsConstructor
@RequestMapping(value = "/backendApi/statistic")
public class BackendStatisticController extends BaseController {

    /**
     * 会员服务接口
     */
    private MemberService memberService;

    /**
     * 订单服务接口
     */
    private OrderService orderService;

    /**
     * 商品服务接口
     */
    private GoodsService goodsService;

    /**
     * 数据概况
     *
     * @return
     */
    @ApiOperation(value = "数据概况")
    @RequestMapping(value = "/main", method = RequestMethod.POST)
    @CrossOrigin
    public ResponseObject main(HttpServletRequest request, @RequestBody Map<String, Object> param) throws BusinessCheckException, ParseException {
        String token = request.getHeader("Access-Token");
        String startTimeStr = param.get("startTime") == null ? "" : param.get("startTime").toString();
        String endTimeStr = param.get("endTime") == null ? "" : param.get("endTime").toString();

        Date startTime = StringUtil.isNotEmpty(startTimeStr) ? DateUtil.parseDate(startTimeStr) : null;
        Date endTime = StringUtil.isNotEmpty(endTimeStr) ? DateUtil.parseDate(endTimeStr) : null;

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        Integer merchantId = accountInfo.getMerchantId();
        Integer storeId = accountInfo.getStoreId();

        // 总会员数
        Long totalUserCount = memberService.getUserCount(merchantId, storeId);
        // 新增会员数量
        Long userCount = memberService.getUserCount(merchantId, storeId, startTime, endTime);

        // 总订单数
        BigDecimal totalOrderCount = orderService.getOrderCount(merchantId, storeId);
        // 订单数
        BigDecimal orderCount = orderService.getOrderCount(merchantId, storeId, startTime, endTime);

        // 交易金额
        BigDecimal payAmount = orderService.getPayMoney(merchantId, storeId, startTime, endTime);
        // 总交易金额
        BigDecimal totalPayAmount = orderService.getPayMoney(merchantId, storeId);

        // 活跃会员数
        Long activeUserCount = memberService.getActiveUserCount(merchantId, storeId, startTime, endTime);

        // 总支付人数
        Integer totalPayUserCount = orderService.getPayUserCount(merchantId, storeId);

        Map<String, Object> result = new HashMap<>();

        result.put("userCount", userCount);
        result.put("totalUserCount", totalUserCount);
        result.put("orderCount", orderCount);
        result.put("totalOrderCount", totalOrderCount);
        result.put("payAmount", payAmount);
        result.put("totalPayAmount", totalPayAmount);
        result.put("activeUserCount", activeUserCount);
        result.put("totalPayUserCount", totalPayUserCount);

        return getSuccessResult(result);
    }

    /**
     * 排行榜数据
     *
     * @return
     */
    @ApiOperation(value = "排行榜数据")
    @RequestMapping(value = "/top", method = RequestMethod.POST)
    @CrossOrigin
    public ResponseObject top(HttpServletRequest request, @RequestBody Map<String, Object> param) throws ParseException {
        String token = request.getHeader("Access-Token");
        String startTimeStr = param.get("startTime") == null ? "" : param.get("startTime").toString();
        String endTimeStr = param.get("endTime") == null ? "" : param.get("endTime").toString();

        Date startTime = StringUtil.isNotEmpty(startTimeStr) ? DateUtil.parseDate(startTimeStr) : null;
        Date endTime = StringUtil.isNotEmpty(endTimeStr) ? DateUtil.parseDate(endTimeStr) : null;

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        Integer merchantId = accountInfo.getMerchantId();
        Integer storeId = accountInfo.getStoreId();

        Map<String, Object> result = new HashMap<>();

        List<GoodsTopDto> goodsList = goodsService.getGoodsSaleTopList(merchantId, storeId, startTime, endTime);
        List<MemberTopDto> memberList = memberService.getMemberConsumeTopList(merchantId, storeId, startTime, endTime);

        result.put("goodsList", goodsList);
        result.put("memberList", memberList);

        return getSuccessResult(result);
    }

    /**
     * 获取会员数量
     *
     * @return
     */
    @ApiOperation(value = "获取会员数量")
    @RequestMapping(value = "/totalMember", method = RequestMethod.GET)
    @CrossOrigin
    public ResponseObject totalMember(HttpServletRequest request) throws BusinessCheckException {
        String token = request.getHeader("Access-Token");
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        Integer merchantId = accountInfo.getMerchantId();
        Integer storeId = accountInfo.getStoreId();

        Long totalMember = memberService.getUserCount(merchantId, storeId);
        Map<String, Object> result = new HashMap<>();
        result.put("totalMember", totalMember);

        return getSuccessResult(result);
    }


    /**
     * 排行榜数据
     *
     * @return
     */
    @ApiOperation(value = "排行榜数据")
    @RequestMapping(value = "/goods/top", method = RequestMethod.POST)
    @CrossOrigin
    public ResponseObject goodsTop(HttpServletRequest request, @RequestBody Map<String, Object> param) throws ParseException {
        String token = request.getHeader("Access-Token");
//        String startTimeStr = param.get("startTime") == null ? "" : param.get("startTime").toString();
//        String endTimeStr = param.get("endTime") == null ? "" : param.get("endTime").toString();

        Date startTime = DateUtil.parseDate(DateUtil.formatDate(new Date(), DatePattern.NORM_DATE_PATTERN), DatePattern.NORM_DATE_PATTERN);
        Date endTime = cn.hutool.core.date.DateUtil.offsetDay(startTime, 1);

        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        Integer merchantId = accountInfo.getMerchantId();
        Integer storeId = accountInfo.getStoreId();

        Map<String, Object> result = new HashMap<>();

        List<GoodsTopDto> goodsList = goodsService.getGoodsSaleTopList(merchantId, storeId, startTime, endTime);
        List<MemberTopDto> memberList = memberService.getMemberConsumeTopList(merchantId, storeId, startTime, endTime);

        result.put("goodsList", goodsList);
        result.put("memberList", memberList);

        return getSuccessResult(result);
    }


    @ApiOperation(value = "商品排行榜列表-分页")
    @GetMapping("/goods-top-list")
    public ResponseObject goodsTopList(HttpServletRequest request, GoodsStatisticsReqVO reqVO) {
        String token = request.getHeader("Access-Token");
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        Integer page = request.getParameter("page") == null ? Constants.PAGE_NUMBER : Integer.parseInt(request.getParameter("page"));
        Integer pageSize = request.getParameter("pageSize") == null ? Constants.PAGE_SIZE : Integer.parseInt(request.getParameter("pageSize"));

        PaginationRequest paginationRequest = new PaginationRequest();
        paginationRequest.setCurrentPage(page);
        paginationRequest.setPageSize(pageSize);
        Page<MtUser> pageHelper = PageHelper.startPage(paginationRequest.getCurrentPage(), paginationRequest.getPageSize());
        reqVO.setMerchantId(accountInfo.getMerchantId());
        reqVO.setStoreId(accountInfo.getStoreId());

        List<GoodsTopBean> list = goodsService.getGoodsSaleTopListByStore(reqVO);

        PageRequest pageRequest = PageRequest.of(paginationRequest.getCurrentPage(), paginationRequest.getPageSize());
        PageImpl pageImpl = new PageImpl(list, pageRequest, pageHelper.getTotal());
        PaginationResponse<GoodsTopBean> paginationResponse = new PaginationResponse(pageImpl, GoodsTopBean.class);
        paginationResponse.setTotalPages(pageHelper.getPages());
        paginationResponse.setTotalElements(pageHelper.getTotal());
        paginationResponse.setContent(list);
        return getSuccessResult(paginationResponse);
    }


    @ApiOperation(value = "导出商品排行记录")
    @RequestMapping(value = "/goods-top-export", method = RequestMethod.GET)
    @ResponseBody
    public void export(HttpServletRequest request, HttpServletResponse response, GoodsStatisticsReqVO reqVO) throws Exception {
        String token = request.getParameter("token");
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            throw new BusinessCheckException("请先登录");
//            return getFailureResult(1001, "请先登录");
        }

        reqVO.setMerchantId(accountInfo.getMerchantId());
        reqVO.setStoreId(accountInfo.getStoreId());

        List<GoodsTopBean> list = goodsService.getGoodsSaleTopListByStore(reqVO);

//        PaginationResponse<GiveDto> paginationResponse = giveService.queryGiveListByPagination(paginationRequest);
//        List<GiveDto> list = paginationResponse.getContent();

        // excel标题
        String[] title = {"商品ID", "商品名称", "商品条码", "销售金额", "销售数量"};
        String fileName;
        fileName = "商品排行榜" + System.currentTimeMillis() + ".xls";

        String[][] content = null;
        if (list.size() > 0) {
            content= new String[list.size()][title.length];
        }

        for (int i = 0; i < list.size(); i++) {
            GoodsTopBean obj = list.get(i);
            content[i][0] = objectConvertToString(obj.getId());
            content[i][1] = objectConvertToString(obj.getName());
            content[i][2] = objectConvertToString(obj.getGoodsNo());
            content[i][3] = objectConvertToString(obj.getAmount());
            content[i][4] = objectConvertToString(obj.getNum());
//            content[i][5] = objectConvertToString(obj.getCreateTime());
        }

        // 创建HSSFWorkbook
        HSSFWorkbook wb = ExcelUtil.getHSSFWorkbook("商品排行榜", title, content, null);

        // 响应到客户端
        try {
            XlsUtil.setXlsHeader(request, response, fileName);
            OutputStream os = response.getOutputStream();
            wb.write(os);
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @ApiOperation(value = "商品排行榜列表-分页")
    @GetMapping("/member-top-list")
    public ResponseObject memberTopList(HttpServletRequest request, MemberStatisticsReqVO reqVO) {
        String token = request.getHeader("Access-Token");
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            return getFailureResult(1001, "请先登录");
        }

        Integer page = request.getParameter("page") == null ? Constants.PAGE_NUMBER : Integer.parseInt(request.getParameter("page"));
        Integer pageSize = request.getParameter("pageSize") == null ? Constants.PAGE_SIZE : Integer.parseInt(request.getParameter("pageSize"));

        PaginationRequest paginationRequest = new PaginationRequest();
        paginationRequest.setCurrentPage(page);
        paginationRequest.setPageSize(pageSize);
        Page<MtUser> pageHelper = PageHelper.startPage(paginationRequest.getCurrentPage(), paginationRequest.getPageSize());
        reqVO.setMerchantId(accountInfo.getMerchantId());
        reqVO.setStoreId(accountInfo.getStoreId());

        List<MemberTopBean> list = memberService.getMembersConsumeTopList(reqVO);

        PageRequest pageRequest = PageRequest.of(paginationRequest.getCurrentPage(), paginationRequest.getPageSize());
        PageImpl pageImpl = new PageImpl(list, pageRequest, pageHelper.getTotal());
        PaginationResponse<MemberTopBean> paginationResponse = new PaginationResponse(pageImpl, MemberTopBean.class);
        paginationResponse.setTotalPages(pageHelper.getPages());
        paginationResponse.setTotalElements(pageHelper.getTotal());
        paginationResponse.setContent(list);
        return getSuccessResult(paginationResponse);
    }




    @ApiOperation(value = "导出会员排行记录")
    @RequestMapping(value = "/member-top-export", method = RequestMethod.GET)
    @ResponseBody
    public void memberExport(HttpServletRequest request, HttpServletResponse response, MemberStatisticsReqVO reqVO) throws Exception {
        String token = request.getParameter("token");
        AccountInfo accountInfo = TokenUtil.getAccountInfoByToken(token);
        if (accountInfo == null) {
            throw new BusinessCheckException("请先登录");
//            return getFailureResult(1001, "请先登录");
        }

        reqVO.setMerchantId(accountInfo.getMerchantId());
        reqVO.setStoreId(accountInfo.getStoreId());

        List<MemberTopBean> list = memberService.getMembersConsumeTopList(reqVO);

//        PaginationResponse<GiveDto> paginationResponse = giveService.queryGiveListByPagination(paginationRequest);
//        List<GiveDto> list = paginationResponse.getContent();

        // excel标题
        String[] title = {"会员ID", "会员名称", "会员号", "消费金额", "购买数量"};
        String fileName;
        fileName = "会员排行" + System.currentTimeMillis() + ".xls";

        String[][] content = null;
        if (list.size() > 0) {
            content= new String[list.size()][title.length];
        }

        for (int i = 0; i < list.size(); i++) {
            MemberTopBean obj = list.get(i);
            content[i][0] = objectConvertToString(obj.getId());
            content[i][1] = objectConvertToString(obj.getName());
            content[i][2] = objectConvertToString(obj.getUserNo());
            content[i][3] = objectConvertToString(obj.getAmount());
            content[i][4] = objectConvertToString(obj.getNum());
//            content[i][5] = objectConvertToString(obj.getCreateTime());
        }

        // 创建HSSFWorkbook
        HSSFWorkbook wb = ExcelUtil.getHSSFWorkbook("会员排行", title, content, null);

        // 响应到客户端
        try {
            XlsUtil.setXlsHeader(request, response, fileName);
            OutputStream os = response.getOutputStream();
            wb.write(os);
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
