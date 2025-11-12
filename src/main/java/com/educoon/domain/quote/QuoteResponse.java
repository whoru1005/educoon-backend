package com.educoon.domain.quote;

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
