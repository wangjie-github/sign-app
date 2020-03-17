package com.example.controller;

import com.example.utils.PdfUtils;
import com.example.utils.SignatureInfo;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * <p>
 * 签名 前端控制器
 * </p>
 *
 * @author WangJie
 * @since 2019-11-28
 */
@Controller
@RequestMapping("/sign")
@Api(tags = "SignController", description = "签名管理")
public class SignController {


    @ApiOperation("主方法")
    @RequestMapping(value = "/main", method = RequestMethod.GET)
    @ResponseBody
    public String main(String[] args) throws IOException, BadElementException {
        // zip -d your.jar 'META-INF/.SF' 'META-INF/.RSA' 'META-INF/*SF'
        //                                                                                            左x       左y    右x     右y
        // java -jar xx.jar <pkcs12-keystore-file> <pin> <input-pdf> <output-pdf> <sign-image> <page> <posRX> <posRY> <posLX> <posLY>
        // write your code here

        boolean result = false;
        String resStr = "";

        if (args.length < 10) {
            return "参数不够";
        }

        File f = new File(args[0]);
        InputStream in = new FileInputStream(f);
        String target = args[3];
        Image image = Image.getInstance(args[4]);
        int page = Integer.parseInt(args[5]);
        int RX = Integer.parseInt(args[6]);
        int RY = Integer.parseInt(args[7]);
        int LX = Integer.parseInt(args[8]);
        int LY = Integer.parseInt(args[9]);

        Rectangle rectangle = new Rectangle(RX, RY, LX, LY);

        SignatureInfo signatureInfo = new SignatureInfo();

        signatureInfo.setImage(image);
        signatureInfo.setPage(page);
        signatureInfo.setVisibleSignature(rectangle);
        try {
            result = PdfUtils.sign(args[2], target, signatureInfo, in, args[1]);
        } catch (Exception e) {
            resStr = e.getMessage();
        }

        in.close();

        if (!result) {
            System.out.println(resStr);
            return resStr;
        }

        System.out.println("success");
        return "success";
    }

    // <pkcs12-keystore-file> <pin> <input-pdf> <output-pdf> <sign-image> <page> <posRX> <posRY> <posLX> <posLY>
    @ApiOperation("主方法参数版")
    @RequestMapping(value = "/main2", method = RequestMethod.GET)
    @ResponseBody
    public String main2(@RequestParam("pkcs12-keystore-file") String arg0,
                        @RequestParam("pin") String arg1,
                        @RequestParam("input-pdf") String arg2,
                        @RequestParam("output-pdf") String arg3,
                        @RequestParam("sign-image") String arg4,
                        @RequestParam("page") String arg5,
                        @RequestParam("posRX") String arg6,
                        @RequestParam("posRY") String arg7,
                        @RequestParam("posLX") String arg8,
                        @RequestParam("posLY") String arg9) throws IOException, BadElementException{
        String[] args = new String[]{arg0,arg1,arg2,arg3,arg4,arg5,arg6,arg7,arg8,arg9};
        return main(args);
    }

}
