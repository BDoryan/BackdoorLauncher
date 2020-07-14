package isotopestudio.backdoor.launcher.interfaces;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;

import org.apache.http.client.ClientProtocolException;
import org.codehaus.plexus.util.ExceptionUtils;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTextField;

import doryanbessiere.isotopestudio.api.IsotopeStudioAPI;
import doryanbessiere.isotopestudio.api.authentification.AuthClient;
import doryanbessiere.isotopestudio.api.news.News;
import doryanbessiere.isotopestudio.api.web.Response;
import isotopestudio.backdoor.launcher.LauncherApplication;
import isotopestudio.backdoor.launcher.ResourceManager;
import isotopestudio.backdoor.launcher.lang.Lang;
import isotopestudio.backdoor.launcher.popup.PopupType;
import isotopestudio.backdoor.launcher.settings.LauncherSettings;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class LoginInterface extends AnchorPane implements Interface {

	private AnchorPane login_pane;
	private AnchorPane news_pane;
	
	private JFXButton login_button;

	private JFXTextField email_field;
	private JFXPasswordField password_field;
	private JFXCheckBox stay_connected_checkbox;
	private JFXCheckBox snapshot_enable_checkbox;

	private JFXProgressBar progressbar;

	private VBox progress_text_box;
	private Text progress_text;

	public JFXButton getLoginButton() {
		return login_button;
	}

	public VBox getProgressBox() {
		return progress_text_box;
	}

	public Text getProgressText() {
		return progress_text;
	}

	public LoginInterface() {
		getStylesheets().add("styles/login.css");
		getStylesheets().add("styles/news.css");
		getStyleClass().add("login");

		this.login_pane = new AnchorPane();
		this.login_pane.getStylesheets().add("styles/login.css");
		this.login_pane.getStyleClass().add("login-pane");
		this.login_pane.setEffect(new DropShadow());

		AnchorPane.setLeftAnchor(this.login_pane, 30D);
		AnchorPane.setTopAnchor(this.login_pane, 0D);
		AnchorPane.setBottomAnchor(this.login_pane, 0D);

		VBox vbox = new VBox(10);
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

		email_field = new JFXTextField();
		email_field.setMinHeight(20D);
		email_field.setMaxWidth(280);
		email_field.setPadding(new Insets(30, 0,0,0));
		email_field.setFont(email_field.getFont().font(16D));
		email_field.setPromptText(Lang.get("email"));

		vbox.getChildren().add(email_field);

		password_field = new JFXPasswordField();
		password_field.setMinHeight(20D);
		password_field.setMaxWidth(280);
		password_field.setPadding(new Insets(30, 0,0,0));
		password_field.setFont(password_field.getFont().font(16D));
		password_field.setPromptText(Lang.get("password"));

		vbox.getChildren().add(password_field);

		stay_connected_checkbox = new JFXCheckBox(Lang.get("stay_connected"));
		stay_connected_checkbox.setFont(stay_connected_checkbox.getFont().font(16D));
		vbox.setMargin(stay_connected_checkbox, new Insets(30, 0, 0, 0));

		vbox.getChildren().add(stay_connected_checkbox);

		snapshot_enable_checkbox = new JFXCheckBox(Lang.get("enable_snapshot"));
		snapshot_enable_checkbox.setFont(snapshot_enable_checkbox.getFont().font(16D));

		vbox.getChildren().add(snapshot_enable_checkbox);

		login_button = new JFXButton(Lang.get("login"));
		login_button.setMinHeight(60);
		login_button.setMinWidth(240);
		login_button.setFont(ResourceManager.getFont(18));
		login_button.setEffect(new DropShadow());
		vbox.setMargin(login_button, new Insets(30, 0, 0, 0));
		login_button.setOnMouseClicked(new EventHandler<MouseEvent>() {

			private boolean in_process = false;

			@Override
			public void handle(MouseEvent event) {
				if (in_process)
					return;
				login_button.setDisable(true);

				AuthClient authClient = new AuthClient(IsotopeStudioAPI.API_URL + "/");
				new Thread(new Runnable() {
					@Override
					public void run() {
						if (LauncherApplication.isAuthentified()) {
							LauncherApplication.launch(LauncherApplication.getUser(), snapshot_enable_checkbox.isSelected());
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
												LauncherApplication.setAuthentification(response.getInformations());
												email_field.setDisable(true);
												password_field.setDisable(true);
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
												Lang.get("account_login_error"), Lang.get("the_text_fields_are_empty"));
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

		this.progress_text = new Text("");
		this.progress_text.setStyle("-fx-text-fill: white;");

		this.progress_text_box = new VBox();
		this.progress_text_box.setMinHeight(20D);
		this.progress_text_box.setAlignment(Pos.CENTER_LEFT);

		AnchorPane.setLeftAnchor(this.progress_text_box, 415D);
		AnchorPane.setBottomAnchor(this.progress_text_box, 10D);
		AnchorPane.setRightAnchor(this.progress_text_box, 20D);

		this.progress_text_box.getChildren().add(this.progress_text);

		this.getChildren().add(this.progress_text_box);

		this.news_pane = new AnchorPane();

		AnchorPane.setLeftAnchor(this.news_pane, 410D);
		AnchorPane.setBottomAnchor(this.news_pane, 40D);
		AnchorPane.setRightAnchor(this.news_pane, 20D);
		AnchorPane.setTopAnchor(this.news_pane, 20D);
		
		this.getChildren().add(this.news_pane);
		
		addNews();
	}

	public JFXProgressBar getProgressbar() {
		return this.progressbar;
	}

	@Override
	public void load() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				LauncherSettings settings = LauncherSettings.getSettings();
				snapshot_enable_checkbox.setSelected(settings.snapshot_enable);
				if (settings.email != null && !settings.email.equalsIgnoreCase("") && settings.token != null
						&& !settings.token.equalsIgnoreCase("")) {

					stay_connected_checkbox.setSelected(true);
					AuthClient authClient = new AuthClient(IsotopeStudioAPI.API_URL + "/");
					try {
						Response response = authClient.loginToken(settings.email, settings.token);
						if (response.getPath().equals("success")) {
							LauncherApplication.setAuthentification(response.getInformations());
						} else {
							LauncherApplication.APPLICATION.getLauncherFrame().popup(PopupType.ERROR,
									Lang.get("account_login_error"), Lang.get("authentification_failed"));

						}
					} catch (ClientProtocolException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
		});
	}

	@Override
	public Pane getContent() {
		return this;
	}

	public JFXCheckBox getSaveAuthCheckbox() {
		return stay_connected_checkbox;
	}

	public JFXTextField getEmailField() {
		return email_field;
	}

	public JFXPasswordField getPasswordField() {
		return password_field;
	}

	private void addNews() {
		News[] news = LauncherApplication.news();
		
		AnchorPane news_1 = new AnchorPane();
		news_1.getStyleClass().add("news");
		news_1.setMinHeight(320);
		if(news.length > 0 && news[0] != null){
			news_1.getChildren().add(title(news[0].getTitle(), news[0].getSubtitle(), news[0].getDate()));
			news_1.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					try {
						Desktop.getDesktop().browse(new URI(news[0].getUrl()));
					} catch (IOException e) {
						e.printStackTrace();
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}
				}
			});
		}
		
		VBox vBox = new VBox(10);

		AnchorPane.setLeftAnchor(vBox, 0D);
		AnchorPane.setRightAnchor(vBox, 0D);
		AnchorPane.setTopAnchor(vBox, 0D);
		AnchorPane.setBottomAnchor(vBox, 0D);
		
		vBox.getChildren().add(news_1);
		
		HBox hBox = new HBox(10);

		AnchorPane news_2 = new AnchorPane();
		news_2.setMinHeight(200);
		news_2.setMinWidth(355);
		news_2.getStyleClass().add("news");
		if(news.length > 1 && news[1] != null){
			news_2.getChildren().add(title(news[1].getTitle(), news[1].getSubtitle(), news[1].getDate()));
			news_2.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					try {
						Desktop.getDesktop().browse(new URI(news[1].getUrl()));
					} catch (IOException e) {
						e.printStackTrace();
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}
				}
			});
		}

		AnchorPane news_3 = new AnchorPane();
		news_3.setMinHeight(200);
		news_3.getStyleClass().add("news");
		news_3.setMinWidth(355);
		if(news.length > 2 && news[2] != null){
			news_3.getChildren().add(title(news[2].getTitle(), news[2].getSubtitle(), news[2].getDate()));
			news_3.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					try {
						Desktop.getDesktop().browse(new URI(news[2].getUrl()));
					} catch (IOException e) {
						e.printStackTrace();
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}
				}
			});
		}

		animation(news_1);
		animation(news_2);
		animation(news_3);
		
		hBox.getChildren().add(news_2);
		hBox.getChildren().add(news_3);
		
		vBox.getChildren().add(hBox);
		
		this.news_pane.getChildren().add(vBox);
	}
	
	private VBox title(String title, String  subtitle, long date) {
		VBox vbox = new VBox(0);
		vbox.setAlignment(Pos.BOTTOM_LEFT);
		
		vbox.setPadding(new Insets(10,10,10,10));

		DropShadow dropShadow = new DropShadow();
		dropShadow.setRadius(5.0);
		dropShadow.setOffsetX(1.90);
		dropShadow.setOffsetY(1.90);
		dropShadow.setColor(Color.BLACK);
		
		Text title_text = new Text(title);
		title_text.setFont(ResourceManager.getFont(22));
		title_text.setStyle("-fx-fill: #FFFFFF;");
		title_text.setEffect(dropShadow);
		
		Text subtitle_text = new Text(subtitle);
		subtitle_text.setFont(ResourceManager.getFont(16));
		subtitle_text.setStyle("-fx-fill: #FFFFFF;");
		subtitle_text.setEffect(dropShadow);
		
		Text date_text = new Text(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(date));
		date_text.setFont(ResourceManager.getFont(16));
		date_text.setStyle("-fx-fill: #FFFFFF;");
		date_text.setEffect(dropShadow);

		vbox.getChildren().add(title_text);
		vbox.getChildren().add(subtitle_text);
		vbox.getChildren().add(date_text);
		
		AnchorPane.setLeftAnchor(vbox, 0D);
		AnchorPane.setBottomAnchor(vbox, 0D);
		
		return vbox;
	}
	
	private void animation(AnchorPane pane) {
		ScaleTransition enterTransition = new ScaleTransition(Duration.millis(100), pane);
		ScaleTransition exitTransition = new ScaleTransition(Duration.millis(100), pane);
		
		pane.setOnMouseEntered(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				exitTransition.stop();
				
				enterTransition.setFromX(getScaleX());
				enterTransition.setFromY(getScaleY());
				enterTransition.setToX(1.025);
				enterTransition.setToY(1.025);
				enterTransition.playFromStart();
			};
		});
		
		pane.setOnMouseExited(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				enterTransition.stop();

				exitTransition.setFromX(1.025);
                exitTransition.setFromY(1.025);
                exitTransition.setToX(getScaleX());
                exitTransition.setToY(getScaleY());
                exitTransition.playFromStart();
			};
		});	
	}
	}
