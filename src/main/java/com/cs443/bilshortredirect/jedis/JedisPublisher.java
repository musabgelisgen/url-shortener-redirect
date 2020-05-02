package com.cs443.bilshortredirect.jedis;


import com.cs443.bilshortredirect.link.models.BrowserType;
import com.cs443.bilshortredirect.link.models.Link;
import com.cs443.bilshortredirect.link.models.PlatformType;
import com.cs443.bilshortredirect.link.repositories.LinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class JedisPublisher {

    @Autowired
    LinkRepository linkRepository;

    private final JedisPool pool = new JedisPool(new JedisPoolConfig());
    private final Jedis jedis = pool.getResource();

    public void cacheNewLink(Link link, List<String> browserAndPlatform){
        Map<String, String> linkMap = new HashMap<>();
        linkMap.put("url", link.getUrl());
        linkMap.put(PlatformType.WINDOWS.value(), String.valueOf(0));
        linkMap.put(PlatformType.LINUX.value(), String.valueOf(0));
        linkMap.put(PlatformType.MAC.value(), String.valueOf(0));
        linkMap.put(PlatformType.ANDROID.value(), String.valueOf(0));
        linkMap.put(PlatformType.IOS.value(), String.valueOf(0));
        linkMap.put(PlatformType.OTHER_OS.value(), String.valueOf(0));

        linkMap.put(BrowserType.CHROME.value(), String.valueOf(0));
        linkMap.put(BrowserType.FIREFOX.value(), String.valueOf(0));
        linkMap.put(BrowserType.SAFARI.value(), String.valueOf(0));
        linkMap.put(BrowserType.INTERNET_EXPLORER.value(), String.valueOf(0));
        linkMap.put(BrowserType.OTHER_BROWSER.value(), String.valueOf(0));

        linkMap.put(browserAndPlatform.get(0), "1");
        linkMap.put(browserAndPlatform.get(1), "1");

        jedis.hset(link.getCode(), linkMap);
        updateShadowExpireTime(link.getCode());
    }

    public void updateCachedLink(String code, List<String> browserAndPlatform) {
        jedis.hincrBy(code, browserAndPlatform.get(0), 1);
        jedis.hincrBy(code, browserAndPlatform.get(1), 1);
    }

    public Optional<String> getCachedLink(String code){
        Optional<String> url = Optional.empty();

        if (jedis.exists(code)){
            url = Optional.of(jedis.hget(code, "url"));
            updateShadowExpireTime(code);
        }

        return url;
    }

    private void updateShadowExpireTime(String code){
        String shadowCode = "shadow:" + code;
        jedis.set(shadowCode, "");
        jedis.expire(shadowCode, 10);
    }

    void syncDatabaseWithRedis(String code){
        Link link = linkRepository.findByCode(code);
        Map<String, String> redisLink = getVisitCountsFromRedis(code);

        updateRepositoryLinkUsingRedisLink(link, redisLink);
        linkRepository.save(link);

        deleteLinkFromRedisWithCode(code);
    }

    private void updateRepositoryLinkUsingRedisLink(Link link, Map<String, String> redisLink) {
        link.setVisitCountFromChrome(link.getVisitCountFromChrome() + Integer.parseInt(redisLink.get(BrowserType.CHROME.value())));
        link.setVisitCountFromFirefox(link.getVisitCountFromFirefox() + Integer.parseInt(redisLink.get(BrowserType.FIREFOX.value())));
        link.setVisitCountFromSafari(link.getVisitCountFromSafari() + Integer.parseInt(redisLink.get(BrowserType.SAFARI.value())));
        link.setVisitCountFromIE(link.getVisitCountFromIE() + Integer.parseInt(redisLink.get(BrowserType.INTERNET_EXPLORER.value())));
        link.setVisitCountFromOtherBrowser(link.getVisitCountFromOtherBrowser() + Integer.parseInt(redisLink.get(BrowserType.OTHER_BROWSER.value())));

        link.setVisitCountFromWindows(link.getVisitCountFromWindows() + Integer.parseInt(redisLink.get(PlatformType.WINDOWS.value())));
        link.setVisitCountFromLinux(link.getVisitCountFromLinux() + Integer.parseInt(redisLink.get(PlatformType.LINUX.value())));
        link.setVisitCountFromOsx(link.getVisitCountFromOsx() + Integer.parseInt(redisLink.get(PlatformType.MAC.value())));
        link.setVisitCountFromAndroid(link.getVisitCountFromAndroid() + Integer.parseInt(redisLink.get(PlatformType.ANDROID.value())));
        link.setVisitCountFromIOS(link.getVisitCountFromIOS() + Integer.parseInt(redisLink.get(PlatformType.IOS.value())));
        link.setVisitCountFromOtherOs(link.getVisitCountFromOtherOs() + Integer.parseInt(redisLink.get(PlatformType.OTHER_OS.value())));
    }

    private void deleteLinkFromRedisWithCode(String code){
        jedis.del(code);
    }

    private Map<String, String> getVisitCountsFromRedis(String code){
        return jedis.hgetAll(code);
    }
}
