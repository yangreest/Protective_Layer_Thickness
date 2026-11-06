package com.example.gtj_sd_dg240.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;

import com.example.gtj_sd_dg240.R;

public class CustomMediaPlayer {

    public interface CallBack {
        void onCompletion();
    }

    public static MediaPlayer getMediaPlayer(CallBack callBack) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(mp -> {
            // 装载完毕回调
            mediaPlayer.start();
        });
        mediaPlayer.setOnCompletionListener(mp -> {
            // 在播放完毕被回调
            if (callBack != null) {
                callBack.onCompletion();
            }
        });
        return mediaPlayer;
    }

    /**
     * 播放
     *
     * @param type：1：采集提示音，2：报警声音
     */
    public static void startSpeaking(Context context, MediaPlayer mediaPlayer, int type) {
        AssetFileDescriptor file;
        switch (type) {
            case 1:
                file = getSpeakingDescriptor(context, "video_prompt");
                play(mediaPlayer, file);
                break;
            case 2:
                file = getSpeakingDescriptor(context, "alarm_01");
                play(mediaPlayer, file);
                break;
        }
    }

    private static void play(MediaPlayer mediaPlayer, AssetFileDescriptor file) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(),
                    file.getLength());
            file.close();
            // 通过异步的方式装载媒体资源
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static AssetFileDescriptor getSpeakingDescriptor(Context context, String content) {
        AssetFileDescriptor assetFileDescriptor = null;
        switch (content) {
            case "alarm_01":  //报警声音
                assetFileDescriptor = context.getResources().openRawResourceFd(R.raw.alarm_01);
                break;
            case "video_prompt":  //报警声音
                assetFileDescriptor = context.getResources().openRawResourceFd(R.raw.video_prompt);
                break;
        }
        return assetFileDescriptor;
    }

    /**
     * 释放播放器资源
     */
    public static void releasePlayer(MediaPlayer mediaPlayer) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
        }
    }
}
