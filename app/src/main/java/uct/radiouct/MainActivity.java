package uct.radiouct;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Util;

public class MainActivity extends AppCompatActivity implements ExoPlayer.EventListener{

    // BARRA DE NOTIFICACIONES
    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;
    private int notification_id;
    private RemoteViews remoteViews;
    private Button notiButton;

    // ----

    //DECLARACION DE VARIABLES REPRODUCTOR
    private Handler mainHandler;
    private BandwidthMeter bandwidthMeter;
    private TrackSelector trackSelector;
    private TrackSelection.Factory trackSelectionFactory;
    private LoadControl loadControl;
    private SimpleExoPlayer player;
    private DataSource.Factory dataSourceFactory;
    private ExtractorsFactory extractorsFactory;
    private MediaSource mediaSource;

    private String radioUrl = "http://audio.uctradio.cl/radio";

    private Button mainButton;
    private Button c1;
    private Button c2;
    private Button c3;
    private Button c4;
    enum CANALES{
        MAIN,
        CHANNEL1,
        CHANNEL2,
        CHANNEL3,
        CHANNEL4,
        DEFAULT
    }

    CANALES canales;



    // Enum utilizado para facilitar la comprension de los canales
    // cada vez que se aprete un boton se asiganara a enum el canal del boton
    // presionado, de esta manera si se apreta nuevamente el mismo boton se hara
    // un checkeo para saber si es que dicho boton se apreto anteriormente
    // si un boton se presiona dos veces, esto parara el sonido
    // el valor default se asigna ya que no se puede presionar por segunda vez un boton en inicio.
    // -----------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ESTA ZONA ES REFERENTE A LA NOTIFICACION DE LA RADIO EN LA ZONA DE NOTIFICACIONES

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        remoteViews = new RemoteViews(getPackageName(), R.layout.radio_notification);

        remoteViews.setTextViewText(R.id.TitleNoti, "UCT Radio");
        remoteViews.setTextViewText(R.id.notif_title, "Universidad Católica de Temuco");

        notification_id = (int)System.currentTimeMillis();
        Intent suspended = new Intent("suspended");
        suspended.putExtra("id", notification_id);

        PendingIntent p_intent = PendingIntent.getBroadcast(this,123, suspended, 0);
        remoteViews.setOnClickPendingIntent(R.id.notiButton, p_intent);

        registerReceiver(broadcastReceiver, new IntentFilter("STOPRADIO"));

        // -----------------------------------------------------------------------------------------


        // ESTAS INSTANCIAS PERTENENCEN A LO QUE SE REFIERE AL STREAMING DE AUDIO Y SUS RESPECTIVOS-
        // BOTONES
        mainButton = (Button)findViewById(R.id.mainButton);
        c1 = (Button)findViewById(R.id.c1);
        c2 = (Button)findViewById(R.id.c2);
        c3 = (Button)findViewById(R.id.c3);
        c4 = (Button)findViewById(R.id.c4);

        canales = CANALES.DEFAULT;

        mainHandler = new Handler();
        bandwidthMeter = new DefaultBandwidthMeter();
        loadControl = new DefaultLoadControl();
        extractorsFactory =  new DefaultExtractorsFactory();

        trackSelectionFactory = new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);

        trackSelector = new DefaultTrackSelector(mainHandler,
                trackSelectionFactory);

        dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "RadioUCT"),
                (TransferListener<? super DataSource>) bandwidthMeter);


        mediaSource = new ExtractorMediaSource(Uri.parse("http://audio.uctradio.cl/radio"),
                dataSourceFactory,
                extractorsFactory,
                null,
                null);

        player = ExoPlayerFactory.newSimpleInstance(getApplicationContext(),
                trackSelector, loadControl);

        player.prepare(mediaSource);
        // -----------------------------------------------------------------------------------------

        // la secuencia de cada boton es la misma, solo que cambian las direcciones de donde
        // se recibe el sonido.
        mainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // se comienza con un verificador que dice, es este el mismo canal de
                // antes? -> si es asi, entonces que se detenga la transmision y asigna el canal
                // a default, de modo que si se presiona de nuevo este se reproduzca.
                // luego -> sino entonces, detiene la transmision anterior (en caso de que hubiera
                // alguna) y luego cambia la direccion de donde se recibe el sonido para luego
                // empezar nuevamente la transmision.
                // una vez empezo la transmision, asigna el canal actual como el canal que esta
                // "sonando" actualmente.
                if(canales == CANALES.MAIN){
                    player.setPlayWhenReady(false);
                    canales = CANALES.DEFAULT;
                }
                else{
                    player.setPlayWhenReady(false);
                    mediaSource = new ExtractorMediaSource(Uri.parse("http://audio.uctradio.cl/radio"),
                            dataSourceFactory,
                            extractorsFactory,
                            null,
                            null);

                    player = ExoPlayerFactory.newSimpleInstance(getApplicationContext(),
                            trackSelector, loadControl);

                    player.prepare(mediaSource);
                    player.setPlayWhenReady(true);
                    canales = CANALES.MAIN;
                    // setear canal que suena actualmente en el main en la notificacion
                    remoteViews.setTextViewText(R.id.notif_title, "UCTRadio MAIN");
                }

            }
        });
        c1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(canales == CANALES.CHANNEL1) {
                    player.setPlayWhenReady(false);
                    canales = CANALES.DEFAULT;
                }
                else
                {

                }
                player.setPlayWhenReady(false);
                mediaSource = new ExtractorMediaSource(Uri.parse("http://audio.uctradio.cl/radio1"),
                        dataSourceFactory,
                        extractorsFactory,
                        null,
                        null);

                player = ExoPlayerFactory.newSimpleInstance(getApplicationContext(),
                        trackSelector, loadControl);

                player.prepare(mediaSource);
                player.setPlayWhenReady(true);
                canales = CANALES.CHANNEL1;
                remoteViews.setTextViewText(R.id.notif_title, "UCTRadio CANAL 1");
            }
        });
        c2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(canales == CANALES.CHANNEL2) {
                    player.setPlayWhenReady(false);
                    canales = CANALES.DEFAULT;
                }
                else
                {
                    player.setPlayWhenReady(false);
                    mediaSource = new ExtractorMediaSource(Uri.parse("http://audio.uctradio.cl/radio2"),
                            dataSourceFactory,
                            extractorsFactory,
                            null,
                            null);

                    player = ExoPlayerFactory.newSimpleInstance(getApplicationContext(),
                            trackSelector, loadControl);

                    player.prepare(mediaSource);
                    player.setPlayWhenReady(true);
                    canales = CANALES.CHANNEL2;
                    remoteViews.setTextViewText(R.id.notif_title, "UCTRadio CANAL 2");
                }

            }
        });
        c3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(canales == CANALES.CHANNEL3) {
                    player.setPlayWhenReady(false);
                    canales = CANALES.DEFAULT;
                }
                else
                {
                    player.setPlayWhenReady(false);
                    mediaSource = new ExtractorMediaSource(Uri.parse("http://audio.uctradio.cl/radio3"),
                            dataSourceFactory,
                            extractorsFactory,
                            null,
                            null);

                    player = ExoPlayerFactory.newSimpleInstance(getApplicationContext(),
                            trackSelector, loadControl);

                    player.prepare(mediaSource);
                    player.setPlayWhenReady(true);
                    canales = CANALES.CHANNEL3;
                    remoteViews.setTextViewText(R.id.notif_title, "UCTRadio CANAL 3");
                }

            }
        });
        c4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(canales == CANALES.CHANNEL4) {
                    player.setPlayWhenReady(false);
                    canales = CANALES.DEFAULT;
                }
                else
                {
                    player.setPlayWhenReady(false);
                    mediaSource = new ExtractorMediaSource(Uri.parse("http://audio.uctradio.cl/radio4"),
                            dataSourceFactory,
                            extractorsFactory,
                            null,
                            null);

                    player = ExoPlayerFactory.newSimpleInstance(getApplicationContext(),
                            trackSelector, loadControl);

                    player.prepare(mediaSource);
                    player.setPlayWhenReady(true);
                    canales = CANALES.CHANNEL4;
                    remoteViews.setTextViewText(R.id.notif_title, "UCTRadio CANAL 4");
                }

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.setPlayWhenReady(false);
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(player.getPlayWhenReady()) {
            Intent noti_intent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT);

            builder = new NotificationCompat.Builder(this);
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setAutoCancel(true);
            builder.setCustomContentView(remoteViews);
            builder.setContentIntent(pendingIntent);
            builder.setOngoing(true);
            notificationManager.notify(notification_id, builder.build());
        }
    }

    @Override
    public void onBackPressed() {

        // este coido emula un "Home Button"
        // y ademas le quite el super.onBackPressed(), para que la app no termine la actividad
        // en clases superiores, de esta manera, la app sigue corriendo en segundo plano
        // y como consescuencia, la musica sigue sonando.

        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    // AQUI CORTAMOS EL AUDIO SI EL USUARIO PRESIONO EL BOTON DE DETENER RADIO

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            player.setPlayWhenReady(false);
            canales = CANALES.DEFAULT;
        }
    };
}



