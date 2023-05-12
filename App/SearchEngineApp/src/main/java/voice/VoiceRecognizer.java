package voice;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;

import java.io.File;
import java.io.IOException;

public class VoiceRecognizer {

    LiveSpeechRecognizer recognizer;
    public VoiceRecognizer()
    {
        Configuration configuration = new Configuration();
        String dicPath =System.getProperty("user.dir")+"\\app\\SearchEngineApp\\src\\main\\java\\voice\\7503.dic";
        String lmPath =System.getProperty("user.dir")+"\\app\\SearchEngineApp\\src\\main\\java\\voice\\7503.lm";

        configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        configuration.setDictionaryPath("file:///"+dicPath);
        configuration.setLanguageModelPath("file:///"+lmPath);
        try {
            recognizer = new LiveSpeechRecognizer(configuration);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    public static void main(String[] args) throws Exception {
        VoiceRecognizer recognizer=new VoiceRecognizer();
    }
    public String Recognize() {
            String query = null;
            try{
            recognizer.startRecognition(true);
            System.out.println("Say something");
            SpeechResult result;
            while (query == null) {
                result = recognizer.getResult();
                query = result.getHypothesis();
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        recognizer.stopRecognition();
        return query;

    }
}