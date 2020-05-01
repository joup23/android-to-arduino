package com.example.bluetooth;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "bluetooth2";

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;

    private ConnectedThread mConnectedThread;

    private static  final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");   //유니크아이디 자신만에 아이디를 만듬
    private static String address = "98:D3:31:F5:12:07";//바꾸기
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();

        Button btn1 = findViewById(R.id.Btn1);
        Button btn2 = findViewById(R.id.Btn2);
        Button btn3 = findViewById(R.id.Btn3);
        Button btn4 = findViewById(R.id.Btn4);
        Button btn5 = findViewById(R.id.Btn5);

        btn1.setOnClickListener(v -> mConnectedThread.write("1"));

        btn2.setOnClickListener(v -> mConnectedThread.write("2"));
        btn3.setOnTouchListener((v, event) -> {
            mConnectedThread.write("3");
            if(event.getAction() == MotionEvent.ACTION_UP) {
                mConnectedThread.write("a\na");
                return false;
            }
            return false;
        });
        btn4.setOnClickListener(v -> mConnectedThread.write("4"));
        btn5.setOnClickListener(v -> mConnectedThread.write("5"));

    }
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if(Build.VERSION.SDK_INT >= 10) {
            try{
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
                return(BluetoothSocket) m.invoke(device, MY_UUID);
            }catch (Exception e){
                Log.e(TAG,"Could not create Insecure RFComm Connection", e);
            }
        }
        return device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    @Override
    protected void onResume() {
        super.onResume();

        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        }catch (IOException e){
            errorExit("Fatal Error", "In onResume() and socket create failed:" +e.getMessage()+ ".");
        }
        btAdapter.cancelDiscovery();

        try {
            btSocket.connect();
            Log.d(TAG,"...Connetion ok");
        }catch (IOException e){
            try {
                btSocket.connect();
            }catch (IOException e2){}
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        try{
            btSocket.close();
        }catch (IOException e2){}
    }
    private void checkBTState() {
        if (btAdapter == null) {
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private  void errorExit(String title, String message) {
        Toast.makeText(getBaseContext(),title+" - "+message,Toast.LENGTH_LONG).show();
        finish();
    }
    private class ConnectedThread extends Thread{
        private  final OutputStream mmOutStream;

        public  ConnectedThread(BluetoothSocket socket){
            OutputStream tmpOut = null;

            try {
                tmpOut = socket.getOutputStream();
            }catch (IOException e){}
            mmOutStream = tmpOut;
        }
        public void write(String message) {
            Log.d(TAG, "...Data to send: "+ message+"...");
            byte[] msgBuffer = message.getBytes();
            try{
                mmOutStream.write(msgBuffer);
            }catch (IOException e){}
        }

    }
}
