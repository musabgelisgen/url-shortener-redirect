package com.cs443.bilshortredirect.link.repositories;

import com.cs443.bilshortredirect.link.models.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface LinkRepository extends JpaRepository<Link, Integer> {

    @Transactional
    Link save(Link link);

    Link findByCode(String code);
}
