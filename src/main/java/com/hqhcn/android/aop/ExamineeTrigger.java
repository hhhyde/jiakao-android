package com.hqhcn.android.aop;


import com.alibaba.fastjson.JSONObject;
import com.hqhcn.android.entity.Exampreasign;
import com.hqhcn.android.service.ExamineeService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.client.AsyncRestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * @author 柯江涛
 * 考生监听器
 */

@Aspect
@Component
public class ExamineeTrigger {

    protected final
    Logger logger = LoggerFactory.getLogger(this.getClass());

    private final
    ExamineeService examineeService;

    private final
    AsyncRestTemplate restTemplate;

    @Autowired
    public ExamineeTrigger(ExamineeService examineeService, AsyncRestTemplate restTemplate) {
        this.examineeService = examineeService;
        this.restTemplate = restTemplate;
    }


    /**
     * 切入com.hqh.service.impl.ExamineeServiceImpl类下的<code>所有update开头</code>和<code>insert</code>方法
     */
    @Pointcut("execution(* com.hqhcn.android.service.impl.ExamineeServiceImpl.update*(..))  || execution(* com.hqhcn.android.service.impl.ExamineeServiceImpl.insert(..))")
    public void statusPoint() {

    }

    @Around("statusPoint()")
    public Object auditHandler(ProceedingJoinPoint pjp) throws Throwable {

        String lsh = Arrays.stream(pjp.getArgs())
                .filter(Objects::nonNull)
                .filter(s -> s instanceof Exampreasign)
                .map(e -> ((Exampreasign) e).getLsh())
                .findFirst()
                .orElse("");

        Exampreasign exampreasign = examineeService.queryByLsh(lsh);
        try {
            Object result = pjp.proceed();
            if (null != exampreasign && exampreasign.getZt() >= 2) {
                pushToLED(lsh);
            }
            return result;
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 切入com.hqh.service.impl.ExamineeServiceImpl类下的<code>所有update开头</code>和<code>insert</code>方法
     */
    @Pointcut("execution(* com.hqhcn.android.service.impl.ExamineeServiceImpl.pull(..))")
    public void pullPoint() {

    }

    @Around("pullPoint()")
    public Object pullHandler(ProceedingJoinPoint pjp) throws Throwable {

        Object result = pjp.proceed();
        if (result instanceof List) {
            List<Exampreasign> objectList = (List<Exampreasign>) result;
            objectList.stream()
                    .filter(Objects::nonNull)
                    .filter(s -> s instanceof Exampreasign)
                    .forEach(e -> pushToLED(e.getLsh()));
        }
        return result;
    }


    /**
     * 异步请求
     *
     * @param lsh 考生流水号
     */
    private void pushToLED(String lsh) {
        String path = "input/pushToLED";
        Exampreasign exampreasign = examineeService.queryByLsh(lsh);
        if (null == exampreasign) {
            logger.warn("找不到考生!lsh=" + lsh);
            return;
        }

        JSONObject push = new JSONObject();
        push.put("lsh", exampreasign.getLsh());
        push.put("jlcxh", exampreasign.getJlcxh());
        push.put("xm", exampreasign.getXm());
        push.put("sfzmhm", exampreasign.getSfzmhm());
        push.put("zt", exampreasign.getZt() + "");

        asyncPost(path, push);
    }

    private void asyncPost(String path, JSONObject json) {
        String url = "http://TCP/";
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("content", json.toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        restTemplate.exchange(url + path, HttpMethod.POST, entity, String.class).addCallback(new ListenableFutureCallback<ResponseEntity<String>>() {
            @Override
            public void onFailure(Throwable throwable) {
                logger.warn("[推送考生状态]失败", throwable);
            }

            @Override
            public void onSuccess(ResponseEntity<String> stringResponseEntity) {
                logger.info(String.format("[推送考生状态]发送:%s,参数:%s,返回信息:%s", url, json, stringResponseEntity.getBody()));
            }
        });
    }
}
