package com.example.gem_fan_club_web.controller;

import com.example.gem_fan_club_web.model.ResponseDTO;
import com.example.gem_fan_club_web.model.quote.Quote;
import com.example.gem_fan_club_web.model.quote.QuotePicture;
import com.example.gem_fan_club_web.service.QuoteService;
import com.example.gem_fan_club_web.utils.AssertTools;
import com.example.gem_fan_club_web.utils.FileTools;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/quote")
public class QuoteController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private QuoteService quoteService;

    private FileTools fileTools = new FileTools();




    @GetMapping("/quoteList")
    public ResponseDTO quoteList() {
        List<Quote> quoteList = quoteService.getAllQuote();
        return new ResponseDTO(200,"success",quoteList);
    }

    @GetMapping("/quotePicture")
    public ResponseDTO quotePicture(
            @RequestParam("quoteId") String quoteId
    ){
        List<QuotePicture> pictureList =  quoteService.getPicturesByQuoteId(Long.valueOf(quoteId));

        return new ResponseDTO(200,"success",pictureList);
    }

    @GetMapping("/addLike")
    public ResponseDTO addLike(
            @RequestParam("quoteId") Long quoteId,
            @RequestParam("userId") String userId
    ) {
        quoteService.addLike(quoteId,userId);
        return new ResponseDTO(200,"success",null);
    }

    @GetMapping("/eraseLike")
    public ResponseDTO eraseLike(
            @RequestParam("quoteId") Long quoteId,
            @RequestParam("userId") String userId
    ) {
        quoteService.eraseLike(quoteId,userId);
        return new ResponseDTO(200,"success",null);
    }

    @GetMapping("/isLiked")
    public ResponseDTO isLiked(
            @RequestParam("quoteId") Long quoteId,
            @RequestParam("userId") String userId
    ) {
        return new ResponseDTO(200,"success",quoteService.isLiked(quoteId,userId));
    }

    @GetMapping("/quoteDetail/{quoteId}")
    public ResponseDTO getQuoteDetail(@PathVariable Long quoteId) {
        Quote quote = quoteService.getQuoteById(quoteId);
        return new ResponseDTO(200,"success",quote);
    }

    @PostMapping("/upload")
    public ResponseDTO uploadQuote(
            @RequestParam("title") String content,
            @RequestParam("userId") String userId,
            @RequestParam("images") List<MultipartFile> images) {

        // 这里可以添加保存文件和其他业务逻辑
        try {

            List<String> imagePaths = fileTools.saveImages(images,uploadDir);

            Quote quote = quoteService.addQuote(content,userId);

            quoteService.addPicture(imagePaths, Long.valueOf(quote.getQuoteId()));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new ResponseDTO(200,"success",null);
    }
}
