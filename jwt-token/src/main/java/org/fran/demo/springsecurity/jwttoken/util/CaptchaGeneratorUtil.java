package org.fran.demo.springsecurity.jwttoken.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

public class CaptchaGeneratorUtil {

    private static char[] mapTable = {
            'a', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'i', 'j', 'k', 'l',
            'm', 'n', 'o', 'p', 'q', 'r',
            's', 't', 'u', 'v', 'w', 'x',
            'y', 'z', '0', '1', '2', '3',
            '4', '5', '6', '7', '8', '9'
    };

    public static String createRandomCode(int num){
        //num代表几位验证码，如果要产生更多位的验证码，则加大数值
        String strEnsure = "";
        for (int i = 0; i < num; ++i) {
            strEnsure += mapTable[(int) (mapTable.length * Math.random())];
        }
        return strEnsure;
    }

    /* 功能：生成彩色验证码图片
     参数wedth为生成图片的宽度，参数height为生成图片的高度，参数os为页面的输出流
    */
    public String getCertPic(int width, int height, OutputStream os){
        return getCertPic(width, height, createRandomCode(4), os);
    }

    public String getCertPic(int width, int height, String code, OutputStream os) {
        if (width <= 0) {
            width = 60;
        }
        if (height <= 0) {
            height = 20;
        }
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        //获取图形上下文
        Graphics g = image.getGraphics();
        //设定背景颜色
        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0, 0, width, height);
        //画边框
        g.setColor(Color.black);
        g.drawRect(0, 0, width - 1, height - 1);
        //将认证码显示到图像中，如果要生成更多位的验证码，增加drawString语句
        g.setColor(Color.black);
        g.setFont(new Font("Atlantic Inline", Font.PLAIN, 18));
        String str = code.substring(0, 1);
        g.drawString(str, 8, 17);
        str = code.substring(1, 2);
        g.drawString(str, 20, 15);
        str = code.substring(2, 3);
        g.drawString(str, 35, 18);
        str = code.substring(3, 4);
        g.drawString(str, 45, 15);
        //随机产生15个干扰点
        Random rand = new Random();
        for (int i = 0; i < 10; i++) {
            int x = rand.nextInt(width);
            int y = rand.nextInt(height);
            g.drawOval(x, y, 1, 1);
        }
        //释放图形上下文
        g.dispose();
        try {
            //输出图形到页面
            ImageIO.write(image, "JPEG", os);

        } catch (IOException e) {
            return "";
        }
        return code;
    }
}
