package com.cs443.bilshortredirect.link.services;

import com.cs443.bilshortredirect.link.models.Link;
import com.cs443.bilshortredirect.link.repositories.LinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LinkServiceImpl implements LinkService {

    @Autowired
    private LinkRepository linkRepository;

    @Override
    public Link getLinkByCode(String code) {
        return linkRepository.findByCode(code);
    }

}