package isotopestudio.backdoor.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import doryanbessiere.isotopestudio.api.Game;
import doryanbessiere.isotopestudio.api.IsotopeStudioAPI;
import doryanbessiere.isotopestudio.api.authentification.User;
import doryanbessiere.isotopestudio.api.news.News;
import doryanbessiere.isotopestudio.api.updater.IUpdater;
import doryanbessiere.isotopestudio.api.updater.Updater;
import doryanbessiere.isotopestudio.commons.GsonInstance;
import doryanbessiere.isotopestudio.commons.logger.Logger;
import doryanbessiere.isotopestudio.commons.logger.file.LoggerFile;
import isotopestudio.backdoor.launcher.interfaces.Interface;
import isotopestudio.backdoor.launcher.interfaces.LoginInterface;
import isotopestudio.backdoor.launcher.lang.Lang;
import isotopestudio.backdoor.launcher.popup.PopupType;
import isotopestudio.backdoor.launcher.settings.LauncherSettings;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class LauncherApplication extends Application {

	public static final String APP_VERSION = "1.0.4";

	public static LauncherApplication APPLICATION;
	public static Logger LOGGER;
	public static User user;

	public static User getUser() {
		return user;
	}

	public static boolean isAuthentified() {
		return user != null;
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
		}

		LauncherApplication.launch(LauncherApplication.class, args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		APPLICATION = this;
		LOGGER = new Logger("BackdoorLauncher", new LoggerFile(new File(localDirectory(), "logs")));

		loadApplication();

		launcherFrame = new LauncherFrame(stage, "Backdoor - Game Launcher", 1150, 610);
		launcherFrame.setContent(Interface.LOGIN);
		launcherFrame.stage.getIcons().addAll(new Image("/images/logo_64x64.png"), new Image("/images/logo_32x32.png"),
				new Image("/images/logo_16x16.png"));
		launcherFrame.show();

		Properties launcher_properties = new Properties();
		try {
			launcher_properties.load(new URL("https://isotope-studio.fr/launcher/launcher.properties").openStream());
			if (!launcher_properties.get("version").equals(APP_VERSION)) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									((LoginInterface) Interface.LOGIN).getLoginButton().setDisable(true);
									((LoginInterface) Interface.LOGIN).getEmailField().setDisable(true);
									((LoginInterface) Interface.LOGIN).getPasswordField().setDisable(true);
								}
							});
					HttpClient client = HttpClientBuilder.create().build();
					HttpGet request = new HttpGet(
							"https://isotope-studio.fr/launcher/BackdoorLauncherSetup.exe");
					HttpResponse response = client.execute(request);
					HttpEntity entity = response.getEntity();

					InputStream is = entity.getContent();

					String tempDir = System.getProperty("java.io.tmpdir");

					File setup_file = new File(tempDir, "BackdoorLauncherSetup.exe");
					setup_file.createNewFile();

					FileOutputStream fos = new FileOutputStream(setup_file);

					byte[] buffer = new byte[1024 * 4];
					int n = 0;
					long total_read = 0;
					while (-1 != (n = is.read(buffer))) {
						fos.write(buffer, 0, n);
						total_read += n;
						int pourcentage = (int) (total_read * 100 / (long) entity.getContentLength());
						double progress = (double) pourcentage / 100;
					}

					is.close();
					fos.close();

							Process process = new ProcessBuilder(setup_file.getPath()).start();
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									exit();
								}
							});
						} catch (Exception e) {
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									LauncherApplication.APPLICATION.getLauncherFrame().popup(PopupType.ERROR, "",
											Lang.get("launcher_update_failed"));
								}
							});
							e.printStackTrace();
						}
					}
				}).start();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		ResourceManager.load();
	}

	public void loadApplication() {
		LauncherSettings settings = new LauncherSettings();

		File lang_directory = new File(localDirectory(), "langs");
		File lang_file = new File(lang_directory, settings.lang + ".lang");

		if (!lang_directory.exists() || !lang_file.exists()) {
			crash(lang_file.getPath() + " not found!");
			return;
		}

		try {
			new Lang(settings.lang, new FileInputStream(lang_file)).read();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void crash(String message) {
		JOptionPane.showMessageDialog(null, message, "Application has crashed", JOptionPane.ERROR_MESSAGE);
		exit(IsotopeStudioAPI.EXIT_CODE_CRASH);
	}

	public void exit() {
		exit(IsotopeStudioAPI.EXIT_CODE_EXIT);
	}

	public void exit(int code) {
		if (launcherFrame != null)
			launcherFrame.close();
		System.exit(0);
	}

	private LauncherFrame launcherFrame;

	public LauncherFrame getLauncherFrame() {
		return launcherFrame;
	}

	public static InputStream getResource(String path) {
		return LauncherApplication.class.getResourceAsStream(path);
	}

	public static File localDirectory() {
		try {
			File file = new File(LauncherApplication.class.getProtectionDomain().getCodeSource().getLocation().toURI())
					.getParentFile();
			return file;
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void launch(User user, boolean snapshot) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				((LoginInterface) Interface.LOGIN).getLoginButton().setDisable(true);
			}
		});
		File backdoor_directory = new File(localDirectory(), "games/backdoor");

		LauncherSettings settings = LauncherSettings.getSettings();
		settings.snapshot_enable = snapshot;
		settings.save();
		
		
		boolean has_update = false;
		if (!backdoor_directory.exists()) {
			backdoor_directory.mkdirs();
			has_update = true;
		} else {
			File game_info = new File(backdoor_directory, "game.ini");
			if (!game_info.exists()) {
				try {
					game_info.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			Properties properties = new Properties();
			try {
				properties.load(new FileInputStream(game_info));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(new URL("https://isotope-studio.fr/games/backdoor/game.php?target="+(snapshot ? "snapshot" : "release")).openStream(),
								Charset.forName("UTF-8")));
				String json = "";
				String line = null;
				while ((line = reader.readLine()) != null) {
					json += line;
				}
				System.out.println(json);
				Game backdoor_info = Game.fromJson(json);
				if (!properties.containsKey("version")
						|| !properties.get("version").equals(backdoor_info.getVersion())) {
					has_update = true;
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (has_update) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					Updater updater = new Updater("https://isotope-studio.fr/games/backdoor/"+(snapshot ? "snapshot" : "release")+"/");
					updater.setListener(new IUpdater() {

						@Override
						public void progress(long total_read, long total_size) {
							int pourcentage = (int) (total_read * 100 / (long) total_size);
							double progress = (double) pourcentage / 100;
							((LoginInterface) Interface.LOGIN).getProgressbar().setProgress(progress);
							((LoginInterface) Interface.LOGIN).getProgressText()
									.setText(Lang.get("downloading_game_update", "%progress%", pourcentage + ""));
						}

						@Override
						public void downloadFile(File file, long file_size) {
						}
					});
					((LoginInterface) Interface.LOGIN).getLoginButton().setDisable(true);
					if (updater.start(backdoor_directory)) {
						extractUpdate(new File(backdoor_directory, "latest"));
						launchGame();
					} else {
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								LauncherApplication.APPLICATION.getLauncherFrame().popup(PopupType.ERROR, 
										Lang.get("game_update_failed"), "");
							}
						});
					}
				}

				private void extractUpdate(File directory) {
					for (File file : directory.listFiles()) {
						String file_path = file.getPath();
						String target_path = file_path.substring(file_path.lastIndexOf("latest") + 6 /* text length */,
								file_path.length());
						System.out.println(target_path);
						File target = new File(backdoor_directory, target_path);
						if (file.isDirectory()) {
							target.mkdirs();
							extractUpdate(file);
						} else {
							if (target.exists()) {
								target.delete();
							}
							// System.out.println(file.getPath() + " -> " + target.getPath());
							file.renameTo(target);
						}
					}
				}
			}).start();
		} else {
			launchGame();
		}
	}

	public static News[] news() {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new URL(IsotopeStudioAPI.API_URL + "/news.php").openStream(), Charset.forName("UTF-8")));
			String json = "";
			String line = null;
			while ((line = reader.readLine()) != null) {
				json += line;
			}
			News[] news = (News[]) GsonInstance.instance().fromJson(json, News[].class);
			return news;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void setAuthentification(HashMap<String, Object> informations) {
		LauncherApplication.user = new User("" + informations.get("uuid"), "" + informations.get("username"),
				"" + informations.get("email"), "" + informations.get("token"));
		LauncherSettings settings = LauncherSettings.getSettings();
		if (((LoginInterface) Interface.LOGIN).getSaveAuthCheckbox().isSelected()) {
			settings.email = LauncherApplication.user.getEmail();
			settings.token = LauncherApplication.user.getToken();

			((LoginInterface) Interface.LOGIN).getEmailField().setText(LauncherApplication.user.getEmail());
			((LoginInterface) Interface.LOGIN).getEmailField().setDisable(true);
			((LoginInterface) Interface.LOGIN).getPasswordField().setDisable(true);
		} else {
			LauncherApplication.disconnect();
		}
		settings.save();
		((LoginInterface) Interface.LOGIN).getLoginButton().setText(Lang.get("run_the_game"));
	}

	public static void disconnect() {
		user = null;

		LauncherSettings settings = LauncherSettings.getSettings();
		settings.email = "";
		settings.token = "";
		settings.save();

		((LoginInterface) Interface.LOGIN).getEmailField().setText("");
		((LoginInterface) Interface.LOGIN).getPasswordField().setText("");

		((LoginInterface) Interface.LOGIN).getEmailField().setDisable(false);
		((LoginInterface) Interface.LOGIN).getPasswordField().setDisable(false);
		((LoginInterface) Interface.LOGIN).getSaveAuthCheckbox().setSelected(false);
		((LoginInterface) Interface.LOGIN).getLoginButton().setText(Lang.get("login"));
	}

	public static void launchGame() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				File backdoor_directory = new File(localDirectory(), "games/backdoor");
				System.out.println(user.toJson());
				ProcessBuilder builder = new ProcessBuilder(
						new String[] { "java", "-jar", "backdoor.jar", "email=" + user.getEmail(), "token="+user.getToken() });
				builder.directory(backdoor_directory);
				LauncherApplication.APPLICATION.getLauncherFrame().stage.close();
				Process process;
				try {
					process = builder.start();

					BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

					String line;
						while ((line = reader.readLine()) != null) {
							System.out.println(line);
						}

					((LoginInterface) Interface.LOGIN).getLoginButton().setDisable(false);

						int code = process.waitFor();

						LauncherApplication.APPLICATION.getLauncherFrame().stage.show();

						if (code == IsotopeStudioAPI.EXIT_CODE_CRASH) {
							LauncherApplication.APPLICATION.getLauncherFrame().popup(PopupType.ERROR,
									Lang.get("game_launch_failed"), "");
						}

						if (code == IsotopeStudioAPI.EXIT_CODE_RESTART) {
							launchGame();
						}
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
