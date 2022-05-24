package com.alexcux.encrysharemob;


import android.os.Build;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class customEncryptorAES {

    public static final String[] chars = new String[]{"А", "а", "Б", "б", "В", "в", "Г", "г", "Д", "д", "Е", "е", "Ё", "ё", "Ж", "ж", "З", "з", "И", "и", "Й", "й", "К", "к", "Л", "л", "М", "м", "Н", "н", "О", "о", "П", "п", "Р", "р", "С", "с", "Т", "т", "У", "у", "Ф", "ф", "Х", "х", "Ц", "ц", "Ч", "ч", "Ш", "ш", "Щ", "щ", "Ъ", "ъ", "Ы", "ы", "Ь", "ь", "Э", "э", "Ю", "ю", "Я", "я","A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z","1","2","3","4","5","6","7","8","9","0"};
    public byte[] key = new byte[32];
    public byte[] initVector = new byte[16];

    public customEncryptorAES(@NonNull String key, @NonNull String initVector){
            ByteBuffer bf = ByteBuffer.wrap(new byte[32]);
            byte[] kbytes = key.getBytes(StandardCharsets.UTF_8);
            for (int i = 0;i<32;i++){
                bf.put(i, kbytes[i]);
            }
            this.key = bf.array();
        bf = ByteBuffer.wrap(new byte[16]);
        byte[] vbytes = initVector.getBytes(StandardCharsets.UTF_8);
        for (int i = 0;i<16;i++){
            bf.put(vbytes[i]);
        }
        this.initVector = bf.array();
    }

    public static String getRandomKey(){
        List<String> res = Arrays.asList(Arrays.stream(chars).toArray(String[]::new));
        Collections.shuffle(res);
        String[] symbols = (String[]) res.toArray();
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (i=0; i<80;i++){
            sb.append(symbols[getRandomNumber(0,chars.length)]);
        }
        return sb.substring(0,32);
    }

    public static int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    public String encrypt(String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector);
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(cipher.doFinal(value.getBytes()));
            return Base64.encodeToString(encrypted,0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public String decrypt(String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector);
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");


            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] original = cipher.doFinal(cipher.doFinal(Base64.decode(encrypted,0)));

            return new String(original);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

}
