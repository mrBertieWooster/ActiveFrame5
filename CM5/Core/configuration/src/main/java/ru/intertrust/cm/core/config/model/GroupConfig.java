package ru.intertrust.cm.core.config.model;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Включает группу пользователей в состав роли.
 * @author atsvetkov
 *
 */
@Root(name = "group")
public class GroupConfig implements Serializable {

    @Attribute(required = true)
    private String name;

    @Element(name = "bind-context", required = false)
    private BindContextConfig bindContext;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BindContextConfig getBindContext() {
        return bindContext;
    }

    public void setBindContext(BindContextConfig bindContext) {
        this.bindContext = bindContext;
    }    
    
}
