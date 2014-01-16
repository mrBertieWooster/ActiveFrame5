package ru.intertrust.cm.core.business.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Synchronization;
import javax.transaction.TransactionSynchronizationRegistry;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.api.ExtensionService;
import ru.intertrust.cm.core.dao.api.extension.OnLoadConfigurationExtensionHandler;
import ru.intertrust.cm.core.dao.exception.DaoException;

/**
 * Класс, предназначенный для загрузки конфигурации доменных объектов
 * 
 * @author vmatsukevich Date: 5/6/13 Time: 9:36 AM
 */
public class ConfigurationLoader implements ApplicationContextAware {

    @Autowired
    private ConfigurationControlService configurationControlService;
    
    private ApplicationContext context;

    @Resource
    private TransactionSynchronizationRegistry txReg;    
    
    private boolean configurationLoaded;

    /*
     * @Autowired private ExtensionService extensionService;
     */

    public ConfigurationLoader() {
    }

    /**
     * Устанавливает {@link #configurationControlService}
     * 
     * @param configurationControlService
     *            сервис для работы с конфигурацией доменных объектов
     */
    public void setConfigurationControlService(
            ConfigurationControlService configurationControlService) {
        this.configurationControlService = configurationControlService;
    }

    /**
     * Загружает конфигурацию доменных объектов, валидирует и создает
     * соответствующие сущности в базе. Добавляет запись администратора
     * (admin/admin) в таблицу authentication_info.
     * 
     * @throws Exception
     */
    public void load() throws Exception {
        configurationControlService.loadConfiguration();

        // Вызов точки расширения
        if (context != null) {
            ExtensionService extensionService = context
                    .getBean(ExtensionService.class);
            OnLoadConfigurationExtensionHandler extension = extensionService.getExtentionPoint(
                    OnLoadConfigurationExtensionHandler.class, null);
            extension.onLoad();
        }
        
        //Установка флага загруженности конфигурации
        setLoadedFlag();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.context = applicationContext;

    }
    
    /**
     * Метод возвращает флаг загруженности конфигурации
     * @return
     */
    public boolean isConfigurationLoaded(){
        return configurationLoaded;
    }

    /**
     * Установка флага доступности конфигурации. 
     * Флаг устанавливается не сразу, а только после окончания транзакции, иначе созданные таблицы не будут доступны другим потокам
     */
    private void setLoadedFlag() {
        //не обрабатываем вне транзакции
        if (getTxReg().getTransactionKey() == null) {
            return;
        }
        SetConfigurationLoaded setConfigurationLoaded =
                (SetConfigurationLoaded) getTxReg().getResource(SetConfigurationLoaded.class);
        if (setConfigurationLoaded == null) {
            setConfigurationLoaded = new SetConfigurationLoaded();
            getTxReg().putResource(SetConfigurationLoaded.class, setConfigurationLoaded);
            getTxReg().registerInterposedSynchronization(setConfigurationLoaded);
        }
    }

    /**
     * Получение контекта транзакции
     * @return
     */
    private TransactionSynchronizationRegistry getTxReg() {
        if (txReg == null) {
            try {
                txReg =
                        (TransactionSynchronizationRegistry) new InitialContext()
                                .lookup("java:comp/TransactionSynchronizationRegistry");
            } catch (NamingException e) {
                throw new DaoException(e);
            }
        }
        return txReg;
    }

    /**
     * Класс используется для установки флага загруженности конфигурации после окончания транзакции
     * @author larin
     *
     */
    private class SetConfigurationLoaded implements Synchronization {

        @Override
        public void afterCompletion(int arg0) {
            configurationLoaded = true;
            
        }

        @Override
        public void beforeCompletion() {
        }
    }
    
    
}
