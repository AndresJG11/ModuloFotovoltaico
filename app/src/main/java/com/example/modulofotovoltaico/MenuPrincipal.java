package com.example.modulofotovoltaico;


import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class MenuPrincipal extends AppCompatActivity implements View.OnClickListener {

    Button btnPanel;
    Button btnControlador;
    Button btnInversor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.menu_principal);

        initButton();
    }

    private void initButton() {
        btnPanel = findViewById(R.id.btnPanel);
        btnPanel.setOnClickListener(this);

        btnControlador = findViewById(R.id.btnControlador);
        btnControlador.setOnClickListener(this);

        btnInversor = findViewById(R.id.btnInversor);
        btnInversor.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnPanel:
                Intent intent = new Intent (v.getContext(), MainActivity.class);
                startActivityForResult(intent, 0);
                break;
            case R.id.btnControlador:
                Toast.makeText(getApplicationContext(),"Controlador", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnInversor:
                Toast.makeText(getApplicationContext(),"Inversor", Toast.LENGTH_SHORT).show();
                break;

        }

    }
}
