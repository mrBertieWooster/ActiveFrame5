package ru.intertrust.cm.core.gui.impl.server.widget;

import com.healthmarketscience.rmiio.RemoteInputStreamServer;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.config.model.gui.form.widget.AttachmentBoxConfig;
import ru.intertrust.cm.core.gui.api.server.widget.MultiObjectWidgetHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.ObjectsNode;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentModel;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
/**
 * @author Yaroslav Bondarchuk
 *         Date: 23.10.13
 *         Time: 10:25
 */
@ComponentName("attachment-box")
public class AttachmentBoxHandler  extends MultiObjectWidgetHandler {
    private static final String ATTACHMENT_NAME = "Name";
    private static final String ATTACHMENT_DESCRIPTION = "Description";

    @Autowired
    AttachmentService attachmentService;
    @Override
    public AttachmentBoxState getInitialState(WidgetContext context) {
        AttachmentBoxConfig widgetConfig = context.getWidgetConfig();
        FieldPath fieldPath = new FieldPath(widgetConfig.getFieldPathConfig().getValue());

        List<AttachmentModel> savedAttachments = new ArrayList<AttachmentModel>();

        ObjectsNode node = context.getFormObjects().getObjects(fieldPath);
        Iterator<DomainObject> iterator = node.iterator();
        while (iterator.hasNext()) {
            DomainObject temp = iterator.next();
            AttachmentModel attachmentModel = new AttachmentModel();
           for (String field : temp.getFields()) {
                if (ATTACHMENT_NAME.equalsIgnoreCase(field)){
                    attachmentModel.setName(temp.getValue(field).toString());
                }
               if (ATTACHMENT_DESCRIPTION.equalsIgnoreCase(field)){
                   attachmentModel.setDescription(temp.getValue(field).toString());
               }
               attachmentModel.setId(temp.getId());

            }
            savedAttachments.add(attachmentModel);
        }

        AttachmentBoxState result = new AttachmentBoxState();
        result.setAttachments(savedAttachments);

        return result;
    }
    public void saveNewObjects(WidgetContext context, WidgetState state) {
        AttachmentBoxState attachmentBoxState = (AttachmentBoxState) state;
        List<AttachmentModel> attachmentModels = attachmentBoxState.getAttachments();
        DomainObject domainObject = context.getFormObjects().getRootObjects().getObject();

        AttachmentBoxConfig widgetConfig = context.getWidgetConfig();
        String attachmentType = widgetConfig.getAttachmentType().getName();
        FieldPath fieldPath = new FieldPath(widgetConfig.getFieldPathConfig().getValue());
        String parentLinkFieldName  =  fieldPath.getLastElement().split("\\^")[1];

        for (AttachmentModel attachmentModel : attachmentModels) {
            if ( attachmentModel.getId() != null) {
                continue;
            }

            InputStream fileData = null;
            RemoteInputStreamServer remoteFileData = null;
            try {
                Properties props = PropertiesLoaderUtils.loadAllProperties("deploy.properties");
                String attachmentStorage = props.getProperty("attachment.save.location");
                fileData = new FileInputStream(attachmentStorage + attachmentModel.getTemporaryName());
                remoteFileData = new SimpleRemoteInputStream(fileData);
                DomainObject attachmentDomainObject = attachmentService.
                        createAttachmentDomainObjectFor(domainObject.getId(),attachmentType);
                attachmentDomainObject.setValue(ATTACHMENT_NAME, new StringValue(attachmentModel.getName()));
                attachmentDomainObject.setValue(ATTACHMENT_DESCRIPTION, new StringValue(attachmentModel.
                        getDescription()));

                attachmentDomainObject.setReference(parentLinkFieldName, domainObject );
                attachmentService.saveAttachment(remoteFileData,attachmentDomainObject);
            } catch (FileNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }  finally {
                close(remoteFileData);

            }

        }
    }
    private  void close(Closeable c) {
        if (c == null) return;
        try {
            c.close();
        } catch (IOException e) {

        }
        catch (Exception e) {

        }
    }
}
