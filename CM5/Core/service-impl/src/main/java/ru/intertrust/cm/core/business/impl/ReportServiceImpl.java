package ru.intertrust.cm.core.business.impl;

import com.healthmarketscience.rmiio.DirectRemoteInputStream;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.business.api.DataSourceContext;
import ru.intertrust.cm.core.business.api.ReportService;
import ru.intertrust.cm.core.business.api.ReportServiceAdmin;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.ReportResult;
import ru.intertrust.cm.core.config.model.ReportMetadataConfig;
import ru.intertrust.cm.core.config.model.ReportParameterData;
import ru.intertrust.cm.core.config.model.ReportParametersData;
import ru.intertrust.cm.core.dao.access.AccessToken;
import ru.intertrust.cm.core.dao.api.CurrentDataSourceContext;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.ExtensionService;
import ru.intertrust.cm.core.dao.api.extension.AfterGenerateReportExtentionHandler;
import ru.intertrust.cm.core.model.ReportServiceException;
import ru.intertrust.cm.core.report.ReportServiceBase;

import javax.ejb.*;
import javax.interceptor.Interceptors;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Имплементация сервиса генерации отчетов
 * @author larin
 * 
 */
@Stateless(name = "ReportService")
@Local(ReportService.class)
@Remote(ReportService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class ReportServiceImpl extends ReportServiceBase implements ReportService {

    private static final String DATE_PATTERN = "dd_MM_yyyy HH_mm_ss";

    private static final Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);

    //private static final String TEMPLATES_FOLDER_NAME = "report-service-templates";
    //private static final String RESULT_FOLDER_NAME = "report-service-results";

    public static final String PDF_FORMAT = "PDF";
    public static final String RTF_FORMAT = "RTF";
    public static final String XLS_FORMAT = "XLS";
    public static final String HTML_FORMAT = "HTML";
    public static final String DOCX_FORMAT = "DOCX";
    public static final String XLSX_FORMAT = "XLSX";

    @Autowired
    private CurrentUserAccessor currentUserAccessor;

    @Autowired
    private CurrentDataSourceContext currentDataSourceContext;

    @Autowired
    protected ReportTemplateCache templateCache;

    @Autowired
    private ExtensionService extensionService;
    
    @org.springframework.beans.factory.annotation.Value("${default.report.format:PDF}")
    private String defaultReportFormat;

    @EJB
    private ReportResultBuilder resultBuilder;

    public ReportResult generate(String name, Map<String, Object> parameters, DataSourceContext dataSource) {
        return generate(name, parameters, null, dataSource);
    }

    @Asynchronous
    public Future<ReportResult> generateAsync(String name, Map<String, Object> parameters, DataSourceContext dataSource) {
        return generateAsync(name, parameters);
    }

    @Override
    @Asynchronous
    public Future<ReportResult> generateAsync(String name, Map<String, Object> parameters) {
        ReportResult result = generate(name, parameters);
        return new AsyncResult<>(result);
    }

    @Override
    public ReportResult generate(String name, Map<String, Object> parameters) {
        return generate(name, parameters, (DataSourceContext) null);
    }

    /**
     * Формирование отчета
     */
    @Override
    public ReportResult generate(String name, Map<String, Object> parameters, Integer keepDays) {
        return generate(name, parameters, keepDays, null);
    }

    /**
     * Формирование отчета
     */
    public ReportResult generate(String name, Map<String, Object> parameters, Integer keepDays, DataSourceContext dataSource) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing Report, name: " + name + ". Parameters list " + parameters);
        }
        try {
            // Получение доменного объекта шаблона отчета
            DomainObject reportTemplate = getReportTemplateObject(name);

            //Получение директории с шаблонами отчета
            File templateFolder = templateCache.getTemplateFolder(reportTemplate);

            //Получение метаинформацию отчета
            ReportMetadataConfig reportMetadata = loadReportMetadata(
                    readFile(new File(templateFolder, ReportServiceAdmin.METADATA_FILE_MAME)));

            //Формирование отчета
            // todo: this method should accept DataSource and if it's MASTER - support transaction
            File result = resultBuilder.generateReport(reportMetadata, templateFolder, parameters, dataSource);
            if (logger.isDebugEnabled()) {
                logger.debug("Generated Report, name: " + name + ". Parameters list " + parameters);
            }

            //Вызов точки расширения после генерации отчета
            //Сначала для точек расширения у которых указан фильтр
            AfterGenerateReportExtentionHandler extentionHandler = 
                    extensionService.getExtentionPoint(AfterGenerateReportExtentionHandler.class, name);
            extentionHandler.onAfterGenerateReport(name, parameters, result);
            //После для точек расширения у которых не указан фильтр
            extentionHandler = extensionService.getExtentionPoint(AfterGenerateReportExtentionHandler.class, "");
            extentionHandler.onAfterGenerateReport(name, parameters, result);

            if (logger.isDebugEnabled()) {
                logger.debug("Saving Report, name: " + name + ". Parameters list " + parameters);
            }
            //Сохранеие результата в хранилище
            Id resultId = saveResult(reportMetadata, result, reportTemplate, parameters, keepDays);

            //Формироание результата
            if (logger.isDebugEnabled()) {
                logger.debug("Creating Report result, name: " + name + ". Parameters list " + parameters);
            }
            ReportResult reportResult = new ReportResult();
            reportResult.setFileName(result.getName());
            final byte[] report = readFile(result); // todo: RE-IMPLEMENT, files can be huge and should not get into RAM
            if (logger.isDebugEnabled()) {
                logger.debug("Report file read, size: " + report.length / 1024 / 1024 + "MB, name: " + name + ". Parameters list " + parameters);
            }
            reportResult.setReport(report);
            reportResult.setTemplateName(name);
            reportResult.setResultId(resultId);

            //Удаляем временный файл
            result.delete();

            return reportResult;
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new ReportServiceException("Error on generate report", ex);
        } finally {
            if (logger.isDebugEnabled()) {
                logger.debug("Report built, name: " + name + ". Parameters list " + parameters);
            }
        }
    }

    private Id saveResult(ReportMetadataConfig reportMetadata, File result, DomainObject template,
            Map<String, Object> params, Integer keepDays) throws Exception {
        Id resultId = null;
        if ((keepDays != null && keepDays > 0) || (reportMetadata.getKeepDays() != null && reportMetadata.getKeepDays() > 0)) {
            AccessToken accessToken = accessControlService.createSystemAccessToken(this.getClass().getName());

            //Создаем объект
            DomainObject reportResult = createDomainObject("report_result");
            reportResult.setString("name", result.getName());
            reportResult.setReference("template_id", template.getId());
            reportResult.setReference("owner", currentUserAccessor.getCurrentUserId());
            Calendar calendar = Calendar.getInstance();
            if (keepDays != null && keepDays > 0) {
                calendar.add(Calendar.DAY_OF_MONTH, keepDays);
            } else {
                calendar.add(Calendar.DAY_OF_MONTH, reportMetadata.getKeepDays());
            }
            reportResult.setTimestamp("keep_to", calendar.getTime());
            reportResult = domainObjectDao.save(reportResult, accessToken);
            resultId = reportResult.getId();

            //Сохраняем результат как вложение вложения
            DomainObject reportAttachment =
                    attachmentService.createAttachmentDomainObjectFor(reportResult.getId(), "report_result_attachment");
            reportAttachment.setString("name", "report");

            ByteArrayInputStream bis = new ByteArrayInputStream(readFile(result));
            DirectRemoteInputStream directRemoteInputStream = new DirectRemoteInputStream(bis, false);

            attachmentService.saveAttachment(directRemoteInputStream, reportAttachment);

            //Сохраняем параметры как вложение
            if (params != null) {
                DomainObject paramAttachment =
                        attachmentService.createAttachmentDomainObjectFor(reportResult.getId(), "report_result_attachment");
                paramAttachment.setString("name", "params");

                bis = new ByteArrayInputStream(getParametersAsByteArray(params));
                directRemoteInputStream = new DirectRemoteInputStream(bis, false);

                attachmentService.saveAttachment(directRemoteInputStream, paramAttachment);
            }
        }
        return resultId;
    }

    private byte[] getParametersAsByteArray(Map<String, Object> params) throws Exception {
        ReportParametersData data = new ReportParametersData();
        data.setParameters(new ArrayList<ReportParameterData>());

        for (String name : params.keySet()) {
            //TODO в зависимости от того как будут использовать файл с параметрами возможно
            //потребуется сохранять значения не как строки а как конкретные объекты
            data.getParameters().add(new ReportParameterData(name, params.get(name).toString()));
        }

        Serializer serializer = new Persister();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        serializer.write(data, stream);

        return stream.toByteArray();
    }
/*
    private File getTemplateFolder(DomainObject reportTemplateDo) throws IOException {
        //Проверка есть директория для данного отчета в файловой системе, и если есть то проверка даты ее создания
        //Получение temp директории
        File tempFolder = getTempFolder();

        //Получение директории с шаблонами
        File templatesFolder = new File(tempFolder, TEMPLATES_FOLDER_NAME);
        File templateFolder = new File(templatesFolder, reportTemplateDo.getString("name"));
        boolean dirCreated = false;
        if (!templateFolder.exists()) {
            templateFolder.mkdirs();
            dirCreated = true;
        }

        //Сравнение даты изменения директории и даты создания доменного объекта шаблонов отчета 
        if (dirCreated || templateFolder.lastModified() < reportTemplateDo.getModifiedDate().getTime()) {
            //Шаблоны требуют перезачитывания
            //Удаляем все содержимое папки
            File[] files = templateFolder.listFiles();
            for (File file : files) {
                file.delete();
            }

            //Получение всех вложений
            List<DomainObject> attachments = getAttachments("report_template_attach", reportTemplateDo);
            for (DomainObject attachment : attachments) {
                byte[] content = getAttachmentContent(attachment);
                //Запись файла на диск
                writeToFile(content, new File(templateFolder, attachment.getString("Name")));
            }
            templateFolder.setLastModified(System.currentTimeMillis());
        }
        return templateFolder;

    }
*/

    private File getResultFolder() throws IOException {
        /*File resultFolder = new File(getTempFolder(), RESULT_FOLDER_NAME);
        if (!resultFolder.exists()) {
            resultFolder.mkdirs();
        }
        return resultFolder;*/
        return getTempFolder();
    }

    /**
     * Получение формата отчета 
     * @param reportMetadata
     * @param params
     * @return
     */
    private String getFormat(ReportMetadataConfig reportMetadata, Map<String, Object> params) {
        
        String format = null;
        //Если формат задан в шаблоне и он только один - то применяем его
        if (reportMetadata.getFormats() != null && reportMetadata.getFormats().size() == 1){
            format = reportMetadata.getFormats().get(0);
        }else{
            //Если задано несколько форматов то сначала применяем формат из параметра а если там не задан берем формат по умолчанию
            if (params != null) {
                format = (String) params.get(FORMAT_PARAM);
            }
            
            //Берем формат по умолчанию
            if (format == null){
                format = defaultReportFormat;
            }
            
            //Проверяем есть ли такой формат в списке поддерживаемых форматов
            if (!reportMetadata.getFormats().contains(format)) {
                throw new ReportServiceException("FORMAT parameter or default report format is not admissible. Need "
                        + reportMetadata.getFormats());
            }
        }

        return format;
    }

    private Connection getConnection() throws ClassNotFoundException, SQLException {
        String connectionString = null;
        connectionString = "jdbc:sochi:local";

        //Загрузка драйвера
        Class.forName("ru.intertrust.cm.core.jdbc.JdbcDriver");
        // Получение соединения с базой данных
        return DriverManager.getConnection(connectionString);
    }

}
