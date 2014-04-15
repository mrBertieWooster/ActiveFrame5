package ru.intertrust.cm.core.config.gui.action;

import java.io.InputStream;
import org.junit.Test;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Sergey.Okolot
 *         Created on 14.04.2014 18:20.
 */
public class ActionEntryConfigTest {
    private static final String ID = "id_value";
    private static final String COMPONENT_NAME = "action.test";
    private static final String STYLE_CLASS = "styleClass_value";

    @Test
    public void testActionConfig() throws Exception {
        final InputStream is = getClass().getResourceAsStream("action-entry-junit.xml");
        final Serializer serializer = new Persister();
        final ActionEntryConfig config = serializer.read(ActionEntryConfig.class, is);
        assertFalse(config.getChildren().isEmpty());
        assertEquals(ID, config.getId());
        assertEquals(COMPONENT_NAME, config.getComponentName());
        assertEquals(STYLE_CLASS, config.getStyleClass());
        assertNotNull(config);
    }
}
