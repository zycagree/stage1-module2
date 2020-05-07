package com.zyc.scanner;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author zhangyongchao
 * @date 2020/5/4 14:40
 * @description 包扫描器
 */
public class Scanner {

    public static void doScan(Class<?> clazz, String packageName, Set<String> classNameSet) {
        // 把所有的.替换成/
        URL url = clazz.getClassLoader().getResource(packageName.replaceAll("\\.", "/"));
        // 是否循环迭代
        if (StringUtils.countMatches(url.getFile(), ".jar") > 0) {
            boolean recursive = true;
            JarFile jar;
            // 获取jar
            try {
                jar = ((JarURLConnection) url.openConnection()).getJarFile();
                // 从此jar包 得到一个枚举类
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    // 如果是以/开头的
                    if (name.charAt(0) == '/') {
                        // 获取后面的字符串
                        name = name.substring(1);
                    }
                    // 如果前半部分和定义的包名相同
                    if (name.startsWith(packageName.replaceAll("\\.", "/"))) {
                        // 如果以"/"结尾 是一个包
                        boolean endsWith = name.endsWith("/");
                        // 如果可以迭代下去 并且是一个包
                        if (endsWith || recursive) {
                            // 如果是一个.class文件 而且不是目录也不是内部类
                            if (name.endsWith(".class") && !entry.isDirectory() && !name.contains("$")) {
                                // 去掉后面的".class" 获取真正的类名
                                String className = name.replace("/", ".").replace(".class", "");
                                try {
                                    // 添加到classes
                                    classNameSet.add(Class.forName(className).getName());
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File dir = null;
        try {
            dir = new File(new URI(url.toString().replaceAll(" ", "%20")).getSchemeSpecificPart());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                // 递归读取包
                doScan(clazz, packageName + "." + file.getName(), classNameSet);
            } else {
                String className = packageName + "." + file.getName().replace(".class", "");
                if(!className.contains("$")) {
                    classNameSet.add(className);
                }
            }
        }
    }

    public static void doScanDir(File basePath, File dir, Set<String> classNameSet) {
        if(dir != null && dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            for(File file : files) {
                if(file.isDirectory()) {
                    doScanDir(basePath, file, classNameSet);
                } else {
                    String className = file.getAbsolutePath()
                            .replace(basePath.getAbsolutePath(), "")
                            .replaceAll("\\\\", ".")
                            .replace("/", ".")
                            .replace(".class", "");
                    if(className.startsWith(".")) {
                        className = className.substring(1);
                    }
                    if(!className.contains("$")) {
                        classNameSet.add(className);
                    }
                }
            }
        }
    }


}
