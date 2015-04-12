package com.android.phoneassistant.manager;

import android.content.Context;
import android.media.AudioManager;

public class SoundManager {

    private static SoundManager sSoundManager = null;
    private AudioManager mAudioManager;
    private SoundManager(Context context) {
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }
    
    public static SoundManager get(Context context) {
        if (sSoundManager == null) {
            sSoundManager = new SoundManager(context);
        }
        return sSoundManager;
    }

    public void muteSound() {
        int ringerMode = mAudioManager.getRingerMode();
        if (mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
            mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            mAudioManager.setRingerMode(ringerMode);
        }
    }
}
