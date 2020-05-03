package com.cs443.bilshortredirect.link.controllers;

import com.cs443.bilshortredirect.jedis.JedisPublisher;
import com.cs443.bilshortredirect.link.models.BrowserType;
import com.cs443.bilshortredirect.link.models.PlatformType;
import eu.bitwalker.useragentutils.UserAgent;
import com.cs443.bilshortredirect.jedis.KeyExpiredListener;
import com.cs443.bilshortredirect.link.models.Link;
import com.cs443.bilshortredirect.link.services.LinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import ro.rasel.throttling.Throttling;
import ro.rasel.throttling.ThrottlingType;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequestMapping("r")
@RestController
public class RedirectController {

    @Autowired
    private LinkService linkService;

    @Autowired
    private JedisPublisher jedisPublisher;

    @Autowired
    KeyExpiredListener keyExpiredListener;

    @PostConstruct
    private void init(){
        new Thread(() -> {
            JedisPool pool = new JedisPool(new JedisPoolConfig());
            Jedis j = pool.getResource();
            j.psubscribe(keyExpiredListener, "*");
        }).start();
    }

    @Throttling(type = ThrottlingType.HeaderValue, headerName = "X-Forwarded-For", limit = 100, timeUnit = TimeUnit.MINUTES)
    @GetMapping("{code}")
    public ResponseEntity<?> redirectUrl(@PathVariable("code") String code, @RequestHeader(value = "User-Agent") String userAgent) {
        HttpHeaders responseHeaders = new HttpHeaders();
        Link link = linkService.getLinkByCode(code);

        if (link == null) {
            System.out.println("No link found: " + code);
            return ResponseEntity.notFound().build();
        }

        UserAgent ua = UserAgent.parseUserAgentString(userAgent);
        String browser = ua.getBrowser().getName();
        String os = ua.getOperatingSystem().getName();

        if (browser.toLowerCase().contains(BrowserType.CHROME.value())) {
            link.setVisitCountFromChrome(link.getVisitCountFromChrome() + 1);
        }
        else if (browser.toLowerCase().contains(BrowserType.FIREFOX.value())) {
            link.setVisitCountFromFirefox(link.getVisitCountFromFirefox() + 1);
        }
        else if (browser.toLowerCase().contains(BrowserType.SAFARI.value())) {
            link.setVisitCountFromSafari(link.getVisitCountFromSafari() + 1);
        }
        else if (browser.toLowerCase().contains(BrowserType.INTERNET_EXPLORER.value())) {
            link.setVisitCountFromIE(link.getVisitCountFromIE() + 1);
        }
        else {
            link.setVisitCountFromOtherBrowser(link.getVisitCountFromOtherBrowser() + 1);
        }


        if (os.toLowerCase().contains(PlatformType.WINDOWS.value())) {
            link.setVisitCountFromWindows(link.getVisitCountFromWindows() + 1);
        }
        else if (os.toLowerCase().contains(PlatformType.LINUX.value())) {
            link.setVisitCountFromLinux(link.getVisitCountFromLinux() + 1);
        }
        else if (os.toLowerCase().contains(PlatformType.MAC.value())) {
            link.setVisitCountFromOsx(link.getVisitCountFromOsx() + 1);
        }
        else if (os.toLowerCase().contains(PlatformType.ANDROID.value())) {
            link.setVisitCountFromAndroid(link.getVisitCountFromAndroid() + 1);
        }
        else if (os.toLowerCase().contains(PlatformType.IOS.value())) {
            link.setVisitCountFromIOS(link.getVisitCountFromIOS() + 1);
        }
        else {
            link.setVisitCountFromOtherOs(link.getVisitCountFromOtherOs() + 1);
        }

        Link updatedLink = linkService.updateLink(link);

        if (updatedLink == null){ //expired
            return ResponseEntity.notFound().build();
        }

        try {
            responseHeaders.setLocation(new URI(link.getUrl()));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return ResponseEntity.status(HttpStatus.OK).headers(responseHeaders).build();
    }

//    @Throttling(type = ThrottlingType.HeaderValue, headerName = "X-Forwarded-For", limit = 100, timeUnit = TimeUnit.MINUTES)
//    @GetMapping("{code}")
//    public ResponseEntity<?> redirectUrl(@PathVariable("code") String code, @RequestHeader(value = "User-Agent") String userAgent) {
//        HttpHeaders responseHeaders = new HttpHeaders();
//        List<String> browserAndPlatform = setBrowserAndPlatformFromUserAgent(userAgent);
//
//        Optional<String> url = jedisPublisher.getCachedLink(code);
//
//        if (url.isPresent()){
//            try {
//                jedisPublisher.updateCachedLink(code, browserAndPlatform);
//                responseHeaders.setLocation(new URI(url.get()));
//            } catch (URISyntaxException e) {
//                e.printStackTrace();
//            }
//        }
//        else {
//            Link link = linkService.getLinkByCode(code);
//
//            if (link == null) {
//                return ResponseEntity.notFound().build();
//            }
//
//            jedisPublisher.cacheNewLink(link, browserAndPlatform);
//
//            try {
//                responseHeaders.setLocation(new URI(link.getUrl()));
//            } catch (URISyntaxException e) {
//                e.printStackTrace();
//            }
//        }
//
////        return ResponseEntity.status(HttpStatus.SEE_OTHER).headers(responseHeaders).build();
//        return ResponseEntity.status(HttpStatus.OK).headers(responseHeaders).build();
//    }

    private List<String> setBrowserAndPlatformFromUserAgent(String userAgent) {
        UserAgent ua = UserAgent.parseUserAgentString(userAgent);
        String browser = ua.getBrowser().getName();
        String os = ua.getOperatingSystem().getName();

        List<String> toBeUpdated = new ArrayList<>();
        setBrowser(toBeUpdated, browser);
        setPlatform(toBeUpdated, os);
        return toBeUpdated;
    }

    private void setPlatform(List<String> list, String os) {
        List<String> platforms = Stream
                .of(PlatformType.WINDOWS, PlatformType.LINUX, PlatformType.MAC, PlatformType.ANDROID, PlatformType.IOS)
                .map(PlatformType::value)
                .collect(Collectors.toList());

        final String osLower = os.toLowerCase();

        int linkSize = list.size();
        platforms.forEach(e -> {
            if (osLower.contains(e)){
                list.add(e);
            }
        });

        if (list.size() == linkSize){
            list.add(PlatformType.OTHER_OS.value());
        }
    }

    private void setBrowser(List<String> list, String browser) {
        List<String> browsers = Stream
                .of(BrowserType.CHROME, BrowserType.FIREFOX, BrowserType.SAFARI, BrowserType.INTERNET_EXPLORER)
                .map(BrowserType::value)
                .collect(Collectors.toList());

        final String browserLower = browser.toLowerCase();

        int linkSize = list.size();
        browsers.forEach(e -> {
            if (browserLower.contains(e)){
                list.add(e);
            }
        });

        if (list.size() == linkSize){
            list.add(BrowserType.OTHER_BROWSER.value());
        }
    }
}