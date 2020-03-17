package com.example;

import com.example.utils.PdfUtils;
import com.example.utils.SignatureInfo;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;

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
public class App {


    public static void main(String[] args) throws IOException, BadElementException {
        // zip -d your.jar 'META-INF/.SF' 'META-INF/.RSA' 'META-INF/*SF'
        //                                                                                            左x       左y    右x     右y
        // java -jar xx.jar <pkcs12-keystore-file> <pin> <input-pdf> <output-pdf> <sign-image> <page> <posRX> <posRY> <posLX> <posLY>
        // write your code here

        System.out.println("进入主程序");

        boolean result = false;
        String resStr = "";

        if (args.length < 10) {
            System.out.println("参数不够");
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
        }

        System.out.println("success");
    }

}
