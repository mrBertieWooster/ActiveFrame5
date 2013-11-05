package ru.intertrust.cm.core.config.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Класс конфигурации коллектора динамической группы
 * @author larin
 *
 */
public class DynamicGroupTrackDomainObjectsConfig extends TrackDomainObjectsConfig {
    @Element(name ="get-person", required = false)    
    private GetPersonConfig getPerson;

    public GetPersonConfig getGetPerson() {
        return getPerson;
    }

    public void setGetPerson(GetPersonConfig getPerson) {
        this.getPerson = getPerson;
    }
    
}
