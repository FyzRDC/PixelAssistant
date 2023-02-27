package center.fyz.pixel;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import org.json.JSONObject;
import org.vosk.Model;
import org.vosk.Recognizer;

public class LiveSpeechRecognition {

	public static void main(String[] args) throws IOException {
		String modelPath = "vosk-model-fr-0.22";
		Model model = new Model(modelPath);

		AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
		if (!AudioSystem.isLineSupported(info)) {
			System.err.println("Line not supported");
			System.exit(-1);
		}
		TargetDataLine line;
		try {
			line = (TargetDataLine) AudioSystem.getLine(info);
			line.open(format);
		} catch (LineUnavailableException ex) {
			System.err.println("Unable to open the line: " + ex.getMessage());
			System.exit(-1);
			model.close();
			return;
		}
		line.start();

		Recognizer recognizer = new Recognizer(model, format.getSampleRate());

		byte[] buffer = new byte[4096];
		int bytesRead;
		while ((bytesRead = line.read(buffer, 0, buffer.length)) > 0) {
			if (recognizer.acceptWaveForm(buffer, bytesRead)) {
				JSONObject json = new JSONObject(recognizer.getResult());
				String result = json.getString("text");
				if (result.length() > 0) {
					System.out.println(result);
				}
			}
		}

		line.stop();
		line.close();
		recognizer.close();
	}

}
