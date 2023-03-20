package com.nowcoder.community;

import java.io.IOException;

/**
 * @Author: XTY~
 * @CreateTime: 20/3/2023 下午12:39
 * @Description:
 */

public class WKTest {
    public static void main(String[] args) {
        String cmd = "D:\\InstallProgram\\wkhtmltopdf\\bin/wkhtmltoimage --quality 75 https://www.baidu.com d:/wk-picturetest/3.png";
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
