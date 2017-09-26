package ru.intertrust.cm.core.business.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.intertrust.cm.core.business.api.AttachmentService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.GlobalServerSettingsService;
import ru.intertrust.cm.core.business.api.crypto.CryptoBean;
import ru.intertrust.cm.core.business.api.crypto.CryptoService;
import ru.intertrust.cm.core.business.api.crypto.SignatureDataService;
import ru.intertrust.cm.core.business.api.crypto.SignatureStorageService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.crypto.DocumentVerifyResult;
import ru.intertrust.cm.core.business.api.dto.crypto.VerifyResult;
import ru.intertrust.cm.core.business.api.util.ObjectCloner;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.crypto.CAdESCryptoSettingsConfig;
import ru.intertrust.cm.core.config.crypto.CryptoSettingsConfig;
import ru.intertrust.cm.core.config.crypto.SignedDataItem;
import ru.intertrust.cm.core.config.crypto.SignedResultItem;
import ru.intertrust.cm.core.config.crypto.TypeCryptoSettingsConfig;
import ru.intertrust.cm.core.config.event.ConfigurationUpdateEvent;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.core.model.SystemException;
import ru.intertrust.cm.core.model.UnexpectedException;

@Stateless(name = "CryptoService")
@Local(CryptoService.class)
@Remote(CryptoService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class CryptoServiceImpl implements CryptoService, ApplicationListener<ConfigurationUpdateEvent> {

    @Autowired
    private ConfigurationExplorer configurationExplorer;
    @Autowired
    private ApplicationContext context;
    @Autowired
    private DomainObjectTypeIdCache domainObjectTypeIdCache;
    @Autowired
    private CrudService crudService;
    @Autowired
    private GlobalServerSettingsService globalServerSettingsService;

    private CryptoBean cryptoBean;
    private Map<String, TypeCryptoSettingsConfig> typeCryptoConfigs = new Hashtable<String, TypeCryptoSettingsConfig>();

    @PostConstruct
    private void init() {
        if (configurationExplorer.getGlobalSettings() != null &&
                configurationExplorer.getGlobalSettings().getCryptoSettingsConfig() != null) {
            String serverSignatureVerifierBeanName = configurationExplorer.getGlobalSettings().getCryptoSettingsConfig().getServerComponentName();
            cryptoBean = (CryptoBean) context.getBean(serverSignatureVerifierBeanName);
        }
    }

    @Override
    public VerifyResult verify(InputStream document) {
        try {
            return cryptoBean.verify(document);
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new UnexpectedException("CryptoServiceImpl", "verify", "document:" + document, ex);
        }
    }

    @Override
    public VerifyResult verify(InputStream document, byte[] signature) {
        try {
            return cryptoBean.verify(document, signature);
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new UnexpectedException("CryptoServiceImpl", "verify", "document:" + document + ", signature:" + signature, ex);
        }
    }

    @Override
    public VerifyResult verify(InputStream document, byte[] signature, byte[] signerSertificate) {
        try {
            return cryptoBean.verify(document, signature, signerSertificate);
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new UnexpectedException("CryptoServiceImpl", "verify", "document:" + document + ", signature:" +
                    signature + ", signerSertificate:" + signerSertificate, ex);
        }
    }

    @Override
    public List<DocumentVerifyResult> verify(Id documentId) {
        String rootType = domainObjectTypeIdCache.getName(documentId);
        TypeCryptoSettingsConfig rootConfig = getTypeCryptoSettingsConfig(rootType);
        InputStream contentStream = null;
        try {
            if (rootConfig != null) {
                List<DocumentVerifyResult> result = new ArrayList<DocumentVerifyResult>();
                
                List<Id> signedDocuments = getBatchForSignature(documentId);
                //Цикл по подписанным документам в пачке
                for (Id id : signedDocuments) {
                    DocumentVerifyResult verifyResult = new DocumentVerifyResult();
                    verifyResult.setDocumentId(id);
                    DomainObject signedDo = crudService.find(id);
                    
                    //Получение всех подписей
                    String signedType = domainObjectTypeIdCache.getName(id);
                    TypeCryptoSettingsConfig config = getTypeCryptoSettingsConfig(signedType);
                    String signatureStorageBeanName = config.getSignatureStorageBeanName();
                    SignatureStorageService signatureStorageService = (SignatureStorageService) context.getBean(signatureStorageBeanName);
                    List<SignedResultItem> signes = signatureStorageService.loadSignature(config.getSignatureStorageBeanSettings(), id);                    
                    
                    //Цикл по всем подписям
                    for (SignedResultItem signedResultItem : signes) {
                        if (signedResultItem.getId().equals(id)){
                            SignedDataItem signedDataItem = getContentForSignature(id); 
                            contentStream = signedDataItem.getContent();
                            verifyResult.setDocumentName(signedDataItem.getName());
                            
                            VerifyResult oneResult = cryptoBean.verify(contentStream, Base64.decodeBase64(signedResultItem.getSignature()));
                            verifyResult.getSignerInfos().addAll(oneResult.getSignerInfos());
                            contentStream.close();
                            contentStream = null;
                        }
                    }
                    result.add(verifyResult);
                }

                return result;
            } else {
                throw new FatalException("Type crypto settings not found for type " + rootType);
            }
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new FatalException("Error verify attachments signature", ex);
        } finally {
            try {
                if (contentStream != null) {
                    contentStream.close();
                }
            } catch (Exception ignoreEx) {
            }
        }
    }

    @Override
    public CryptoSettingsConfig getCryptoSettingsConfig() {
        try {
            //Получаем глобальные настройки
            CryptoSettingsConfig globalCryptoSettingsConfig = configurationExplorer.getGlobalSettings().getCryptoSettingsConfig();
            
            if (globalCryptoSettingsConfig == null){
                throw new FatalException("Crypto Settings not configured in global config");
            }
            
            CryptoSettingsConfig result = ObjectCloner.getInstance().cloneObject(globalCryptoSettingsConfig);
            //Заменяем параметры из глобальных настроек.
            
            //Место хэширования, поддерживаются true и false
            Boolean hashOnServer = globalServerSettingsService.getBoolean(CryptoService.HASH_ON_SERVER);
            if (hashOnServer != null){
                result.setHashOnServer(hashOnServer);
            }
            
            if (result.getSettings() instanceof CAdESCryptoSettingsConfig){
                CAdESCryptoSettingsConfig providerConfig = (CAdESCryptoSettingsConfig)result.getSettings();
                
                //Тип подписи. Поддерживаются CAdES-X b CAdES-BES
                String signatureType = globalServerSettingsService.getString(CryptoService.SIGNATURE_TYPE);
                if (signatureType != null){
                    providerConfig.setSignatureType(signatureType);
                } 
                if (providerConfig.getSignatureType() == null){
                    providerConfig.setSignatureType(CAdESCryptoSettingsConfig.CADES_BES_SIGNATURE_TYPE);
                }
             
                //Алгоритм хэшировапния
                String hashAlgorithm = globalServerSettingsService.getString(CryptoService.HASH_ALGORITHM);
                if (hashAlgorithm != null){
                    providerConfig.setHashAlgorithm(hashAlgorithm);
                } 
                if (providerConfig.getHashAlgorithm() == null){
                    providerConfig.setHashAlgorithm(HASH_ALGORITHM_GOST_3411_2012_256);
                }                
                
                //Сервер штампов времени
                String timeStampServer = globalServerSettingsService.getString(CryptoService.TIME_STAMP_SERVER);
                if (timeStampServer != null){
                    providerConfig.setTsAddress(timeStampServer);;
                } 
            }

            return result;
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new UnexpectedException("CryptoServiceImpl getCryptoSettingsConfig", ex);
        }
    }

    private TypeCryptoSettingsConfig getTypeCryptoSettingsConfig(String rootTypeName) {
        //Ищем сначала в кэше
        TypeCryptoSettingsConfig typeSignatureConfig = typeCryptoConfigs.get(rootTypeName);

        if (typeSignatureConfig == null) {
            //Ищем непосредственно для типа
            typeSignatureConfig = configurationExplorer.getConfig(TypeCryptoSettingsConfig.class, rootTypeName);

            //Ищем для родительских типов
            if (typeSignatureConfig == null) {
                String parentType = rootTypeName;
                while (parentType != null && typeSignatureConfig == null) {
                    parentType = configurationExplorer.getDomainObjectParentType(parentType);
                    if (parentType != null) {
                        typeSignatureConfig = configurationExplorer.getConfig(TypeCryptoSettingsConfig.class, parentType);
                    }
                }
            }

            //Ищем конфигурацию по умолчанию с name="*"
            if (typeSignatureConfig == null) {
                typeSignatureConfig = configurationExplorer.getConfig(TypeCryptoSettingsConfig.class, "*");
            }

            //Записываем в кэш
            if (typeSignatureConfig != null) {
                typeCryptoConfigs.put(rootTypeName, typeSignatureConfig);
            }
        }

        return typeSignatureConfig;
    }

    @Override
    public void saveSignedResult(SignedResultItem signedResult) {
        try {
            String signedType = domainObjectTypeIdCache.getName(signedResult.getId());
            TypeCryptoSettingsConfig config = getTypeCryptoSettingsConfig(signedType);

            if (config != null) {
                String signatureStorageBeanName = config.getSignatureStorageBeanName();
                SignatureStorageService signatureResultService = (SignatureStorageService) context.getBean(signatureStorageBeanName);
                signatureResultService.saveSignature(config.getSignatureStorageBeanSettings(), signedResult);
            } else {
                throw new FatalException("Type crypto settings not found for type " + signedType);
            }
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new UnexpectedException("CryptoServiceImpl", "saveSignedResult", "signedResult:" + signedResult, ex);
        }
    }

    @Override
    public void onApplicationEvent(ConfigurationUpdateEvent event) {
        try {
            //Очищаем кэш
            typeCryptoConfigs.clear();
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new UnexpectedException("CryptoServiceImpl", "onApplicationEvent", "event:" + event, ex);
        }
    }

    @Override
    public byte[] hash(InputStream document) {
        try {
            return cryptoBean.hash(document);
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new UnexpectedException("CryptoServiceImpl", "hash", "document:" + document, ex);
        }
    }

    @Override
    public SignedDataItem getContentForSignature(Id id) {
        try {
            String signedType = domainObjectTypeIdCache.getName(id);
            TypeCryptoSettingsConfig config = getTypeCryptoSettingsConfig(signedType);
            SignedDataItem result = null;
            if (config != null) {
                String getContentBeanName = config.getGetContentBeanName();
                SignatureDataService signatureDataService = (SignatureDataService) context.getBean(getContentBeanName);
                result = signatureDataService.getContentForSignature(config.getGetContentBeanSettings(), id);
            } else {
                throw new FatalException("Type crypto settings not found for type " + signedType);
            }
            return result;
        } catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new UnexpectedException("CryptoServiceImpl", "getContentForSignature", "id:" + id, ex);
        }
    }

    @Override
    public List<Id> getBatchForSignature(Id rootId) {
        try {
            String signedType = domainObjectTypeIdCache.getName(rootId);
            TypeCryptoSettingsConfig config = getTypeCryptoSettingsConfig(signedType);
            List<Id> result = null;
            if (config != null) {
                String getContentBeanName = config.getGetContentBeanName();
                SignatureDataService signatureDataService = (SignatureDataService) context.getBean(getContentBeanName);
                result = signatureDataService.getBatchForSignature(config.getGetContentBeanSettings(), rootId);
            } else {
                throw new FatalException("Type crypto settings not found for type " + signedType);
            }
            return result;
        }  catch (SystemException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new UnexpectedException("CryptoServiceImpl", "getBatchForSignature", "rootId:" + rootId, ex);
        }
    }

}
