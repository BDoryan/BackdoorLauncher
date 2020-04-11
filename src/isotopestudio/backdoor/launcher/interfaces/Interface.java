package isotopestudio.backdoor.launcher.interfaces;

import javafx.scene.layout.Pane;

public interface Interface {

	public static final Interface LOGIN = new LoginInterface();

	public abstract Pane getContent();
	public abstract void load();
	
}
