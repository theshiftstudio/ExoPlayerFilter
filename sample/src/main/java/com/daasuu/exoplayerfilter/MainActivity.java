package com.daasuu.exoplayerfilter;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.daasuu.epf.EPlayerView;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final int PICK_MEDIA = 1;

    private DataSource.Factory dataSourceFactory;

    private EPlayerView ePlayerView;
    private SimpleExoPlayer player;
    private Button button;
    private Button pickVideo;
    private SeekBar seekBar;
    private PlayerTimer playerTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupViews();
        setupSimpleExoPlayer();
        setupGlPlayerView();
        setupTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
        if (playerTimer != null) {
            playerTimer.stop();
            playerTimer.removeMessages(0);
        }
    }

    private void setupViews() {
        // play pause
        button = (Button) findViewById(R.id.btn);
        pickVideo = ((Button) findViewById(R.id.pick));

        View tools = findViewById(R.id.tools);
        ToggleButton toolsToggle = ((ToggleButton) findViewById(R.id.tools_toggle));

        toolsToggle.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                tools.setVisibility(View.VISIBLE);
            } else {
                tools.setVisibility(View.GONE);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player == null) return;

                if (button.getText().toString().equals(MainActivity.this.getString(R.string.pause))) {
                    player.setPlayWhenReady(false);
                    button.setText(R.string.play);
                } else {
                    player.setPlayWhenReady(true);
                    button.setText(R.string.pause);
                }
            }
        });

        pickVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickVideo();
            }
        });



        // seek
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (player == null) return;

                if (!fromUser) {
                    // We're not interested in programmatically generated changes to
                    // the progress bar's position.
                    return;
                }

                player.seekTo(progress * 1000);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // do nothing
            }
        });

        // list
        ListView listView = (ListView) findViewById(R.id.list);
        final List<FilterType> filterTypes = FilterType.createFilterList();
        listView.setAdapter(new FilterAdapter(this, R.layout.row_text, filterTypes));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ePlayerView.setGlFilter(FilterType.createGlFilter(filterTypes.get(position), getApplicationContext()));
            }
        });
    }


    private void setupSimpleExoPlayer() {


        // Produces DataSource instances through which media data is loaded.
        dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "yourApplicationName"));

        player = ExoPlayerFactory.newSimpleInstance(this);

    }


    private void setupGlPlayerView() {
        ePlayerView = new EPlayerView(this);
        ePlayerView.setSimpleExoPlayer(player);
        ePlayerView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ((MovieWrapperView) findViewById(R.id.layout_movie_wrapper)).addView(ePlayerView);
        ePlayerView.onResume();
    }


    private void setupTimer() {
        playerTimer = new PlayerTimer();
        playerTimer.setCallback(new PlayerTimer.Callback() {
            @Override
            public void onTick(long timeMillis) {
                long position = player.getCurrentPosition();
                long duration = player.getDuration();

                if (duration <= 0) return;

                seekBar.setMax((int) duration / 1000);
                seekBar.setProgress((int) position / 1000);
            }
        });
        playerTimer.start();
    }


    private void releasePlayer() {
        ePlayerView.onPause();
        ((MovieWrapperView) findViewById(R.id.layout_movie_wrapper)).removeAllViews();
        ePlayerView = null;
        player.stop();
        player.release();
        player = null;
    }

    private void pickVideo() {
        pickMedia("video/*");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data.getData() != null) {
            Log.i("FILTERS", "onActivityResult: "+data.getData());
            ProgressiveMediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(data.getData());
            player.prepare(mediaSource);
            player.setPlayWhenReady(true);
        }
    }

    private void pickMedia(@NonNull String type) {

        Intent intent = new Intent();
        intent.setType(type);
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        startActivityForResult(Intent.createChooser(intent, "Pick Media"), PICK_MEDIA);
    }

}