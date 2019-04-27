package paublanes.travelnet;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class BlankActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank);

        //Start Auth flow
        if (FirebaseManager.getInstance().getUser() == null) {
            startActivityForResult(FirebaseManager.getInstance().getSignInActivity(),Keys.K_SIGN_IN);
        }else{
            startActivity(new Intent(BlankActivity.this, ProfileActivity.class));
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            if (requestCode == Keys.K_SIGN_IN) {
                Log.d("SIGN IN", "OK");
                FirebaseManager.getInstance().createUser();
                startActivity(new Intent(BlankActivity.this, ProfileActivity.class));
            }
        }
    }
}
