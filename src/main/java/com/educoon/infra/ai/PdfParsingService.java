package com.educoon.infra.ai;

import com.educoon.exception.CustomException;
import com.educoon.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
public class PdfParsingService {

    public String extractText(InputStream inputStream){
        if(inputStream == null){
            throw new IllegalArgumentException("InputStream cannot be null");
        }

        try (PDDocument document = Loader.loadPDF(new RandomAccessReadBuffer(inputStream))){

            PDFTextStripper stripper = new PDFTextStripper();

            String text = stripper.getText(document);

            log.info("PDF 텍스트 추출 성공. ({} 글자)", text.length());

            return text;

        }catch (IOException e){
            log.error("PDF 파싱 중 오류 발생", e);
            throw new CustomException(ErrorCode.PDF_PARSING_ERROR);
        }
    }


}
