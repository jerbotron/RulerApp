package jeremy.tinderruler;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;


public class RulerActivity extends AppCompatActivity {
    private MyCustomSurfaceView surfaceView;
    volatile SurfaceMode surfaceMode = SurfaceMode.DEFAULT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Hide status bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ruler);

        surfaceView = (MyCustomSurfaceView) findViewById(R.id.id_surface_view);

        final TextView measureButton = (TextView) findViewById(R.id.id_button_measure);
        final TextView gameButton = (TextView) findViewById(R.id.id_button_game);

        measureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                surfaceMode = (surfaceMode != SurfaceMode.MEASURE) ? SurfaceMode.MEASURE : SurfaceMode.DEFAULT;
                if (surfaceMode == SurfaceMode.MEASURE) {
                    measureButton.setTextSize(20);
                    gameButton.setTextSize(16);
                } else {
                    measureButton.setTextSize(16);
                }
                surfaceView.setMode(surfaceMode);
            }
        });

        gameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                surfaceMode = (surfaceMode != SurfaceMode.GAME) ? SurfaceMode.GAME : SurfaceMode.DEFAULT;
                if (surfaceMode == SurfaceMode.GAME) {
                    gameButton.setTextSize(20);
                    measureButton.setTextSize(16);
                } else {
                    gameButton.setTextSize(16);
                }
                surfaceView.setMode(surfaceMode);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        surfaceView.onResumeMySurfaceView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        surfaceView.onPauseMySurfaceView();
    }
}

enum SurfaceMode {
    DEFAULT,
    MEASURE,
    GAME
}
