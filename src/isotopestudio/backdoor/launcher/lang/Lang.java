package isotopestudio.backdoor.launcher.lang;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;

import javax.swing.JOptionPane;

import doryanbessiere.isotopestudio.api.IsotopeStudioAPI;
import isotopestudio.backdoor.launcher.LauncherApplication;

public class Lang {

	public static HashMap<String, String> messages = new HashMap<String, String>();

	public InputStream inputStream;
	public String name;

	public Lang(String name, InputStream inputStream) {
		this.name = name;
		this.inputStream = inputStream;
	}

	public String getName() {
		return name;
	}

	public boolean read() {
		messages.clear();

		BufferedReader read = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
		try {
			String line = null;
			while ((line = read.readLine()) != null) {
				if (!line.startsWith("#")) {
					if (line.contains("=")) {
						String[] args = line.split("=");

						if (args.length == 2) {
							String key = args[0];
							String value = args[1];

							messages.put(key, value == "null" ? null : value);
						} else {
							System.err.println("[Lang] ["+name+"] " + "args.length != 2 [line=" + line + "]");
						}
					}
				}
			}
			read.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "The language file ["+name+"] could not be read!", "Fatal error", JOptionPane.ERROR_MESSAGE);
			LauncherApplication.APPLICATION.exit(IsotopeStudioAPI.EXIT_CODE_CRASH);
		}
		return false;
	}

	public static String get(String key, String... replace) {
		String message = key;
		if (messages.containsKey(key)) {
			message = messages.get(key);

			if (replace != null && replace.length % 2 == 0) {
				int i = 0;
				while (i < replace.length) {
					message = message.replace(replace[i], replace[i + 1]);
					i += 2;
				}
			}
		} else {
			System.err.println("[Lang] : The key you entered is not found in the list of messages");
		}
		return message;
	}
}
