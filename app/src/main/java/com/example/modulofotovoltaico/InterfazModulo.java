package com.example.modulofotovoltaico;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothSocket;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

public class InterfazModulo extends AppCompatActivity implements View.OnClickListener {

    private String nombreModulo;

    Button bConnect;
    Button btnDesconectar;

    private GridView gridData;
    private GridAdapter gridAdapter;
    ArrayList<Sensor> dataSensores;
    public String strData[] = {"NaN", "NaN", "NaN", "NaN", "NaN", "NaN", "NaN"};

    Bluetooth BT;

    private static LinearLayout graphLayout;
    private static GraphView graphView;
    private static ArrayList<LineGraphSeries> serieDatos = new ArrayList<>();

    private double xActual = 0.0;
    private double xAnt = 0.0;
    private double delayTime = 1.0; // Frecuencia de envio en segundos

    private Modulo modulo;


    @Override
    protected void onDestroy() {
        super.onDestroy();
        xActual = 0.0;
        xAnt = 0.0;
        Toast.makeText(getApplicationContext(),"On Destroy",Toast.LENGTH_LONG).show();
    }

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
        modulo = (Modulo)getIntent().getExtras().getSerializable("Modulo");
        setContentView(R.layout.modulo_panel);

        init();
        buttonInit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        BT.disconnect();
        finish();
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
                    Log.d("DATA",strIncom);
                    // Se actualiza el valor de x y se grafican los datos recibidos
                    xActual = xAnt + delayTime;
                    xAnt = xActual;
                    // Grafica los datos en orden de llegada
                    int i = 0;
                    for(LineGraphSeries serie:serieDatos){
                        double yValue = Double.parseDouble(strData[i]);
                        Log.d("IMPRIME X ACUALTUAL",Double.toString(serie.getHighestValueX()));
                        serie.appendData(new DataPoint(xActual,yValue),true,100);
                        i++;
                    }

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

        //dataSensores = prepareDataSet();
        dataSensores = modulo.getSensores();
        nombreModulo = modulo.getNombre();
        gridData = (GridView) findViewById(R.id.gridData);

        gridAdapter = new GridAdapter(this, dataSensores);

        gridData.setAdapter(gridAdapter);

        graphView = (GraphView) findViewById(R.id.graph);

        for(int i = 0;i<dataSensores.size();i++){
            LineGraphSeries serie = new LineGraphSeries();
            serie.setColor(dataSensores.get(i).getColor());
            serie.setThickness(10);
            serie.setDrawDataPoints(true);
            serie.setDataPointsRadius(10);
            serieDatos.add(serie);
            graphView.addSeries(serie);
        }

        // activate horizontal zooming and scrolling
        graphView.getViewport().setScalable(true);

        // activate horizontal scrolling
        graphView.getViewport().setScrollable(true);

        // activate horizontal and vertical zooming and scrolling
        graphView.getViewport().setScalableY(true);

        // activate vertical scrolling
        graphView.getViewport().setScrollableY(true);
    }

    public void actualizarSensores() {
        // Se actualiza cada elemento del grid con cada dato nuevo
        for (int i = 0; i < dataSensores.size(); i++) {
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
