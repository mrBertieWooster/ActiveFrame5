package ru.intertrust.cm.core.config;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;
import ru.intertrust.cm.core.config.model.base.Configuration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static ru.intertrust.cm.core.config.Constants.*;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 10/9/13
 *         Time: 12:05 PM
 */
public class NavigationPanelLogicalValidatorTest {
    private static final String NAVIGATION_PANEL_XML_PATH =
            "config/navigation-panel-test.xml";

    private static final String NAVIGATION_PANEL_INVALID_CHILD_TO_OPEN_XML_PATH =
            "config/navigation-panel-invalid-child-to-open.xml";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testValidate() throws Exception {
        ConfigurationExplorer configurationExplorer = createConfigurationExplorer(NAVIGATION_PANEL_XML_PATH);

    }
    @Test
    public void testValidateInvalidChildToOpen() throws Exception {
        expectedException.expect(ConfigurationException.class);
        expectedException.expectMessage("Child link to open is not found for name 'Administration'");
        ConfigurationExplorer configurationExplorer =
                createConfigurationExplorer(NAVIGATION_PANEL_INVALID_CHILD_TO_OPEN_XML_PATH);


    }

    private ConfigurationExplorer createConfigurationExplorer(String configPath) throws Exception {
        TopLevelConfigurationCache.getInstance().build(); 
        ConfigurationSerializer configurationSerializer = new ConfigurationSerializer();

        Set<String> configPaths = new HashSet<>(Arrays.asList(configPath, DOMAIN_OBJECTS_CONFIG_PATH));

        configurationSerializer.setCoreConfigurationFilePaths(configPaths);
        configurationSerializer.setCoreConfigurationSchemaFilePath(CONFIGURATION_SCHEMA_PATH);

        configurationSerializer.setModulesConfigurationFolder(MODULES_CONFIG_FOLDER);
        configurationSerializer.setModulesConfigurationPath(MODULES_CONFIG_PATH);
        configurationSerializer.setModulesConfigurationSchemaPath(MODULES_CONFIG_SCHEMA_PATH);

        Configuration configuration = configurationSerializer.deserializeConfiguration();
        System.out.println(ConfigurationSerializer.serializeConfiguration(configuration));

        ConfigurationExplorer configurationExplorer = new ConfigurationExplorerImpl(configuration);
        return configurationExplorer;
    }

}

