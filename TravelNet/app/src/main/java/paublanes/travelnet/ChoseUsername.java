package paublanes.travelnet;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

public class ChoseUsername extends AppCompatActivity implements View.OnClickListener {

    EditText name, uniqueName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chose_username);

        findViewById(R.id.btn_chose_username).setOnClickListener(this);
        name = findViewById(R.id.et_name);
        uniqueName = findViewById(R.id.et_unique_name);
        uniqueName.setOnClickListener(this);
        uniqueName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (uniqueName.getCurrentTextColor() == Color.RED) {
                    uniqueName.getText().clear();
                    uniqueName.setTextColor(Color.BLACK);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_chose_username:
                if (!name.getText().toString().isEmpty() && !uniqueName.getText().toString().isEmpty()
                && !uniqueName.getText().toString().equals(R.string.name_not_unique)) {

                    //no ho pujo amb el @ pq sino per fer cerques no va bé
                    FirebaseManager.getInstance().addNameAndUniqueNameIfNotTaken(
                            name.getText().toString(), uniqueName.getText().toString(), this::updateUI);

                }
                else{
                    Toast.makeText(this, getString(R.string.enter_all_fields), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.et_unique_name:
                if (uniqueName.getCurrentTextColor() == Color.RED) {
                    uniqueName.getText().clear();
                    uniqueName.setTextColor(Color.BLACK);
                }
                break;
        }
    }

    void updateUI(boolean sucess) {
        if (sucess) {
            FirebaseManager.getInstance().createUser();
            Intent i = new Intent(ChoseUsername.this, ProfileActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }else{
            uniqueName.setText(getString(R.string.name_not_unique));
            uniqueName.setTextColor(Color.RED);
        }
    }

    @Override
    public void onBackPressed() {
        FirebaseManager.getInstance().logOut(this, () -> {
            Log.d("CHOSE USERNAME","log out success");
            Intent i = new Intent(ChoseUsername.this, BlankActivity.class);
            startActivity(i);
        });
    }
}


