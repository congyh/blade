package com.blade.ioc;

import com.blade.ioc.bean.BeanDefine;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * The default IOC container implementation
 *
 * Note: 接口中无需定义数据结构, 具体的数据结构是在实现类中定义的, 例如这里的pool
 *
 * @author <a href="mailto:biezhi.me@gmail.com" target="_blank">biezhi</a>
 * @since 1.5
 */
// Note: Slf4j这个注解会生成一个类同名的slf4j的log对象
@Slf4j
public class SimpleIoc implements Ioc {

    // 使用简单的HashMap作为缓存池
    private final Map<String, BeanDefine> pool = new HashMap<>(32);

    /**
     * Add user-defined objects
     */
    @Override
    public void addBean(Object bean) {
        addBean(bean.getClass().getName(), bean);
    }

    /**
     * Add user-defined objects
     */
    @Override
    public void addBean(String name, Object bean) {
        BeanDefine beanDefine = new BeanDefine(bean);
        addBean(name, beanDefine);
        // add interface
        Class<?>[] interfaces = beanDefine.getType().getInterfaces();
        if (interfaces.length > 0) {
            // Note: 对所有bean实现的接口, 也会根据接口名注册一个bean
            // 便于接口inject时能够inject进实现类.
            for (Class<?> interfaceClazz : interfaces) {
                this.addBean(interfaceClazz.getName(), beanDefine);
            }
        }
    }

    /**
     * Update BeanDefine
     */
    @Override
    public void setBean(Class<?> type, Object proxyBean) {
        BeanDefine beanDefine = pool.get(type.getName());
        if (beanDefine != null) {
            beanDefine.setBean(proxyBean);
        } else {
            beanDefine = new BeanDefine(proxyBean, type);
        }
        pool.put(type.getName(), beanDefine);
    }

    /**
     * Register @Bean marked objects
     */
    @Override
    public <T> T addBean(Class<T> type) {
        Object bean = addBean(type, true);
        return type.cast(bean);
    }

    @Override
    public <T> T getBean(Class<T> type) {
        Object bean = this.getBean(type.getName());
        try {
            // Note: Class<T>类的cast方法可以显式将对象转换为指定类型
            // 注意: 这里不要用Class<?>这样的参数, 因为这样会失去具体的类型信息.
            return type.cast(bean);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Object getBean(String name) {
        BeanDefine beanDefine = pool.get(name);
        if (beanDefine == null) {
            return null;
        }
        return beanDefine.getBean();
    }

    @Override
    public List<BeanDefine> getBeanDefines() {
        return new ArrayList<>(pool.values());
    }

    @Override
    public BeanDefine getBeanDefine(Class<?> type) {
        return this.getBeanDefine(type, true);
    }

    @Override
    public List<Object> getBeans() {
        Set<String>  beanNames = this.getBeanNames();
        List<Object> beans     = new ArrayList<>(beanNames.size());
        for (String beanName : beanNames) {
            Object bean = this.getBean(beanName);
            if (null != bean) {
                beans.add(bean);
            }
        }
        return beans;
    }

    @Override
    public Set<String> getBeanNames() {
        return pool.keySet();
    }

    @Override
    public void remove(String beanName) {
        pool.remove(beanName);
    }

    @Override
    public void remove(Class<?> type) {
        pool.remove(type.getSimpleName());
    }

    @Override
    public void clearAll() {
        pool.clear();
    }

    /**
     * Add user-defined objects
     */
    private void addBean(String name, BeanDefine beanDefine) {
        if (pool.put(name, beanDefine) != null) {
            log.warn("Duplicated Bean: {}", name);
        }
    }

    /**
     * Register @Bean marked objects
     */
    private Object addBean(Class<?> type, boolean singleton) {
        return addBean(type.getName(), type, singleton);
    }

    /**
     * Register @Bean marked objects
     */
    private Object addBean(String name, Class<?> beanClass, boolean singleton) {
        BeanDefine beanDefine = this.getBeanDefine(beanClass, singleton);

        if (pool.put(name, beanDefine) != null) {
            log.warn("Duplicated Bean: {}", name);
        }

        // add interface
        Class<?>[] interfaces = beanClass.getInterfaces();
        if (interfaces.length > 0) {
            for (Class<?> interfaceClazz : interfaces) {
                if (null != this.getBean(interfaceClazz)) {
                    break;
                }
                this.addBean(interfaceClazz.getName(), beanDefine);
            }
        }

        return beanDefine.getBean();
    }

    private BeanDefine getBeanDefine(Class<?> beanClass, boolean singleton) {
        try {
            Object object = beanClass.newInstance();
            return new BeanDefine(object, beanClass, singleton);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

}