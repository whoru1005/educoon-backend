package com.educoon.domain.quote.dto;

import com.educoon.domain.quote.entity.Quote;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class QuoteResponse {

    private String content;
    private String author;

    public QuoteResponse(Quote quote){
        this.content = quote.getContent();
        this.author = quote.getAuthor();
    }
}
