package isotopestudio.backdoor.launcher;

import java.awt.MouseInfo;

import com.jfoenix.controls.JFXButton;

import isotopestudio.backdoor.launcher.interfaces.Interface;
import isotopestudio.backdoor.launcher.lang.Lang;
import isotopestudio.backdoor.launcher.popup.PopupType;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class LauncherFrame {

	public String title;
	public int width;
	public int height;
	public Image icon;

	public Stage stage;
	public AnchorPane root = new AnchorPane();
	public Scene scene;

	public LauncherFrame(Stage stage, String title, int width, int height) {
		this.stage = stage;
		this.title = title;
		this.width = width;
		this.height = height;
	}

	public void setRoot(AnchorPane root) {
		this.root = root;
	}

	public void setIcon(Image image) {
		this.icon = image;
	}

	private void setTitlebar() {
		AnchorPane pane = new AnchorPane();
		pane.getStyleClass().add("titlebar");
		pane.setEffect(new DropShadow());

		AnchorPane.setTopAnchor(pane, 0D);
		AnchorPane.setRightAnchor(pane, 0D);
		AnchorPane.setLeftAnchor(pane, 0D);

		pane.setMinHeight(30D);
		pane.getStylesheets().add("styles/titlebar.css");

		EventHandler<MouseEvent> event = new EventHandler<MouseEvent>() {

			private java.awt.Point click;

			@Override
			public void handle(MouseEvent event) {
				if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
					click = null;
				}

				if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
					click = new java.awt.Point((int) event.getX(), (int) event.getY());
				}

				if (event.getEventType() == MouseEvent.MOUSE_DRAGGED && click != null) {
					java.awt.Point draggedPoint = MouseInfo.getPointerInfo().getLocation();

					LauncherFrame.this.stage.setX((int) draggedPoint.getX() - (int) click.getX());
					LauncherFrame.this.stage.setY((int) draggedPoint.getY() - (int) click.getY());
				}
			}
		};

		Circle close = new Circle(0, 0, 8);
		close.getStyleClass().add("close-button");

		close.setOnMouseEntered(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				scene.setCursor(Cursor.HAND);
			};
		});

		close.setOnMouseExited(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				scene.setCursor(Cursor.DEFAULT);
			};
		});

		close.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				LauncherApplication.APPLICATION.exit();
			};
		});

		Circle hide = new Circle(0, 0, 8);
		hide.getStyleClass().add("hide-button");

		hide.setOnMouseEntered(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				scene.setCursor(Cursor.HAND);
			};
		});

		hide.setOnMouseExited(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				scene.setCursor(Cursor.DEFAULT);
			};
		});

		hide.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				LauncherApplication.APPLICATION.getLauncherFrame().stage.setIconified(true);
				;
			};
		});

		AnchorPane.setTopAnchor(close, 8D);
		AnchorPane.setRightAnchor(close, 8D);

		AnchorPane.setTopAnchor(hide, 8D);
		AnchorPane.setRightAnchor(hide, 30D);

		pane.setOnMousePressed(event);
		pane.setOnMouseReleased(event);
		pane.setOnMouseDragged(event);

		root.getChildren().add(pane);
		pane.getChildren().add(close);
		pane.getChildren().add(hide);
	}

	public void popup(PopupType type, String title, String message) {
		VBox popup = new VBox();
		popup.getStylesheets().add("styles/popup.css");
		popup.getStyleClass().add("my-popup");
		popup.setAlignment(Pos.CENTER);

		AnchorPane.setTopAnchor(popup, 30D);
		AnchorPane.setBottomAnchor(popup, 0D);
		AnchorPane.setRightAnchor(popup, 0D);
		AnchorPane.setLeftAnchor(popup, 0D);

		BorderPane popup_box = new BorderPane();
		
		popup_box.setPrefWidth(200);
		popup_box.getStyleClass().add(type.getClassStyle());
		popup_box.setPadding(new Insets(10, 10, 10, 10));

		ImageView close = new ImageView(new Image(LauncherApplication.getResource("/images/icons/close.png")));
		close.setFitHeight(13);
		close.setFitWidth(13);
		close.getStyleClass().add("close-button");
		
		popup_box.setMargin(close, new Insets(5,0,0,10));

		close.setOnMouseEntered(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				scene.setCursor(Cursor.HAND);
			};
		});

		close.setOnMouseExited(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				scene.setCursor(Cursor.DEFAULT);
			};
		});

		close.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				root.getChildren().remove(popup);
			};
		});

		Text title_text = new Text(title);
		title_text.setFont(ResourceManager.getFont(10));
		title_text.setSmooth(true);
		title_text.getStyleClass().add("title");
		
		Text message_text = new Text(message);
		message_text.setFont(ResourceManager.getFont(10));
		message_text.getStyleClass().add("message");
		message_text.setSmooth(true);

		title_text.applyCss();
		message_text.applyCss();

		popup_box.setMaxWidth(
				message_text.getBoundsInLocal().getWidth() > title_text.getBoundsInLocal().getWidth()
				? message_text.getBoundsInLocal().getWidth()
				: title_text.getBoundsInLocal().getWidth());

		popup_box.setRight(close);
		popup_box.setLeft(title_text);
		popup_box.setBottom(message_text);
		
		popup.getChildren().add(popup_box);
		root.getChildren().add(popup);
	}

	private Interface content;

	public void setContent(Interface pane) {
		AnchorPane.setTopAnchor(pane.getContent(), 20D);
		AnchorPane.setBottomAnchor(pane.getContent(), 0D);
		AnchorPane.setRightAnchor(pane.getContent(), 0D);
		AnchorPane.setLeftAnchor(pane.getContent(), 0D);

		if (content != null)
			root.getChildren().remove(content.getContent());

		root.getChildren().add(pane.getContent());

		this.content = pane;
		this.content.load();
	}

	public void close() {
		this.stage.close();
	}

	public void show() {
		this.stage.setTitle(this.title);
		this.stage.setScene((this.scene = new Scene(this.root, this.width, this.height)));
		if (this.icon != null) {
			this.stage.getIcons().add(this.icon);
		}
		this.stage.initStyle(StageStyle.UNDECORATED);
		Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
		this.stage.setX((primScreenBounds.getWidth() / 2) - (this.width / 2));
		this.stage.setY((primScreenBounds.getHeight() / 2) - (this.height / 2));

		this.stage.show();

		setTitlebar();
	}
}
