package com.blade.ioc.bean;

import com.blade.Environment;
import com.blade.ioc.Injector;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.*;
/**
 * Config annotation can be injected
 *
 * Note: 之所以要有FieldInjector和ValueInjector, 是因为fieldInjector负责的是实例bean的注入.
 * 而valueInjector能够方便的从配置文件中注入一些简单属性.
 *
 * @author <a href="mailto:chenchen_839@126.com" target="_blank">ccqy66</a>
 */
@Slf4j
public class ValueInjector implements Injector {
    private Environment environment;
    private Field target;
    private String key;

    public ValueInjector(Environment environment, Field target, String key) {
        this.environment = environment;
        this.target      = target;
        this.key         = key;
    }

    @Override
    public void injection(Object bean) {
        try {
            if (!key.isEmpty()) {
                Class<?> clazz = target.getType();
                target.setAccessible(true);
                Optional<String> value = environment.get(key);
                if (!value.isPresent()) {
                    log.warn("config is absent,so can't be injected:target is {}",bean.getClass().getName());
                    return;
                }
                if (value.get().isEmpty()) {
                    log.warn("config is empty,so can't be injected:target is {}",bean.getClass().getName());
                    return;
                }
                //target field type is String Note: isAssignableFrom是用来方便判断指定类型是否是参数的超类的.
                if (clazz.isAssignableFrom(String.class)) {
                    // TODO 前面已经做了相关处理了, 为什么还是需要做判断.
                    target.set(bean, value.isPresent()? value.get() : "");
                    return;
                }

                //List and Map support,just support String element
                // 这里是定义在Environment中的两个默认值, 意思分别是list默认的spliter是',', 而map是':'
                String split    = environment.get("value.split",",");
                String mapSplit = environment.get("value.map.split",":");
                if (clazz.isAssignableFrom(List.class)) {
                    target.set(bean,Arrays.asList(value.get().split(split)));
                    return;
                }
                Map<String,String> map = new HashMap(16);
                if (clazz.isAssignableFrom(Map.class)) {
                    // Note: stream的使用, 将定长Array转换成stream, 然后进行一系列操作
                    Arrays.stream(value.get().split(split))
                            .filter(d -> d.indexOf(mapSplit) != -1) // 找到所有包含';'的元素, 确认是map元素
                            .map(d -> d.split(mapSplit)) // 进行切分 key:value -> key, value
                            .forEach(keyValue -> map.put(keyValue[0],keyValue[1]));
                    target.set(bean,map);
                    return;
                }
            }else {
                log.warn("key is empty,so can't be injected:target is {}",bean.getClass().getName());
            }
        } catch (IllegalAccessException e) {
            log.error("inject config error! key is {},bean is {}",key,bean.getClass().getSimpleName(),e);
        }
    }
}
