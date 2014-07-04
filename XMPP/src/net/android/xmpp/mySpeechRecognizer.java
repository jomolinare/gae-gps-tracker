package net.android.xmpp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

public class mySpeechRecognizer implements RecognitionListener {

    private SpeechRecognizer SR;

    public mySpeechRecognizer(Context context) {
        SR = SpeechRecognizer.createSpeechRecognizer(context);
        SR.setRecognitionListener(this);
    }
    public interface CallBack {
        public void process(java.util.List<String> phrases);
        public void error(String message);
    }
    private CallBack callback;
    public void listen(CallBack callback) {
        Intent I = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        I.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
        I.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        this.callback = callback;
        SR.startListening(I);
    }
    public void onResults(Bundle results) {
        callback.process(results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION));
    }
    public void onError(int code) { 
        String error = "Unknown error";
        switch (code) {
            case SpeechRecognizer.ERROR_AUDIO:
                error="Audio recording error"; break;
            case SpeechRecognizer.ERROR_CLIENT:
                error="Other client side errors"; break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                error="Insufficient permissions"; break;
            case SpeechRecognizer.ERROR_NETWORK:
                error="Other network related errors"; break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                error="Network operation timed out"; break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                error="No recognition result matched"; break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                error="RecognitionService busy"; break;
            case SpeechRecognizer.ERROR_SERVER:
                error="Server sends error status"; break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                error="No speech input"; break;
        }
        callback.error("ERROR: "+error);
    }
    public void onReadyForSpeech(Bundle x) { }
    public void onBeginningOfSpeech() { }
    public void onRmsChanged(float x) { }
    public void onBufferReceived(byte[] x) { }
    public void onEndOfSpeech() { }
    public void onPartialResults(Bundle x) { }
    public void onEvent(int v, Bundle x) { }
}