package com.zyc.factory;

import com.zyc.annotation.*;
import com.zyc.exception.NoBeanDefinitionException;
import com.zyc.exception.NoUniqueBeanDefinitionException;
import com.zyc.scanner.Scanner ;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import javax.annotation.Resource;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author zhangyongchao
 * @date 2020/5/4 12:36
 * @description
 */
@EnableAnnotationDrive
@AnnotationBeanComponentScan(basePackages = {"com.zyc"})
public class BeanFactory {

    /**
     * 任务一：读取解析xml，通过反射技术实例化对象并且存储待用（map集合）
     * 任务二：对外提供获取实例对象的接口（根据id获取）
     */

    private static Map<String,Object> map = new HashMap<>();  // 存储对象

    private static Map<Class<?>, List<String>> allBeanNamesByType = new HashMap<>();

    private static Set<String> classNameSet = new HashSet<String>();


    static {
        // 将classpath下xml中配置的bean加入容器
//        instantiateClassPathXmlBeans();

        // 实例化注解加入容器的bean
        instantiateAnnotationBeans();

    }

    /**
     * 实例化注解配置的bean到容器
     */
    private static void instantiateAnnotationBeans() {
        EnableAnnotationDrive enableAnnotationDrive = BeanFactory.class.getAnnotation(EnableAnnotationDrive.class);
        if (enableAnnotationDrive != null){
            // 1、扫描指定base包及其子包下所有的类
            doPackageScan();
            // 2、将被注解@Service、@Repository和@Component标注的类加入到容器
            creatBean();
            // 3、为创建并加入容器的bean注入属性
            populateBeans();
            // 4、处理Transactional，创建代理对象
            processTransactional();
        }
    }

    private static void processTransactional() {
        map.forEach((beanName, bean) -> {
            Class<?> beanClass = bean.getClass();
            boolean createProxy =  shouldCreateProxy(beanClass);
            if (createProxy) {
                ProxyFactory proxyFactory = (ProxyFactory) BeanFactory.getBean("proxyFactory");
                Object proxy;
                // 如果实现了接口创建JDK代理，否则创建Cglib代理
                if (hasInterface(beanClass)) {
                    proxy = proxyFactory.getJdkProxy(bean);
                } else {
                    proxy = proxyFactory.getCglibProxy(bean);
                }
                map.put(beanName, proxy);
            }

        });
    }

    private static boolean hasInterface(Class<?> beanClass) {
        Class<?>[] interfaces = beanClass.getInterfaces();
        return interfaces != null && interfaces.length > 0;
    }

    private static boolean shouldCreateProxy(Class<?> beanClass) {
        Transactional transactional = beanClass.getAnnotation(Transactional.class);
        if (transactional != null) {
            return true;
        }
        Method[] methods = beanClass.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.isAnnotationPresent(Transactional.class)) {
                return true;
            }
        }
        return false;
    }

    private static void populateBeans() {
        map.forEach((beanName, bean) -> {
            Field[] fields = bean.getClass().getDeclaredFields();
            Arrays.stream(fields).forEach(field -> {
                // 如果字段存在@Autowired注解，则为该字段注入
                if (field.isAnnotationPresent(Autowired.class) || field.isAnnotationPresent(Resource.class)){
                    // 获取beanName
                    String beanId = getBeanName(bean, field);
                    //  注入
                    injectReference(bean, field, beanId);
                }
            });
        });
    }

    private static void injectReference(Object bean, Field field, String beanId) {
        try {
            if (beanId != null) {
                field.setAccessible(true);
                field.set(bean, map.get(beanId));
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static String getBeanName(Object bean, Field field) {
        String beanId = null;
        if (field.isAnnotationPresent(Autowired.class) && field.isAnnotationPresent(Resource.class)){
            throw new IllegalStateException("不能同时使用@Autowired和@Resource注入引用!");
        }

        if (field.getAnnotation(Resource.class) != null) {
            beanId = field.getAnnotation(Resource.class).name();
            return beanId;
        }

        if (field.getAnnotation(Autowired.class) != null) {
            if (Modifier.isStatic(field.getModifiers())){
                throw new IllegalStateException("@Autowired注解暂不支持为static变量注入引用!");
            }
            // 如果使用Qualifier指定了beanName,则根据@Qualifier指定的beanName注入
            if (field.isAnnotationPresent(Qualifier.class)){
                Qualifier qualifier = field.getAnnotation(Qualifier.class);
                beanId = qualifier.value();
                return beanId;
            }

            // 未使用Qualifier指定beanName
            Class<?> fieldType = field.getType();
            // 如果字段类型是接口，则以字段名作为beanName
            if (fieldType.isInterface()){
                beanId = field.getName();
                return beanId;
            }
            // 如果字段类型是类，则根据是否存在@Qualifier注解为字段注入
            // 如果存在@Qualifier注解，则根据@Qualifier指定的beanName注入
            if (isClass(fieldType)){
                // 不存在@Qualifier
                List<String> beanNames = allBeanNamesByType.get(fieldType);
                // 容器中存在多个该类型的bean，需要指定beanName才能为其注入
                if (beanNames != null && beanNames.size() > 1){
                    throw new NoUniqueBeanDefinitionException(String.format("容器中存在多个[%s]类型的bean，需要通过@Qualifier指定beanName为Bean[%s]的field[%s]注入引用!",
                            fieldType.getName(),bean.getClass().getName(), field.getName()));
                }
                // 容器中不存在该类型的bean
                if (beanNames != null && beanNames.size() == 0){
                    throw new NoBeanDefinitionException(String.format("容器中不存在[%s]类型的bean，无法为Bean[%s]的field[%s]注入引用!",
                            fieldType.getName(), bean.getClass().getName(), field.getName()));
                }
                beanId = beanNames.get(0);

            }

        }
        return beanId;
    }

    private static boolean isClass(Class<?> fieldType) {
        String nameWithType = fieldType.toString().trim();
        return nameWithType.startsWith("class");
    }

    /**
     * 扫描basePackages指定的包及其子包，得到这些包下所有的类(不包括内部类)
     */
    private static void doPackageScan() {
        AnnotationBeanComponentScan componentScan = BeanFactory.class.getAnnotation(AnnotationBeanComponentScan.class);
        if (componentScan != null && componentScan.basePackages() != null){
            String[] basePackages = componentScan.basePackages();
            Arrays.stream(basePackages)
                    .forEach(packageName -> Scanner.doScan(BeanFactory.class, packageName, classNameSet));
        }
    }

    /**
     * 创建bean实例
     */
    private static void creatBean() {
        classNameSet.forEach(className -> {
            try {
                Class<?> clazz = Class.forName(className);
                Annotation[] annotations = clazz.getAnnotations();
                for (Annotation annotation : annotations){
                    if (annotation.annotationType().equals(Service.class)
                            || annotation.annotationType().equals(Repository.class)
                            || annotation.annotationType().equals(Component.class)){
                        try {
                            // 根据注解是否指定value属性值得到beanName
                            // 获取注解Service、Repository或Component指定的beanName
                            Method annotationValueMethod = annotation.getClass().getDeclaredMethod("value");
                            String beanName = (String) annotationValueMethod.invoke(annotation);
                            if (beanName == null || beanName.equals("")){
                                beanName = uncapitalize(clazz.getSimpleName());
                            }

                            // 创建并保存对象
                            Object instance = clazz.newInstance();
                            map.put(beanName, instance);

                            // 将beanName按照类型存储起来
                            List<String> beanNamesByType = allBeanNamesByType.get(clazz);
                            if (beanNamesByType == null){
                                List<String> names = new ArrayList<>();
                                names.add(beanName);
                                allBeanNamesByType.put(clazz, names);
                            } else {
                                beanNamesByType.add(beanName);
                            }
                        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        });
    }

    public static String uncapitalize(String str) {
        return changeFirstCharacterCase(str, false);
    }

    private static String changeFirstCharacterCase(String str, boolean capitalize) {
        if (!hasLength(str)) {
            return str;
        }

        char baseChar = str.charAt(0);
        char updatedChar;
        if (capitalize) {
            updatedChar = Character.toUpperCase(baseChar);
        }
        else {
            updatedChar = Character.toLowerCase(baseChar);
        }
        if (baseChar == updatedChar) {
            return str;
        }

        char[] chars = str.toCharArray();
        chars[0] = updatedChar;
        return new String(chars, 0, chars.length);
    }

    public static boolean hasLength(String str) {
        return (str != null && !str.isEmpty());
    }

    private static void instantiateClassPathXmlBeans() {
        // 任务一：读取解析xml，通过反射技术实例化对象并且存储待用（map集合）
        // 加载xml
        InputStream resourceAsStream = BeanFactory.class.getClassLoader().getResourceAsStream("beans.xml");
        // 解析xml
        SAXReader saxReader = new SAXReader();
        try {
            Document document = saxReader.read(resourceAsStream);
            Element rootElement = document.getRootElement();
            List<Element> beanList = rootElement.selectNodes("//bean");
            for (int i = 0; i < beanList.size(); i++) {
                Element element =  beanList.get(i);
                // 处理每个bean元素，获取到该元素的id 和 class 属性
                String id = element.attributeValue("id");        // accountDao
                String clazz = element.attributeValue("class");  // com.lagou.edu.dao.impl.JdbcAccountDaoImpl
                // 通过反射技术实例化对象
                Class<?> aClass = Class.forName(clazz);
                Object o = aClass.newInstance();  // 实例化之后的对象

                // 存储到map中待用
                map.put(id,o);

            }

            // 实例化完成之后维护对象的依赖关系，检查哪些对象需要传值进入，根据它的配置，我们传入相应的值
            // 有property子元素的bean就有传值需求
            List<Element> propertyList = rootElement.selectNodes("//property");
            // 解析property，获取父元素
            for (int i = 0; i < propertyList.size(); i++) {
                Element element =  propertyList.get(i);   //<property name="AccountDao" ref="accountDao"></property>
                String name = element.attributeValue("name");
                String ref = element.attributeValue("ref");

                // 找到当前需要被处理依赖关系的bean
                Element parent = element.getParent();

                // 调用父元素对象的反射功能
                String parentId = parent.attributeValue("id");
                Object parentObject = map.get(parentId);
                // 遍历父对象中的所有方法，找到"set" + name
                Method[] methods = parentObject.getClass().getMethods();
                for (int j = 0; j < methods.length; j++) {
                    Method method = methods[j];
                    if(method.getName().equalsIgnoreCase("set" + name)) {  // 该方法就是 setAccountDao(AccountDao accountDao)
                        method.invoke(parentObject,map.get(ref));
                    }
                }

                // 把处理之后的parentObject重新放到map中
                map.put(parentId,parentObject);

            }


        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    // 任务二：对外提供获取实例对象的接口（根据id获取）
    public static  Object getBean(String id) {
        return map.get(id);
    }

}
