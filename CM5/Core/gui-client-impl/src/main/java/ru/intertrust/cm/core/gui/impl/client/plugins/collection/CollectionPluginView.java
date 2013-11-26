package ru.intertrust.cm.core.gui.impl.client.plugins.collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.impl.client.event.SplitterInnerScrollEvent;
import ru.intertrust.cm.core.gui.impl.client.event.SplitterInnerScrollEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.SplitterWidgetResizerEvent;
import ru.intertrust.cm.core.gui.impl.client.event.SplitterWidgetResizerEventHandler;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.CellTableEventHandler;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.CheckedSelectionModel;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel.TableController;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.resources.CellTableResourcesEx;
import ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.resources.DGCellTableResourceAdapter;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.form.widget.CollectionRowItemList;
import ru.intertrust.cm.core.gui.model.plugin.CollectionPluginData;
import ru.intertrust.cm.core.gui.model.plugin.CollectionRowItem;
import ru.intertrust.cm.core.gui.model.plugin.CollectionRowsRequest;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseServiceAsync;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 17/9/13
 *         Time: 12:05 PM
 */
public class CollectionPluginView extends PluginView {
    private CellTable<CollectionRowItem> tableHeader;
    private CellTable<CollectionRowItem> tableBody;
    private ScrollPanel scrollTableBody = new ScrollPanel();
    private TableController tableController;
    private ArrayList<CollectionRowItem> items;
    private LinkedHashMap<String, String> columnNamesOnDoFieldsMap;
    private FlowPanel headerPanel = new FlowPanel();
    private FlowPanel bodyPanel = new FlowPanel();
    private VerticalPanel verticalPanel = new VerticalPanel();
    private FlowPanel root = new FlowPanel();
    private String collectionName;
    private int scrollStart = 0;
    private int listCount = 0;
    private int tableWidth;
    private int tableHeight;
    private EventBus eventBus;

    /**
     * Создание стилей для ящеек таблицы
     */
    private final DGCellTableResourceAdapter adapter;

    protected CollectionPluginView(Plugin plugin) {
        super(plugin);
        adapter = new DGCellTableResourceAdapter(CellTableResourcesEx.I);
        tableHeader = new CellTable<CollectionRowItem>(999, adapter.getResources());
        tableBody = new CellTable<CollectionRowItem>(999, adapter.getResources());
        tableController = new TableController(tableHeader, tableBody);
        updateSizes();

        eventBus = plugin.getEventBus();

    }

    private void updateSizes() {
        tableWidth = plugin.getOwner().getPanelWidth();
        tableHeight = plugin.getOwner().getPanelHeight();

    }

    private void createCollectionData() {
        CollectionRowsRequest collectionRowsRequest = new CollectionRowsRequest(listCount, 15, collectionName, columnNamesOnDoFieldsMap);
        Command command = new Command("generateCollectionRowItems", "collection.plugin", collectionRowsRequest);

        BusinessUniverseServiceAsync.Impl.getInstance().executeCommand(command, new AsyncCallback<Dto>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("something was going wrong while obtaining generateCollectionRowItems for ''");
                caught.printStackTrace();
            }

            @Override
            public void onSuccess(Dto result) {
                CollectionRowItemList collectionRowItemList = (CollectionRowItemList) result;
                List<CollectionRowItem> collectionRowItems = collectionRowItemList.getCollectionRows();
                insertMoreRows(collectionRowItems);

            }
        });
    }

    @Override
    protected IsWidget getViewWidget() {

        CollectionPluginData collectionPluginData = plugin.getInitialData();
        collectionName = collectionPluginData.getCollectionName();
        columnNamesOnDoFieldsMap = collectionPluginData.getDomainObjectFieldOnColumnNameMap();
        items = collectionPluginData.getItems();
        init();
        return root;

    }

    public void init() {
        buildPanel();
        buildTableColumns(columnNamesOnDoFieldsMap);
        insertRows(items);
        applyStyles();
        addHandlers();

    }

    private void addResizeHandler() {

        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {

                tableController.columnWindowResize(columnMinWidth(tableWidth / tableHeader.getColumnCount()));
                scrollTableBody.setHeight(tableHeight + "px");
            }
        });
    }

    private int columnMinWidth(int width) {
        if (width < 100) {
            width = 100;
        }
        return width;
    }

    public void onPluginPanelResize() {
        updateSizes();
        tableController.columnWindowResize(columnMinWidth(tableWidth / tableHeader.getColumnCount()));
        scrollTableBody.setHeight(tableHeight + "px");
    }

    private void addHandlers() {
        addResizeHandler();
        tableHeader.addCellPreviewHandler(new CellTableEventHandler<CollectionRowItem>(tableHeader, plugin));
        eventBus.addHandler(SplitterInnerScrollEvent.TYPE, new SplitterInnerScrollEventHandler() {
            @Override
            public void setScrollPanelHeight(SplitterInnerScrollEvent event) {

                scrollTableBody.setHeight((event.getUpperPanelHeight() - headerPanel.getOffsetHeight()) + "px");
                tableController.columnWindowResize(columnMinWidth(event.getUpperPanelWidth() / tableBody.getColumnCount()));
            }
        });

        eventBus.addHandler(SplitterWidgetResizerEvent.TYPE, new SplitterWidgetResizerEventHandler() {

            @Override
            public void setWidgetSize(SplitterWidgetResizerEvent event) {
                if (event.isType()) {
                    if ((event.getFirstWidgetHeight() * 2) < Window.getClientHeight()) {
                        scrollTableBody.setHeight(((event.getFirstWidgetHeight() * 2) - headerPanel.getOffsetHeight()) + "px");
                    } else {
                        scrollTableBody.setHeight((event.getFirstWidgetHeight() - headerPanel.getOffsetHeight()) + "px");
                    }
                } else {
                    scrollTableBody.setHeight((event.getFirstWidgetHeight() - headerPanel.getOffsetHeight()) + "px");
                }
                tableController.columnWindowResize(columnMinWidth(event.getFirstWidgetWidth() / tableBody.getColumnCount()));
            }
        });


        scrollTableBody.addScrollHandler(new ScrollHandler() {
            @Override
            public void onScroll(ScrollEvent event) {

                if (scrollStart + 25 < scrollTableBody.getVerticalScrollPosition() || scrollStart == scrollTableBody.
                        getMaximumVerticalScrollPosition()) {
                    createCollectionData();
                    scrollStart = scrollTableBody.getVerticalScrollPosition();
                    listCount += 15;
                }

            }
        });

    }


    private TextColumn<CollectionRowItem> buildNameColumn(final String string) {

        return new TextColumn<CollectionRowItem>() {
            @Override
            public String getValue(CollectionRowItem object) {
                return object.getStringValue(string);
            }
        };
    }

    private void buildPanel() {
        headerPanel.add(tableHeader);
        bodyPanel.add(tableBody);
        verticalPanel.add(headerPanel);
        scrollTableBody.getElement().getStyle().setOverflowX(Style.Overflow.HIDDEN);
        scrollTableBody.setHeight(tableHeight + "px");
        scrollTableBody.add(bodyPanel);
        verticalPanel.add(scrollTableBody);
        verticalPanel.setSize("100%", "100%");
        root.add(verticalPanel);

    }

    private void buildTableColumns(LinkedHashMap<String, String> domainObjectFieldsOnColumnNamesMap) {
        int numberOfColumns = domainObjectFieldsOnColumnNamesMap.keySet().size();
        int columnWidth = (tableWidth / numberOfColumns);
        for (String field : domainObjectFieldsOnColumnNamesMap.keySet()) {
            Column<CollectionRowItem, String> column = buildNameColumn(field);
            String columnName = domainObjectFieldsOnColumnNamesMap.get(field);
            tableHeader.addColumn(column, columnName);
            tableHeader.setColumnWidth(column, columnWidth + "px");
            column.setDataStoreName(columnName);
            tableBody.addColumn(column);
            tableBody.setColumnWidth(column, columnWidth + "px");
        }

    }

    private void insertRows(List<CollectionRowItem> list) {
        tableBody.setRowData(items);
        listCount = items.size();

    }

    private void insertMoreRows(List<CollectionRowItem> list) {

        items.addAll(list);
        tableBody.setRowData(items);
    }

    private void applyStyles() {
        applyHeaderTableStyle();
        applyBodyTableStyle();
    }

    private void applyHeaderTableStyle() {
        tableHeader.setStyleName(adapter.getResources().cellTableStyle().docsCommonCelltableHeader());
        tableHeader.setTableLayoutFixed(true);
        headerPanel.setStyleName(adapter.getResources().cellTableStyle().docsCommonCelltableHeaderPanel());

    }

    private void applyBodyTableStyle() {
        final CheckedSelectionModel<CollectionRowItem> selectionModel = new CheckedSelectionModel<CollectionRowItem>();
        String emptyTableText = null;

        HTML emptyTableWidget = new HTML("<br/><div align='center'> <h1> " + emptyTableText + " </h1> </div>");
        emptyTableWidget.getElement().getStyle().setPaddingLeft(60, Style.Unit.PX);

        tableBody.setRowStyles(new RowStyles<CollectionRowItem>() {
            @Override
            public String getStyleNames(CollectionRowItem row, int rowIndex) {
                return adapter.getResources().cellTableStyle().docsCommonCelltableTrCommon();

            }
        });
        tableBody.setStyleName(adapter.getResources().cellTableStyle().docsCommonCelltableBody());

        tableBody.setEmptyTableWidget(emptyTableWidget);
        tableBody.setSelectionModel(selectionModel);
        tableBody.setTableLayoutFixed(true);

    }

    public ScrollPanel getScrollTableBody() {
        return scrollTableBody;
    }

}

