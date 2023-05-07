package stemmer;

import java.util.Scanner;

public class StemmerTest {

    public static void main(String[] args) {
        // TODO Auto-generated method stub

        englishStemmer stemmer= new englishStemmer();
        String test= new String();
        Scanner sc = new Scanner(System.in);
        test=sc.nextLine();
        System.out.println(test);
        stemmer.setCurrent(test);
        stemmer.stem();
        test=stemmer.getCurrent();
        System.out.println(test);
        sc.close();

    }

}
