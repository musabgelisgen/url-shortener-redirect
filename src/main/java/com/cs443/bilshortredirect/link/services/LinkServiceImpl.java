package com.cs443.bilshortredirect.link.services;

import com.cs443.bilshortredirect.link.models.Link;
import com.cs443.bilshortredirect.link.repositories.LinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

@Service
public class LinkServiceImpl implements LinkService {

    @Autowired
    private LinkRepository linkRepository;

    @Override
    public Link getLinkByCode(String code) {
        return linkRepository.findByCode(code);
    }

    @Override
    public Link updateLink(Link link) {
        Date date = new Date();
        String expTimeHandler = "" + link.getExpTime();
        if (expTimeHandler.length() < 10){
            return null;
        }
        Long expTime = Long.parseLong(expTimeHandler.substring(0, 10));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        if (expTime < calendar.getTime().getTime() / 1000){
            return null;
        }
        link.setExpTime(expTime);
        return linkRepository.save(link);
    }

}