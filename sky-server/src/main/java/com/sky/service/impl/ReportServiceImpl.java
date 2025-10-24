package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;//查阅订单表

    @Autowired
    private UserMapper  userMapper;

    /**
     * 根据时间区间统计营业额
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {

        //当前集合用于存放从begin到end范围内的每天的日期
        List<LocalDate> datalist = new ArrayList<>();

        datalist.add(begin);

        while (!begin.equals(end)) {
            //日期计算,计算指定日期后一天对应的日期
                begin = begin.plusDays(1);
                datalist.add(begin);
        }


        //存每天营业额
        List<Double> turnoverList = new ArrayList<>();
        for(LocalDate date:datalist){
            //查询date日期对应的营业额数据，营业额是指，状态“已完成”的订单金额合并
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 :turnover;
            //存每天营业额
            turnoverList.add(turnover);

        }

        //封装返回结果
        return TurnoverReportVO
                .builder()
                .dateList(StringUtils.join(datalist,","))//日期都设置到dataList这个属性上.//将datalist集合元素取出来，用，号隔开，变成了字符串
                .turnoverList(StringUtils.join(turnoverList,","))//将turnoverList集合元素取出来，用，号隔开，变成了字符串
                .build();

    }

    /**
     * 根据时间区间统计用户数量
     * @param begin
     * @param end
     * @return
     */
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
       //存放从begin到end之间的每天对应的日期
        List<LocalDate> datalist = new ArrayList<>();
       datalist.add(begin);
       while (!begin.equals(end)) {
           begin = begin.plusDays(1);
            datalist.add(begin);
       }

        //存放每天新增的用户数量 select count(id) from user where create_time < ?  and create_time > ?
        List<Integer> newUserList = new ArrayList<>();
        //存放每天的总用户数量       select count(id) from user where create_time < ?
        List<Integer> totalUserList = new ArrayList<>();

        for(LocalDate date:datalist){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();

            map.put("endTime", endTime);
            //总用户数量
            Integer totalUser = userMapper.countByMap(map);

            map.put("beginTime", beginTime);
            //新增用户数量
            Integer newUser = userMapper.countByMap(map);

            totalUserList.add(totalUser);
            newUserList.add(newUser);
        }
        //封装结果数据
        return UserReportVO.builder()
                .dateList(StringUtils.join(datalist,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .build();
    }


}