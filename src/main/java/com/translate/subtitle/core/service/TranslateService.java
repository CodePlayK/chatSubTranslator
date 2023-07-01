package com.translate.subtitle.core.service;

import com.alibaba.fastjson.JSONArray;
import com.translate.subtitle.core.entity.Line;
import com.translate.subtitle.core.entity.Subtitle;
import com.translate.subtitle.core.util.CookieUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TranslateService {
    public static final Map<String, String> WORD_MAP = new HashMap<>();
    private static final Map<String, String> LANGUAGE_MAP = new HashMap<>();
    private static final String PATH = "https://translate.googleapis.com/translate_a/single"; //地址
    private static final String CLIENT = "gtx";
    private final Logger LOGGER = LogManager.getLogger(this.getClass());
    @Autowired
    private CookieUtils cookieUtils;
    public TranslateService() {
        init();
    }

    public void translateLineByGoogle(Subtitle subtitle) {
        List<Line> lines = subtitle.getLine();
        for (Line line : lines) {
            try {
                LOGGER.info("原文[{}]", line.getOriginal());
                line.setTranslation(translateText(line.getOriginal(), "en", "zh_cn"));
                LOGGER.info("译文[{}]", line.getTranslation());
                Thread.sleep(3000);
            } catch (Exception e) {
                LOGGER.warn("翻译异常!", e);
            }
        }
    }

    /**
     * 翻译文本
     *
     * @param text       文本内容
     * @param sourceLang 文本所属语言。如果不知道，可以使用auto
     * @param targetLang 目标语言。必须是明确的有效的目标语言
     */
    public String translateText(String text, String sourceLang, String targetLang) throws Exception {

        for (Map.Entry<String, String> stringStringEntry : WORD_MAP.entrySet()) {
            if (text.contains(stringStringEntry.getKey())) {
                text = StringUtils.replaceIgnoreCase(text, stringStringEntry.getKey(), stringStringEntry.getValue());
            }
        }

        StringBuilder retStr = new StringBuilder();
        if (!(isSupport(sourceLang) || isSupport(targetLang))) {
            throw new Exception("不支持的语言类型");
        }
        String finalPath = PATH + "?client=" + CLIENT + "&sl=" + sourceLang + "&tl=" + targetLang + "&dt=t&q=" + text;
        String resp = postHttp(finalPath);
        if (null == resp) {
            throw new Exception("网络异常");
        }
        JSONArray jsonObject = JSONArray.parseArray(resp);
        for (Object o : jsonObject.getJSONArray(0)) {
            JSONArray a = (JSONArray) o;
            retStr.append(a.getString(0));
        }

        return retStr.toString();
    }

    public boolean isSupport(String language) {
        return null != LANGUAGE_MAP.get(language);
    }

    /**
     * post 请求
     *
     * @param url 请求地址
     */
    private String postHttp(String url) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        //报错js不支持是因为User-Agent没有对应到谷歌浏览器的版本
        httpHeaders.add("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Safari/537.36");
        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setProxy(new Proxy(Proxy.Type.HTTP, CookieUtils.inetSocketAddress));
        restTemplate.setRequestFactory(simpleClientHttpRequestFactory);
        org.springframework.http.HttpEntity<Object> httpEntity = new org.springframework.http.HttpEntity<>(httpHeaders);
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);

        return exchange.getBody();
    }


    /**
     * 初始化语言类
     */
    private void init() {
        //FF15
        WORD_MAP.put("Cloud", "克劳德");
        WORD_MAP.put("Noctis", "诺克提斯");
        WORD_MAP.put("Ravus", "拉弗斯");
        WORD_MAP.put("Gladiolus", "格拉迪欧拉斯");
        WORD_MAP.put("Aranea", "阿拉尼雅");
        WORD_MAP.put("Ignis", "伊格尼斯");
        WORD_MAP.put("Regis", "雷吉斯");
        WORD_MAP.put("Prompto", "普伦普特");
        WORD_MAP.put("Cor", "科尔");
        WORD_MAP.put("O K I B E L I E V E Y O U", "行吧");
        WORD_MAP.put("Ardyn", "亚丹");
        WORD_MAP.put("Lunafreya", "露娜芙蕾雅");
        WORD_MAP.put("C U P N O D L E S™", "日清泡面™");


        LANGUAGE_MAP.put("auto", "Automatic");
        LANGUAGE_MAP.put("af", "Afrikaans");
        LANGUAGE_MAP.put("sq", "Albanian");
        LANGUAGE_MAP.put("am", "Amharic");
        LANGUAGE_MAP.put("ar", "Arabic");
        LANGUAGE_MAP.put("hy", "Armenian");
        LANGUAGE_MAP.put("az", "Azerbaijani");
        LANGUAGE_MAP.put("eu", "Basque");
        LANGUAGE_MAP.put("be", "Belarusian");
        LANGUAGE_MAP.put("bn", "Bengali");
        LANGUAGE_MAP.put("bs", "Bosnian");
        LANGUAGE_MAP.put("bg", "Bulgarian");
        LANGUAGE_MAP.put("ca", "Catalan");
        LANGUAGE_MAP.put("ceb", "Cebuano");
        LANGUAGE_MAP.put("ny", "Chichewa");
        LANGUAGE_MAP.put("zh_cn", "Chinese Simplified");
        LANGUAGE_MAP.put("zh_tw", "Chinese Traditional");
        LANGUAGE_MAP.put("co", "Corsican");
        LANGUAGE_MAP.put("hr", "Croatian");
        LANGUAGE_MAP.put("cs", "Czech");
        LANGUAGE_MAP.put("da", "Danish");
        LANGUAGE_MAP.put("nl", "Dutch");
        LANGUAGE_MAP.put("en", "English");
        LANGUAGE_MAP.put("eo", "Esperanto");
        LANGUAGE_MAP.put("et", "Estonian");
        LANGUAGE_MAP.put("tl", "Filipino");
        LANGUAGE_MAP.put("fi", "Finnish");
        LANGUAGE_MAP.put("fr", "French");
        LANGUAGE_MAP.put("fy", "Frisian");
        LANGUAGE_MAP.put("gl", "Galician");
        LANGUAGE_MAP.put("ka", "Georgian");
        LANGUAGE_MAP.put("de", "German");
        LANGUAGE_MAP.put("el", "Greek");
        LANGUAGE_MAP.put("gu", "Gujarati");
        LANGUAGE_MAP.put("ht", "Haitian Creole");
        LANGUAGE_MAP.put("ha", "Hausa");
        LANGUAGE_MAP.put("haw", "Hawaiian");
        LANGUAGE_MAP.put("iw", "Hebrew");
        LANGUAGE_MAP.put("hi", "Hindi");
        LANGUAGE_MAP.put("hmn", "Hmong");
        LANGUAGE_MAP.put("hu", "Hungarian");
        LANGUAGE_MAP.put("is", "Icelandic");
        LANGUAGE_MAP.put("ig", "Igbo");
        LANGUAGE_MAP.put("id", "Indonesian");
        LANGUAGE_MAP.put("ga", "Irish");
        LANGUAGE_MAP.put("it", "Italian");
        LANGUAGE_MAP.put("ja", "Japanese");
        LANGUAGE_MAP.put("jw", "Javanese");
        LANGUAGE_MAP.put("kn", "Kannada");
        LANGUAGE_MAP.put("kk", "Kazakh");
        LANGUAGE_MAP.put("km", "Khmer");
        LANGUAGE_MAP.put("ko", "Korean");
        LANGUAGE_MAP.put("ku", "Kurdish (Kurmanji)");
        LANGUAGE_MAP.put("ky", "Kyrgyz");
        LANGUAGE_MAP.put("lo", "Lao");
        LANGUAGE_MAP.put("la", "Latin");
        LANGUAGE_MAP.put("lv", "Latvian");
        LANGUAGE_MAP.put("lt", "Lithuanian");
        LANGUAGE_MAP.put("lb", "Luxembourgish");
        LANGUAGE_MAP.put("mk", "Macedonian");
        LANGUAGE_MAP.put("mg", "Malagasy");
        LANGUAGE_MAP.put("ms", "Malay");
        LANGUAGE_MAP.put("ml", "Malayalam");
        LANGUAGE_MAP.put("mt", "Maltese");
        LANGUAGE_MAP.put("mi", "Maori");
        LANGUAGE_MAP.put("mr", "Marathi");
        LANGUAGE_MAP.put("mn", "Mongolian");
        LANGUAGE_MAP.put("my", "Myanmar (Burmese)");
        LANGUAGE_MAP.put("ne", "Nepali");
        LANGUAGE_MAP.put("no", "Norwegian");
        LANGUAGE_MAP.put("ps", "Pashto");
        LANGUAGE_MAP.put("fa", "Persian");
        LANGUAGE_MAP.put("pl", "Polish");
        LANGUAGE_MAP.put("pt", "Portuguese");
        LANGUAGE_MAP.put("ma", "Punjabi");
        LANGUAGE_MAP.put("ro", "Romanian");
        LANGUAGE_MAP.put("ru", "Russian");
        LANGUAGE_MAP.put("sm", "Samoan");
        LANGUAGE_MAP.put("gd", "Scots Gaelic");
        LANGUAGE_MAP.put("sr", "Serbian");
        LANGUAGE_MAP.put("st", "Sesotho");
        LANGUAGE_MAP.put("sn", "Shona");
        LANGUAGE_MAP.put("sd", "Sindhi");
        LANGUAGE_MAP.put("si", "Sinhala");
        LANGUAGE_MAP.put("sk", "Slovak");
        LANGUAGE_MAP.put("sl", "Slovenian");
        LANGUAGE_MAP.put("so", "Somali");
        LANGUAGE_MAP.put("es", "Spanish");
        LANGUAGE_MAP.put("su", "Sundanese");
        LANGUAGE_MAP.put("sw", "Swahili");
        LANGUAGE_MAP.put("sv", "Swedish");
        LANGUAGE_MAP.put("tg", "Tajik");
        LANGUAGE_MAP.put("ta", "Tamil");
        LANGUAGE_MAP.put("te", "Telugu");
        LANGUAGE_MAP.put("th", "Thai");
        LANGUAGE_MAP.put("tr", "Turkish");
        LANGUAGE_MAP.put("uk", "Ukrainian");
        LANGUAGE_MAP.put("ur", "Urdu");
        LANGUAGE_MAP.put("uz", "Uzbek");
        LANGUAGE_MAP.put("vi", "Vietnamese");
        LANGUAGE_MAP.put("cy", "Welsh");
        LANGUAGE_MAP.put("xh", "Xhosa");
        LANGUAGE_MAP.put("yi", "Yiddish");
        LANGUAGE_MAP.put("yo", "Yoruba");
        LANGUAGE_MAP.put("zu", "Zulu");
    }

}
