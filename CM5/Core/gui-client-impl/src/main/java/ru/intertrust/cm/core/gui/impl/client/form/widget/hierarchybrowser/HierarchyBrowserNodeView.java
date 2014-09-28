package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser.*;
import ru.intertrust.cm.core.gui.impl.client.themes.GlobalThemesManager;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.impl.client.util.GuiUtil;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 16.12.13
 *         Time: 13:15
 */
public class HierarchyBrowserNodeView implements IsWidget {
    private List<HierarchyBrowserItem> items = new ArrayList<HierarchyBrowserItem>();
    private VerticalPanel currentNodePanel = new VerticalPanel();
    private VerticalPanel root = new VerticalPanel();
    private EventBus eventBus;
    private int factor;
    private ScrollPanel scroll = new ScrollPanel();
    private TextBox textBox;

    private boolean selective;
    private HorizontalPanel styledActivePanel = new HorizontalPanel();

    public HierarchyBrowserNodeView(EventBus eventBus, int nodeHeight,
                                    boolean selective) {
        this.eventBus = eventBus;
        this.selective = selective;
        scroll.setHeight(nodeHeight + "px");
        scroll.setAlwaysShowScrollBars(false);
        scroll.getElement().getStyle().setOverflowX(Style.Overflow.HIDDEN);
        root.addStyleName("hierarchyBrowserNode");
        scroll.addStyleName("oneNodeScroll");
        currentNodePanel.addStyleName("one-node-body");

    }

    public void drawMoreItems(List<HierarchyBrowserItem> moreItems) {
        items.addAll(moreItems);
        currentNodePanel.clear();
        for (HierarchyBrowserItem item : items) {
            drawNodeChild(item);
        }

    }

    public void redrawNode(List<HierarchyBrowserItem> items) {
        scroll.setHorizontalScrollPosition(0);
        this.items = items;
        currentNodePanel.clear();
        for (HierarchyBrowserItem item : items) {
            drawNodeChild(item);
        }
        factor = 0;

    }


    @Override
    public Widget asWidget() {
        return root;
    }

    public void drawNode(Id parentId, String parentCollectionName, final List<HierarchyBrowserItem> items,
                         Map<String, String> domainObjectTypesAndTitles) {
        this.items = items;
        currentNodePanel.clear();
        drawNodeHeader(parentId, parentCollectionName,domainObjectTypesAndTitles);
        scroll.add(currentNodePanel);
        root.add(scroll);
        for (HierarchyBrowserItem item : items) {
            drawNodeChild(item);
        }
       addScrollHandler(parentId,parentCollectionName);
    }
    private void addScrollHandler(final Id parentId, final String parentCollectionName) {
        scroll.addScrollHandler(new ScrollHandler() {
            @Override
            public void onScroll(ScrollEvent event) {

                if (scroll.getVerticalScrollPosition() == scroll.getMaximumVerticalScrollPosition()) {
                    factor++;
                    eventBus.fireEvent(new HierarchyBrowserScrollEvent(parentId,parentCollectionName,
                            factor, textBox.getText()));
                }
            }
        });
    }

    public void drawNodeChild(final HierarchyBrowserItem item) {
        final HorizontalPanel currentItemPanel = new HorizontalPanel();
        HorizontalPanel panelLeft = new HorizontalPanel();
        if (selective) {
            final CheckBox checkBox = new CheckBox();
            if (item.isChosen()) {
                checkBox.setValue(true);
            }
            checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    boolean chosen = event.getValue();
                    item.setChosen(chosen);
                    eventBus.fireEvent(new HierarchyBrowserCheckBoxUpdateEvent(item));
                }
            });
            eventBus.addHandler(HierarchyBrowserCheckBoxUpdateEvent.TYPE,new HierarchyBrowserCheckBoxUpdateEventHandler() {
                @Override
                public void onHierarchyBrowserCheckBoxUpdate(HierarchyBrowserCheckBoxUpdateEvent event) {

                    if(item.getId().equals(event.getItem().getId())){
                     boolean value = event.getItem().isChosen();
                     checkBox.setValue(value);
                    }
                }
            });
            panelLeft.add(checkBox);
            panelLeft.setCellVerticalAlignment(checkBox, HasVerticalAlignment.ALIGN_MIDDLE);
        }

        Label anchor = new Label(item.getStringRepresentation());
        anchor.addStyleName("node-item-link");
        anchor.removeStyleName("gwt-Hyperlink ");
        anchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                eventBus.fireEvent(new HierarchyBrowserItemClickEvent(item.getId(), item.getNodeCollectionName()));

            }
        });
        FocusPanel focusPanel = new FocusPanel();
        if(item.isMayHaveChildren()){
        Label arrow = new Label("►");
        arrow.addStyleName("hBArrowRight");
        focusPanel.add(arrow);
        focusPanel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                eventBus.fireEvent(new HierarchyBrowserNodeClickEvent(item.getNodeCollectionName(), item.getId()));
                styledActivePanel.removeStyleName("node-item-row-active");
                currentItemPanel.addStyleName("node-item-row-active");
                styledActivePanel = currentItemPanel;
            }
        });   }
        panelLeft.add(anchor);
        currentItemPanel.add(panelLeft);
        currentItemPanel.add(focusPanel);
        currentItemPanel.addStyleName("node-item-row");
        final String id = item.getId().toStringRepresentation();
        currentItemPanel.getElement().setId(id);
        currentItemPanel.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (GuiUtil.isChildClicked(event, id)) {
                    return;
                }
                eventBus.fireEvent(new HierarchyBrowserNodeClickEvent(item.getNodeCollectionName(), item.getId()));
                styledActivePanel.removeStyleName("node-item-row-active");
                currentItemPanel.addStyleName("node-item-row-active");
                styledActivePanel = currentItemPanel;
            }
        }, ClickEvent.getType());
        currentNodePanel.add(currentItemPanel);
    }

    private void drawNodeHeader(Id parentId, String parentCollectionName, Map<String, String> domainObjectTypesAndTitles) {

        AbsolutePanel searchBar = createSearchBar(parentId,parentCollectionName);
        HorizontalPanel buttonsPanel = new HorizontalPanel();
        buttonsPanel.getElement().getStyle().setMarginLeft(5, Style.Unit.PX);

        for (Map.Entry<String, String> entry : domainObjectTypesAndTitles.entrySet()) {
        FocusPanel addItemPanel = createAddItemButton(parentId, parentCollectionName,entry);
        buttonsPanel.add(addItemPanel);
        }
        FocusPanel refreshButton = createRefreshButton(parentId,parentCollectionName);
        buttonsPanel.add(refreshButton);
        root.add(searchBar);
        root.add(buttonsPanel);
    }

    private FocusPanel createAddItemButton(final Id parentId, final String parentCollectionName,
                                           final Map.Entry<String, String> entry) {
        final FocusPanel addItemPanel = new FocusPanel();
        addItemPanel.addStyleName("light-button button-extra-style");
        AbsolutePanel buttonPanel = new AbsolutePanel();
        Image plus = new Image("images/green-plus.png");

        Label text = new Label(entry.getValue());
        text.addStyleName("hierarchyBrowserLabel");
        buttonPanel.add(plus);
        buttonPanel.add(text);
        addItemPanel.add(buttonPanel);
        addItemPanel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                eventBus.fireEvent(new HierarchyBrowserAddItemClickEvent(parentId, parentCollectionName, entry));

            }
        });
        return addItemPanel;
    }

    private FocusPanel createRefreshButton(final Id parentId, final String parentCollectionName) {
        FocusPanel refreshButton = new FocusPanel();
        refreshButton.setStyleName("button-refresh");
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                eventBus.fireEvent(new HierarchyBrowserRefreshClickEvent(parentId, parentCollectionName));
                factor = 0;
            }
        });

        return refreshButton;
    }

    private AbsolutePanel createSearchBar(final Id parentId, final String parentCollectionName) {
        final AbsolutePanel result = new AbsolutePanel();
        result.addStyleName("search-bar");
        textBox = new TextBox();
        Panel resetButton = initResetButton(parentId, parentCollectionName);
        initFilterInput(resetButton, parentId, parentCollectionName);
        Panel magnifier = initMagnifier(parentId, parentCollectionName);
        result.add(magnifier);
        result.add(textBox);
        result.add(resetButton);

        return result;
    }

    private Panel initMagnifier(final Id parentId, final String parentCollectionName){

        Panel magnifier = new AbsolutePanel();
        magnifier.setStyleName(GlobalThemesManager.getCurrentTheme().commonCss().magnifierButton());
        FocusPanel result = new FocusPanel();
        result.addStyleName("magnifier");
        result.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String inputText = textBox.getText();
                if ("".equalsIgnoreCase(inputText)) {
                    return;
                }
                eventBus.fireEvent(new HierarchyBrowserSearchClickEvent(parentId, parentCollectionName, inputText));

            }
        });
        result.add(magnifier);
        return result;
    }

    private Panel initResetButton(final Id parentId, final String parentCollectionName){
        final Panel result = new AbsolutePanel();
        result.addStyleName("reset-button");
        result.getElement().getStyle().setDisplay(Style.Display.NONE);
        result.setStyleName(GlobalThemesManager.getCurrentTheme().commonCss().hBFilterClearButton());
        result.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                textBox.setText(BusinessUniverseConstants.EMPTY_VALUE);
                eventBus.fireEvent(new HierarchyBrowserRefreshClickEvent(parentId, parentCollectionName));
                result.removeFromParent();
                factor = 0;
            }
        }, ClickEvent.getType());

        return result;
    }

    private void initFilterInput(final Panel resetButton, final Id parentId, final String parentCollectionName){
        textBox.getElement().setAttribute("placeholder", "Поиск");
        textBox.setStyleName("input-text");
        textBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent event) {
                Style style = resetButton.getElement().getStyle();
                String inputText = textBox.getText();
                if (BusinessUniverseConstants.EMPTY_VALUE.equalsIgnoreCase(inputText)) {
                    style.setDisplay(Style.Display.NONE);
                } else {
                    style.clearDisplay();

                }
            }

        });
        textBox.addKeyDownHandler(new KeyDownHandler() {

            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    String inputText = textBox.getValue();
                    eventBus.fireEvent(new HierarchyBrowserSearchClickEvent(parentId, parentCollectionName, inputText));
                }
            }
        });

    }

}
