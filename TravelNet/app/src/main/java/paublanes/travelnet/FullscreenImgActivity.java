package paublanes.travelnet;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class FullscreenImgActivity extends AppCompatActivity {

    ImageView iv_big;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_img);

        iv_big = findViewById(R.id.iv_big_img);

        Picasso.get()
                .load(getIntent().getStringExtra(Keys.K_IMG_TO_SHOW))
                .into(iv_big);
    }
}
