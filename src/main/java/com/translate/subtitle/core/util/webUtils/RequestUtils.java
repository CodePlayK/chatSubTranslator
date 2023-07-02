package com.translate.subtitle.core.util.webUtils;

import com.alibaba.fastjson.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.Proxy;

@Component
public class RequestUtils {
    private final static int WAIT_TIME = 30000;
    private final static int RETRY_TIME = 5;
    private final Logger LOGGER = LogManager.getLogger(this.getClass());

    public ResponseEntity<String> request(String url, JSONObject body, HttpHeaders headers) {
        RestTemplate restTemplate = new RestTemplate();
        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setProxy(new Proxy(Proxy.Type.HTTP, CookieUtils.inetSocketAddress));
        restTemplate.setRequestFactory(simpleClientHttpRequestFactory);
        HttpEntity<Object> httpEntity = new HttpEntity<>(body.toString(), headers);
        ResponseEntity<String> stringResponseEntity = null;
        for (int i = 0; i < RETRY_TIME; i++) {
            try {
                stringResponseEntity = restTemplate.postForEntity(url, httpEntity, String.class);
                return stringResponseEntity;
            } catch (RestClientException e) {
                LOGGER.warn("请求异常，等待{}s后重试……", WAIT_TIME / 1000, e);
            }
            try {
                Thread.sleep(WAIT_TIME);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
