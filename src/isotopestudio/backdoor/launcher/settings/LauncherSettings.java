package isotopestudio.backdoor.launcher.settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JOptionPane;

import com.google.gson.JsonSyntaxException;

import isotopestudio.backdoor.launcher.LauncherApplication;
import isotopestudio.backdoor.launcher.utils.GsonUtils;

public class LauncherSettings {

	private static final String NAME = "game.settings";

	public String lang = "french";
	
	public String email;
	public String token;
	
	public static File getFileLauncherSettings() {
		return new File(LauncherApplication.localDirectory(), NAME);
	}

	public static LauncherSettings getSettings() {
		File target = getFileLauncherSettings();
		if (target.exists() && !target.isFile()) {
			if (!target.delete()) {
				JOptionPane.showMessageDialog(null, "Impossible to delete the impostor file (parameter file)!",
						"Fatal error", JOptionPane.ERROR_MESSAGE);
				System.exit(-1);
				return null;
			}
		}
		
		if (!target.exists()) {
			try {
				target.createNewFile();
				LauncherSettings game_setting = new LauncherSettings();
				game_setting.save();
				return game_setting;
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "Cannot create parameter file", "Fatal error",
						JOptionPane.ERROR_MESSAGE);
				System.exit(-1);
			}
		} else {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(target));
				String json = "";
				String current = null;
				while ((current = reader.readLine()) != null) {
					json += current;
				}
				reader.close();

				try {
					return GsonUtils.instance().fromJson(json, LauncherSettings.class);
				} catch (JsonSyntaxException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null,
							"Error when loading game parameters, synthax problem with json!", "Fatal error",
							JOptionPane.ERROR_MESSAGE);
					System.exit(-1);
				}
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, "The parameter file cannot be found", "Fatal error",
						JOptionPane.ERROR_MESSAGE);
				System.exit(-1);
			} catch (IOException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, "Cannot read the meter file", "Fatal error",
						JOptionPane.ERROR_MESSAGE);
				System.exit(-1);
			}
		}
		return null;
	}

	/**
	 * <pre>Attention: le code ne doit pas être éxécuter autre part que dans le Thread principal pour éviter un désynchronisation des paramètres</pre>
	 */
	public void save() {
		File target = getFileLauncherSettings();
		if (target.exists() && target.isFile()) {
			target.delete();
			try {
				target.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				FileWriter writer = new FileWriter(target);
				writer.write(GsonUtils.instance().toJson(this));
				writer.flush();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			JOptionPane.showMessageDialog(null, "Impossible to save the parameter file because it cannot be found!",
					"Fatal error", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
	}
}
