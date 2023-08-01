package com.nowcoder.community;

import java.io.IOException;

/**
 * @Author: XTY~
 * @CreateTime: 20/3/2023 下午12:39
 * @Description:
 */

public class WKTest {
    public static void main(String[] args) {
        String cmd = "D:\\InstallProgram\\wkhtmltopdf\\bin/wkhtmltoimage --quality 75 https://blog.csdn.net/woyaottk/article/details/123649107?ops_request_misc=%257B%2522request%255Fid%2522%253A%2522167937346116800180676146%2522%252C%2522scm%2522%253A%252220140713.130102334.pc%255Fall.%2522%257D&request_id=167937346116800180676146&biz_id=0&utm_medium=distribute.pc_search_result.none-task-blog-2~all~first_rank_ecpm_v1~hot_rank-2-123649107-null-null.142^v74^control_1,201^v4^add_ask,239^v2^insert_chatgpt&utm_term=%E4%BB%BF%E7%89%9B%E5%AE%A2%E8%AE%BA%E5%9D%9B%20%E6%80%BB%E7%BB%93&spm=1018.2226.3001.4187 d:/wk-picturetest/note.png";
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
