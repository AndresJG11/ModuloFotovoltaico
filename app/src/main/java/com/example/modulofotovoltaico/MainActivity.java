package com.example.modulofotovoltaico;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothSocket;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private String nombreModulo = "HC-06";

    Button bConnect;
    Button btnDesconectar;

    private GridView gridData;
    private GridAdapter gridAdapter;
    ArrayList<Sensor> dataSensores;
    private final int POS_TEMPERATURA = 0;
    private final int POS_HUMEDAD = 1;
    private final int POS_LUZ_UV = 2;
    private final int POS_LUZ_IR = 3;
    private final int POS_VOLTAJE = 4;
    private final int POS_CORRIENTE = 5;
    private final int POS_POTENCIA = 6;
    public String strData[] = {"NaN", "NaN", "NaN", "NaN", "NaN", "NaN", "NaN"};

    Bluetooth BT;

    private static LinearLayout graphLayout;
    private static GraphView graphView;
    private static LineGraphSeries serieTemperatura;
    private static LineGraphSeries serieHumedad;

    private double xActual = 0.0;
    private double xAnt = 0.0;
    private double delayTime = 1.0; // Frecuencia de envio en segundos


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        try {
            this.getSupportActionBar().hide();
        } catch (NullPointerException e) {
        }
        setContentView(R.layout.activity_main);
        init();
        buttonInit();
    }


    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Bluetooth.SUCCESS_CONNECT:
                    Bluetooth.connectedThread = new Bluetooth.ConnectedThread((BluetoothSocket) msg.obj);
                    Toast.makeText(getApplicationContext(), "Conectado", Toast.LENGTH_LONG).show();
                    Bluetooth.connectedThread.start();
                    break;
                case Bluetooth.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;

                    String strIncom = new String(readBuf, 0, msg.arg1);

                    // Se obtienen los datos separados por comas
                    strData = strIncom.split(",");

                    // Se actualiza el valor de x y se grafican los datos recibidos
                    xActual = xAnt + delayTime;
                    xAnt = xActual;
                    serieTemperatura.appendData(new DataPoint(xActual, Double.parseDouble(strData[POS_TEMPERATURA])), true, 100);
                    serieHumedad.appendData(new DataPoint(xActual, Double.parseDouble(strData[POS_HUMEDAD])), true, 100);

                    // Actualiza los TextView con los datos recibidos
                    actualizarSensores();
                    break;
            }
        }

        public boolean isFloatNumber(String num) {
            try {
                Double.parseDouble(num);
            } catch (NumberFormatException nfe) {
                return false;
            }
            return true;
        }
    };

    void init() {
        Bluetooth.gethandler(mHandler);

        dataSensores = prepareDataSet();
        gridData = (GridView) findViewById(R.id.gridData);

        gridAdapter = new GridAdapter(this, dataSensores);

        gridData.setAdapter(gridAdapter);

        graphView = (GraphView) findViewById(R.id.graph);


        // Serie de temperatura
        serieTemperatura = new LineGraphSeries();
        serieTemperatura.setColor(dataSensores.get(POS_TEMPERATURA).getColor());
        serieTemperatura.setThickness(10);
        serieTemperatura.setDrawDataPoints(true);
        serieTemperatura.setDataPointsRadius(10);
        graphView.addSeries(serieTemperatura);


        serieHumedad = new LineGraphSeries();
        serieHumedad.setColor(dataSensores.get(POS_HUMEDAD).getColor());
        serieHumedad.setThickness(10);
        serieHumedad.setDrawDataPoints(true);
        serieHumedad.setDataPointsRadius(10);
        graphView.addSeries(serieHumedad);

        // activate horizontal zooming and scrolling
        graphView.getViewport().setScalable(true);

        // activate horizontal scrolling
        graphView.getViewport().setScrollable(true);

        // activate horizontal and vertical zooming and scrolling
        graphView.getViewport().setScalableY(true);

        // activate vertical scrolling
        graphView.getViewport().setScrollableY(true);
    }

    private ArrayList<Sensor> prepareDataSet() {
        // Se declaran los sensores que aparecerán en el GridView
        ArrayList<Sensor> sensores = new ArrayList<>();
        Sensor sensor;

        // getResources().getColor(R.color.colorData1)

        // Temperatura
        sensor = new Sensor("Temperatura");
        sensor.setMedida(strData[POS_TEMPERATURA]);
        sensor.setColor("#708AE4"); // Color del BackGround en Hexa
        sensores.add(sensor);


        // Humedad
        sensor = new Sensor("Humedad");
        sensor.setMedida(strData[POS_HUMEDAD]);
        sensor.setColor("#9770E4");
        sensores.add(sensor);

        // Luz UV
        sensor = new Sensor("Luz UV");
        sensor.setMedida(strData[POS_LUZ_UV]);
        sensor.setColor("#379EBF");
        sensores.add(sensor);

        // Luz IR
        sensor = new Sensor("Luz IR");
        sensor.setMedida(strData[POS_LUZ_IR]);
        sensor.setColor("#DBE470");
        sensores.add(sensor);

        // Voltaje
        sensor = new Sensor("Voltaje");
        sensor.setMedida(strData[POS_VOLTAJE]);
        sensor.setColor("#E49E70");
        sensores.add(sensor);

        // Corriente
        sensor = new Sensor("Corriente");
        sensor.setMedida(strData[POS_CORRIENTE]);
        sensor.setColor("#E470A5");
        sensores.add(sensor);

        // Potencia
        sensor = new Sensor("Potencia");
        sensor.setMedida(strData[POS_POTENCIA]);
        sensor.setColor("#6DBF37");
        sensores.add(sensor);

        return sensores;
    }

    public void actualizarSensores() {
        // Se actualiza cada elemento del grid con cada dato nuevo
        for (int i = 0; i < strData.length; i++) {
            gridAdapter.setMedida(i, strData[i]);
        }
        gridData.setAdapter(gridAdapter);
    }

    void buttonInit() {
        // Agrega el evento a cada boton y fija Views
        bConnect = (Button) findViewById(R.id.bConnect);
        bConnect.setOnClickListener(this);

        btnDesconectar = (Button) findViewById(R.id.btnDesconectar);
        btnDesconectar.setOnClickListener(this);

        //lblData = (TextView)findViewById(R.id.lblData);
        //lblData.setText("Conectado");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bConnect:
                // Inicializa la conexion BT con un modulo especifico
                BT = new Bluetooth(nombreModulo);
                break;
            case R.id.btnDesconectar:
                // Cierra la conexion BT
                BT.disconnect();
                Toast.makeText(getApplicationContext(), "Desconectado", Toast.LENGTH_LONG).show();
                break;
        }
    }
}
