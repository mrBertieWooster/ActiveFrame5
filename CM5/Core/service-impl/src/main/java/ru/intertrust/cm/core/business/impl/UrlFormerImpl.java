package ru.intertrust.cm.core.business.impl;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.env.Environment;

import ru.intertrust.cm.core.business.api.UrlFormer;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.UrlConfig;
import ru.intertrust.cm.core.model.NotificationException;
import ru.intertrust.cm.core.tools.DomainObjectAccessor;
import ru.intertrust.cm.core.tools.Session;

public class UrlFormerImpl implements UrlFormer{

    private static final Logger logger = LoggerFactory.getLogger(UrlFormerImpl.class);

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private Environment environment;

    @Override
    public URL getUrl(String clientName, Id addressee, Id objectId) {

        UrlConfig urlConfig = configurationExplorer.getConfig(UrlConfig.class, clientName);
        if (urlConfig == null) {
            throw new NotificationException("There is no URL configuration for client name:" + clientName);
        }

        String urlTemplate = urlConfig.getUrlTemplate();

        Map<String, Object> model = new HashMap<>();
        model.put("session", new Session());
        injectBeans(model);
        model.put("domainObject", new DomainObjectAccessor(objectId));
        model.put("addressee", new DomainObjectAccessor(addressee));

        String url = FreeMarkerHelper.format(urlTemplate, model);

        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            logger.error(e.getMessage());
            throw new NotificationException(e);
        }
    }

    private void injectBeans(Map<String, Object> model){
        String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            if (applicationContext.isSingleton(beanDefinitionName) && 
                    !((AbstractApplicationContext)applicationContext).getBeanFactory().getBeanDefinition(beanDefinitionName).isAbstract()){
                Object bean = applicationContext.getBean(beanDefinitionName);
                model.put(beanDefinitionName, bean);
            }
        }
        //Добавляем переменные из server.properties
        model.put("environment", environment);
    }
}
