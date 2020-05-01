package com.cs443.bilshortredirect.link.services;

import com.cs443.bilshortredirect.link.models.Link;

public interface LinkService {
    Link getLinkByCode(String code);
}
