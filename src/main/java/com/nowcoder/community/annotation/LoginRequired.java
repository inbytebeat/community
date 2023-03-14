package com.nowcoder.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 表示该注解有效范围
@Target(ElementType.METHOD)
// 表示该注解有效时机 程序运行时有效
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {

}
