package com.bijlipay.ATFFileGeneration.Util;

import java.security.SecureRandom;

public class RandomKeyGeneration {

    static final String AB = "012345678967834567CDEFGHIertyuioijhgbcdefghijklmnopqrstuvwxyz";

    static SecureRandom rnd = new SecureRandom();

    public String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }
}
