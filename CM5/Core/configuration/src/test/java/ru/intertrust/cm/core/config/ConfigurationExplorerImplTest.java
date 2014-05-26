package ru.intertrust.cm.core.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import junit.framework.Assert;
import org.junit.rules.ExpectedException;
import ru.intertrust.cm.core.config.base.CollectionConfig;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.converter.ConfigurationClassesCache;
import ru.intertrust.cm.core.config.module.ModuleConfiguration;
import ru.intertrust.cm.core.config.module.ModuleService;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;
import static ru.intertrust.cm.core.config.Constants.COLLECTIONS_CONFIG_PATH;
import static ru.intertrust.cm.core.config.Constants.CONFIGURATION_SCHEMA_PATH;
import static ru.intertrust.cm.core.config.Constants.DOMAIN_OBJECTS_CONFIG_PATH;
import static ru.intertrust.cm.core.config.Constants.DOMAIN_OBJECTS_LOOP_IN_HIERARCHY_CONFIG_PATH;
import static ru.intertrust.cm.core.config.Constants.MODULES_CUSTOM_CONFIG;
import static ru.intertrust.cm.core.config.Constants.MODULES_CUSTOM_SCHEMA;
import static ru.intertrust.cm.core.config.Constants.MODULES_DOMAIN_OBJECTS;

/**
 * @author vmatsukevich
 *         Date: 6/24/13
 *         Time: 3:43 PM
 */
public class ConfigurationExplorerImplTest {

    private static final String PERSON_CONFIG_NAME = "Person";
    private static final String EMPLOYEES_CONFIG_NAME = "Employees";
    private static final String E_MAIL_CONFIG_NAME = "EMail";
    private static final String GLOBAL_XML_PATH = "config/global-test.xml";
    private static final String ACCESS_CONFIG_PATH = "config/access-test.xml";


    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private Configuration config;

    private ConfigurationExplorerImpl configExplorer;

    @Before
    public void setUp() throws Exception {
        ConfigurationSerializer configurationSerializer = createConfigurationSerializer(DOMAIN_OBJECTS_CONFIG_PATH);

        config = configurationSerializer.deserializeConfiguration();
        configExplorer = new ConfigurationExplorerImpl(config);
    }

    @Test
    public void testGetConfiguration() throws Exception {
        Configuration testConfiguration = configExplorer.getConfiguration();
        assertTrue(config == testConfiguration);
        Assert.assertEquals(config, testConfiguration);
    }

    @Test
    public void testInit() throws Exception {
        DomainObjectTypeConfig domainObjectTypeConfig =
                configExplorer.getConfig(DomainObjectTypeConfig.class, PERSON_CONFIG_NAME);
        assertNotNull(domainObjectTypeConfig);

        FieldConfig fieldConfig = configExplorer.getFieldConfig(PERSON_CONFIG_NAME, E_MAIL_CONFIG_NAME);
        assertNotNull(fieldConfig);

        CollectionConfig collectionConfig =
                configExplorer.getConfig(CollectionConfig.class, EMPLOYEES_CONFIG_NAME);
        assertNotNull(collectionConfig);
    }

    @Test
    public void testGetDomainObjectConfigs() throws Exception {
        Collection<DomainObjectTypeConfig> domainObjectTypeConfigs =
                configExplorer.getConfigs(DomainObjectTypeConfig.class);

        assertNotNull(domainObjectTypeConfigs);
        assertEquals(18, domainObjectTypeConfigs.size());

        List<String> domainObjectNames = new ArrayList<>();
        domainObjectNames.addAll(Arrays.asList("Outgoing_Document", PERSON_CONFIG_NAME, "Employee", "Department",
                "Assignment", "Incoming_Document", "Incoming_Document2", "Attachment", "Person_Attachment",
                "Authentication_Info", "User_Group", "Group_Member", "Group_Admin", "Delegation", "Negotiation_Card", "Organization",
                "Internal_Document", "Delegation", "Status", "A1", "A2", "B1", "B2", "C1", "C2", "D1", "D2", "EmployeeNew"));

        for (DomainObjectTypeConfig domainObjectTypeConfig : domainObjectTypeConfigs) {
            String name = domainObjectTypeConfig.getName();
            assertTrue(domainObjectNames.contains(name));
            domainObjectNames.remove(name);
        }
    }

    @Test
    public void testIsReadPermittedToEverybodyCaseInsensistive() throws Exception {
        String type = "Outgoing_Document";
        assertTrue(configExplorer.isReadPermittedToEverybody(type));
        type = "outgoing_document";
        assertTrue(configExplorer.isReadPermittedToEverybody(type));
        type = "OUTGOING_DOCUMENT";
        assertTrue(configExplorer.isReadPermittedToEverybody(type));

    }
    
    @Test
    public void testIsReadPermittedToEverybody() throws Exception {
        String type = "Person";
        assertTrue(configExplorer.isReadPermittedToEverybody(type));
        //test inherited read-everybody = "true"
        type = "Employee";
        assertTrue(configExplorer.isReadPermittedToEverybody(type));
        //test overriden read-everybody = "true"
        type = "EmployeeNew";
        assertTrue(!configExplorer.isReadPermittedToEverybody(type));
        // test default value
        type = "Internal_Document";
        assertTrue(!configExplorer.isReadPermittedToEverybody(type));        
    }
    
    @Test
    public void testGetCollectionConfigs() throws Exception {
        Collection<CollectionConfig> collectionConfigs = configExplorer.getConfigs(CollectionConfig.class);

        assertNotNull(collectionConfigs);
        assertEquals(collectionConfigs.size(), 2);

        List<String> collectionNames = new ArrayList<>();
        collectionNames.addAll(Arrays.asList(EMPLOYEES_CONFIG_NAME, "Employees_2"));

        for(CollectionConfig collectionConfig : collectionConfigs) {
            String name = collectionConfig.getName();
            assertTrue(collectionNames.contains(name));
            collectionNames.remove(name);
        }
    }

    @Test
    public void testGetDomainObjectConfig() throws Exception {
        DomainObjectTypeConfig domainObjectTypeConfig =
                configExplorer.getConfig(DomainObjectTypeConfig.class, PERSON_CONFIG_NAME);
        assertNotNull(domainObjectTypeConfig);
        assertEquals(domainObjectTypeConfig.getName(), PERSON_CONFIG_NAME);
    }

    @Test
    public void testGetCollectionConfig() throws Exception {
        CollectionConfig collectionConfig =
                configExplorer.getConfig(CollectionConfig.class, EMPLOYEES_CONFIG_NAME);
        assertNotNull(collectionConfig);
        assertEquals(collectionConfig.getName(), EMPLOYEES_CONFIG_NAME);
    }

    @Test
    public void testGetFieldConfig() throws Exception {
        FieldConfig fieldConfig = configExplorer.getFieldConfig(PERSON_CONFIG_NAME, E_MAIL_CONFIG_NAME);
        assertNotNull(fieldConfig);
        assertEquals(fieldConfig.getName(), E_MAIL_CONFIG_NAME);
    }

    @Test
    public void testGetSystemFieldConfig() throws Exception {
        FieldConfig fieldConfig = configExplorer.getFieldConfig(PERSON_CONFIG_NAME, SystemField.id.name());
        assertNotNull(fieldConfig);
        Assert.assertEquals(fieldConfig.getName(), SystemField.id.name());

        fieldConfig = configExplorer.getFieldConfig(PERSON_CONFIG_NAME, SystemField.created_date.name());
        assertNotNull(fieldConfig);
        Assert.assertEquals(fieldConfig.getName(), SystemField.created_date.name());

        fieldConfig = configExplorer.getFieldConfig(PERSON_CONFIG_NAME, SystemField.updated_date.name());
        assertNotNull(fieldConfig);
        Assert.assertEquals(fieldConfig.getName(), SystemField.updated_date.name());
    }

    @Test
    public void testFindChildDomainObjectTypes() {
        Collection<DomainObjectTypeConfig> types = configExplorer.findChildDomainObjectTypes("Person", true);
        assertTrue(types.contains(configExplorer.getConfig(DomainObjectTypeConfig.class, "Employee")));
        assertTrue(types.size() == 2);
    }

    @Test
    public void testDomainObjectTypesHierarchy() throws Exception {
        ConfigurationSerializer configurationSerializer = createConfigurationSerializer(DOMAIN_OBJECTS_CONFIG_PATH);

        config = configurationSerializer.deserializeConfiguration();
        configExplorer = new ConfigurationExplorerImpl(config);

        String parent = configExplorer.getDomainObjectParentType("Employee");
        assertEquals("Person", parent);
        String rootType = configExplorer.getDomainObjectRootType("Employee");
        assertEquals("Person", rootType);
        String[] employeeTypesHierarchy = configExplorer.getDomainObjectTypesHierarchy("Employee");
        assertEquals(Arrays.asList(new String[]{"Person"}), Arrays.asList(employeeTypesHierarchy));

        String[] dTypesHierarchy = configExplorer.getDomainObjectTypesHierarchy("D1");
        List<String> expDTypesHierarchy = new ArrayList<>();
        expDTypesHierarchy.add("A1");
        expDTypesHierarchy.add("B1");
        expDTypesHierarchy.add("C1");
        assertEquals(expDTypesHierarchy, Arrays.asList(dTypesHierarchy));

        assertEquals("A1", configExplorer.getDomainObjectRootType("D1"));
        assertEquals("A1", configExplorer.getDomainObjectRootType("C1"));
        assertEquals("A1", configExplorer.getDomainObjectRootType("B1"));
        assertEquals("A1", configExplorer.getDomainObjectParentType("B1"));
        assertEquals("B1", configExplorer.getDomainObjectParentType("C1"));
        assertEquals("C1", configExplorer.getDomainObjectParentType("D1"));

        assertNull(configExplorer.getDomainObjectParentType("Department"));
        assertEquals("Department", configExplorer.getDomainObjectRootType("Department"));
    }

    @Test
    public void testLoopInHierarchy() throws Exception {
        ConfigurationSerializer configurationSerializer = createConfigurationSerializer(DOMAIN_OBJECTS_LOOP_IN_HIERARCHY_CONFIG_PATH);

        config = configurationSerializer.deserializeConfiguration();

        expectedException.expect(ConfigurationException.class);
        expectedException.expectMessage("Loop in the hierarchy, typeName: A2");

        configExplorer = new ConfigurationExplorerImpl(config);
    }


    private ConfigurationSerializer createConfigurationSerializer(String configPath) throws Exception {
        ConfigurationClassesCache.getInstance().build(); // Инициализируем кэш конфигурации тэг-класс

        ConfigurationSerializer configurationSerializer = new ConfigurationSerializer();
        configurationSerializer.setModuleService(createModuleService(configPath));

        return configurationSerializer;
    }

    private ModuleService createModuleService(String configPath) throws MalformedURLException {
        URL moduleUrl = getClass().getClassLoader().getResource(".");

        ModuleService result = new ModuleService();
        ModuleConfiguration confCore = new ModuleConfiguration();
        confCore.setName("core");
        result.getModuleList().add(confCore);
        confCore.setConfigurationPaths(new ArrayList<String>());
        confCore.getConfigurationPaths().add(configPath);
        confCore.getConfigurationPaths().add(COLLECTIONS_CONFIG_PATH);
        confCore.getConfigurationPaths().add(GLOBAL_XML_PATH);
        confCore.getConfigurationPaths().add(ACCESS_CONFIG_PATH);        
        confCore.setConfigurationSchemaPath(CONFIGURATION_SCHEMA_PATH);
        confCore.setModuleUrl(moduleUrl);

        ModuleConfiguration confCustom = new ModuleConfiguration();
        confCustom.setName("custom");
        result.getModuleList().add(confCustom);
        confCustom.setConfigurationPaths(new ArrayList<String>());
        confCustom.getConfigurationPaths().add(MODULES_CUSTOM_CONFIG);
        confCustom.getConfigurationPaths().add(MODULES_DOMAIN_OBJECTS);
        confCustom.setConfigurationSchemaPath(MODULES_CUSTOM_SCHEMA);
        confCustom.setDepends(new ArrayList<String>());
        confCustom.getDepends().add(confCore.getName());
        confCustom.setModuleUrl(moduleUrl);

        return result;
    }
}
