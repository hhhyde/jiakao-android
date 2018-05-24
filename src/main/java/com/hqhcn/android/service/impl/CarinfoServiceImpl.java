package com.hqhcn.android.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.hqhcn.android.dao.CarinfoMapper;
import com.hqhcn.android.dao.ExampreasignMapper;
import com.hqhcn.android.entity.*;
import com.hqhcn.android.service.CarinfoService;
import com.hqhcn.android.service.ExamineeService;
import com.hqhcn.android.service.KsldService;
import com.hqhcn.android.tool.AttrUtils;
import com.hqhcn.android.tool.DateTools;
import com.hqhcn.android.web.InitLoad;
import com.hqhcn.android.webservice.TmriInvoker;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static java.util.stream.Collectors.toList;

@Service
public class CarinfoServiceImpl implements CarinfoService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    CarinfoMapper mapper;
    @Autowired
    ExamineeService examineeService;
    @Autowired
    private ExampreasignMapper exampreasignMapper;
    @Autowired
    private TmriInvoker tmri;
    @Autowired
    private KsldService ksldService;


    @Override
    public List<Carinfo> query(CarinfoExample example) {
        try {
            return mapper.selectByExample(example);
        } catch (Exception e) {
            return Collections.emptyList();
        }

    }

    @Override
    public List<Carinfo> selectByExampleToPage(CarinfoExample example) {
        return mapper.selectByExampleToPage(example);
    }

    @Override
    public List<String> queryJLCXH(CarinfoExample example) {
        return mapper.selectJLCXHByExample(example);
    }

    @Override
    public Carinfo query(String kcdddh, String jlcxh) {
        CarinfoExample example = new CarinfoExample();
        if (StringUtils.isEmpty(jlcxh)){
            example.createCriteria().andKcdddhEqualTo(kcdddh);
        }else {
            example.createCriteria().andKcdddhEqualTo(kcdddh).andJlcxhEqualTo(jlcxh);
        }
        List<Carinfo> carinfos = query(example);
        return carinfos == null || carinfos.size() == 0 ? null : carinfos.get(0);
    }

    @Override
    @Transactional
    public void update(Carinfo entity) {
        if (ZT_UNUSABLE.equals(entity.getJlczt())){
            breakdown(entity.getJlcxh());
        }

        mapper.updateByPrimaryKeySelective(entity);
        InitLoad.init_jlcxh_carinfo();
    }

    @Override
    public void update(String jlcxh, String zt) {
        Carinfo carinfo = new Carinfo();
        carinfo.setKcdddh(InitLoad.kcdddh);
        carinfo.setJlcxh(jlcxh);
        carinfo.setJlczt(zt);
        update(carinfo);
    }

    @Override
    public void add(Carinfo entity) {
        mapper.insertSelective(entity);
        InitLoad.init_jlcxh_carinfo();
    }

    @Override
    public Carinfo queryByIP(String ip) {
        CarinfoExample example = new CarinfoExample();
        example.createCriteria().andJbrEqualTo(ip).andKcdddhEqualTo(InitLoad.kcdddh);
        List<Carinfo> carinfos = query(example);
        return carinfos == null || carinfos.size() == 0 ? null : carinfos.get(0);
    }

    @Override
    public List<Carinfo> usable(String kskm) {
        CarinfoExample example = new CarinfoExample();
        example.createCriteria().andJlcztEqualTo("0").andKskmEqualTo(kskm).andKcdddhEqualTo(InitLoad.kcdddh);
        return mapper.selectByExample(example);
    }

    @Override
    public List<Carinfo> queryByNvrName(String NvrName) {
        CarinfoExample example1 = new CarinfoExample();
        example1.createCriteria().andCam1NvrEqualTo(NvrName);

        CarinfoExample.Criteria criteria2 = new CarinfoExample().createCriteria();
        criteria2.andCam2NvrEqualTo(NvrName);

        CarinfoExample.Criteria criteria3 = new CarinfoExample().createCriteria();
        criteria3.andCam3NvrEqualTo(NvrName);

        example1.or(criteria2);
        example1.or(criteria3);
        List<Carinfo> carinfos =mapper.selectByExample(example1);
        return carinfos;
    }

    @Override
    @Transactional
    public void breakdown(String jlcxh) {
        jlcxh = jlcxh.trim();

        // 将该车上 未完成考试 的考生状态改成未分配,保留 考试路段信息,然后清除已分配的车辆记录
        ExampreasignExample example57 = new ExampreasignExample();
        example57.createCriteria().andJlcxhEqualTo(jlcxh).andZtIn(Arrays.asList(
                ExamineeService.ZT_EXAMING, ExamineeService.ZT_EXAM1STEND));
        Exampreasign exampreasign57 = new Exampreasign();
        exampreasign57.setZt(ExamineeService.ZT_UNASSIGN);
        exampreasign57.setJlcxh("");
        examineeService.update(exampreasign57, example57);

        // 将该车上 未开始考试的考生 状态改成未分配,清除 考试路段信息,然后清除已分配的车辆记录
        ExampreasignExample example34 = new ExampreasignExample();
        example34.createCriteria().andJlcxhEqualTo(jlcxh).andZtIn(Arrays.asList(
                ExamineeService.ZT_NOTINCAR, ExamineeService.ZT_NOEXAM));
        Exampreasign Exampreasign34 = new Exampreasign();
        Exampreasign34.setZt(ExamineeService.ZT_UNASSIGN);
        Exampreasign34.setKsld("");
        Exampreasign34.setJlcxh("");
        examineeService.update(Exampreasign34, example34);
    }

    @Override
    @Transactional
    public List<Exampreasign> pull(String jlcxh, String jlc_kskm) throws Exception {

        Carinfo carinfo = query(InitLoad.kcdddh, jlcxh);
        if (org.apache.commons.lang3.StringUtils.isEmpty(carinfo.getKsld())) {
            throw new Exception("该考车没有分配考试路线");
        }

        List<Exampreasign> result = Collections.emptyList();
        ExampreasignExample example = new ExampreasignExample();
        example.setOrderByClause(" bdsj ");
        example.createCriteria()
                .andJlcxhEqualTo(jlcxh)
                .andYkrqEqualTo(DateTools.toDate(DateTools.getYMD(), DateTools.yyyyMMdd))
                .andZtIn(Arrays.asList(ExamineeService.ZT_NOEXAM, ExamineeService.ZT_EXAMING, ExamineeService.ZT_EXAM1STEND));
        // 车上没有考完的考生列表(已上车未考完,没及格等第二次考试)
        List<Exampreasign> assigns = exampreasignMapper.selectByExample(example);

        if (assigns == null || assigns.size() == 0) {
            // 全部考完
            // 1. 将车上 <考完> 考生状态 全 设置成 <考完下车>
            ExampreasignExample example8 = new ExampreasignExample();
            example8.setOrderByClause(" bdsj ");
            example8.createCriteria()
                    .andJlcxhEqualTo(jlcxh)
                    .andZtEqualTo(ExamineeService.ZT_EXAMEND)
                    .andYkrqEqualTo(DateTools.toDate(DateTools.getYMD(), DateTools.yyyyMMdd));
            Exampreasign exampreasign9 = new Exampreasign();
            exampreasign9.setZt(ExamineeService.ZT_EXAMENDOUTCAR);
            examineeService.updateByExampleSelective(exampreasign9, example8);

            // 2. 拿到全部 <已分配到该车待考> 的考生, 状态设置成  <已上车>
            ExampreasignExample example3 = new ExampreasignExample();
            example3.setOrderByClause(" bdsj ");
            example3.createCriteria().andJlcxhEqualTo(jlcxh).andZtEqualTo(ExamineeService.ZT_NOTINCAR)
                    .andYkrqEqualTo(DateTools.toDate(DateTools.getYMD(), DateTools.yyyyMMdd));
            List<Exampreasign> assigns3 = exampreasignMapper.selectByExample(example3);
            for (Exampreasign exampreasign : assigns3) {
                exampreasign.setZt(ExamineeService.ZT_NOEXAM);
               examineeService.updateByLSH(exampreasign);
            }
            // 并返回
            result = assigns3;


            // 3. 从状态<验证通过考车未分>中  获取下一轮 备考考生
            List<Exampreasign> exampreasign2= Collections.emptyList();

            if (tmri.enabled17CB3) {
                // 监管平台分配考生
                JSONObject result17CB3 = tmri.jk17CB3(carinfo.getHphm(), AttrUtils.考场序号.toString(),"C1","*", "17CB3", "", InitLoad.examineeNum() + "");
                String code = ((Map) result17CB3.get("head")).get("code") + "";
                String message = ((Map) result17CB3.get("head")).get("message") + "";
//                String code="1";
//                String message="320481198701221828,320282199112313978,320223197904056499,500236198810243883";
                if ("1".equals(code)){
                    exampreasign2 = Arrays.stream(message.split(","))
                            .map(sfzmhm -> examineeService.queryBySfzmhmToday(sfzmhm).get(0))
                            .collect(toList());
                }else {
                    logger.error(String.format("[17CB3]监管平台分配考生下载失败!错误信息=%s", message));
                }
            } else {
                // 本系统分配考生
                ExampreasignExample example2 = new ExampreasignExample();
                example2.setOrderByClause(" bdsj ");
                example2.createCriteria().andKskmEqualTo(jlc_kskm)
                        .andZtEqualTo(ExamineeService.ZT_UNASSIGN).andKsldIn(Arrays.asList(carinfo.getKsld(), ""))
                        .andYkrqEqualTo(DateTools.toDate(DateTools.getYMD(), DateTools.yyyyMMdd));
                exampreasign2 = exampreasignMapper.selectByExample(example2);
            }
            for (int i = 0; i < InitLoad.examineeNum(); i++) {
                if (exampreasign2.size() > i) {
                    Ksld ksld = ksldService.getByName(carinfo.getKsld().trim());
                    if (org.apache.commons.lang3.StringUtils.isEmpty(ksld.getKsxmpx())) {
                        throw new Exception("该车辆考试线路没有考试项目");
                    }
                    List<String> ksxmNames = new ArrayList<String>();
                    for (String ksxmCode : ksld.getKsxmpx().split(",")) {
                        ksxmNames.add(InitLoad.ksxmcode_name.get(ksxmCode));
                    }
                    String ksxmName = "";
                    for (String name : ksxmNames) {
                        ksxmName += name + ",";
                    }

                    exampreasign2.get(i).setKsxm(ksxmName.substring(0, ksxmName.length() - 1));
                    exampreasign2.get(i).setZt(ExamineeService.ZT_NOTINCAR);
                    exampreasign2.get(i).setJlcxh(jlcxh);
                    exampreasign2.get(i).setKsld(carinfo.getKsld());
                    exampreasign2.get(i).setKscx(carinfo.getJklx());
                    exampreasign2.get(i).setHphm(carinfo.getHphm());
                    exampreasign2.get(i).setKsy1(carinfo.getPky());
                    exampreasign2.get(i).setKcdddh(carinfo.getKcdddh());

                    examineeService.updateByLSH(exampreasign2.get(i));
                } else {
                    continue;
                }
            }

        } else {
            // 4. 没有全部考完,还是返回 当前组考生(不管其他考生完成与否)
            ExampreasignExample example45678 = new ExampreasignExample();
            example45678.setOrderByClause(" bdsj ");
            example45678.createCriteria().andJlcxhEqualTo(jlcxh)
                    .andZtIn(Arrays.asList(ExamineeService.ZT_NOEXAM, ExamineeService.ZT_EXAMING, ExamineeService.ZT_EXAM1STEND, ExamineeService.ZT_EXAMEND))
                    .andYkrqEqualTo(DateTools.toDate(DateTools.getYMD(), DateTools.yyyyMMdd));
            result = exampreasignMapper.selectByExample(example45678);
        }
        return result;

    }

}
