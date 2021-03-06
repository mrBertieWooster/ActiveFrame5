package ru.intertrust.cm.core.service.it;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Date;

import javax.ejb.EJB;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.access.AccessVerificationService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.webcontext.ApplicationContextProvider;

/**
 * 
 * @author atsvetkov
 *
 */
@RunWith(Arquillian.class)
public class AccessVerificationServiceIT extends IntegrationTestBase {

    @EJB
    private CrudService.Remote crudService;

    @EJB
    private AccessVerificationService.Remote accessVerificationService;
    
    private ConfigurationExplorer configurationExplorer;
    
    protected DomainObjectTypeIdCache domainObjectTypeIdCache;

    @Before
    public void init() throws IOException, LoginException {
        LoginContext lc = login("person1", "admin");
        lc.login();
        initializeSpringBeans();
    }

    @After
    public void tearDown() throws LoginException {
        LoginContext lc = login("person1", "admin");
        lc.logout();
    }

    private void initializeSpringBeans() {
        ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
        domainObjectTypeIdCache = applicationContext.getBean(DomainObjectTypeIdCache.class);
        configurationExplorer = applicationContext.getBean(ConfigurationExplorer.class);
    }

    @Test
    public void testPermissionVerifications() throws LoginException {

        DomainObject country = createCountryDomainObject();
        DomainObject savedCountry = crudService.save(country);
        DomainObject city = createCityDomainObject(savedCountry);
        DomainObject savedCity = crudService.save(city);
        Id cityId = savedCity.getId();

        assertTrue(accessVerificationService.isReadPermitted(cityId));
        assertTrue(accessVerificationService.isWritePermitted(cityId));
        assertTrue(accessVerificationService.isCreatePermitted(savedCity.getTypeName()));
        assertTrue(accessVerificationService.isDeletePermitted(cityId));
        assertTrue(accessVerificationService.isCreateChildPermitted(savedCity.getTypeName(), savedCountry.getId()));
        assertTrue(accessVerificationService.isExecuteActionPermitted("action1", cityId));
    }

    @Test
    public void testNotAllowedPermissions() throws LoginException {
        LoginContext lc = login("person2", "admin");
        lc.login();
     
        DomainObject country = createCountryDomainObject();
        DomainObject savedCountry = crudService.save(country);
        DomainObject city = createCityDomainObject(savedCountry);
        DomainObject savedCity = crudService.save(city);
        Id cityId = savedCity.getId();

        assertFalse(accessVerificationService.isReadPermitted(cityId));
        assertFalse(accessVerificationService.isWritePermitted(cityId));
        assertTrue(accessVerificationService.isCreatePermitted(savedCity.getTypeName()));
        assertFalse(accessVerificationService.isDeletePermitted(cityId));
        assertFalse(accessVerificationService.isCreateChildPermitted(savedCity.getTypeName(), savedCountry.getId()));
        assertFalse(accessVerificationService.isExecuteActionPermitted("action1", cityId));

        lc.logout();

    }

    @Test
    public void testAdministratorAccess() throws LoginException {
        LoginContext lc = login("person1", "admin");
        lc.login();
        
        DomainObject testDOWithourPermissions = createTestDOWithoutPermissions();
        DomainObject savedTestDOWithoutPermissions = crudService.save(testDOWithourPermissions);

        DomainObject testDOWithPermissions = createTestDOWithPermissions();
        DomainObject savedTestDOWithPermissions = crudService.save(testDOWithPermissions);
        
        assertTrue(accessVerificationService.isReadPermitted(savedTestDOWithPermissions.getId()));
        assertFalse(accessVerificationService.isReadPermitted(savedTestDOWithoutPermissions.getId()));

    }
    
    @Test
    public void testCreatePermissionForInheretedAccessMatrix() throws LoginException {
        assertTrue(accessVerificationService.isCreatePermitted("super_employee_test"));
    }

    private DomainObject createCountryDomainObject() {
        DomainObject organizationDomainObject = crudService.createDomainObject("country_test");
        organizationDomainObject.setString("name", "Country" + System.currentTimeMillis());
        return organizationDomainObject;
    }

    private DomainObject createTestDOWithoutPermissions() {
        DomainObject testDomainObject = crudService.createDomainObject("test_DO_No_Permissions");
        testDomainObject.setString("name", "test_DO_No_Permissions" + System.currentTimeMillis());
        return testDomainObject;
    }
    
    private DomainObject createTestDOWithPermissions() {
        DomainObject testDomainObject = crudService.createDomainObject("test_DO_Permissions");
        testDomainObject.setString("name", "test_DO_Permissions" + System.currentTimeMillis());
        return testDomainObject;
    }
    private DomainObject createCityDomainObject(DomainObject savedCountryObject) {
        DomainObject departmentDomainObject = crudService.createDomainObject("city_test");
        departmentDomainObject.setString("Name", "City" + new Date());
        if (savedCountryObject != null) {
            departmentDomainObject.setReference("country", savedCountryObject.getId());
        }
        return departmentDomainObject;
    }
    
    @Test
    public void testIsReadPermited() throws LoginException {
        assertTrue(configurationExplorer.isReadPermittedToEverybody("test_DO_1"));
        assertTrue(configurationExplorer.isReadPermittedToEverybody("test_DO_2"));
        assertFalse(configurationExplorer.isReadPermittedToEverybody("test_DO_3"));

        assertTrue(configurationExplorer.isReadPermittedToEverybody("ref_DO_3"));
        assertTrue(configurationExplorer.isReadPermittedToEverybody("ref_DO_2"));
        assertTrue(configurationExplorer.isReadPermittedToEverybody("ref_DO_1"));

        assertFalse(configurationExplorer.isReadPermittedToEverybody("ref_DO_3_1"));
        assertTrue(configurationExplorer.isReadPermittedToEverybody("ref_DO_3_2"));

    }
    
}
