package com.bijlipay.ATFFileGeneration.Util;

public class JWVerifyObject {

    boolean isSignatureValid = false ;

    String payloadAfterVerification ;

    /**
     * @return the isSignatureValid
     */
    public boolean isSignatureValid() {
        return isSignatureValid;
    }

    /**
     * @param isSignatureValid the isSignatureValid to set
     */
    public void setSignatureValid(boolean isSignatureValid) {
        this.isSignatureValid = isSignatureValid;
    }

    /**
     * @return the payloadAfterVerification
     */
    public String getPayloadAfterVerification() {
        return payloadAfterVerification;
    }

    /**
     * @param payloadAfterVerification the payloadAfterVerification to set
     */
    public void setPayloadAfterVerification(String payloadAfterVerification) {
        this.payloadAfterVerification = payloadAfterVerification;
    }


}
