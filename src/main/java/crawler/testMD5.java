package crawler;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class testMD5 {

    private static String getContentHashFromURL(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            int attempts = 0;
            while (true)
            {
                try
                {
                    connection.connect();
                    break;
                }
                catch (IOException e)
                {
                    if (++attempts > 5)
                    {
                        return "";
                    }
                    // Wait for 1 second before retrying
                    Thread.sleep(10);
                }
            }

            byte[] contentBytes = new byte[0];
            try (InputStream inputStream = connection.getInputStream())
            {
                contentBytes = inputStream.readAllBytes();
            }
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(contentBytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash)
                sb.append(String.format("%02x", b & 0xff));
            return sb.toString();
        }
        catch (IOException | NoSuchAlgorithmException | InterruptedException e)
        {
            return "";
        }
    }

    public static void main (String[] args) throws InterruptedException {
        String url1="https://www.iana.org/domains/reserved";
        String url2="https://www.iana.org/domains/reserved";
        int curr_time= (int) System.currentTimeMillis();
        if(getContentHashFromURL(url1).equals(getContentHashFromURL(url2)))
            System.out.println("Equal");
        else
            System.out.println("Not Equal");
        int end_time= (int) System.currentTimeMillis();
        System.out.println(end_time-curr_time);
    }
}
