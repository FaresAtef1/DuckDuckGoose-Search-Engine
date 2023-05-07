package crawler;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class testMD5 {

    private static String getContentHashFromURL(String input) throws InterruptedException, MalformedURLException {
        try {
            // Get an instance of the MD5 hash algorithm
            MessageDigest md = MessageDigest.getInstance("MD5");

            // Convert the input string to a byte array and hash it
            byte[] inputBytes = input.getBytes();
            byte[] hashBytes = md.digest(inputBytes);

            // Convert the hash bytes to a hexadecimal string representation
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < hashBytes.length; i++) {
                String hex = Integer.toHexString(0xff & hashBytes[i]);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // This should never happen since "MD5" is a standard algorithm
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    public static void main (String[] args) throws InterruptedException, MalformedURLException {
        String url1="https://web.archive.org/web/20190314025235/https://www.geeksforgeeks.org/lower-and-upper-bound-theory/";
        String url2="https://web.archive.org/web/20200229205039/https://www.geeksforgeeks.org/lower-and-upper-bound-theory/";
        String hash1=getContentHashFromURL(url1);
        Thread.sleep(4000);
        String hash2=getContentHashFromURL(url2);
        System.out.println(hash1);
        System.out.println(hash2);
        if(hash1.equals(hash2))
            System.out.println("Equal");
        else
            System.out.println("Not Equal");
    }
}
