package com.example.grim.tutmap;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Welcome extends AppCompatActivity {
    Button myWecomeButtom;
    TextView myLogoText;
    TextView myDescriptText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        myWecomeButtom = (Button)findViewById(R.id.weclome_btn_enter);
        myWecomeButtom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Welcome.this, Map.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        myLogoText = (TextView)findViewById(R.id.weclome_tv);
        myDescriptText = (TextView)findViewById(R.id.welcom_tv_tut);
        Typeface keys = Typeface.createFromAsset(getAssets(), getString(R.string.digit_keyboard_font));
        myLogoText.setTypeface(keys);
        myWecomeButtom.setTypeface(keys);
        myLogoText.setShadowLayer(
                1f,
                1f,
                1f,
                Color.BLACK
        );
        myDescriptText.setShadowLayer(
                1f,
                1f,
                1f,
                Color.WHITE
        );


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_welcome, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
