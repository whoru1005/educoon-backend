package com.educoon.domain.quote;

import com.educoon.exception.CustomException;
import com.educoon.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final Random random = new Random();

    public QuoteResponse getRandomQuote(){
        long count = quoteRepository.count();

        if(count == 0){
            throw new CustomException(ErrorCode.QUOTE_NOT_FOUND);
        }

        int randomIndex = random.nextInt((int) count);

        Page<Quote> quotePage = quoteRepository.findAll(PageRequest.of(randomIndex, 1));

        if(!quotePage.hasContent()){
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        Quote randomQuote = quotePage.getContent().get(0);
        return new QuoteResponse(randomQuote);
    }
}
