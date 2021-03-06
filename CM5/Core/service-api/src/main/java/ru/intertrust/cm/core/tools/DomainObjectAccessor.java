package ru.intertrust.cm.core.tools;

import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.*;
import ru.intertrust.cm.core.config.doel.DoelExpression;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.DoelEvaluator;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.util.SpringApplicationContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Класс обертка над сервисом domainObjectDao для более удобной работы с объектм
 * Внутри класса нельзя использовать @Autowired, так как обязательно обьект
 * должен серелиазоватся
 *
 * @author larin
 *
 */
public class DomainObjectAccessor implements Dto {
    /**
	 *
	 */
    private static final long serialVersionUID = -8436920175908035925L;
    private DomainObject domainObject = null;

    /**
     * Конструктор. Инициализирует доменный обьект
     *
     * @param id
     */
    public DomainObjectAccessor(Id id) {
        load(id);
    }

    /**
     * Перезачитывание ДО из хранилища
     */
    public void reload(){
        load(domainObject.getId());
    }
    
    private void load(Id id){
        // TODO как сделается нормальная работа с токенами заменить токен на
        // токен для конкретного пользователя
        AccessToken accessToken = getAccessControlService()
                .createSystemAccessToken("DomainObjectAccessor");
        domainObject = getDomainObjectDao().find(id, accessToken);
    }

    public DomainObjectAccessor(DomainObject domainObject) {
        this.domainObject = domainObject;
    }

    /**
     * Получает поле объекта
     *
     * @param fieldName - имя поля. Может быть составным Negotiator.Employee.Name - вернет значение поля "Name" объекта Employee, ссылка на который хранится в поле Negotiator объекта domainObject
     * @return
     */
    public Object get(String fieldName) {
    	Object result = null;

    	//Если fieldName - простое
    	if (fieldName.indexOf(".")==-1 && fieldName.indexOf("^")==-1){
            Value value = domainObject.getValue(fieldName);
            if (value != null) {
                result = value.get();
            }
        //Если fieldName - DOEL (пример: Negotiator.Employee.Name)
    	}else{
    	    DoelEvaluator doelEvaluator = getDoelEvaluator();
    	    List<Value> values = doelEvaluator.evaluate(DoelExpression.parse(fieldName), domainObject.getId(), getAccessToken());
    	    if (values.size() > 0){
    	        result = values.get(0).get(); 
    	    }    	    
    	}

        return result;
    }

    /**
     * Выполнение DOEL выражения и возвращение массива значений
     * @param doel
     * @return
     */
    public List<Object> evaluate(String doel){
        DoelEvaluator doelEvaluator = getDoelEvaluator();
        List<Value> values = doelEvaluator.evaluate(DoelExpression.parse(doel), domainObject.getId(), getAccessToken());
        List<Object> result = new ArrayList<Object>();
        for (Value value : values) {
            if (value != null){
                result.add(value.get());
            }
        }
        return result;
    }
    
    /**
     * Устанавливает поле объекта
     *
     * @param fieldName
     * @param value
     */
    public void set(String fieldName, Object value) {
        FieldConfig config = getConfigurationExplorer().getFieldConfig(domainObject.getTypeName(), fieldName);
        if (config instanceof LongFieldConfig) {
            if (value instanceof Long) {
                domainObject.setLong(fieldName, (long) value);
            } else if (value instanceof Double) {
                domainObject.setLong(fieldName, ((Double) value).longValue());
            } else {
                domainObject.setLong(fieldName, Long.parseLong(value.toString()));
            }
        } else if (config instanceof DecimalFieldConfig) {
            if (value instanceof BigDecimal) {
                domainObject.setDecimal(fieldName, (BigDecimal) value);
            } else if (value instanceof Double){
                domainObject.setDecimal(fieldName, BigDecimal.valueOf((double) value));
            } else {
                domainObject.setDecimal(fieldName, BigDecimal.valueOf((int) value));
            }
        } else if (config instanceof DateTimeFieldConfig) {
            if (value instanceof Date) {
                domainObject.setTimestamp(fieldName, (Date) value);
            } else {
                // Преобразовываем из других форматов
                // domainObject.setTimestamp(fieldName,
                // BigDecimal.valueOf((double)value));
            }
        } else if (config instanceof ReferenceFieldConfig) {
            if (value instanceof Id) {
                domainObject.setReference(fieldName, (Id) value);
            } else {
                // Преобразовываем из других форматов
                // domainObject.setReference(fieldName, value);
            }
        } else {
            domainObject.setString(fieldName, value.toString());
        }
    }

    /**
     * Сохраняет обьект
     */
    public void save() {
        domainObject = getDomainObjectDao().save(domainObject, getAccessToken());
    }
    
    private AccessToken getAccessToken(){
        AccessToken accessToken = getAccessControlService().createSystemAccessToken(this.getClass().getName());
        return accessToken;
    }


    /**
     * Получение сервиса DomainObjectDao
     *
     * @return
     */
    private DomainObjectDao getDomainObjectDao() {
        ApplicationContext ctx = SpringApplicationContext.getContext();
        return ctx.getBean(DomainObjectDao.class);
    }

    /**
     * Получение сервиса AccessControlService
     *
     * @return
     */
    private AccessControlService getAccessControlService() {
        ApplicationContext ctx = SpringApplicationContext.getContext();
        return ctx.getBean(AccessControlService.class);
    }

    /**
     * 
     * @return
     */
    private DoelEvaluator getDoelEvaluator() {
        ApplicationContext ctx = SpringApplicationContext.getContext();
        return ctx.getBean(DoelEvaluator.class);
    }

    /**
     * Получение сервиса для работы с конфигурацией
     * @return
     */
    private ConfigurationExplorer getConfigurationExplorer() {
        ApplicationContext ctx = SpringApplicationContext.getContext();
        return (ConfigurationExplorer) ctx.getBean("configurationExplorer");
    }

    /**
     * Установка статуса объекта
     * @param status
     */
    public void setStatus(String statusName){

    	CrudService crudService = getCrudService();
    	//Создание статуса черновик
        List<DomainObject> allStatuses = crudService.findAll("Status");
        Iterator<DomainObject> iter = allStatuses.iterator();
        DomainObject status = null;
        DomainObject newStatus = null;
       //TODO  Создать статусы заранее
        while (iter.hasNext()){
        	status = iter.next();
        	if ((status.getString("Name").equals(statusName))){
        		newStatus = status;
        	}
        }
        if (newStatus ==null){
        	newStatus = createStatus(statusName);
        }

        AccessToken accessToken = getAccessControlService().createSystemAccessToken("DomainObjectAccessor");
        domainObject = getDomainObjectDao().setStatus(domainObject.getId(), newStatus.getId(), accessToken);
    }
    /**
     * Получить статус объекта
     * @return
     */
    public String getStatus(){
    	//TODO Разобрать почему в domainObject.getReference("status") ).getTypeId() не обновляется статус

    	CrudService crudService = getCrudService();
    	DomainObject status = crudService.find(domainObject.getStatus());
    	return status.getString("Name");
    }

    /**
     * Получение сервиса CrudService
     * @return
     */
    private CrudService getCrudService() {
        ApplicationContext ctx = SpringApplicationContext.getContext();
        return ctx.getBean(CrudService.class);
    }

    private  DomainObject createStatus(String statusName) {
        GenericDomainObject statusDO = new GenericDomainObject();
        statusDO.setTypeName(GenericDomainObject.STATUS_DO);
        Date currentDate = new Date();
        statusDO.setCreatedDate(currentDate);
        statusDO.setModifiedDate(currentDate);
        statusDO.setString("Name", statusName);
        AccessToken accessToken = getAccessControlService().createSystemAccessToken("InitialDataLoader");
        return getDomainObjectDao().save(statusDO, accessToken);
    }

    /**
     * Возвращает строковое представление идентификатора доменного объекта
     * @return
     */
    public String getIdAsString(){
        return domainObject.getId().toStringRepresentation();
    }

    /**
     * Возвращает идентификатор доменного объекта
     * @return
     */
    public Id getId(){
        return domainObject.getId();
    }
    
    /**
     * Возвращает доменный объект, обрабатываемый акцессором
     * @return
     */
    public DomainObject getDomainObject(){
        return domainObject;
    }

}
