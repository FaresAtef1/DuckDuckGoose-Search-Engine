package engine.app;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;

public class VoiceTest {
    public static void main(String[] args) throws Exception {
        // Set up the configuration
        Configuration configuration = new Configuration();
        configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        configuration.setDictionaryPath("file:///A:\\Engineering\\Y2 Sem.2 Docs\\SearchEngineFinal\\App\\SearchEngineApp\\src\\main\\java\\engine\\app\\7503.dic");
        configuration.setLanguageModelPath("file:///A:\\Engineering\\Y2 Sem.2 Docs\\SearchEngineFinal\\App\\SearchEngineApp\\src\\main\\java\\engine\\app\\7503.lm");

        // Create the recognizer
        LiveSpeechRecognizer recognizer = new LiveSpeechRecognizer(configuration);

        // Start recognition
        recognizer.startRecognition(true);

        // Loop through speech input until the user says "stop"
        SpeechResult result;
        String text;
        do {
            result = recognizer.getResult();
            text = result.getHypothesis();
            System.out.println("You said: " + text);
        } while (!text.equals("stop"));

        // Stop recognition
        recognizer.stopRecognition();
    }
}