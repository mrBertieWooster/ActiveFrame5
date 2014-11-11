package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.form.PopupTitlesHolder;
import ru.intertrust.cm.core.config.gui.form.widget.*;
import ru.intertrust.cm.core.gui.api.client.Application;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.action.SaveAction;
import ru.intertrust.cm.core.gui.impl.client.event.ActionSuccessListener;
import ru.intertrust.cm.core.gui.impl.client.event.CentralPluginChildOpeningRequestedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser.*;
import ru.intertrust.cm.core.gui.impl.client.form.widget.BaseWidget;
import ru.intertrust.cm.core.gui.impl.client.form.widget.EventBlocker;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.FormDialogBox;
import ru.intertrust.cm.core.gui.impl.client.form.widget.tooltip.TooltipSizer;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.impl.client.util.GuiUtil;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.filters.ComplicatedFiltersParams;
import ru.intertrust.cm.core.gui.model.form.widget.*;
import ru.intertrust.cm.core.gui.model.form.widget.hierarchybrowser.HierarchyBrowserUtil;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 10.12.13
 *         Time: 13:15
 */
@ComponentName("hierarchy-browser")
public class HierarchyBrowserWidget extends BaseWidget implements HierarchyBrowserCheckBoxUpdateEventHandler,
        HierarchyBrowserItemClickEventHandler, HierarchyBrowserNodeClickEventHandler,
        HierarchyBrowserRefreshClickEventHandler, HierarchyBrowserSearchClickEventHandler,
        HierarchyBrowserScrollEventHandler, HierarchyBrowserAddItemClickEventHandler,
        HierarchyBrowserHyperlinkStateUpdatedEventHandler, HierarchyBrowserCloseDialogEventHandler,
        HierarchyBrowserShowTooltipEventHandler {
    private HierarchyBrowserWidgetState currentState;
    private HierarchyBrowserMainPopup mainPopup;
    private EventBus localEventBus = new SimpleEventBus();
    private HandlerRegistration handlerRegistration;

    @Override
    public Component createNew() {
        HierarchyBrowserWidget widget = new HierarchyBrowserWidget();
        widget.registerEventsHandling();
        return widget;
    }

    public void setCurrentState(WidgetState currentState) {
        this.currentState = (HierarchyBrowserWidgetState) currentState;

        initialData = currentState;
        if (isEditable()) {
            setCurrentStateForEditableWidget();
        } else {
            setCurrentStateForNoneEditableWidget();
        }
    }

    @Override
    protected boolean isChanged() {
        final List<Id> initialValue = ((HierarchyBrowserWidgetState) getInitialData()).getIds();
        final List<Id> currentValue = currentState.getIds(); //looks like every time will be the same
        return initialValue == null ? currentValue != null : !initialValue.equals(currentValue);
    }

    @Override
    public Object getValue() {
        return currentState.getChosenItems();
    }

    private void setCurrentStateForEditableWidget() {
        final HierarchyBrowserView view = (HierarchyBrowserView) impl;
        view.clear();
        final HierarchyBrowserConfig config = currentState.getHierarchyBrowserConfig();

        final WidgetDisplayConfig displayConfig = getDisplayConfig();
        view.initWidgetContent(config, new ClearButtonClickHandler());

        view.displayBaseWidget(displayConfig.getWidth(), displayConfig.getHeight(), currentState.getChosenItems(),
                currentState.isTooltipAvailable());
        if (handlerRegistration != null) {
            handlerRegistration.removeHandler();
        }
        handlerRegistration = view.addButtonClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                currentState.initTemporaryState();
                mainPopup = new HierarchyBrowserMainPopup(localEventBus, currentState);
                mainPopup.createAndShowPopup(currentState.getTemporaryChosenItems());
                mainPopup.addOkClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        currentState.applyChanges();
                        view.displayChosenItems(currentState.getChosenItems(), currentState.isTooltipAvailable());
                        disposeOfMainPopup();

                    }
                });
                mainPopup.addCancelClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        disposeOfMainPopup();
                    }
                });
                final NodeContentManager nodeContentManager = new FirstNodeContentManager(config,
                        mainPopup, currentState.getIds(), currentState.getCollectionNameNodeMap());
                mainPopup.addLinkClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        nodeContentManager.fetchNodeContent();
                    }
                });

                nodeContentManager.fetchNodeContent();
            }
        });
    }

    private void setCurrentStateForNoneEditableWidget() {
        List<HierarchyBrowserItem> hierarchyBrowserItems = currentState.getChosenItems();
        HierarchyBrowserNoneEditablePanel noneEditablePanel = (HierarchyBrowserNoneEditablePanel) impl;

        if (HierarchyBrowserUtil.isDisplayingHyperlinks(currentState)) {
            noneEditablePanel.display(hierarchyBrowserItems, currentState.isTooltipAvailable());
        } else {
            noneEditablePanel.displayHierarchyBrowserItems(hierarchyBrowserItems, currentState.isTooltipAvailable());

        }

    }

    @Override
    protected HierarchyBrowserWidgetState createNewState() {
        HierarchyBrowserWidgetState state = new HierarchyBrowserWidgetState();
        ArrayList<HierarchyBrowserItem> chosenItems = currentState.getChosenItems();
        state.setChosenItems(chosenItems);
        state.setSelectedIds(currentState.getIds());

        return state;
    }

    @Override
    public WidgetState getFullClientStateCopy() {
        if (!isEditable()) {
            return super.getFullClientStateCopy();
        }

        HierarchyBrowserWidgetState state = new HierarchyBrowserWidgetState();
        state.setChosenItems(currentState.getChosenItems());

        state.setSingleChoice(currentState.isSingleChoice());
        state.setCollectionNameNodeMap(currentState.getCollectionNameNodeMap());
        state.setHierarchyBrowserConfig(currentState.getHierarchyBrowserConfig());
        state.setConstraints(currentState.getConstraints());
        state.setWidgetProperties(currentState.getWidgetProperties());
        return state;
    }

    @Override
    protected Widget asEditableWidget(WidgetState state) {
        currentState = (HierarchyBrowserWidgetState) state;
        HierarchyBrowserConfig hierarchyBrowserConfig = currentState.getHierarchyBrowserConfig();
        SelectionStyleConfig selectionStyleConfig = hierarchyBrowserConfig.getSelectionStyleConfig();
        boolean displayAsHyperlinks = HierarchyBrowserUtil.isDisplayingHyperlinks(currentState);

        return new HierarchyBrowserView(selectionStyleConfig, localEventBus, displayAsHyperlinks);
    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        currentState = (HierarchyBrowserWidgetState) state;
        HierarchyBrowserConfig hierarchyBrowserConfig = currentState.getHierarchyBrowserConfig();
        SelectionStyleConfig selectionStyleConfig = hierarchyBrowserConfig.getSelectionStyleConfig();
        return new HierarchyBrowserNoneEditablePanel(selectionStyleConfig, localEventBus,
                HierarchyBrowserUtil.isDisplayingHyperlinks(currentState));

    }

    private void registerEventsHandling() {
        localEventBus.addHandler(HierarchyBrowserCheckBoxUpdateEvent.TYPE, this);
        localEventBus.addHandler(HierarchyBrowserNodeClickEvent.TYPE, this);
        localEventBus.addHandler(HierarchyBrowserRefreshClickEvent.TYPE, this);
        localEventBus.addHandler(HierarchyBrowserItemClickEvent.TYPE, this);
        localEventBus.addHandler(HierarchyBrowserAddItemClickEvent.TYPE, this);
        localEventBus.addHandler(HierarchyBrowserSearchClickEvent.TYPE, this);
        localEventBus.addHandler(HierarchyBrowserScrollEvent.TYPE, this);
        localEventBus.addHandler(HierarchyBrowserHyperlinkStateUpdatedEvent.TYPE, this);
        localEventBus.addHandler(HierarchyBrowserShowTooltipEvent.TYPE, this);
    }

    @Override
    public void onHierarchyBrowserCheckBoxUpdate(HierarchyBrowserCheckBoxUpdateEvent event) {
        boolean singleChoiceCase = handleAsSingleChoice(event);
        if (singleChoiceCase) {
            return;
        }
        final HierarchyBrowserItem item = event.getItem();
        int delta = item.isChosen() ? 1 : 0;
        if (HierarchyBrowserUtil.shouldInitializeTooltip(currentState, delta)) {
            fetchWidgetItems(new TooltipCallback() {
                @Override
                public void perform() {
                    handleItemChangeState(item);

                }
            });
        } else {
            handleItemChangeState(item);
        }

    }

    private boolean handleAsSingleChoice(HierarchyBrowserCheckBoxUpdateEvent event) {
        HierarchyBrowserItem previous = event.getPreviousChosenItem();
        if (previous == null) {
            return false;
        }
        HierarchyBrowserItem item = event.getItem();
        if (item.isSingleChoice() == null || !item.isSingleChoice()) {
            if (currentState.isSingleChoice()) {
                currentState.handleCommonSingleChoice(item);
                return true;
            }
            return false;
        }

        currentState.handleNodeSingleChoice(item, previous);
        localEventBus.fireEvent(new HierarchyBrowserCheckBoxUpdateEvent(previous, null));
        refreshView();
        return true;

    }

    private void handleItemChangeState(HierarchyBrowserItem item) {
        boolean chosen = item.isChosen();
        if (chosen) {
            currentState.handleAddingItem(item);
        } else {
            currentState.handleRemovingItem(item);
        }
        refreshView();

    }

    private void refreshView() {
        if (mainPopup != null) {
            mainPopup.displayChosenItems(currentState.getTemporaryChosenItems(), currentState.isTooltipAvailable());
        } else {
            HierarchyBrowserView view = (HierarchyBrowserView) impl;
            view.displayChosenItems(currentState.getChosenItems(), currentState.isTooltipAvailable());
        }
    }

    @Override
    public void onHierarchyBrowserItemClick(HierarchyBrowserItemClickEvent event) {
        HierarchyBrowserItem item = event.getItem();
        final Id id = item.getId();
        final String collectionName = item.getNodeCollectionName();
        final HierarchyBrowserDisplay display = event.getHyperlinkDisplay();
        final boolean tooltipContent = event.isTooltipContent();
        if (BusinessUniverseConstants.UNDEFINED_COLLECTION_NAME.equalsIgnoreCase(collectionName)) {
            return;
        }
        NodeCollectionDefConfig nodeCollectionDefConfig = currentState.getCollectionNameNodeMap().get(collectionName);
        final String domainObjectType = item.getDomainObjectType();
        final FormPluginConfig config = GuiUtil.createFormPluginConfig(id, nodeCollectionDefConfig, domainObjectType, false);

        final FormDialogBox noneEditableFormDialogBox = new FormDialogBox(currentState.getHyperlinkPopupTitle(collectionName,
                domainObjectType));

        final FormPlugin noneEditableFormPlugin = noneEditableFormDialogBox.createFormPlugin(config, eventBus);
        noneEditableFormDialogBox.initButton("Открыть в полном окне", new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                noneEditableFormPlugin.setDisplayActionToolBar(true);
                noneEditableFormPlugin.setLocalEventBus(getEventBus());
                Application.getInstance().getEventBus()
                        .fireEvent(new CentralPluginChildOpeningRequestedEvent(noneEditableFormPlugin));
                noneEditableFormDialogBox.hide();
                mainPopup.hidePopup();
            }
        });
        noneEditableFormDialogBox.initButton("Изменить", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

                noneEditableFormDialogBox.hide();
                config.getPluginState().setEditable(true);
                final FormDialogBox editableFormDialogBox = new FormDialogBox(currentState
                        .getHyperlinkPopupTitle(collectionName, domainObjectType));
                final FormPlugin editableFormPlugin = editableFormDialogBox.createFormPlugin(config, eventBus);
                editableFormDialogBox.initButton("Изменить", new ClickHandler() {
                    @Override
                    public void onClick(final ClickEvent event) {
                        final SaveAction action = GuiUtil.createSaveAction(editableFormPlugin, id);
                        action.addActionSuccessListener(new ActionSuccessListener() {
                            @Override
                            public void onSuccess() {
                                editableFormDialogBox.hide();
                                localEventBus.fireEvent(new HierarchyBrowserHyperlinkStateUpdatedEvent(id,
                                        collectionName, display, tooltipContent));

                            }
                        });
                        action.perform();

                    }
                });
                editableFormDialogBox.initButton("Отмена", new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {

                        editableFormDialogBox.hide();
                        config.getPluginState().setEditable(false);
                    }
                });

            }

        });
        noneEditableFormDialogBox.initButton("Отмена", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                noneEditableFormDialogBox.hide();
            }
        });

    }

    @Override
    public void onHierarchyBrowserNodeClick(HierarchyBrowserNodeClickEvent event) {
        HierarchyBrowserConfig config = currentState.getHierarchyBrowserConfig();
        String collectionName = event.getCollectionName();
        Id parentId = event.getParentId();
        ArrayList<Id> chosenIds = currentState.getTemporarySelectedIds();
        NodeContentManager nodeContentManager = new NewNodeContentManager(config, mainPopup, chosenIds,
                collectionName, parentId, currentState.getCollectionNameNodeMap());
        nodeContentManager.fetchNodeContent();
    }

    @Override
    public void onHierarchyBrowserRefreshClick(HierarchyBrowserRefreshClickEvent event) {
        HierarchyBrowserConfig config = currentState.getHierarchyBrowserConfig();
        String parentCollectionName = event.getParentCollectionName();
        Id parentId = event.getParentId();
        ArrayList<Id> chosenIds = currentState.getTemporarySelectedIds();
        NodeContentManager nodeContentManager = new RefreshNodeContentManager(config, mainPopup, chosenIds,
                parentCollectionName, parentId, "", currentState.getCollectionNameNodeMap());
        nodeContentManager.fetchNodeContent();
    }

    @Override
    public void onHierarchyBrowserSearchClick(HierarchyBrowserSearchClickEvent event) {
        HierarchyBrowserConfig config = currentState.getHierarchyBrowserConfig();
        String parentCollectionName = event.getParentCollectionName();
        Id parentId = event.getParentId();
        String inputText = event.getInputText();
        ArrayList<Id> chosenIds = currentState.getTemporarySelectedIds();
        NodeContentManager nodeContentManager = new RefreshNodeContentManager(config, mainPopup, chosenIds,
                parentCollectionName, parentId, inputText, currentState.getCollectionNameNodeMap());
        nodeContentManager.fetchNodeContent();
    }

    @Override
    public void onHierarchyBrowserScroll(HierarchyBrowserScrollEvent event) {
        HierarchyBrowserConfig config = currentState.getHierarchyBrowserConfig();
        String parentCollectionName = event.getParentCollectionName();
        Id parentId = event.getParentId();
        int factor = event.getFactor();
        int offset = factor * config.getPageSize();
        String inputText = event.getInputText();
        ArrayList<Id> chosenIds = currentState.getTemporarySelectedIds();
        NodeContentManager nodeContentManager = new ScrollNodeContentManager(config, mainPopup,
                chosenIds, parentCollectionName, parentId, inputText, offset,
                currentState.getCollectionNameNodeMap());
        nodeContentManager.fetchNodeContent();
    }

    @Override
    public void onHierarchyBrowserAddItemClick(HierarchyBrowserAddItemClickEvent event) {

        final Id parentId = event.getParentId();
        NodeCollectionDefConfig nodeConfig = event.getNodeConfig();
        String domainObjectTypeToCreate = event.getDomainObjectType();
        final String parentCollectionName = event.getParentCollectionName();
        FormPluginConfig config = GuiUtil.createFormPluginConfig(null, nodeConfig, domainObjectTypeToCreate, true);
        FillParentOnAddConfig fillParentOnAddConfig = nodeConfig.getFillParentOnAddConfig();
        if (fillParentOnAddConfig != null) {
            HierarchyBrowserUpdaterContext hierarchyBrowserUpdaterContext = new HierarchyBrowserUpdaterContext(fillParentOnAddConfig,
                    parentId);
            config.setUpdaterContext(hierarchyBrowserUpdaterContext);
            config.setDomainObjectUpdatorComponent("hierarchy-browser-do-updater");
        }
        PopupTitlesHolder popupTitlesHolder = nodeConfig.getDoTypeTitlesMap().get(domainObjectTypeToCreate);
        String newObjectTitle = popupTitlesHolder == null ? null  : popupTitlesHolder.getTitleNewObject();

        final FormDialogBox createItemDialogBox = new FormDialogBox(newObjectTitle);
        final FormPlugin createFormPlugin = createItemDialogBox.createFormPlugin(config, eventBus);
        createItemDialogBox.initButton("Cохранить", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final SaveAction action = GuiUtil.createSaveAction(createFormPlugin, parentId);
                action.addActionSuccessListener(new ActionSuccessListener() {
                    @Override
                    public void onSuccess() {
                        createItemDialogBox.hide();
                        localEventBus.fireEvent(new HierarchyBrowserRefreshClickEvent(parentId, parentCollectionName));
                    }
                });
                action.perform();

            }
        });
        createItemDialogBox.initButton("Отмена", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                createItemDialogBox.hide();
            }
        });
    }

    @Override
    public void onHierarchyBrowserHyperlinkStateUpdatedEvent(final HierarchyBrowserHyperlinkStateUpdatedEvent event) {
        Id id = event.getId();
        String collectionName = event.getCollectionName();
        HierarchyBrowserConfig config = currentState.getHierarchyBrowserConfig();

        HierarchyBrowserHyperlinkContentManager hyperlinkContentManager =
                new EditableHierarchyBrowserHyperlinkContentManager(id, collectionName, config,
                        currentState.getCollectionNameNodeMap()) {
                    @Override
                    protected void handleHyperlinkUpdate(HierarchyBrowserItem updatedItem) {
                        HierarchyBrowserDisplay hyperlinkDisplay = event.getHyperlinkDisplay();
                        if (hyperlinkDisplay == null) { //event generated when node item changes
                            handleNodeItemChanges(updatedItem);
                        } else {
                            handleHyperlinkItemChanges(hyperlinkDisplay, updatedItem, event.isTooltipContent());
                        }

                    }
                };
        hyperlinkContentManager.updateHyperlink();
    }

    private void handleNodeItemChanges(HierarchyBrowserItem updatedItem) {
        handleHyperlinkItemChanges(mainPopup, updatedItem, false);

    }

    private void handleHyperlinkItemChanges(HierarchyBrowserDisplay hyperlinkDisplay,
                                            HierarchyBrowserItem updatedItem, boolean tooltipContent) {
        boolean shouldDrawTooltipButton = currentState.isTooltipAvailable();
        List<HierarchyBrowserItem> itemsToRedraw = tooltipContent ? currentState.getTooltipChosenItems()
                : currentState.getCurrentItems();
        HierarchyBrowserUtil.handleUpdateChosenItem(updatedItem, itemsToRedraw);

        hyperlinkDisplay.display(itemsToRedraw, shouldDrawTooltipButton);
        if (mainPopup != null) {
            mainPopup.refreshNode(updatedItem);
            //Main content should be updated accordingly, saving hyperlink DO could not be reverted
            List<HierarchyBrowserItem> mainContent = currentState.getChosenItems();
            boolean updated = HierarchyBrowserUtil.handleUpdateChosenItem(updatedItem, mainContent);
            if (updated) {
                ((HierarchyBrowserDisplay) impl).display(mainContent, shouldDrawTooltipButton);
            }
        }
    }

    @Override
    public void onHierarchyBrowserCloseDialogEvent(HierarchyBrowserCloseDialogEvent event) {
        if (mainPopup != null) {
            disposeOfMainPopup();
        }
    }

    private void disposeOfMainPopup() {
        mainPopup.hidePopup();
        currentState.resetChanges();
        mainPopup = null;

    }

    @Override
    public void onHierarchyBrowserShowTooltip(HierarchyBrowserShowTooltipEvent event) {
        HierarchyBrowserItemsView itemsView = event.getItemsView();
        onTooltipElementClick(itemsView);
    }

    private void fetchWidgetItems(final TooltipCallback tooltipCallback) {
        final HierarchyBrowserConfig config = currentState.getHierarchyBrowserConfig();
        ComplicatedFiltersParams filtersParams =
                GuiUtil.createComplicatedFiltersParams(getContainer(), currentState.getSelectionWidgetIdsComponentNames());
        final HierarchyBrowserTooltipRequest request = new HierarchyBrowserTooltipRequest(config, currentState.getIds(), filtersParams);
        Command command = new Command("fetchWidgetItems", getName(), request);
        BusinessUniverseServiceAsync.Impl.executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onSuccess(Dto result) {
                HierarchyBrowserTooltipResponse response = (HierarchyBrowserTooltipResponse) result;
                ArrayList<HierarchyBrowserItem> items = response.getItems();
                currentState.setTooltipChosenItems(items);
                tooltipCallback.perform();

            }

            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining rows");
            }
        });
    }

    private void onTooltipElementClick(final HierarchyBrowserItemsView itemsView) {

        if (currentState.getTooltipChosenItems() == null) {
            final HandlerRegistration blockerHandler = Event.addNativePreviewHandler(new EventBlocker(impl));
            fetchWidgetItems(new TooltipCallback() {
                @Override
                public void perform() {
                    blockerHandler.removeHandler();
                    createAndShowTooltip(itemsView, currentState.getTooltipChosenItems());
                }
            });
        } else {
            createAndShowTooltip(itemsView, currentState.getTooltipChosenItems());

        }
    }

    private void createAndShowTooltip(HierarchyBrowserItemsView itemsView, ArrayList<HierarchyBrowserItem> items) {
        final HierarchyBrowserConfig config = currentState.getHierarchyBrowserConfig();
        SelectionStyleConfig styleConfig = config.getSelectionStyleConfig();

        if (itemsView == null) {
            HierarchyBrowserNoneEditableTooltip tooltip = new HierarchyBrowserNoneEditableTooltip(styleConfig,
                    localEventBus, HierarchyBrowserUtil.isDisplayingHyperlinks(currentState));
            TooltipSizer.setWidgetBounds(config, tooltip);
            tooltip.displayItems(items);
            tooltip.showRelativeTo(impl);
        } else {
            HierarchyBrowserEditableTooltip tooltip = new HierarchyBrowserEditableTooltip(styleConfig, localEventBus,
                    HierarchyBrowserUtil.isDisplayingHyperlinks(currentState));
            TooltipSizer.setWidgetBounds(config, tooltip);
            tooltip.displayItems(items);
            tooltip.showRelativeTo(itemsView);
        }
    }


    private class ClearButtonClickHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            currentState.clearState();
            HierarchyBrowserView view = (HierarchyBrowserView) impl;
            view.displayChosenItems(currentState.getChosenItems(), false);

        }
    }

}


