package ru.intertrust.cm.core.config.crypto;

import java.io.InputStream;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;

public class SignedDataItem implements Dto {
    private static final long serialVersionUID = -3132225111239223064L;
    private Id id;
    private String name;
    private InputStream content;
      
    public SignedDataItem() {
    }
    
    public SignedDataItem(Id id, String name, InputStream content) {
        super();
        this.id = id;
        this.name = name;
        this.content = content;
    }
    public Id getId() {
        return id;
    }
    public void setId(Id id) {
        this.id = id;
    }
    public InputStream getContent() {
        return content;
    }
    public void setContent(InputStream content) {
        this.content = content;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
