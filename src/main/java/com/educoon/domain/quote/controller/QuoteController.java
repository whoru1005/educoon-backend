package com.educoon.domain.quote.controller;

import com.educoon.domain.quote.dto.QuoteResponse;
import com.educoon.domain.quote.service.QuoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quotes")
public class QuoteController {

    private final QuoteService quoteService;

    @GetMapping("/random")
    public ResponseEntity<QuoteResponse> getRandomQuote(){

        QuoteResponse quoteResponse = quoteService.getRandomQuote();

        return ResponseEntity.ok(quoteResponse);
    }
}
