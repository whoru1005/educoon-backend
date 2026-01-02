package com.educoon.domain.quote.repository;

import com.educoon.domain.quote.entity.Quote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuoteRepository extends JpaRepository<Quote, Long> {
}
