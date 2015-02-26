package ru.intertrust.cm.crypto;

import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bouncycastle.asn1.DEREncodable;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.X509CertificateStructure;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.tsp.TimeStampTokenInfo;
import org.bouncycastle.util.Store;

public class CAdESSigner {

    public static final Integer CAdES_BES = 0;
    public static final Integer CAdES_X_Long_Type_1 = 1;
    public static final Integer PKCS7 = 6;
    public static final Integer CAdES_Unknown = -1;

    private X509Certificate certificate;
    private Store store;
    private SignerInformation signer;
    private Integer signatureType;
    private TimeStampToken signatureTimestampToken;
    private TimeStampToken cadESCTimestampToken;
    private CertificateVerifier certificateVerifier = new CertificateVerifier();

    public CAdESSigner(Store store, SignerInformation signer) throws CertificateParsingException, CertificateEncodingException {
        this.store = store;
        this.signer = signer;
        signatureType = getCAdESSignatureType(signer);
    }

    private X509Certificate findCertificate(Store store, SignerInformation signer) throws CertificateParsingException, CertificateEncodingException,
            IOException {
        X509Certificate result = null;
        //Сначала ищем сертификат в обьекте контейнера подписи
        Collection signedDataCerts = store.getMatches(signer.getSID());
        if (!signedDataCerts.isEmpty()) {
            X509CertificateHolder ch = (X509CertificateHolder) signedDataCerts.iterator().next();
            result = new X509CertificateObject(ch.toASN1Structure());
        }

        //Если не найдено ищем в атрибуте самой подписи
        if (result == null) {
            if (signer.getUnsignedAttributes() != null
                    && signer.getUnsignedAttributes().get(PKCSObjectIdentifiers.id_aa_ets_certValues) != null) {

                DERSequence seq = (DERSequence) signer.getUnsignedAttributes()
                        .get(PKCSObjectIdentifiers.id_aa_ets_certValues).getAttrValues().getObjectAt(0);

                for (int i = 0; i < seq.size(); i++) {
                    X509CertificateStructure cs = X509CertificateStructure.getInstance(seq.getObjectAt(i));
                    X509Certificate certificate = new X509CertificateObject(cs);
                    if (signer.getSID().match(certificate)) {
                        result = certificate;
                        break;
                    }
                }
            }
        }

        return result;
    }

    public void verify() throws CertificateEncodingException, OperatorCreationException, CertificateException, IOException, CMSException, TSPException {

        certificate = findCertificate(store, signer);

        certificateVerifier.verifyCertificate(certificate, getAllCertificates(store, signer));

        ContentVerifierProvider contentVerifierProvider =
                new GostContentVerifierProviderBuilder(new DefaultDigestAlgorithmIdentifierFinder()).build(new X509CertificateHolder(certificate
                        .getEncoded()));

        SignerInformationVerifier signerInformationVerifier =
                new SignerInformationVerifier(contentVerifierProvider, new CAdESDigestCalculatorProvider());

        if (!signer.verify(signerInformationVerifier)) {
            throw new CMSException("Signature is invalid");
        }

        verifyTimeStamp();
    }

    private Set<X509Certificate> getAllCertificates(Store store, SignerInformation signer) throws CertificateParsingException {
        Set<X509Certificate> result = new HashSet<X509Certificate>();
        //Сначала ищем сертификат в обьекте контейнера подписи
        Collection signedDataCerts = store.getMatches(signer.getSID());
        if (!signedDataCerts.isEmpty()) {
            X509CertificateHolder ch = (X509CertificateHolder) signedDataCerts.iterator().next();
            result.add(new X509CertificateObject(ch.toASN1Structure()));
        }

        if (signer.getUnsignedAttributes() != null
                && signer.getUnsignedAttributes().get(PKCSObjectIdentifiers.id_aa_ets_certValues) != null) {

            DERSequence seq = (DERSequence) signer.getUnsignedAttributes()
                    .get(PKCSObjectIdentifiers.id_aa_ets_certValues).getAttrValues().getObjectAt(0);

            for (int i = 0; i < seq.size(); i++) {
                X509CertificateStructure cs = X509CertificateStructure.getInstance(seq.getObjectAt(i));
                X509Certificate certificate = new X509CertificateObject(cs);
                if (signer.getSID().match(certificate)) {
                    result.add(certificate);
                }
            }
        }

        return result;
    }

    private void verifyTimeStamp() throws CMSException, IOException, TSPException, CertificateEncodingException, OperatorCreationException,
            CertificateParsingException {
        signatureTimestampToken = verifyTimeStampAttr(signer.getUnsignedAttributes().get(PKCSObjectIdentifiers.id_aa_signatureTimeStampToken));
        cadESCTimestampToken = verifyTimeStampAttr(signer.getUnsignedAttributes().get(PKCSObjectIdentifiers.id_aa_ets_escTimeStamp));
    }

    private TimeStampToken verifyTimeStampAttr(Attribute timeStampAttr) throws CMSException, IOException, TSPException, CertificateEncodingException,
            OperatorCreationException, CertificateParsingException {
        TimeStampToken result = null;
        if (timeStampAttr != null) {
            DEREncodable dob = timeStampAttr.getAttrValues().getObjectAt(0);
            CMSSignedData signedData = new CMSSignedData(dob.getDERObject().getEncoded());
            result = new TimeStampToken(signedData);
            //TimeStampTokenInfo tstInfo = result.getTimeStampInfo();

            for (Object signerObj : signedData.getSignerInfos().getSigners()) {
                SignerInformation signer = (SignerInformation) signerObj;
                X509Certificate cer = findCertificate(signedData.getCertificates(), signer);

                
                certificateVerifier.verifyCertificate(cer, getAllCertificates(store, signer));
                ContentVerifierProvider contentVerifierProvider =
                        new GostContentVerifierProviderBuilder(new DefaultDigestAlgorithmIdentifierFinder()).build(new X509CertificateHolder(cer
                                .getEncoded()));

                SignerInformationVerifier signerInformationVerifier =
                        new SignerInformationVerifier(contentVerifierProvider, new CAdESDigestCalculatorProvider());

                result.validate(signerInformationVerifier);
            }
        }
        return result;
    }

    public X509Certificate getCertificate() {
        return certificate;
    }

    public TimeStampToken getSignatureTimestampToken() {
        return signatureTimestampToken;
    }

    public TimeStampToken getCAdESCTimestampToken() {
        return cadESCTimestampToken;
    }

    public Integer getCAdESSignatureType(SignerInformation signerinformation)
    {
        Object certificateRefsArrt;
        Attribute signingCertificateAttribute;
        if ((certificateRefsArrt = signerinformation.getSignedAttributes()) == null)
            return CAdES_Unknown;

        signingCertificateAttribute = null;
        try {
            signingCertificateAttribute = signerinformation.getSignedAttributes().get(PKCSObjectIdentifiers.id_aa_signingCertificateV2);
        } catch (Exception ignoreEx) {
        }

        if (signingCertificateAttribute == null) {

            try {
                signingCertificateAttribute = signerinformation.getSignedAttributes().get(PKCSObjectIdentifiers.id_aa_signingCertificate);
            } catch (Exception ignoreEx) {
            }
        }

        if (signingCertificateAttribute == null) {
            try {
                signingCertificateAttribute = signerinformation.getSignedAttributes().get(PKCSObjectIdentifiers.id_aa_otherSigCert);
            } catch (Exception ignoreEx) {
            }
        }

        if (signingCertificateAttribute == null)
            return PKCS7;

        Attribute etsCertValuesAttr;
        Attribute etsRevocationValuesAttr;
        Attribute etsEscTimeStampAttr;

        if ((signerinformation.getUnsignedAttributes()) == null)
            return CAdES_BES;

        Attribute timeStamp = null;
        certificateRefsArrt = null;
        signingCertificateAttribute = null;
        etsCertValuesAttr = null;
        etsRevocationValuesAttr = null;
        etsEscTimeStampAttr = null;
        try {
            timeStamp = signerinformation.getUnsignedAttributes().get(PKCSObjectIdentifiers.id_aa_signatureTimeStampToken);
            certificateRefsArrt = signerinformation.getUnsignedAttributes().get(PKCSObjectIdentifiers.id_aa_ets_certificateRefs);
            signingCertificateAttribute = signerinformation.getUnsignedAttributes().get(PKCSObjectIdentifiers.id_aa_ets_revocationRefs);
            etsCertValuesAttr = signerinformation.getUnsignedAttributes().get(PKCSObjectIdentifiers.id_aa_ets_certValues);
            etsRevocationValuesAttr = signerinformation.getUnsignedAttributes().get(PKCSObjectIdentifiers.id_aa_ets_revocationValues);
            etsEscTimeStampAttr = signerinformation.getUnsignedAttributes().get(PKCSObjectIdentifiers.id_aa_ets_escTimeStamp);
        } catch (Exception ignoreEx) {
        }

        if (timeStamp == null && certificateRefsArrt == null && signingCertificateAttribute == null && etsCertValuesAttr == null
                && etsRevocationValuesAttr == null && etsEscTimeStampAttr == null)
            return CAdES_BES;
        if (timeStamp != null && certificateRefsArrt != null && signingCertificateAttribute != null && etsCertValuesAttr != null
                && etsRevocationValuesAttr != null && etsEscTimeStampAttr != null)
            return CAdES_X_Long_Type_1;
        else
            return CAdES_Unknown;
    }

    public Integer getSignatureType() {
        return signatureType;
    }

}
