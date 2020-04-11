package isotopestudio.backdoor.launcher.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonUtils {

	private static Gson instance;
	
	public static Gson instance() {
		return instance != null ? instance : new GsonBuilder().create();
	}
}
