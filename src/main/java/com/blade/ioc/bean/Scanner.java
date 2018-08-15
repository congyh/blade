package com.blade.ioc.bean;

import lombok.Builder;
import lombok.Data;

import java.lang.annotation.Annotation;

/**
 * @author biezhi
 * @date 2017/10/19
 */
@Data
@Builder // Note: Builder方便的实现了builder模式.
public class Scanner {

    private String                      packageName;
    private boolean                     recursive;
    private Class<?>                    parent;
    private Class<? extends Annotation> annotation;
}
