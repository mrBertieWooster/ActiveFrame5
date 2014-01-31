package ru.intertrust.cm.core.business.impl.search;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.ContentStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.doel.DoelExpression;
import ru.intertrust.cm.core.config.search.IndexedDomainObjectConfig;
import ru.intertrust.cm.core.config.search.IndexedFieldConfig;
import ru.intertrust.cm.core.config.search.LinkedDomainObjectConfig;
import ru.intertrust.cm.core.config.search.ParentLinkConfig;
import ru.intertrust.cm.core.dao.access.AccessControlService;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.AttachmentContentDao;
import ru.intertrust.cm.core.dao.api.DoelEvaluator;
import ru.intertrust.cm.core.dao.api.extension.AfterSaveExtensionHandler;
import ru.intertrust.cm.core.dao.api.extension.ExtensionPoint;

@ExtensionPoint
public class DomainObjectIndexAgent implements AfterSaveExtensionHandler {

    protected Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private SolrUpdateRequestQueue requestQueue;

    @Autowired
    private SearchConfigHelper configHelper;

    @Autowired
    private DoelEvaluator doelEvaluator;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private AttachmentContentDao attachmentContentDao;

    @Override
    public void onAfterSave(DomainObject domainObject, List<FieldModification> changedFields) {
        List<SearchConfigHelper.SearchAreaDetailsConfig> configs = configHelper.findEffectiveConfigs(domainObject);
        if (configs.size() == 0) {
            return;
        }
        ArrayList<SolrInputDocument> solrDocs = new ArrayList<>(configs.size());
        for (SearchConfigHelper.SearchAreaDetailsConfig config : configs) {
            if (configHelper.isAttachmentObject(domainObject)) {
                sendAttachment(domainObject, config);
                continue;
            }
            Id mainId = calculateMainObject(domainObject.getId(), config.getObjectConfig());
            if (mainId == null) {
                // Объект не имеет главного; индексация в данной области не нужна
                continue;
            }
            SolrInputDocument doc = new SolrInputDocument();
            doc.addField(SolrFields.OBJECT_ID, domainObject.getId().toStringRepresentation());
            doc.addField(SolrFields.AREA, config.getAreaName());
            doc.addField(SolrFields.TARGET_TYPE, config.getTargetObjectType());
            doc.addField(SolrFields.MAIN_OBJECT_ID, mainId.toStringRepresentation());
            for (IndexedFieldConfig fieldConfig : config.getObjectConfig().getFields()) {
                SearchFieldType type = configHelper.getFieldType(fieldConfig, config.getObjectConfig().getType());
                Object value = calculateField(domainObject, fieldConfig);
                StringBuilder fieldName = new StringBuilder()
                        .append(SolrFields.FIELD_PREFIX)
                        .append(fieldConfig.getName().toLowerCase())
                        .append(type.getSuffix());
                doc.addField(fieldName.toString(), value);
            }
            doc.addField("id", createUniqueId(domainObject, config));
            solrDocs.add(doc);
        }
        if (solrDocs.size() == 0) {
            return;
        }
        requestQueue.addDocuments(solrDocs);
        if (log.isInfoEnabled()) {
            log.info(Integer.toString(solrDocs.size()) + " Solr document(s) queued for indexing");
        }
    }

    private void sendAttachment(DomainObject object, SearchConfigHelper.SearchAreaDetailsConfig config) {
        Id mainId = calculateMainObject(object.getReference(configHelper.getAttachmentParentLinkName(
                config.getObjectConfig().getType())), config.getObjectConfig());
        if (mainId == null) {
            // Объект не имеет главного; индексация в данной области не нужна
            return;
        }
        ContentStreamUpdateRequest request = new ContentStreamUpdateRequest("/update/extract");
        request.addContentStream(new SolrAttachmentFeeder(object));
        request.setParam("literal." + SolrFields.OBJECT_ID, object.getId().toStringRepresentation());
        request.setParam("literal." + SolrFields.AREA, config.getAreaName());
        request.setParam("literal." + SolrFields.TARGET_TYPE, config.getTargetObjectType());
        request.setParam("literal." + SolrFields.MAIN_OBJECT_ID, mainId.toStringRepresentation());
        addFieldToContentRequest(request, object, AttachmentService.NAME, SearchFieldType.TEXT);
        addFieldToContentRequest(request, object, AttachmentService.DESCRIPTION, SearchFieldType.TEXT);
        addFieldToContentRequest(request, object, AttachmentService.CONTENT_LENGTH, SearchFieldType.LONG);
        request.setParam("literal.id", createUniqueId(object, config));
        request.setParam("uprefix", "cm_c_");
        request.setParam("fmap.content", SolrFields.CONTENT);
        //request.setParam("extractOnly", "true");

        requestQueue.addRequest(request);
        if (log.isInfoEnabled()) {
            log.info("Attachment queued for indexing");
        }
    }

    private void addFieldToContentRequest(ContentStreamUpdateRequest request,
            DomainObject object, String fieldName, SearchFieldType fieldType) {
        Object value = convertValue(object.getValue(fieldName));
        if (value != null) {
            StringBuilder paramName = new StringBuilder()
                    .append("literal.")
                    .append(SolrFields.FIELD_PREFIX)
                    .append(fieldName)
                    .append(fieldType.getSuffix());
            request.setParam(paramName.toString(), value.toString());
        }
    }

    private Object calculateField(DomainObject object, IndexedFieldConfig config) {
        if (config.getDoel() != null) {
            AccessToken accessToken = accessControlService.createSystemAccessToken(getClass().getName());
            List<? extends Value> values = doelEvaluator.evaluate(
                    DoelExpression.parse(config.getDoel()), object.getId(), accessToken);
            if (values.size() == 0) {
                return null;
            } else if (values.size() == 1) {
                return convertValue(values.get(0));
            } else {
                ArrayList<Object> result = new ArrayList<>(values.size());
                for (Value value : values) {
                    result.add(convertValue(value));
                }
                return result;
            }
        }
        Value value = object.getValue(config.getName());
        return convertValue(value);
    }

    private Id calculateMainObject(Id objectId, IndexedDomainObjectConfig config) {
        if (!LinkedDomainObjectConfig.class.isAssignableFrom(config.getClass())) {
            return objectId;
        }
        ParentLinkConfig parentConfig = ((LinkedDomainObjectConfig) config).getParentLink();
        AccessToken accessToken = accessControlService.createSystemAccessToken(getClass().getName());
        List<? extends Value> values = doelEvaluator.evaluate(
                DoelExpression.parse(parentConfig.getDoel()), objectId, accessToken);
        if (values.size() == 0) {
            return null;
        }
        if (values.size() != 1) {
            log.warn("Unexpected result count (" + values.size() + ") while calculating main object for " + objectId +
                    " by expression: " + parentConfig.getDoel());
        }
        Value value = values.get(0);
        if (!(value instanceof ReferenceValue)) {
            log.warn("Wrong result type (" + value.getFieldType() + ") of main object reference for " + objectId +
                    " by expression: " + parentConfig.getDoel());
            return null;
        }
        return ((ReferenceValue) value).get();
    }

    private Object convertValue(Value value) {
        if (value == null) {
            return null;
        }
        Object result;
        if (value instanceof ReferenceValue) {
            Id id = ((ReferenceValue) value).get();
            result = (id == null) ? null : id.toStringRepresentation();
        } else {
            result = value.get();
        }
        return result;
    }

    private String createUniqueId(DomainObject object, SearchConfigHelper.SearchAreaDetailsConfig config) {
        StringBuilder buf = new StringBuilder();
        buf.append(object.getId().toStringRepresentation())
           .append(":").append(config.getAreaName())
           .append(":").append(config.getTargetObjectType());
        return buf.toString();
    }

    public class SolrAttachmentFeeder implements ContentStream {

        private DomainObject attachment;

        public SolrAttachmentFeeder(DomainObject attachment) {
            this.attachment = attachment;
        }

        @Override
        public String getName() {
            return attachment.getString(AttachmentService.NAME);
        }

        @Override
        public String getSourceInfo() {
            return attachment.getString(AttachmentService.DESCRIPTION);
        }

        @Override
        public String getContentType() {
            return attachment.getString(AttachmentService.MIME_TYPE);
        }

        @Override
        public Long getSize() {
            return attachment.getLong(AttachmentService.CONTENT_LENGTH);
        }

        @Override
        public InputStream getStream() throws IOException {
            return attachmentContentDao.loadContent(attachment);
        }

        @Override
        public Reader getReader() throws IOException {
            return new InputStreamReader(getStream());
        }

    }
}
