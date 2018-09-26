package com.zjasm.captcha;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Random;

import javax.imageio.ImageIO;

/**
 * 验证码生成器
 */
public class CaptchaUtil{

    /**
     * 验证码图片的宽度。
     */
    private int width = 95;

    /**
     * 验证码图片的高度。
     */
    private int height = 38;

    /**
     * 验证码字符个数
     */
    private int codeCount = 4;

    /**
     * xx
     */
    private int xx = 0;

    /**
     * 字体高度
     */
    private int fontHeight;

    /**
     * codeY
     */
    private int codeY;


    // 验证码
    private String code = null;
    // 验证码图片Buffer
    private BufferedImage buffImg = null;

    /**
     * codeSequence
     */
    // 验证码范围,去掉0(数字)和O(拼音)容易混淆的(小写的1和L也可以去掉,大写不用了)
    char[] codeSequence = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J',
            'K', 'L', 'M', 'N',  'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
            'X', 'Y', 'Z', '2', '3', '4', '5', '6', '7', '8', '9' };
    /**
     * 默认构造函数,设置默认参数
     */
    public CaptchaUtil() {
        this.createCode();
    }

    /**
     * @param width  图片宽
     * @param height 图片高
     */
    public CaptchaUtil(int width, int height) {
        this.width = width;
        this.height = height;
        xx = width / (codeCount + 1);
        fontHeight = height - 2;
        codeY = height - 4;
        this.createCode();
    }

    /**
     * @param width     图片宽
     * @param height    图片高
     * @param codeCount 字符个数
     */
    public CaptchaUtil(int width, int height, int codeCount) {
        this.width = width;
        this.height = height;
        xx = width / (codeCount + 1);
        fontHeight = height - 2;
        codeY = height - 4;
        this.createCode();
    }

    /**
     *
     */
    protected void createCode(){

        // 定义图像buffer
        buffImg = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D gd = buffImg.createGraphics();

        // 创建一个随机数生成器类
        Random random = new Random();

        // 将图像填充为白色
        gd.setColor(Color.WHITE);
        gd.fillRect(0, 0, width, height);

        // 创建字体，字体的大小应该根据图片的高度来定。
        Font font = new Font("Fixedsys", Font.PLAIN, fontHeight);
        // 设置字体。
        gd.setFont(font);

        // 画边框。
        gd.setColor(Color.lightGray);
        //gd.drawRect(0, 0, width - 1, height - 1);
        //设置圆角
        gd.drawRoundRect(0, 0, width-1, height-1,8,8);

        // 随机产生160条干扰线，使图象中的认证码不易被其它程序探测到。
        gd.setColor(Color.BLACK);
        for (int i = 0; i <  2; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int xl = random.nextInt(12);
            int yl = random.nextInt(12);
            gd.drawLine(x, y, x + xl, y + yl);
        }

        // randomCode用于保存随机产生的验证码，以便用户登录后进行验证。
        StringBuffer randomCode = new StringBuffer();
        int red = 0, green = 0, blue = 0;

        // 随机产生codeCount数字的验证码。
        for (int i = 0; i < codeCount; i++) {
            // 得到随机产生的验证码数字。
            String strRand = String.valueOf(codeSequence[random.nextInt(codeSequence.length)]);
//			String strRand = String.valueOf(codeSequence[random.nextInt(1)]);
            // 产生随机的颜色分量来构造颜色值，这样输出的每位数字的颜色值都将不同。
            red = random.nextInt(255);
            green = random.nextInt(255);
            blue = random.nextInt(255);

            // 用随机产生的颜色将验证码绘制到图像中。
            gd.setColor(new Color(red, green, blue));
            if(i==0){
                gd.drawString(strRand, 5, codeY);

            }else{
                gd.drawString(strRand, 5+(i * xx), codeY);
            }

            // 将产生的四个随机数组合在一起。
            randomCode.append(strRand);
        }
        // 将四位数字的验证码保存到Session中。
        code = randomCode.toString();
    }

    public void write(String path) throws IOException {
        OutputStream sos = new FileOutputStream(path);
        this.write(sos);
    }

    public void write(OutputStream sos) throws IOException {
        ImageIO.write(buffImg, "png", sos);
        sos.close();
    }

    public BufferedImage getBuffImg() {
        return buffImg;
    }

    public String getCode() {
        return code;
    }

    /**
     * 测试函数,默认生成到d盘
     *
     * @param args
     */
    public static void main(String[] args) {
        CaptchaUtil vCode = new CaptchaUtil(60, 20, 5);
        try {
            String path = "E:/" + new Date().getTime() + ".png";
            System.out.println(vCode.getCode() + " >" + path);
            vCode.write(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

