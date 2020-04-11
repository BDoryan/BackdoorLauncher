package isotopestudio.backdoor.launcher.popup;

public enum PopupType {
	
	SUCCESS("popup-success"),
	INFO("popup-info"),
	ERROR("popup-error"),
	WARNING("popup-warning");

	private String class_style;

	private PopupType(String class_style) {
		this.class_style = class_style;
	}

	public String getClassStyle() {
		return class_style;
	}
}
