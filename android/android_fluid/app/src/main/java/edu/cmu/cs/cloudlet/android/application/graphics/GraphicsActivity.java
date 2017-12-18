package edu.cmu.cs.cloudlet.android.application.graphics;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import edu.cmu.cs.cloudlet.android.R;

public class GraphicsActivity extends Activity {
    protected static final String CLOUDLET_IP = "130.237.43.83";
    //	protected static final String AMAZON_EAST_IP = "23.21.103.194";
//	protected static final String AMAZON_WEST_IP = "184.169.142.70";
//	protected static final String AMAZON_EU_IP = "176.34.100.63";
//	protected static final String AMAZON_ASIA_IP = "46.137.209.173";
    protected static final int FLUID_SIMULATION_PORT = 9093;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graphics_main);

        findViewById(R.id.testCloudlet).setOnClickListener(clickListener);
        //findViewById(R.id.testEast).setOnClickListener(clickListener);
        //findViewById(R.id.testWest).setOnClickListener(clickListener);
        //findViewById(R.id.testEU).setOnClickListener(clickListener);
        //findViewById(R.id.testAsia).setOnClickListener(clickListener);

    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(GraphicsActivity.this, GraphicsClientActivity.class);
            //String ipAddress = "localhost";

			/*
            if (v.getId() == R.id.testCloudlet) {
				ipAddress = CLOUDLET_IP;
			} else if (v.getId() == R.id.testEast) {
				ipAddress = AMAZON_EAST_IP;
			} else if (v.getId() == R.id.testWest) {
				ipAddress = AMAZON_WEST_IP;
			} else if (v.getId() == R.id.testEU) {
				ipAddress = AMAZON_EU_IP;
			} else if (v.getId() == R.id.testAsia) {
				ipAddress = AMAZON_ASIA_IP;
			}
			*/

            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle("Enter URL");
            final EditText input = new EditText(v.getContext());
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
            input.setText(CLOUDLET_IP);
            builder.setView(input);

            builder.setPositiveButton("OK", (dialogInterface, i) ->
            {
                intent.putExtra("address", input.getText().toString());
                intent.putExtra("port", FLUID_SIMULATION_PORT);
                startActivityForResult(intent, 0);
            });
            builder.setNegativeButton("Cancel", ((dialogInterface, i) -> dialogInterface.cancel()));
            builder.show();
        }
    };

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
