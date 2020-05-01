package com.cs443.bilshortredirect.link.repositories;

import com.cs443.bilshortredirect.link.models.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LinkRepository extends JpaRepository<Link, Integer> {
    Link findByCode(String code);
}
