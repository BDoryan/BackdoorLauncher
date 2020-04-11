package isotopestudio.backdoor.launcher;

import javafx.scene.text.Font;

public class ResourceManager {
	
	public static void load() {
		System.setProperty("prism.lcdtext", "false");
	}
	
	public static Font getFont(int size) {
		return Font.loadFont(LauncherApplication.getResource("/fonts/jackinput.ttf"), size);
	}
}
