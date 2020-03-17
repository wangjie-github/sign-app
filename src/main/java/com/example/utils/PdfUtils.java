package com.example.utils;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;

public class PdfUtils {


    static {
        try {
            Security.addProvider(new BouncyCastleProvider());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 单多次签章通用
     *
     * @param src           需签名文件的路径
     * @param target        输出的文件路径
     * @param signatureInfo 签名的信息
     * @param certPath      证书
     * @throws Exception 签名失败异常
     *                   true 时间戳超时，false 签署正常
     */
    public static boolean sign(String src, String target, SignatureInfo signatureInfo, InputStream certPath, String certPassWord) throws Exception {
        InputStream inputStream = new FileInputStream(src);
        FileOutputStream outputStream;
        ByteArrayOutputStream result = new ByteArrayOutputStream();

//            TSAClient tsc = getTsaClient();
//            if(tsc == null){
//                return true;
//            }

        KeyStore ks = KeyStore.getInstance("PKCS12", "BC");
        ks.load(certPath, certPassWord.toCharArray());
        String alias = ks.aliases().nextElement();
        PrivateKey pk = (PrivateKey) ks.getKey(alias, certPassWord.toCharArray());
        Certificate[] chain = ks.getCertificateChain(alias);

        ByteArrayOutputStream tempArrayOutputStream = new ByteArrayOutputStream();
        PdfReader reader = new PdfReader(inputStream);

        // 创建签章工具PdfStamper ，最后一个boolean参数是否允许被追加签名
        // false的话，pdf文件只允许被签名一次，多次签名，最后一次有效
        // true的话，pdf可以被追加签名，验签工具可以识别出每次签名之后文档是否被修改
        PdfStamper stamper = PdfStamper.createSignature(reader, tempArrayOutputStream, '\0', null, true);

        // 获取数字签章属性对象
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setReason(signatureInfo.getReason());
        appearance.setLocation(signatureInfo.getLocation());
        //设置签名的签名域名称，多次追加签名的时候，签名预名称不能一样，图片大小受表单域大小影响（过小导致压缩）
        //设置签名的位置，页码，签名域名称，多次追加签名的时候，签名预名称不能一样签名的位置，是图章相对于pdf页面的位置坐标，原点为pdf页面左下角
        //四个参数的分别是，图章左下角x，图章左下角y，图章右上角x，图章右上角y
        appearance.setVisibleSignature(signatureInfo.getVisibleSignature(), signatureInfo.getPage(), signatureInfo.getFieldName());
        //读取图章图片
        appearance.setSignatureGraphic(signatureInfo.getImage());

        //设置图章的显示方式，如下选择的是只显示图章（还有其他的模式，可以图章和签名描述一同显示）
        //表现形式：仅描述，仅图片，图片和描述，签章者和描述
        appearance.setRenderingMode(PdfSignatureAppearance.RenderingMode.GRAPHIC);

        // 摘要算法
        ExternalDigest digest = new BouncyCastleDigest();
        // 签名算法
        ExternalSignature signature = new PrivateKeySignature(pk, "SHA-256", "BC");
        // 调用itext签名方法完成pdf签章
        // 支持标准，CMS,CADES
        MakeSignature.signDetached(appearance, digest, signature, chain, null, null, null, 0, MakeSignature.CryptoStandard.CMS);

        //LtvTimestamp.timestamp(appearance, tsc, null);

        //定义输入流为生成的输出流内容，以完成多次签章的过程
        inputStream = new ByteArrayInputStream(tempArrayOutputStream.toByteArray());
        result = tempArrayOutputStream;
        outputStream = new FileOutputStream(new File(target));
        outputStream.write(result.toByteArray());
        outputStream.flush();
        outputStream.close();
        inputStream.close();
        result.close();
        return false;
    }

    /**
     * 获取时间戳
     *
     * @return
     */
    private static TSAClient getTsaClient() {
        TSAClient tsaClient = null;
        try {
            // CFCA
            tsaClient = new TSAClientBouncyCastle("http://210.74.42.17/timestamp");
        } catch (Exception e) {
            try {
                // DigCert
                tsaClient = new TSAClientBouncyCastle("http://timestamp.digicert.com");
            } catch (Exception ex) {
                return null;
            }
        }
        //TSAClient tsaClient = new TSAClientBouncyCastle("http://210.74.42.17/timestamp");  // CFCA
        //TSAClient tsaClient = new TSAClientBouncyCastle("http://timestamp.digicert.com"); // DigCert
        return tsaClient;
    }


    /**
     * 由平台权威签名
     * @param srcPath 签名的pdf路径
     * @param targetSrc 输入的pdf文件夹路径
     * @param reason 理由
     * @param location 位置
     */
//        public static void platformSign(String srcPath, String targetSrc,String targetFilePath, String reason, String location,String signType) throws Exception{
//            File targetFile = new File(targetSrc);
//            if (!targetFile.exists()) {
//                targetFile.mkdirs();
//            }
//
//            String fieldName = "1";
//
//            PdfReader reader = new PdfReader(srcPath);
//            AcroFields af = reader.getAcroFields();
//            ArrayList<String> names = af.getSignatureNames();
//            for (String name : names) {
//                if(name.length() >= 10){
//                    String pdfFieldName = name.substring(0,10);
//                    if(pdfFieldName.contains("Signature")){
//                        pdfFieldName = pdfFieldName.substring(pdfFieldName.length()-1, pdfFieldName.length());
//                        if(pdfFieldName.equals(fieldName)){
//                            fieldName = String.valueOf(Integer.parseInt(pdfFieldName) + 1);
//                        }
//                    }
//                }
//            }
//
//
//            String certPath = null;
//            try {
//                if ("\\".equals(File.separator)) {
//                    certPath = ResourceUtils.getFile("classpath:mySign").getPath() + "/mysign.p12";
//                }else if ("/".equals(File.separator)){
//                    certPath= getFileRealPath("","mysign.p12");
//                }
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//
//            InputStream cert = null;
//            if (certPath != null) {
//                cert = new FileInputStream(certPath);
//            }
//            TSAClient tsc = getTsaClient();
//            KeyStore ks = KeyStore.getInstance("pkcs12", "BC");
//            ks.load(cert, "MySign".toCharArray());
//            String alias = (String)ks.aliases().nextElement();
//            PrivateKey pk = (PrivateKey) ks.getKey(alias, "MySign".toCharArray());
//            Certificate[] chain = ks.getCertificateChain(alias);
//            // reader / stamper
//            //PdfReader reader = new PdfReader(srcPath);
//            File file = new File(targetFilePath);
//            if(file.exists()){
//                file.delete();
//            }
//            FileOutputStream os = new FileOutputStream(targetFilePath);
//            //PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0');
//
//            PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0', null, true);
//
//            // appearance
//            PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
//            appearance.setReason(reason);
//            appearance.setLocation(location);
//            appearance.setVisibleSignature(new Rectangle(0, 0, 0, 0), 1, "Signature" + fieldName);
//
//            // NOT_CERTIFIED 不验证
//            // CERTIFIED_NO_CHANGES_ALLOWED 经认证的不允许更改（作者签名，不允许更改）
//            // CERTIFIED_FORM_FILLING 注册表格填写（作者签名，表格填充允许）
//            // CERTIFIED_FORM_FILLING_AND_ANNOTATIONS 认证表格填写和注释（作者签名、表单填充和注释允许）
//            // certificationLevel 不验证
//
//            if(signType.equals("平台签署")){
//                appearance.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED);
//            }
//
//            // digital signature
//            ExternalSignature es = new PrivateKeySignature(pk, "SHA-256", "BC");
//            ExternalDigest digest = new BouncyCastleDigest();
//
//            MakeSignature.signDetached(appearance, digest, es, chain, null, null, tsc, 0, MakeSignature.CryptoStandard.CMS);
//            File file2 = new File(srcPath);
//            if(file2.exists()){
//                file2.delete();
//            }
//        }

    /**
     * 切割图片
     * 作者：jay
     * 日期：2018-01-10
     *
     * @param imgPath 原始图片路径
     * @param count   切割份数
     * @return itextPdf的Image[]
     * @throws IOException
     * @throws BadElementException
     */
    public static Image[] subImages(String imgPath, int count) throws IOException, BadElementException {
        Image[] nImage = new Image[count];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BufferedImage img = ImageIO.read(new File(imgPath));
        int h = img.getHeight();
        int w = img.getWidth();

        int sw = w / count;
        for (int i = 0; i < count; i++) {
            BufferedImage subImg;
            if (i == count - 1) {//最后剩余部分
                subImg = img.getSubimage(i * sw, 0, w - i * sw, h);
            } else {//前n-1块均匀切
                subImg = img.getSubimage(i * sw, 0, sw, h);
            }
            ImageIO.write(subImg, imgPath.substring(imgPath.lastIndexOf('.') + 1), out);
            nImage[i] = Image.getInstance(out.toByteArray());
            out.flush();
            out.reset();
        }
        return nImage;
    }


//        /**
//         * 给签名添加LTV
//         * @param src
//         * @param dest
//         * @throws Exception
//         */
//        public static void addLtv(String src, String dest) throws Exception{
//
//            OcspClient ocspClient = new OcspClientBouncyCastle();
//            CrlClient crlClient = new CrlClientOnline();
//            TSAClient tsc = getTsaClient();
//
//            PdfReader r = new PdfReader(src);
//            FileOutputStream fos = new FileOutputStream(dest);
//            PdfStamper stp = PdfStamper.createSignature(r, fos, '\0', null, true);
//            LtvVerification v = stp.getLtvVerification();
//            AcroFields fields = stp.getAcroFields();
//            List<String> names = fields.getSignatureNames();
//            String sigName = names.get(names.size() - 1);
//            PdfPKCS7 pkcs7 = fields.verifySignature(sigName);
//            if (pkcs7.isTsp()) {
//                v.addVerification(sigName, ocspClient, crlClient,
//                        LtvVerification.CertificateOption.SIGNING_CERTIFICATE,
//                        LtvVerification.Level.OCSP_CRL,
//                        LtvVerification.CertificateInclusion.NO);
//            }else {
//                for (String name : names) {
//                    v.addVerification(name, ocspClient, crlClient,
//                            LtvVerification.CertificateOption.WHOLE_CHAIN,
//                            LtvVerification.Level.OCSP_CRL,
//                            LtvVerification.CertificateInclusion.NO);
//                }
//            }
//            PdfSignatureAppearance sap = stp.getSignatureAppearance();
//            LtvTimestamp.timestamp(sap, tsc, null);
//        }
//
}


