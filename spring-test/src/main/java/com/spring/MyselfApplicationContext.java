package com.spring;


import java.beans.Introspector;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class MyselfApplicationContext {

    private Class configClass;

    private Map<String,BeanDefinition> beanDefinitionMap = new HashMap<>();
    //单例池
    private Map<String,Object> singletonObjects = new HashMap<>();

    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    public MyselfApplicationContext(Class configClass) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        this.configClass = configClass;
        //扫描
        scan(configClass);

        //创建单例bean
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();

            if (beanDefinition.getScope().equals("singleton")){
                Object bean = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName,bean);
            }
        }

    }

    private Object createBean(String beanName,BeanDefinition beanDefinition){

        Class clazz = beanDefinition.getType();
        Object instance = null;
        try {
            instance = clazz.getConstructor().newInstance();


            //依赖注入
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)){
                    field.setAccessible(true);
                    field.set(instance,getBean(field.getName()));
                }
            }
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                beanPostProcessor.postProcessBeforeInitialization(instance,beanName);
            }
            //初始化
            if (instance instanceof InitializingBean){
                ((InitializingBean) instance).afterPropertiesSet();
            }

            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                beanPostProcessor.postProcessAfterInitialization(instance,beanName);
            }

        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        return instance;
    }

    public Object getBean(String beanName){
        if (!beanDefinitionMap.containsKey(beanName)){
            throw new NullPointerException();
        }else {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (beanDefinition.getScope().equals("singleton")){
                //单例 只有一个bean对象，从单例池中拿
                Object singletonBean = singletonObjects.get(beanName);
                if (singletonBean==null){
                    createBean(beanName,beanDefinition);
                }
                return singletonBean;
            }else {
                // 原型bean 每次getBean都是一个新的bean对象
                Object prototypeBean = createBean(beanName, beanDefinition);
                return prototypeBean;
            }
        }
    }


    /**
     * 扫描方法
     * @param configClass
     * @throws ClassNotFoundException
     */
    private void scan(Class configClass) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (configClass.isAnnotationPresent(ComponentScan.class)){
            ComponentScan componentScanAnnotation = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
            String path = componentScanAnnotation.value();
            path = path.replaceAll("\\.","/");
            System.out.println(path);

            //从target中找到这个路径，获得所有的文件

            ClassLoader classLoader = MyselfApplicationContext.class.getClassLoader();
            URL resource = classLoader.getResource(path);

            File file = new File(resource.getFile());
            if (file.isDirectory()){

                for (File f : file.listFiles()) {
                    String absolutePath = f.getAbsolutePath();
                    absolutePath = absolutePath.substring(absolutePath.indexOf("com"),absolutePath.indexOf(".class"));
                    absolutePath = absolutePath.replace("/",".");
                    Class<?> aClass = classLoader.loadClass(absolutePath);

                    //获得的文件判断是否有@Component注解
                    if (aClass.isAnnotationPresent(Component.class)){

                        if (BeanPostProcessor.class.isAssignableFrom(aClass)) {
                            BeanPostProcessor beanPostProcessor = (BeanPostProcessor) aClass.getConstructor().newInstance();
                            beanPostProcessorList.add(beanPostProcessor);
                        }

                        Component component = aClass.getAnnotation(Component.class);

                        //创建bean的定义
                        BeanDefinition beanDefinition = new BeanDefinition();
                        beanDefinition.setType(aClass);

                        String beanName = component.value();
                        if ("".equals(beanName)){
                            //如果未设置名称，将类的名称设置为beanName
                            beanName = Introspector.decapitalize(aClass.getSimpleName());
                        }

                        //判断当前bean是否为单例
                        if (aClass.isAnnotationPresent(Scope.class)){
                            Scope scope = aClass.getAnnotation(Scope.class);
                            beanDefinition.setScope(scope.value());
                            if (scope.value().equals("prototype")){
                                //原型


                            }else {
                                //单例

                            }
                        }else {
                            //单例
                            beanDefinition.setScope("singleton");
                        }
                        beanDefinitionMap.put(beanName,beanDefinition);

                    }
                }

            }
        }
    }

}
