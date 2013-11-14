package ru.intertrust.cm.core.dao.impl;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import ru.intertrust.cm.core.dao.api.ExtensionService;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPointHandler;
import ru.intertrust.cm.core.dao.impl.extension.ExtentionInvocationHandler;
import ru.intertrust.cm.core.model.ExtensionPointException;

/**
 * Имплементация сервиса точек расширения.
 * 
 * @author larin
 * 
 */
public class ExtensionServiceImpl implements ExtensionService, ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(ExtensionServiceImpl.class);
    /**
     * Реестр точек расширения
     */
    private static Hashtable<Class<? extends ExtentionInvocationHandler>, Hashtable<String, List<ExtensionPointHandler>>> reestr;
    /**
     * Базовый пакет, в котором ищутся классы точек расширения
     */
    private String basePackage;
    /**
     * Спринг контекст
     */
    private ApplicationContext applicationContext;

    /**
     * Флаг инициализации
     */
    private boolean isInit = false;

    /**
     * Получение точки расширения по имени интерфейса точки расширения и
     * фильтру. Фильтр может быть null в этом случае возвратятся все точки
     * расширения для переданного интерфейса
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getExtentionPoint(
            Class<T> extentionPointInterface,
            String filter) {

        if (!isInit) {
            init();
        }

        ExtentionInvocationHandler handler = new ExtentionInvocationHandler(
                this, filter);
        T proxy = (T) Proxy.newProxyInstance(extentionPointInterface.getClassLoader(),
                new Class<?>[] { extentionPointInterface }, handler);
        return proxy;
    }

    /**
     * Инициализация точек расширения, путем сканирования всех классов,
     * аннотированых ExtensionPoint аннотацией и заполнение реестра
     */
    @SuppressWarnings("unchecked")
    private void init() {
        try {
            if (!isInit) {
                reestr = new Hashtable<Class<? extends ExtentionInvocationHandler>,
                        Hashtable<String, List<ExtensionPointHandler>>>();

                // Сканирование класспаса
                ClassPathScanningCandidateComponentProvider scanner =
                        new ClassPathScanningCandidateComponentProvider(true);
                scanner.addIncludeFilter(new AnnotationTypeFilter(ExtensionPoint.class));
                // Цикл по найденным классам
                for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
                    String className = bd.getBeanClassName();
                    // Получение найденного класса
                    Class<?> extentionPointClass = Class.forName(className);
                    // Получение анотации ExtensionPoint
                    ExtensionPoint annatation = (ExtensionPoint) extentionPointClass
                            .getAnnotation(ExtensionPoint.class);

                    // Проверка наличия анотации в классе
                    if (annatation != null) {

                        // Получение интерфейса, который имплементит точка
                        // расширения, интерфейс должен быть наследником
                        // ExtensionPointHandler
                        Class<?>[] interfaces = extentionPointClass.getInterfaces();
                        //List<Class> interfaceClasses = new ArrayList<Class>();
                        ExtensionPointHandler extentionPoint = null;
                        for (Class<?> interfaceClass : interfaces) {
                            if (ExtensionPointHandler.class.isAssignableFrom(interfaceClass)) {

                                // Получаем фильтр из аннотации
                                String filter = annatation.filter();

                                if (extentionPoint == null) {
                                    // Проверяем есть ли спринг бин этого
                                    // класса, если есть то используем его
                                    String[] beanNames = applicationContext.getBeanNamesForType(extentionPointClass);
                                    if (beanNames.length > 0) {
                                        extentionPoint = (ExtensionPointHandler) applicationContext
                                                .getBean(extentionPointClass);
                                    } else {
                                        // Если такого бина нет то создаем
                                        // экземпляр класса
                                        // Создаем экземпляр точки расширения
                                        // Добавляем
                                        // класс как спринговый бин с поддержкой
                                        // autowire
                                        extentionPoint = (ExtensionPointHandler) applicationContext
                                                .getAutowireCapableBeanFactory().createBean(extentionPointClass,
                                                        AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
                                    }
                                }

                                // Сохраняем точку расширения в реестр
                                Hashtable<String, List<ExtensionPointHandler>> oneTypeExtensions = reestr
                                        .get(interfaceClass);
                                if (oneTypeExtensions == null) {
                                    oneTypeExtensions = new Hashtable<String, List<ExtensionPointHandler>>();
                                    reestr.put((Class<? extends ExtentionInvocationHandler>) interfaceClass,
                                            oneTypeExtensions);
                                }
                                List<ExtensionPointHandler> filteredExtension = oneTypeExtensions
                                        .get(filter);
                                if (filteredExtension == null) {
                                    filteredExtension = new ArrayList<ExtensionPointHandler>();
                                    oneTypeExtensions.put(filter, filteredExtension);
                                }
                                filteredExtension.add(extentionPoint);
                                logger.info("Register extensionPoint " + interfaceClass.getName()
                                        + "(" + filter + ") = " + extentionPoint.getClass().getName());
                            }
                        }
                    }
                }
                isInit = true;
            }
        } catch (Exception ex) {
            throw new ExtensionPointException(
                    "Error on init extension point classes", ex);
        }
    }

    /**
     * Получает список точек расширения определенного интерфейса
     */
    public List<ExtensionPointHandler> getExtentionPointList(
            Class<? extends ExtensionPointHandler> extentionPointInterface,
            String filter) {
        List<ExtensionPointHandler> result = new ArrayList<ExtensionPointHandler>();

        // Получаем все хандлеры точек расширения по интерфейсу
        Map<String, List<ExtensionPointHandler>> oneTypeExtensions = reestr
                .get(extentionPointInterface);

        // Проверка на наличие точек расширения определенного интерфейса
        if (oneTypeExtensions != null) {
            // Проверка на переданный фильтр точек расширения, в пределах одного
            // интерфейса
            if (filter != null) {
                // Получение точек расширения по фильтру
                List<ExtensionPointHandler> filteredExtension = oneTypeExtensions
                        .get(filter);
                // Если точки расширения по фильтру найдены то добавляем их в
                // результат
                if (filteredExtension != null && filteredExtension.size() > 0) {
                    result.addAll(filteredExtension);
                }

                // Проверяем наличие обработчиков точек расширения, в которых не
                // указали фильтр, если такие есть то добавляем их при любом
                // значение переданного фильтра
                if (oneTypeExtensions.containsKey("")) {
                    result.addAll(oneTypeExtensions.get(""));
                }
            } else {
                // Если фильтр не указан то получаем обработчики
                // зарегистрированные со всеми значениями фильтрв и добавляем их
                // в результат
                for (List<ExtensionPointHandler> extentionPoints : oneTypeExtensions
                        .values()) {
                    result.addAll(extentionPoints);
                }

            }
        }
        return result;
    }

    /**
     * Установка базовового пакета, в котором производится поиск точек
     * расширения
     * 
     * @param basePackage
     */
    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    /**
     * Установка spring контекста
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        init();
    }
}
