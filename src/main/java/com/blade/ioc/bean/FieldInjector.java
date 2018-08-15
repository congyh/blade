package com.blade.ioc.bean;

import com.blade.ioc.Injector;
import com.blade.ioc.Ioc;

import java.lang.reflect.Field;

/**
 * Bean Field Injector
 *
 * @author <a href="mailto:biezhi.me@gmail.com" target="_blank">biezhi</a>
 * @since 1.5
 */
public class FieldInjector implements Injector {

    private Ioc   ioc;
    private Field field;

    public FieldInjector(Ioc ioc, Field field) {
        this.ioc = ioc;
        this.field = field;
    }

    /**
     * injection方法的思路:
     * 1. 获取field的类型
     * 2. 根据类型获取能够inject的value bean
     * 3. 通过set方法来将value bean注册进指定bean中
     * @param bean bean instance
     */
    @Override
    public void injection(Object bean) {
        try {
            // Note: Field类是jdk对filed的抽象
            Class<?> fieldType = field.getType();
            Object   value     = ioc.getBean(fieldType);
            if (value == null) {
                throw new IllegalStateException("Can't inject bean: " + fieldType.getName() + " for field: " + field);
            }
            field.setAccessible(true);
            field.set(bean, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}