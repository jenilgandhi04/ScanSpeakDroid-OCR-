
package android.ssd.language;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import android.ssd.PreferencesActivity;
import android.ssd.R;


public class LanguageCodeHelper {
	public static final String TAG = "LanguageCodeHelper";

	private LanguageCodeHelper() {
		throw new AssertionError();
	}

	
	public static String mapLanguageCode(String languageCode) {	  
		
		if (languageCode.equals("eng")) { // English
		      return "en";
	    }
		else {
	     return "";
	  }
	}

	
	public static String getOcrLanguageName(Context context, String languageCode) {
		Resources res = context.getResources();
		String[] language6393 = res.getStringArray(R.array.iso6393);
		String[] languageNames = res.getStringArray(R.array.languagenames);
		int len;

		
		for (len = 0; len < language6393.length; len++) {
			if (language6393[len].equals(languageCode)) {
				Log.d(TAG, "getOcrLanguageName: " + languageCode + "->"
						+ languageNames[len]);
				return languageNames[len];
			}
		}
		
		Log.d(TAG, "languageCode: Could not find language name for ISO 693-3: "
				+ languageCode);
		return languageCode;
	}
	
	
	public static String getTranslationLanguageName(Context context, String languageCode) {
    Resources res = context.getResources();
    String[] language6391 = res.getStringArray(R.array.translationtargetiso6391_google);
    String[] languageNames = res.getStringArray(R.array.translationtargetlanguagenames_google);
    int len;

   
    for (len = 0; len < language6391.length; len++) {
      if (language6391[len].equals(languageCode)) {
        Log.d(TAG, "getTranslationLanguageName: " + languageCode + "->" + languageNames[len]);
        return languageNames[len];
      }
    }
    
    
    language6391 = res.getStringArray(R.array.translationtargetiso6391_microsoft);
    languageNames = res.getStringArray(R.array.translationtargetlanguagenames_microsoft);
    for (len = 0; len < language6391.length; len++) {
      if (language6391[len].equals(languageCode)) {
        Log.d(TAG, "languageCode: " + languageCode + "->" + languageNames[len]);
        return languageNames[len];
      }
    }    
    
    Log.d(TAG, "getTranslationLanguageName: Could not find language name for ISO 693-1: " + 
            languageCode);
    return "";
	}

}
