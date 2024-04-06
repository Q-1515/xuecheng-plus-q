package com.xuecheng;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * packageName com.xuecheng
 *
 * @author Q
 * @version JDK 8
 * @className demo
 * @date 2024/4/5 15:31
 * @description TODO
 */
public class demo {
        public static void main(String[] args) {
            // 创建 BCryptPasswordEncoder 实例
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

            // 加密密码 "123456"
            String encodedPassword = encoder.encode("123456");

            // 输出加密后的密码
            System.out.println("Encoded password: " + encodedPassword);
        }
}
