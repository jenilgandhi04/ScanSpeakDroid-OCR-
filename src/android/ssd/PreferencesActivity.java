
package android.ssd;

import android.ssd.language.LanguageCodeHelper;


import android.content.SharedPreferences;

import android.preference.EditTextPreference;
import android.preference.ListPreference;

public class PreferencesActivity {
  
  // Preference keys not carried over from ZXing project
  public static final String KEY_SOURCE_LANGUAGE_PREFERENCE = "sourceLanguageCodeOcrPref";
  public static final String KEY_TARGET_LANGUAGE_PREFERENCE = "targetLanguageCodeTranslationPref";
  public static final String KEY_TOGGLE_TRANSLATION = "preference_translation_toggle_translation";
  public static final String KEY_CONTINUOUS_PREVIEW = "preference_capture_continuous";
  public static final String KEY_PAGE_SEGMENTATION_MODE = "preference_page_segmentation_mode";
  public static final String KEY_OCR_ENGINE_MODE = "preference_ocr_engine_mode";
  public static final String KEY_CHARACTER_BLACKLIST = "preference_character_blacklist";
  public static final String KEY_CHARACTER_WHITELIST = "preference_character_whitelist";
  public static final String KEY_TOGGLE_LIGHT = "preference_toggle_light";
  public static final String KEY_TRANSLATOR = "preference_translator";
  
  // Preference keys carried over from ZXing project
  public static final String KEY_HELP_VERSION_SHOWN = "preferences_help_version_shown";
  public static final String KEY_NOT_OUR_RESULTS_SHOWN = "preferences_not_our_results_shown";
  public static final String KEY_REVERSE_IMAGE = "preferences_reverse_image";
  public static final String KEY_PLAY_BEEP = "preferences_play_beep";
  public static final String KEY_VIBRATE = "preferences_vibrate";

  public static final String TRANSLATOR_BING = "Bing Translator";
  public static final String TRANSLATOR_GOOGLE = "Google Translate";
  
  //private CheckBoxPreference checkBoxPreferenceContinuousPreview;
  private ListPreference listPreferenceSourceLanguage;
  //private CheckBoxPreference checkBoxTranslate;
  private ListPreference listPreferenceTargetLanguage;  
  private ListPreference listPreferenceTranslator;
  private ListPreference listPreferenceOcrEngineMode;
  //private CheckBoxPreference checkBoxBeep;
  private EditTextPreference editTextPreferenceCharacterBlacklist;
  private EditTextPreference editTextPreferenceCharacterWhitelist;
  private ListPreference listPreferencePageSegmentationMode;
  //private CheckBoxPreference checkBoxReversedImage;
  
  private static SharedPreferences sharedPreferences;
 
  
}