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
        Path projPath= Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        System.out.println(projPath);
        projPath=projPath.getParent();
        String dicPath=projPath.toString()+"\\App\\SearchEngineApp\\src\\main\\java\\voice\\5746.dic";
        String lmPath= projPath.toString()+"\\App\\SearchEngineApp\\src\\main\\java\\voice\\5746.lm";
        configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        configuration.setDictionaryPath("file:///"+"A:\\Engineering\\Y2 Sem.2 Docs\\SearchEngineFinal\\App\\SearchEngineApp\\src\\main\\java\\voice\\5746.dic");
        configuration.setLanguageModelPath("file:///"+"A:\\Engineering\\Y2 Sem.2 Docs\\SearchEngineFinal\\App\\SearchEngineApp\\src\\main\\java\\voice\\5746.lm");
        try {
            recognizer = new LiveSpeechRecognizer(configuration);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    public static void main(String[] args) throws Exception {

        VoiceRecognizer voiceTest = new VoiceRecognizer();
        voiceTest.Recognize();
    }
    public String Recognize() {
            String query = null;
            try{
            recognizer.startRecognition(true);
            System.out.println("Say something");
            SpeechResult result;
            while (query == null || query.equals("")){
                result = recognizer.getResult();
                query = result.getHypothesis();
            }
            System.out.println("You said: " + query + "\n");
        }
        catch (Exception e) {
                System.out.println("Error: in catch agooga!");
        }
        recognizer.stopRecognition();

        return query;

    }
}