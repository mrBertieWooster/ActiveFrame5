package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.SearchAreaRefConfig;
import ru.intertrust.cm.core.config.gui.form.widget.SingleChoiceConfig;
import ru.intertrust.cm.core.config.gui.form.widget.TableBrowserConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionRefConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionViewRefConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionViewerConfig;
import ru.intertrust.cm.core.config.gui.navigation.SortCriteriaConfig;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.ComponentRegistry;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.event.*;
import ru.intertrust.cm.core.gui.impl.client.form.FacebookStyleView;
import ru.intertrust.cm.core.gui.impl.client.form.widget.support.ButtonConstructor;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.CollectionPlugin;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.*;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.ArrayList;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 06.11.13
 *         Time: 11:15
 */
@ComponentName("table-browser")
public class TableBrowserWidget extends BaseWidget {
    private TableBrowserConfig tableBrowserConfig;
    private PluginPanel pluginPanel;
    private FocusPanel openDialogButton;
    private FocusPanel clearButton;
    private TextBox filterEditor;
    private EventBus eventBus = new SimpleEventBus();
    private ArrayList<Id> chosenIds = new ArrayList<Id>();
    private boolean singleChoice;
    private int width;
    private int height;
    private DialogBox dialogBox;
    private FacebookStyleView facebookStyleView;
    FlowPanel root = new FlowPanel();

    @Override
    public void setCurrentState(WidgetState currentState) {
        TableBrowserState tableBrowserState = (TableBrowserState) currentState;
        tableBrowserConfig = tableBrowserState.getTableBrowserConfig();
        singleChoice = tableBrowserState.isSingleChoice();
        facebookStyleView.initDisplayStyle(tableBrowserConfig.getSelectionStyleConfig().getName());
        facebookStyleView.setChosenItems(tableBrowserState.getSelectedItemsRepresentations());
        facebookStyleView.showSelectedItems();
        initSizes();
        initDialogView();
        createAddButton();
        createClearAllButton();
    }

    @Override
    public TableBrowserState getCurrentState() {
        TableBrowserState state = new TableBrowserState();
        state.setSelectedItemsRepresentations(facebookStyleView.getChosenItems());

        return state;
    }

    @Override
    protected Widget asEditableWidget() {

        return initWidgetView();
    }

    @Override
    protected Widget asNonEditableWidget() {
        return asEditableWidget();
    }

    @Override
    public Component createNew() {
        return new TableBrowserWidget();
    }

    private void initCollectionPluginPanel() {
        pluginPanel = new PluginPanel();
        pluginPanel.setVisibleWidth(width);
        pluginPanel.setVisibleHeight(height);

    }

    private CollectionViewerConfig initCollectionConfig() {
        CollectionViewerConfig collectionViewerConfig = new CollectionViewerConfig();
        CollectionViewRefConfig collectionViewRefConfig = new CollectionViewRefConfig();
        SearchAreaRefConfig searchAreaRefConfig = new SearchAreaRefConfig();
        searchAreaRefConfig.setName(tableBrowserConfig.getInputTextFilterConfig().getName());
        searchAreaRefConfig.setValue(filterEditor.getValue());
        collectionViewRefConfig.setName(tableBrowserConfig.getCollectionViewRefConfig().getName());
        CollectionRefConfig collectionRefConfig = new CollectionRefConfig();
        collectionRefConfig.setName(tableBrowserConfig.getCollectionRefConfig().getName());
        SortCriteriaConfig sortCriteriaConfig = new SortCriteriaConfig();
        collectionViewerConfig.setSortCriteriaConfig(sortCriteriaConfig);
        collectionViewerConfig.setCollectionRefConfig(collectionRefConfig);
        collectionViewerConfig.setCollectionViewRefConfig(collectionViewRefConfig);
        collectionViewerConfig.setSearchAreaRefConfig(searchAreaRefConfig);
        collectionViewerConfig.setSingleChoice(singleChoice);
        collectionViewerConfig.setDisplayChosenValues(tableBrowserConfig.getDisplayChosenValues().isDisplayChosenValues());
        collectionViewerConfig.setExcludedIds(facebookStyleView.getChosenIds());
        return collectionViewerConfig;
    }

    private void openCollectionPlugin() {

        CollectionPlugin collectionPlugin = ComponentRegistry.instance.get("collection.plugin");
        CollectionViewerConfig collectionViewerConfig = initCollectionConfig();
        collectionPlugin.setConfig(collectionViewerConfig);
        collectionPlugin.setLocalEventBus(eventBus);
        collectionPlugin.addViewCreatedListener(new PluginViewCreatedEventListener() {
            @Override
            public void onViewCreation(PluginViewCreatedEvent source) {
                dialogBox.center();
            }
        });
        pluginPanel.open(collectionPlugin);

    }

    private FlowPanel initWidgetView() {
        facebookStyleView = new FacebookStyleView();
        filterEditor = new TextBox();
        filterEditor.getElement().setClassName("table-browser-filter-editor");
        openDialogButton = new FocusPanel();

        openDialogButton.addClickHandler(new FetchFilteredRowsClickHandler());
        root.add(filterEditor);
        root.add(openDialogButton);
        root.add(facebookStyleView);

        return root;
    }

    private void createAddButton(){
        ButtonConstructor buttonConstructor;
        if (tableBrowserConfig.getClearAllButtonConfig() != null ){
            String img = tableBrowserConfig.getAddButtonConfig().getImage();
            String text = tableBrowserConfig.getAddButtonConfig().getText();
            if (text == null || text.equals("...") || text.length() == 0 ){
                text = "Добавить";
            }
            buttonConstructor = new ButtonConstructor(openDialogButton, img, text);
        }   else {
            buttonConstructor = new ButtonConstructor(openDialogButton, null, "Добавить");
        }

        openDialogButton.add(buttonConstructor);
    }

    private void createClearAllButton(){
        if (tableBrowserConfig.getClearAllButtonConfig() != null ){
        String img = tableBrowserConfig.getClearAllButtonConfig().getImage();
        String text = tableBrowserConfig.getClearAllButtonConfig().getText();

             clearButton = new FocusPanel();
             ButtonConstructor buttonConstructor = new ButtonConstructor(clearButton, img, text );

             clearButton.add(buttonConstructor);
             root.insert(clearButton, 2);


             clearButton.addClickHandler(new ClickHandler() {
                 @Override
                 public void onClick(ClickEvent event) {
                     chosenIds.clear();
                     facebookStyleView.getChosenItems().clear();
                     facebookStyleView.showSelectedItems();

                 }
             });

         }


    }

    private void initDialogView() {

        dialogBox = new DialogBox();
        dialogBox.getElement().getStyle().setZIndex(10);
        initCollectionConfig();
        initCollectionPluginPanel();
        Button okButton = new Button("OK");
        okButton.removeStyleName("gwt-Button");
        okButton.addStyleName("dialog-box-button buttons-fixed");
        Button cancelButton = new Button("CANCEL");
        cancelButton.removeStyleName("gwt-Button");
        cancelButton.addStyleName("dialog-box-button buttons-fixed position-margin-left");
        if (isSingleChoice()) {
            addClickHandlersForSingleChoice(okButton, cancelButton, dialogBox);
        } else {
            addClickHandlersForMultiplyChoice(okButton, cancelButton, dialogBox);
        }
        AbsolutePanel buttonsContainer = new AbsolutePanel();
        buttonsContainer.addStyleName("table-browser-dialog-box-button-panel");
        buttonsContainer.add(okButton);
        buttonsContainer.add(cancelButton);
        FlowPanel dialogBoxContent = new FlowPanel();

        dialogBoxContent.setWidth(width + "px");
        dialogBoxContent.getElement().getStyle().setOverflow(Style.Overflow.AUTO);
        dialogBoxContent.add(pluginPanel);
        dialogBoxContent.add(buttonsContainer);

        dialogBox.add(dialogBoxContent);
        dialogBox.setWidth(width + "px");
        dialogBox.setHeight(height + "px");

    }

    private boolean isSingleChoice() {
        final SingleChoiceConfig singleChoice = tableBrowserConfig.getSingleChoice();
        return singleChoice != null && singleChoice.isSingleChoice();
    }

    private void addCancelButtonClickHandler(final Button cancelButton, final DialogBox dialogBox) {

        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dialogBox.hide();
            }
        });
    }

    private class FetchFilteredRowsClickHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            openCollectionPlugin();

        }
    }

    private void addClickHandlersForMultiplyChoice(final Button okButton, final Button cancelButton, final DialogBox dialogBox) {

        addCancelButtonClickHandler(cancelButton, dialogBox);
        okButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                fetchParsedRows();
                dialogBox.hide();
            }
        });
        eventBus.addHandler(CheckBoxFieldUpdateEvent.TYPE, new CheckBoxFieldUpdateEventHandler() {
            @Override
            public void onCheckBoxFieldUpdate(CheckBoxFieldUpdateEvent event) {
                if (event.isDeselected()) {
                    chosenIds.remove(event.getId());
                    facebookStyleView.removeChosenItem(event.getId());
                } else {
                    chosenIds.add(event.getId());
                }
            }
        });

    }

    private void addClickHandlersForSingleChoice(final Button okButton, final Button cancelButton, final DialogBox dialogBox) {
        addCancelButtonClickHandler(cancelButton, dialogBox);
        okButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

                fetchParsedRows();
                dialogBox.hide();
            }
        });
        eventBus.addHandler(CollectionRowSelectedEvent.TYPE, new CollectionRowSelectedEventHandler() {
            @Override
            public void onCollectionRowSelect(CollectionRowSelectedEvent event) {
                chosenIds.clear();
                chosenIds.add(event.getId());

            }
        });

    }

    private void initSizes() {
        String widthString = displayConfig.getWidth();
        String heightString = displayConfig.getHeight();
        width = widthString == null ? 500 : Integer.parseInt(widthString.replaceAll("\\D+", ""));
        height = heightString == null ? 300 : Integer.parseInt(heightString.replaceAll("\\D+", ""));

    }

    private void fetchParsedRows() {

        if (chosenIds.isEmpty()) {
            facebookStyleView.showSelectedItems();
            return;
        }
        FormatRowsRequest formatRowsRequest = new FormatRowsRequest();
        formatRowsRequest.setSelectionPattern(tableBrowserConfig.getSelectionPatternConfig().getValue());
        formatRowsRequest.setIdsShouldBeFormatted(chosenIds);

        Command command = new Command("fetchParsedRows", getName(), formatRowsRequest);
        BusinessUniverseServiceAsync.Impl.getInstance().executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                ParsedRowsList list = (ParsedRowsList) result;
                ArrayList<FacebookStyleItem> items = list.getFilteredRows();
                if (isSingleChoice()) {
                    facebookStyleView.setChosenItems(items);
                } else {
                facebookStyleView.getChosenItems().addAll(items);
                }
                facebookStyleView.showSelectedItems();
                chosenIds.clear();
            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining rows");
            }
        });

    }

}