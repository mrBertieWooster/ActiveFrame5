package ru.intertrust.cm.core.config.crypto;

import java.util.ArrayList;
import java.util.List;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;

public class SignedData implements Dto{
    private static final long serialVersionUID = 1094911161784917225L;

    private List<SignedDataItem> signedDataItems = new ArrayList<SignedDataItem>();
    
    private Id rootId;

    public Id getRootId() {
        return rootId;
    }

    public void setRootId(Id rootId) {
        this.rootId = rootId;
    }

    public List<SignedDataItem> getSignedDataItems() {
        return signedDataItems;
    }

    public void setSignedDataItems(List<SignedDataItem> signedDataItems) {
        this.signedDataItems = signedDataItems;
    }
}
