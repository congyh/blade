package com.blade.ioc;

/**
 * Bean Injector interface
 *
 * @author <a href="mailto:biezhi.me@gmail.com" target="_blank">biezhi</a>
 * @since 1.5
 */
public interface Injector {

    /**
     * Injection bean
     *
     * 注意: 这里的bean是inject的受体
     *
     * @param bean bean instance
     */
    void injection(Object bean);

}