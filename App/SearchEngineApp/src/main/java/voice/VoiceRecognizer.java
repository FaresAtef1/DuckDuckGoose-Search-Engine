package voice;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class VoiceRecognizer {

    LiveSpeechRecognizer recognizer;
    public VoiceRecognizer()
    {
        Configuration configuration = new Configuration();
        //adjust the paths to the dictionary and the language model
        Path DicPath= Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        DicPath=DicPath.getParent().getParent().getParent();
        String dicPath=DicPath.toString()+"\\App\\SearchEngineApp\\src\\main\\java\\voice\\7503.dic";
        Path LmPath= Paths.get(System.getProperty("user.dir"));
        LmPath=LmPath.getParent().getParent().getParent();
        String lmPath= LmPath.toString()+"\\App\\SearchEngineApp\\src\\main\\java\\voice\\7503.lm";
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

        VoiceRecognizer voiceTest = new VoiceRecognizer();
    }
    public String Recognize() {
            String query = null;
            try{
            recognizer.startRecognition(false);
            System.out.println("Say something");
            SpeechResult result;
            while (query == null || query.equals("")){
                result = recognizer.getResult();
                query = result.getHypothesis();
            }
            System.out.println("You said: " + query + "\n");
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        recognizer.stopRecognition();
        return query;

    }
}