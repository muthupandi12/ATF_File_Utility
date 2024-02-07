package com.bijlipay.ATFFileGeneration.Util;

import com.bijlipay.ATFFileGeneration.ServiceImpl.AtfFileServiceImpl;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;

public class JWEUtilsForPartners {


    private static final Logger logger = LoggerFactory.getLogger(JWEUtilsForPartners.class);

    /**
     *
     * @param alg
     * @param contentKeyEncMethod
     * @param publicKey
     * @param payload
     * @return
     * @throws JOSEException
     */
    private static String jweEncrypt(JWEAlgorithm alg, EncryptionMethod contentKeyEncMethod, RSAPublicKey publicKey,
                                     String payload) throws JOSEException, NoSuchAlgorithmException {

// Generate the preset Content Encryption (CEK) key
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(contentKeyEncMethod.cekBitLength());
        SecretKey cek = keyGenerator.generateKey();

        JWEHeader jewHeader = new JWEHeader(alg, contentKeyEncMethod);

// Encrypt the JWE with the RSA public key + specified AES CEK
        JWEObject jwe = new JWEObject(jewHeader, new Payload(payload));
        jwe.encrypt(new RSAEncrypter(publicKey));
        String jweString = jwe.serialize();
        return jweString;
    }


    private static String jweDecrypt(RSAPrivateKey privateKey, String jweEncryptedPayload)
            throws ParseException, JOSEException {

        JWEObject jwe = JWEObject.parse(jweEncryptedPayload);
        jwe.decrypt(new RSADecrypter(privateKey));
        String decryptedValue = jwe.getPayload().toString();
        return decryptedValue;
    }

    private static String jwSign(RSAPrivateKey privateKey, String payloadToSign) throws JOSEException {
// Create RSA-signer with the private key
        JWSSigner signer = new RSASSASigner(privateKey);
// Prepare JWS object with simple string as payload
        JWSObject jwsObject = new JWSObject(
                new JWSHeader(JWSAlgorithm.RS256),
                new Payload(payloadToSign));

// Compute the RSA signature
        jwsObject.sign(signer);
// To serialize to compact form, produces something like
// eyJhbGciOiJSUzI1NiJ9.SW4gUlNBIHdlIHRydXN0IQ.IRMQENi4nJyp4er2L
// mZq3ivwoAjqa1uUkSBKFIX7ATndFF5ivnt-m8uApHO4kfIFOrW7w2Ezmlg3Qd
// maXlS9DhN0nUk_hGI3amEjkKd0BWYCB8vfUbUv0XGjQip78AI4z1PrFRNidm7
// -jPDm5Iq0SZnjKjCNS5Q15fokXZc8u0A
        return(jwsObject.serialize());
    }

    private static JWVerifyObject jwSignatureVerify(RSAPublicKey publicKey , String signedPayloadToVerify) throws JOSEException, ParseException {
        JWSObject jwsObject = JWSObject.parse(signedPayloadToVerify);
        JWSVerifier verifier = new RSASSAVerifier(publicKey);
        boolean isSignatureValid =  jwsObject.verify(verifier);
        JWVerifyObject jwverifyObject = new JWVerifyObject();
        jwverifyObject.setSignatureValid(isSignatureValid);
        if(isSignatureValid) {
            jwverifyObject.setPayloadAfterVerification(jwsObject.getPayload().toString());
        }
        return jwverifyObject;
    }





    public static String jweEncryptAndSign(RSAPublicKey publicKeyToEncrypt, RSAPrivateKey privateKeyToSign, String payloadToEncryptAndSign)
            throws NoSuchAlgorithmException, CertificateException, FileNotFoundException, KeyStoreException,
            IOException, UnrecoverableKeyException, JOSEException {
        JWEAlgorithm alg = JWEAlgorithm.RSA_OAEP_256;
        EncryptionMethod enc = EncryptionMethod.A256GCM;
        String encryptedResult = jweEncrypt(alg, enc, publicKeyToEncrypt, payloadToEncryptAndSign);
//        logger.info("Encrypted Out --{}",encryptedResult);
        String signedResult = jwSign(privateKeyToSign, encryptedResult);
        return signedResult;
    }

//    public static String jweEncryptAndSign(RSAPublicKey publicKeyToEncrypt,String payloadToEncryptAndSign)
//            throws NoSuchAlgorithmException, CertificateException, FileNotFoundException, KeyStoreException,
//            IOException, UnrecoverableKeyException, JOSEException {
//        JWEAlgorithm alg = JWEAlgorithm.RSA_OAEP_256;
//        EncryptionMethod enc = EncryptionMethod.A256GCM;
//        String encryptedResult = jweEncrypt(alg, enc, publicKeyToEncrypt, payloadToEncryptAndSign);
//        logger.info("Encrypted Out --{}",encryptedResult);
//        return encryptedResult;
//    }

    public static String jweVerifyAndDecrypt(RSAPublicKey publicKeyToVerify, RSAPrivateKey privateKeyToDecrypt,  String payloadToVerifyAndDecrypt)
            throws JOSEException, ParseException, NoSuchAlgorithmException, CertificateException, FileNotFoundException,
            KeyStoreException, IOException, UnrecoverableKeyException {

        JWVerifyObject jwVerifyObject = jwSignatureVerify(publicKeyToVerify, payloadToVerifyAndDecrypt);

        if (!jwVerifyObject.isSignatureValid()) {
// throw new RuntimeErrorException("Signature is not valid");
            return null;

        } else {
            return jweDecrypt(privateKeyToDecrypt, jwVerifyObject.getPayloadAfterVerification());
        }

    }

//    public static String jweVerifyAndDecrypt(RSAPublicKey publicKeyToVerify,String payloadToVerifyAndDecrypt)
//            throws JOSEException, ParseException, NoSuchAlgorithmException, CertificateException, FileNotFoundException,
//            KeyStoreException, IOException, UnrecoverableKeyException {
//
//        JWVerifyObject jwVerifyObject = jwSignatureVerify(publicKeyToVerify, payloadToVerifyAndDecrypt);
//
//        if (!jwVerifyObject.isSignatureValid()) {
//// throw new RuntimeErrorException("Signature is not valid");
//            return null;
//
//        } else {
////            return jweDecrypt(privateKeyToDecrypt, jwVerifyObject.getPayloadAfterVerification());
//            return  null;
//        }
//
//    }
}
