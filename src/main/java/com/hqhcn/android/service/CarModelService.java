package com.hqhcn.android.service;

import com.hqhcn.android.entity.Carmodel;
import com.hqhcn.android.entity.CarmodelExample;
import com.hqhcn.android.entity.CarmodelKey;

import java.util.List;

public interface CarModelService {
    List<String> getCLPPXH(String kcdddh);

    void insert(Carmodel entity);

    Carmodel get(String id, String kcdddh);

    /**
     * 取第一个符合条件的记录
     * @param clppxh 车辆品牌型号
     * @param kcdddh 考场地点代号
     * @return
     */
    Carmodel getByCLPPXH(String clppxh, String kcdddh);

    void update(Carmodel entity);

    /**
     * 分页查询
     *
     * @param example
     * @return
     */
    List<Carmodel> queryToPage(CarmodelExample example);

    /**
     * 普通查询
     *
     * @param example
     * @return
     */
    List<Carmodel> query(CarmodelExample example);


    void delete(CarmodelKey carmodelKey) throws Exception;
}
