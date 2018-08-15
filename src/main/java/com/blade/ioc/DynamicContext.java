package com.blade.ioc;

import com.blade.ioc.bean.ClassInfo;
import com.blade.ioc.bean.Scanner;
import com.blade.ioc.reader.ClassPathClassReader;
import com.blade.ioc.reader.JarReaderImpl;
import com.blade.kit.StringKit;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.Enumeration;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Get ClassReader by JAR or folder
 *
 * 本类的作用是, 根据所处的环境是JAR还是folder来决定用哪种ClassReader(ClassPathClassReader或者JarReader)
 *
 * TODO 本类的实现还没有理清楚.
 *
 * @author <a href="mailto:biezhi.me@gmail.com" target="_blank">biezhi</a>
 * @since 1.6.6
 */
@Slf4j
@NoArgsConstructor
public final class DynamicContext {

    // ClassReader对象中含有Set<ClassInfo>, 也就是所有的Class定义信息
    private static final ClassReader classpathReader = new ClassPathClassReader();
    private static final ClassReader jarReader       = new JarReaderImpl();
    private static final String      SUFFIX_JAR      = ".jar";

    private static boolean isJarContext = false;

    public static void init(Class<?> clazz) {
        String rs = clazz.getResource("").toString();
        if (rs.contains(SUFFIX_JAR)) {
            isJarContext = true;
        }
    }

    public static Stream<ClassInfo> recursionFindClasses(String packageName) {
        Scanner        scanner    = Scanner.builder().packageName(packageName).recursive(true).build();
        Set<ClassInfo> classInfos = getClassReader(packageName).readClasses(scanner);
        return classInfos.stream();
    }

    public static ClassReader getClassReader(String packageName) {
        if (isJarPackage(packageName)) {
            return jarReader;
        }
        return classpathReader;
    }

    public static boolean isJarPackage(String packageName) {
        if (StringKit.isBlank(packageName)) {
            return false;
        }
        try {
            packageName = packageName.replace(".", "/");
            Enumeration<URL> dirs = DynamicContext.class.getClassLoader().getResources(packageName);
            if (dirs.hasMoreElements()) {
                String url = dirs.nextElement().toString();
                return url.indexOf(".jar!") != -1 || url.indexOf(".zip!") != -1;
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return false;
    }

    public static boolean isJarContext() {
        return isJarContext;
    }

}