package ru.intertrust.cm.core.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.PersonServiceDao;
import ru.intertrust.cm.core.util.SpringApplicationContext;

import javax.ejb.EJBContext;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Реализация доступа к текущему пользователю, вошедшему в систему.
 * @author atsvetkov
 *
 */
public class CurrentUserAccessorImpl implements CurrentUserAccessor {

    final static Logger logger = LoggerFactory.getLogger(CurrentUserAccessorImpl.class);

    private EJBContext ejbContext;

    public EJBContext getEjbContext() {
        if (ejbContext == null) {
            try {
                InitialContext ic = new InitialContext();
                ejbContext = (SessionContext) ic.lookup("java:comp/EJBContext");
            } catch (NamingException ex) {
                throw new IllegalStateException(ex);
            }
        }

        return ejbContext;
    }    

    /**
     * Возвращает логин текущего пользователя. Если пользователя нет в EJB контексте и в случае возникновения исключений
     * выозвращает null.
     * @return логин текущего пользователя
     */
    public String getCurrentUser() {
        String result = null;
        try {
            // В случае если вызов идет изнутри представившись как system то подставляем пользователя admin,
            // возможно понадобится иметь иного системного пользователя
            // TODO разобратся почему не устанавливается роль

            // Workaround for JBoss7 bug in @RunAs
            EJBContext ejbContext = getEjbContext();
            if (Boolean.TRUE.equals(ejbContext.getContextData().get(INITIAL_DATA_LOADING))) {
                return null;
            } else if (ejbContext.isCallerInRole("system")) {
                result = "admin"; // TODO возможно стоит подумать над иным пользователем, например system
            } else {
                String principalName = ejbContext.getCallerPrincipal().getName();
                if (principalName.equals("anonymous")) {
                    result = "admin"; // TODO возможно стоит подумать над иным пользователем, например system
                } else {
                    result = principalName;
                }
            }
        } catch (Exception e) {
            result = null;
            if (logger.isDebugEnabled()) {
                logger.debug("Error getting current user: " + e.getMessage());
            }
        }

        return result;
   }
    
    /**
     * Возвращает идентификатор текущего пользователя. Возвращает null, если невозможно получить пользователя из EJB
     * контекста.
     * @return идентификатор текущего пользователя.
     */
    public Id getCurrentUserId() {
        String login = getCurrentUser();
        if (login != null) {
            return getPersonServiceDao().findPersonByLogin(login).getId();
        } else {
            return null;
        }
    }
    
    private PersonServiceDao getPersonServiceDao() {
        ApplicationContext ctx = SpringApplicationContext.getContext();
        return ctx.getBean(PersonServiceDao.class);
    }
    
}
