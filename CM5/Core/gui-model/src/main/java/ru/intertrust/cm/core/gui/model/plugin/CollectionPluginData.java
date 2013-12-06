package ru.intertrust.cm.core.gui.model.plugin;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 17/9/13
 *         Time: 12:05 PM
 */
public class CollectionPluginData extends PluginData {

    private String collectionName;
    private boolean singleChoice;
    private boolean displayChosenValues;
    private ArrayList<CollectionRowItem> items;
    private LinkedHashMap<String, String> domainObjectFieldOnColumnNameMap;

    public CollectionPluginData() {

    }

    public boolean isSingleChoice() {
        return singleChoice;
    }

    public void setSingleChoice(boolean singleChoice) {
        this.singleChoice = singleChoice;
    }

    public boolean isDisplayChosenValues() {
        return displayChosenValues;
    }

    public void setDisplayChosenValues(boolean displayChosenValues) {
        this.displayChosenValues = displayChosenValues;
    }

    public ArrayList<CollectionRowItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<CollectionRowItem> items) {
        this.items = items;
    }

    public LinkedHashMap<String, String> getDomainObjectFieldOnColumnNameMap() {
        return domainObjectFieldOnColumnNameMap;
    }

    public void setDomainObjectFieldOnColumnNameMap(LinkedHashMap<String, String> domainObjectFieldOnColumnNameMap) {
        this.domainObjectFieldOnColumnNameMap = domainObjectFieldOnColumnNameMap;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }
}
