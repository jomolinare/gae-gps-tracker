package net.android.xmpp;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import java.util.HashMap;
import java.util.UUID;

public class myTextToSpeech implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener {

    private TextToSpeech TTS;
    private final Object lock = new Object();
    
    public myTextToSpeech(Context context) {
        TTS = new TextToSpeech(context,this);
    }
    public void onInit(int status ) {
        if (status == TextToSpeech.SUCCESS)
            TTS.setOnUtteranceCompletedListener(this);
    }
    public void speak(String message) {
        TTS.speak(message, TextToSpeech.QUEUE_ADD, null);
    }
    public void speakAndWait(String message) {
        String ID = UUID.randomUUID().toString();
        synchronized (lock) {
            HashMap<String, String> params = new HashMap<String, String>(); 
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, ID); 
            TTS.speak(message, TextToSpeech.QUEUE_ADD, params);
            try {lock.wait();} catch (Exception e) { }
        }
    }
    public void onUtteranceCompleted(String ID) {
        synchronized (lock) {
            lock.notifyAll();
        }
    }
}
/*
public void setLanguage(String language) {
    TTS.setLanguage(new Locale(language));
}
public String[] getLanguages() {
    ArrayList<String> languages = new ArrayList<String>();
    for (Locale locale : Locale.getAvailableLocales()) {
        if (TTS.isLanguageAvailable(locale) == TextToSpeech.LANG_AVAILABLE) {
            String language = locale.getDisplayLanguage()+'/'+locale.getLanguage();
            if (!languages.contains(language)) languages.add(language);
        }
    }
    return languages.toArray(new String[languages.size()]);
}
*/
