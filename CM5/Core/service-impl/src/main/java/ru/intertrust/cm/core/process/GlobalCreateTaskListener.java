package ru.intertrust.cm.core.process;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.activiti.engine.FormService;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.TaskFormData;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.IdService;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.dao.api.DomainObjectDao;
import ru.intertrust.cm.core.tools.SpringClient;

/**
 * Глобальный слушатель создания UserTask. Создает соответствующий доменный
 * объект и выполняет отсылку по электронной почте
 */
public class GlobalCreateTaskListener extends SpringClient implements
		TaskListener {

	@Autowired
	private DomainObjectDao domainObjectDao;

	@Autowired
	private IdService idService;
	
	@Autowired
	FormService formService;

	/**
	 * Входная точка слушителя. вызывается при создание пользовательской задачи
	 */
	@Override
	public void notify(DelegateTask delegateTask) {
		// Создание доменного обьекта Task
		DomainObject taskDomainObject = createDomainObject("Person_Task");
		taskDomainObject.setString("TaskId", delegateTask.getId());
		taskDomainObject.setString("ActivityId", delegateTask.getTaskDefinitionKey());
		taskDomainObject.setString("Name", delegateTask.getName());
		taskDomainObject
				.setString("Description", delegateTask.getDescription());
		taskDomainObject.setLong("Priority", (long) delegateTask.getPriority());
		
		//TODO установка статуса отправлено. Временно сделано через просто поле в дальнейшем надо 
		//переделать на системное поле state доменного объекта
		taskDomainObject.setLong("State", ProcessService.TASK_STATE_SEND);
		
		String mainAttachmentId = (String)delegateTask.getVariable("MAIN_ATTACHMENT_ID");
		if (mainAttachmentId != null){
			taskDomainObject.setString("MainAttachment", mainAttachmentId); 
		}
		//Получение полей формы задачи ACTIONS и сохранение его в обьект задача
		TaskFormData taskData = formService.getTaskFormData(delegateTask.getId());
		List<FormProperty> formProperties = taskData.getFormProperties();
		for (FormProperty formProperty : formProperties) {
			if (formProperty.getId().equals("ACTIONS")){
				Map<String, String> values = (Map<String, String>)formProperty.getType().getInformation("values");
				StringBuilder actions = new StringBuilder();
				boolean firstItem = true;
				for (String key : values.keySet()) {
					if (firstItem){
						firstItem = false;
					}else{
						actions.append(";");
					}
					actions.append(key);
					actions.append("=");
					actions.append(values.get(key));
				}
				taskDomainObject.setString("Actions", actions.toString());
			}
		}
		
		//Сохранение доменного объекта
		taskDomainObject = domainObjectDao.save(taskDomainObject);

		// Создание связанного AssigneePerson или AssigneeGroup
		if (delegateTask.getAssignee().startsWith("person")) {
			DomainObject assigneePersonDomainObject = createDomainObject("Assignee_Person");

			assigneePersonDomainObject.setReference("PersonTask",
					taskDomainObject);
			assigneePersonDomainObject.setReference("Person",
					idService.createId(delegateTask.getAssignee()));
			
			domainObjectDao.save(assigneePersonDomainObject);
		} else {
			DomainObject assigneePersonDomainObject = createDomainObject("Assignee_Group");

			assigneePersonDomainObject.setReference("PersonTask",
					taskDomainObject);
			assigneePersonDomainObject.setReference("UserGroup",
					idService.createId(delegateTask.getAssignee()));

			domainObjectDao.save(assigneePersonDomainObject);
		}

		// Отправка почтовых сообщений
		// TODO Отправка почтовых сообщений
	}

	/**
	 * Создание нового доменного обьекта переданного типа
	 * 
	 * @param type
	 * @return
	 */
	private DomainObject createDomainObject(String type) {
		GenericDomainObject taskDomainObject = new GenericDomainObject();
		taskDomainObject.setTypeName(type);
		Date currentDate = new Date();
		taskDomainObject.setCreatedDate(currentDate);
		taskDomainObject.setModifiedDate(currentDate);
		return taskDomainObject;
	}

}
