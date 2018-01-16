package com.example.jean.jcplayer;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.jean.jcplayer.JcPlayerExceptions.AudioListNullPointerException;

import java.util.ArrayList;
import java.util.List;

public class JcPlayerView extends LinearLayout implements
        View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = JcPlayerView.class.getSimpleName();

    private static final int PULSE_ANIMATION_DURATION = 200;
    private static final int TITLE_ANIMATION_DURATION = 600;

    private TextView txtCurrentMusic;
    private ImageButton btnPrev;
    private ImageButton btnPlay;
    private ProgressBar progressBarPlayer;
    private JcAudioPlayer jcAudioPlayer;
    private TextView txtDuration;
    private ImageButton btnNext;
    private SeekBar seekBar;
    private TextView txtCurrentDuration;
    private boolean isInitialized;

    private OnInvalidPathListener onInvalidPathListener = new OnInvalidPathListener() {
        @Override
        public void onPathError(JcAudio jcAudio) {
            dismissProgressBar();
        }
    };

    JcPlayerViewServiceListener jcPlayerViewServiceListener = new JcPlayerViewServiceListener() {

        @Override
        public void onPreparedAudio(String audioName, int duration) {
            dismissProgressBar();
            resetPlayerInfo();

            long aux = duration / 1000;
            int minute = (int) (aux / 60);
            int second = (int) (aux % 60);

            final String sDuration = // Minutes
                    (minute < 10 ? "0" + minute : minute + "")
                            + ":" +
                            // Seconds
                            (second < 10 ? "0" + second : second + "");

            seekBar.setMax(duration);

            txtDuration.post(new Runnable() {
                @Override
                public void run() {
                    txtDuration.setText(sDuration);
                }
            });
        }

        @Override
        public void onCompletedAudio() {
            resetPlayerInfo();

            try {
                jcAudioPlayer.nextAudio();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onPaused() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                btnPlay.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.ic_play_black, null));
            } else {
                btnPlay.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.ic_play_black, null));
            }
            btnPlay.setTag(R.drawable.ic_play_black);
        }

        @Override
        public void onContinueAudio() {
            dismissProgressBar();
        }

        @Override
        public void onPlaying() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                btnPlay.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.ic_pause_black, null));
            } else {
                btnPlay.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.ic_pause_black, null));
            }
            btnPlay.setTag(R.drawable.ic_pause_black);
        }

        @Override
        public void onTimeChanged(long currentPosition) {
            long aux = currentPosition / 1000;
            int minutes = (int) (aux / 60);
            int seconds = (int) (aux % 60);
            final String sMinutes = minutes < 10 ? "0" + minutes : minutes + "";
            final String sSeconds = seconds < 10 ? "0" + seconds : seconds + "";

            seekBar.setProgress((int) currentPosition);
            txtCurrentDuration.post(new Runnable() {
                @Override
                public void run() {
                    txtCurrentDuration.setText(String.valueOf(sMinutes + ":" + sSeconds));
                }
            });
        }

        @Override
        public void updateTitle(final String title) {
//            final String mTitle = title;

            YoYo.with(Techniques.FadeInLeft)
                    .duration(TITLE_ANIMATION_DURATION)
                    .playOn(txtCurrentMusic);

            txtCurrentMusic.post(new Runnable() {
                @Override
                public void run() {
                    txtCurrentMusic.setText(title);
                }
            });
        }
    };

    //JcPlayerViewStatusListener jcPlayerViewStatusListener = new JcPlayerViewStatusListener() {
    //
    //    @Override public void onPausedStatus(JcStatus jcStatus) {
    //
    //    }
    //
    //    @Override public void onContinueAudioStatus(JcStatus jcStatus) {
    //
    //    }
    //
    //    @Override public void onPlayingStatus(JcStatus jcStatus) {
    //
    //    }
    //
    //    @Override public void onTimeChangedStatus(JcStatus jcStatus) {
    //        Log.d(TAG, "song id = " + jcStatus.getJcAudio().getId() + ", position = " + jcStatus.getCurrentPosition());
    //    }
    //
    //    @Override public void onCompletedAudioStatus(JcStatus jcStatus) {
    //
    //    }
    //
    //    @Override public void onPreparedAudioStatus(JcStatus jcStatus) {
    //
    //    }
    //};

    public interface OnInvalidPathListener {
        void onPathError(JcAudio jcAudio);
    }

    public interface JcPlayerViewStatusListener {
        void onPausedStatus(JcStatus jcStatus);

        void onContinueAudioStatus(JcStatus jcStatus);

        void onPlayingStatus(JcStatus jcStatus);

        void onTimeChangedStatus(JcStatus jcStatus);

        void onCompletedAudioStatus(JcStatus jcStatus);

        void onPreparedAudioStatus(JcStatus jcStatus);
    }

    public interface JcPlayerViewServiceListener {
        void onPreparedAudio(String audioName, int duration);

        void onCompletedAudio();

        void onPaused();

        void onContinueAudio();

        void onPlaying();

        void onTimeChanged(long currentTime);

        void updateTitle(String title);
    }

    public JcPlayerView(Context context) {
        super(context);
        init();
    }

    public JcPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public JcPlayerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void init() {
        inflate(getContext(), R.layout.view_jcplayer, this);

        this.progressBarPlayer = (ProgressBar) findViewById(R.id.progress_bar_player);
        this.btnNext = (ImageButton) findViewById(R.id.btn_next);
        this.btnPrev = (ImageButton) findViewById(R.id.btn_prev);
        this.btnPlay = (ImageButton) findViewById(R.id.btn_play);
        this.txtDuration = (TextView) findViewById(R.id.txt_total_duration);
        this.txtCurrentDuration = (TextView) findViewById(R.id.txt_current_duration);
        this.txtCurrentMusic = (TextView) findViewById(R.id.txt_current_music);
        this.seekBar = (SeekBar) findViewById(R.id.seek_bar);
        this.btnPlay.setTag(R.drawable.ic_play_black);

        btnNext.setOnClickListener(this);
        btnPrev.setOnClickListener(this);
        btnPlay.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);
    }

    /**
     * Initialize the playlist and controls.
     *
     * @param playlist List of JcAudio objects that you want play
     */
    public void initPlaylist(List<JcAudio> playlist) {
        // Don't sort if the playlist have position number.
        // We need to do this because there is a possibility that the user reload previous playlist
        // from persistence storage like sharedPreference or SQLite.
        if (!isAlreadySorted(playlist)) {
            sortPlaylist(playlist);
        }
        jcAudioPlayer = new JcAudioPlayer(getContext(), playlist, jcPlayerViewServiceListener);
        jcAudioPlayer.registerInvalidPathListener(onInvalidPathListener);
        //jcAudioPlayer.registerStatusListener(jcPlayerViewStatusListener);
        isInitialized = true;
    }

    /**
     * Initialize an anonymous playlist with a default JcPlayer title for all audios
     *
     * @param playlist List of urls strings
     */
    public void initAnonPlaylist(List<JcAudio> playlist) {
        sortPlaylist(playlist);
        generateTitleAudio(playlist, getContext().getString(R.string.track_number));
        jcAudioPlayer = new JcAudioPlayer(getContext(), playlist, jcPlayerViewServiceListener);
        jcAudioPlayer.registerInvalidPathListener(onInvalidPathListener);
        //jcAudioPlayer.registerStatusListener(jcPlayerViewStatusListener);
        isInitialized = true;
    }

    /**
     * Initialize an anonymous playlist, but with a custom title for all audios
     *
     * @param playlist List of JcAudio files.
     * @param title    Default title for all audios
     */
    public void initWithTitlePlaylist(List<JcAudio> playlist, String title) {
        sortPlaylist(playlist);
        generateTitleAudio(playlist, title);
        jcAudioPlayer = new JcAudioPlayer(getContext(), playlist, jcPlayerViewServiceListener);
        jcAudioPlayer.registerInvalidPathListener(onInvalidPathListener);
        //jcAudioPlayer.registerStatusListener(jcPlayerViewStatusListener);
        isInitialized = true;
    }

    //TODO: Should we expose this to user?
    // A: Yes, because the user can add files to playlist without creating a new List of JcAudio
    // objects, just adding this files dynamically.

    /**
     * Add an audio for the playlist. We can track the JcAudio by
     * its id. So here we returning its id after adding to list.
     *
     * @param jcAudio audio file generated from {@link JcAudio}
     * @return id of jcAudio.
     */
    public long addAudio(JcAudio jcAudio) {
        createJcAudioPlayer();
        List<JcAudio> playlist = jcAudioPlayer.getPlaylist();
        int lastPosition = playlist.size();

        jcAudio.setId(lastPosition + 1);
        jcAudio.setPosition(lastPosition + 1);

        if (!playlist.contains(jcAudio)) {
            playlist.add(lastPosition, jcAudio);
        }
        return jcAudio.getId();
    }

    /**
     * Remove an audio for the playlist
     *
     * @param jcAudio JcAudio object
     */
    public void removeAudio(JcAudio jcAudio) {
        if (jcAudioPlayer != null) {
            List<JcAudio> playlist = jcAudioPlayer.getPlaylist();

            if (playlist != null && playlist.contains(jcAudio)) {
                if (playlist.size() > 1) {
                    // play next audio when currently played audio is removed.
                    if (jcAudioPlayer.isPlaying()) {
                        if (jcAudioPlayer.getCurrentAudio().equals(jcAudio)) {
                            playlist.remove(jcAudio);
                            pause();
                            resetPlayerInfo();
                        } else {
                            playlist.remove(jcAudio);
                        }
                    } else {
                        playlist.remove(jcAudio);
                    }
                } else {
                    //TODO: Maybe we need jcAudioPlayer.stopPlay() for stopping the player
                    playlist.remove(jcAudio);
                    pause();
                    resetPlayerInfo();
                }
            }
        }
    }


    public void playAudio(JcAudio jcAudio) {
        showProgressBar();
        createJcAudioPlayer();
        if (!jcAudioPlayer.getPlaylist().contains(jcAudio))
            jcAudioPlayer.getPlaylist().add(jcAudio);

        try {
            jcAudioPlayer.playAudio(jcAudio);
        } catch (AudioListNullPointerException e1) {
            dismissProgressBar();
            e1.printStackTrace();
        } catch (NullPointerException e2) {
            // Service is not bounded yet.
            jcAudioPlayer.lazyPlayAudio(jcAudio);
        }
    }

    public void next() {
        if (jcAudioPlayer.getCurrentAudio() == null) {
            return;
        }
        resetPlayerInfo();
        showProgressBar();

        try {
            jcAudioPlayer.nextAudio();
        } catch (AudioListNullPointerException e) {
            dismissProgressBar();
            e.printStackTrace();
        }
    }

    public void continueAudio() {
        showProgressBar();

        try {
            jcAudioPlayer.continueAudio();
        } catch (AudioListNullPointerException e) {
            dismissProgressBar();
            e.printStackTrace();
        }
    }

    public void pause() {
        jcAudioPlayer.pauseAudio();
    }

    public void previous() {
        resetPlayerInfo();
        showProgressBar();

        try {
            jcAudioPlayer.previousAudio();
        } catch (AudioListNullPointerException e) {
            dismissProgressBar();
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        if (isInitialized) {
            if (view.getId() == R.id.btn_play) {
                YoYo.with(Techniques.Pulse)
                        .duration(PULSE_ANIMATION_DURATION)
                        .playOn(btnPlay);

                if (btnPlay.getTag().equals(R.drawable.ic_pause_black)) {
                    pause();
                } else {
                    continueAudio();
                }
            }
        }
        if (view.getId() == R.id.btn_next) {
            YoYo.with(Techniques.Pulse)
                    .duration(PULSE_ANIMATION_DURATION)
                    .playOn(btnNext);
            next();
        }

        if (view.getId() == R.id.btn_prev) {
            YoYo.with(Techniques.Pulse)
                    .duration(PULSE_ANIMATION_DURATION)
                    .playOn(btnPrev);
            previous();
        }
    }

    /**
     * Create a notification player with same playlist with a custom icon.
     *
     * @param iconResource icon path.
     */
    public void createNotification(int iconResource) {
        if (jcAudioPlayer != null) jcAudioPlayer.createNewNotification(iconResource);
    }

    /**
     * Create a notification player with same playlist with a default icon
     */
    public void createNotification() {
        if (jcAudioPlayer != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // For light theme
                jcAudioPlayer.createNewNotification(R.drawable.ic_notification_default_black);
            } else {
                // For dark theme
                jcAudioPlayer.createNewNotification(R.drawable.ic_notification_default_white);
            }
        }
    }

    public List<JcAudio> getMyPlaylist() {
        return jcAudioPlayer.getPlaylist();
    }

    public boolean isPlaying() {
        return jcAudioPlayer.isPlaying();
    }

    public boolean isPaused() {
        return jcAudioPlayer.isPaused();
    }

    public JcAudio getCurrentAudio() {
        return jcAudioPlayer.getCurrentAudio();
    }

    private void createJcAudioPlayer() {
        if (jcAudioPlayer == null) {
            List<JcAudio> playlist = new ArrayList<>();
            jcAudioPlayer = new JcAudioPlayer(getContext(), playlist, jcPlayerViewServiceListener);
        }
        jcAudioPlayer.registerInvalidPathListener(onInvalidPathListener);
        //jcAudioPlayer.registerStatusListener(jcPlayerViewStatusListener);
        isInitialized = true;
    }

    private void sortPlaylist(List<JcAudio> playlist) {
        for (int i = 0; i < playlist.size(); i++) {
            JcAudio jcAudio = playlist.get(i);
            jcAudio.setId(i);
            jcAudio.setPosition(i);
        }
    }

    /**
     * Check if playlist already sorted or not.
     * We need to check because there is a possibility that the user reload previous playlist
     * from persistence storage like sharedPreference or SQLite.
     *
     * @param playlist list of JcAudio
     * @return true if sorted, false if not.
     */
    private boolean isAlreadySorted(List<JcAudio> playlist) {
        // If there is position in the first audio, then playlist is already sorted.
        if (playlist != null) {
            if (playlist.get(0).getPosition() != -1) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private void generateTitleAudio(List<JcAudio> playlist, String title) {
        for (int i = 0; i < playlist.size(); i++) {
            if (title.equals(getContext().getString(R.string.track_number))) {
                playlist.get(i).setTitle(getContext().getString(R.string.track_number) + " " + String.valueOf(i + 1));
            } else {
                playlist.get(i).setTitle(title);
            }
        }
    }

    private void showProgressBar() {
        progressBarPlayer.setVisibility(ProgressBar.VISIBLE);
        btnPlay.setVisibility(Button.GONE);
        btnNext.setClickable(false);
        btnPrev.setClickable(false);
    }

    private void dismissProgressBar() {
        progressBarPlayer.setVisibility(ProgressBar.GONE);
        btnPlay.setVisibility(Button.VISIBLE);
        btnNext.setClickable(true);
        btnPrev.setClickable(true);
    }

    private void resetPlayerInfo() {
        seekBar.setProgress(0);
        txtCurrentMusic.setText("");
        txtCurrentDuration.setText(getContext().getString(R.string.play_initial_time));
        txtDuration.setText(getContext().getString(R.string.play_initial_time));
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
        if (fromUser && jcAudioPlayer != null) jcAudioPlayer.seekTo(i);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        showProgressBar();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        dismissProgressBar();
    }

    public void registerInvalidPathListener(OnInvalidPathListener registerInvalidPathListener) {
        if (jcAudioPlayer != null) {
            jcAudioPlayer.registerInvalidPathListener(registerInvalidPathListener);
        }
    }

    public void kill() {
        if (jcAudioPlayer != null) jcAudioPlayer.kill();
    }

    public void registerServiceListener(JcPlayerViewServiceListener jcPlayerServiceListener) {
        if (jcAudioPlayer != null) {
            jcAudioPlayer.registerServiceListener(jcPlayerServiceListener);
        }
    }

    public void registerStatusListener(JcPlayerViewStatusListener statusListener) {
        if (jcAudioPlayer != null) {
            jcAudioPlayer.registerStatusListener(statusListener);
        }
    }

}
