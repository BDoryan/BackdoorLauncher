package isotopestudio.backdoor.launcher.interfaces;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.codehaus.plexus.util.ExceptionUtils;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTextField;

import doryanbessiere.isotopestudio.api.IsotopeStudioAPI;
import doryanbessiere.isotopestudio.api.authentification.AuthClient;
import doryanbessiere.isotopestudio.api.authentification.Response;
import doryanbessiere.isotopestudio.api.authentification.User;
import isotopestudio.backdoor.launcher.LauncherApplication;
import isotopestudio.backdoor.launcher.ResourceManager;
import isotopestudio.backdoor.launcher.lang.Lang;
import isotopestudio.backdoor.launcher.popup.PopupType;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class LoginInterface extends AnchorPane implements Interface {

	private AnchorPane login_pane;
	private JFXProgressBar progressbar;
	private User user;

	public User getUser() {
		return user;
	}

	public boolean isConnected() {
		return this.user != null;
	}

	public LoginInterface() {
		getStylesheets().add("styles/login.css");
		getStyleClass().add("login");

		this.login_pane = new AnchorPane();
		this.login_pane.getStylesheets().add("styles/login.css");
		this.login_pane.getStyleClass().add("login-pane");
		this.login_pane.setEffect(new DropShadow());

		AnchorPane.setLeftAnchor(this.login_pane, 30D);
		AnchorPane.setTopAnchor(this.login_pane, 0D);
		AnchorPane.setBottomAnchor(this.login_pane, 0D);

		VBox vbox = new VBox(40);
		vbox.setAlignment(Pos.CENTER);
		vbox.setMaxWidth(150);

		ImageView banner = new ImageView(new Image("/images/banner.png"));
		banner.setFitWidth(330);
		banner.setFitHeight(70);
		banner.setPreserveRatio(true);
		banner.setSmooth(true);
		banner.setCache(true);
		banner.setPickOnBounds(true);
		banner.setEffect(new DropShadow());

		vbox.getChildren().add(banner);
		vbox.setPadding(new Insets(15, 15, 15, 15));

		AnchorPane.setLeftAnchor(vbox, 0D);
		AnchorPane.setRightAnchor(vbox, 0D);
		AnchorPane.setTopAnchor(vbox, 0D);
		AnchorPane.setBottomAnchor(vbox, 0D);

		Text text = new Text(Lang.get("login_title"));
		text.setFont(ResourceManager.getFont(10));
		text.getStyleClass().add("login-text");
		text.setSmooth(false);

		vbox.getChildren().add(text);

		JFXTextField email_field = new JFXTextField();
		email_field.setMinHeight(20D);
		email_field.setMaxWidth(280);
		email_field.setFont(ResourceManager.getFont(16));
		email_field.setPromptText(Lang.get("email"));

		vbox.getChildren().add(email_field);

		JFXPasswordField password_field = new JFXPasswordField();
		password_field.setMinHeight(20D);
		password_field.setMaxWidth(280);
		password_field.setFont(ResourceManager.getFont(16));
		password_field.setPromptText(Lang.get("password"));

		vbox.getChildren().add(password_field);

		JFXCheckBox stay_connected_checkbox = new JFXCheckBox(Lang.get("stay_connected"));
		stay_connected_checkbox.setFont(ResourceManager.getFont(16));

		vbox.getChildren().add(stay_connected_checkbox);

		JFXButton login_button = new JFXButton(Lang.get("login"));
		login_button.setMinHeight(60);
		login_button.setMinWidth(240);
		login_button.setFont(ResourceManager.getFont(18));
		login_button.setEffect(new DropShadow());
		login_button.setOnMouseClicked(new EventHandler<MouseEvent>() {

			private boolean in_process = false;

			@Override
			public void handle(MouseEvent event) {
				if (in_process)
					return;
				login_button.setDisable(true);

				AuthClient authClient = new AuthClient(IsotopeStudioAPI.API_URL+"/");
				new Thread(new Runnable() {
					@Override
					public void run() {
						if (isConnected()) {
							LauncherApplication.launch(user);
						} else {
							in_process = true;

							if ((email_field.getText() != null && email_field.getText().length() > 0)
									&& (password_field.getText() != null && password_field.getText().length() > 0)) {
								try {
									Response response = authClient.loginPassword(email_field.getText(),
											password_field.getText());
									Platform.runLater(new Runnable() {
										@Override
										public void run() {
											if (response.getPath().equals("success")) {
												user = new User("" + response.getInformations().get("username"),
														"" + response.getInformations().get("email"),
														"" + response.getInformations().get("token"));
												login_button.setText(Lang.get("run_the_game"));
											} else {
												LauncherApplication.APPLICATION.getLauncherFrame().popup(
														PopupType.ERROR, Lang.get("account_login_error"),
														Lang.get(response.getPath()));
											}
										}
									});
								} catch (ClientProtocolException e) {
									Platform.runLater(new Runnable() {
										@Override
										public void run() {
											String stacktrace = ExceptionUtils.getStackTrace(e);
											LauncherApplication.APPLICATION.crash(stacktrace);
										};
									});
								} catch (IOException e) {
									Platform.runLater(new Runnable() {
										@Override
										public void run() {
											String stacktrace = ExceptionUtils.getStackTrace(e);
											LauncherApplication.APPLICATION.crash(stacktrace);
										};
									});
								} catch (Exception e) {
									Platform.runLater(new Runnable() {
										@Override
										public void run() {
											String stacktrace = ExceptionUtils.getStackTrace(e);
											LauncherApplication.APPLICATION.crash(stacktrace);
										};
									});
								}
							} else {
								Platform.runLater(new Runnable() {
									@Override
									public void run() {
										LauncherApplication.APPLICATION.getLauncherFrame().popup(PopupType.ERROR,
												Lang.get("account_login_error"),
												Lang.get("the_text_fields_are_empty"));
									};
								});
							}
							in_process = false;
						}
						login_button.setDisable(false);
					}

				}).start();
				;
			}
		});

		vbox.getChildren().add(login_button);

		this.login_pane.getChildren().add(vbox);

		this.getChildren().add(this.login_pane);

		this.progressbar = new JFXProgressBar();
		this.progressbar.setMinHeight(20D);
		this.progressbar.setProgress(0D);
		
		AnchorPane.setLeftAnchor(this.progressbar, 410D);
		AnchorPane.setBottomAnchor(this.progressbar, 10D);
		AnchorPane.setRightAnchor(this.progressbar, 20D);
		
		this.getChildren().add(this.progressbar);
	}

	public JFXProgressBar getProgressbar(){
		return this.progressbar;
	}
	
	@Override
	public void load() {
	}

	@Override
	public Pane getContent() {
		return this;
	}
}
