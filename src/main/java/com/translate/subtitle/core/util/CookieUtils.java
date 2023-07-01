package com.translate.subtitle.core.util;

import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.net.*;
import java.util.Iterator;
import java.util.List;

@Component
@Data
public class CookieUtils {
    private static final CookieUtils cookieUtils = new CookieUtils();
    public static InetSocketAddress inetSocketAddress;
    private final Logger LOGGER = LogManager.getLogger();

    private CookieUtils() {

    }

    public static CookieUtils getInstance() {
        return cookieUtils;
    }

    /**
     * 获取系统代理
     *
     * @return
     */
    public void getSysProxy() {
        inetSocketAddress = new InetSocketAddress(0);
        System.setProperty("java.net.useSystemProxies", "true");
        List<Proxy> l = null;
        try {
            l = ProxySelector.getDefault().select(new URI("https://nhentai.net/favorites/"));
        } catch (URISyntaxException e) {
            LOGGER.error(e.getMessage());
        }
        for (Iterator<Proxy> iter = l.iterator(); iter.hasNext(); ) {
            Proxy proxy = iter.next();
            InetSocketAddress addr = (InetSocketAddress) proxy.address();
            if (null == addr || null == addr.getHostName()) {
                LOGGER.error("系统无代理,无法连接Pixiv!");
            } else {
                LOGGER.warn("当前代理地址:{}:{}", addr.getHostName(), addr.getPort());
                inetSocketAddress = addr;
            }
        }
    }

}
