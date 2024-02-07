package com.bijlipay.ATFFileGeneration.Util;

import com.bijlipay.ATFFileGeneration.Controller.AtfFileController;
import com.bijlipay.ATFFileGeneration.Model.Dto.AxisDto;
import com.bijlipay.ATFFileGeneration.Model.Dto.DummyDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.nimbusds.jose.JOSEException;
import jdk.nashorn.internal.parser.JSONParser;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.security.KeyFactory;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.ParseException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


public class JWEMainClient {




    private static final Logger logger = LoggerFactory.getLogger(AtfFileController.class);

    private static RSAPrivateKey getRSAPrivateKey(String filename) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String strKeyPEM = "";
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = br.readLine()) != null) {
            strKeyPEM += line + "\n";
        }
        br.close();
        strKeyPEM = strKeyPEM.
                replaceAll("\\n", "").
                replace("-----BEGIN PRIVATE KEY-----", "").
                replace("-----END PRIVATE KEY-----", "");
        logger.info("Private Key String --{}",strKeyPEM);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(strKeyPEM));
        RSAPrivateKey privKey = (RSAPrivateKey)kf.generatePrivate(keySpecPKCS8);
        return privKey;
    }

    private static RSAPublicKey getRSAPublicKey(String filename) throws CertificateException, FileNotFoundException {
        CertificateFactory fac = CertificateFactory.getInstance("X509");
        FileInputStream is = new FileInputStream(filename);
        X509Certificate cert = (X509Certificate) fac.generateCertificate(is);
        RSAPublicKey certPub = (RSAPublicKey) cert.getPublicKey();
        return certPub;
    }

    public static String encryptData(AxisDto axisDto) throws IOException, CertificateException, NoSuchAlgorithmException, InvalidKeySpecException, UnrecoverableKeyException, KeyStoreException, JOSEException, ParseException {

        RSAPublicKey axisCertPub = getRSAPublicKey(Constants.Axis_Public_Key);
        RSAPrivateKey clientPrivKey = getRSAPrivateKey(Constants.Axis_Private_Key);
        logger.info("Public Key And Private Key --{}---{}", axisCertPub,clientPrivKey);

//        ObjectMapper jsonObjectMapper = new ObjectMapper();
//        ObjectNode axisObjectNode = jsonObjectMapper.createObjectNode();
//        axisObjectNode.put("RRN",axisDto.getRrn());
//        axisObjectNode.put("MID",axisDto.getMid());
//        axisObjectNode.put("LATITUDE",axisDto.getLatitude());
//        axisObjectNode.put("LONGITUDE",axisDto.getLongitude());
//        axisObjectNode.put("AGGRNAME",axisDto.getAggrName());
//        axisObjectNode.put("ACTION",axisDto.getAction());
//        axisObjectNode.put("TID",axisDto.getTid());
//        axisObjectNode.put("DATEOFCOMMISSIONING",axisDto.getDateOfCommissioning());
//
//        JsonNode node = jsonObjectMapper.valueToTree(axisObjectNode);
//        logger.info("input node --{}",node);
//        Map<String, Object> axisRequest = new HashMap<>();
//        axisRequest.put("Data", node);
//        axisRequest.put("Risk", "{}");
//        logger.info("AxisRequest ---{}",axisRequest);


        String plainJSONRequest = "{\n" +
                "    \"Data\": {\n" +
                "        \"RRN\": \"1688382163040\",\n" +
                "        \"MID\": \"037322025110024\",\n" +
                "        \"LATITUDE\": \"13.0346034\",\n" +
                "        \"LONGITUDE\": \"80.2381735\",\n" +
                "        \"AGGRNAME\": \"BIJLIPAY\",\n" +
                "        \"ACTION\": \"I\",\n" +
                "        \"TID\": \"24578956\",\n" +
                "        \"DATEOFCOMMISSIONING\": \"07022024\"\n" +
                "    },\n" +
                "    \"Risk\": {}\n" +
                "}";

//        Gson gson =new Gson();




//        String plainJSONRequest =axisRequest.toString().replaceAll("=",":");

        System.out.println("Plain Request     = " + plainJSONRequest);

        String encrptedString = JWEUtilsForPartners.jweEncryptAndSign(axisCertPub,clientPrivKey, plainJSONRequest);
        return encrptedString;
    }



    public static String decryptData(String encryptData) throws IOException, CertificateException, NoSuchAlgorithmException, InvalidKeySpecException, UnrecoverableKeyException, KeyStoreException, JOSEException, ParseException {
        RSAPublicKey axisCertPub = getRSAPublicKey(Constants.Axis_Public_Key);
        RSAPrivateKey clientPrivKey = getRSAPrivateKey(Constants.Axis_Private_Key);
        String decrypt = JWEUtilsForPartners.jweVerifyAndDecrypt(axisCertPub, clientPrivKey,encryptData);
        System.out.println("Decrypted Request = " + decrypt);
        return decrypt;
    }
}
