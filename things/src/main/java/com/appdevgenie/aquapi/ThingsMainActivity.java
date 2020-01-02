package com.appdevgenie.aquapi;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.appdevgenie.aquapi.models.Control;
import com.appdevgenie.aquapi.models.TemperatureInfo;
import com.appdevgenie.aquapi.service.UsbService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.appdevgenie.aquapi.utils.Constants.DB_CHILD_AQUA_PI;
import static com.appdevgenie.aquapi.utils.Constants.DB_CHILD_BYPASS;
import static com.appdevgenie.aquapi.utils.Constants.DB_CHILD_CONTROL;
import static com.appdevgenie.aquapi.utils.Constants.DB_CHILD_IP;
import static com.appdevgenie.aquapi.utils.Constants.DB_CHILD_LAST_ONLINE;
import static com.appdevgenie.aquapi.utils.Constants.DB_CHILD_LIGHT;
import static com.appdevgenie.aquapi.utils.Constants.DB_CHILD_ONLINE_ARDUINO;
import static com.appdevgenie.aquapi.utils.Constants.DB_CHILD_ONLINE_PI;
import static com.appdevgenie.aquapi.utils.Constants.DB_CHILD_STATUS;
import static com.appdevgenie.aquapi.utils.Constants.DB_CHILD_TEMPERATURES;

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class ThingsMainActivity extends Activity {

    private static final String TAG = ThingsMainActivity.class.getSimpleName();

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private DatabaseReference connectedRef;
    private DatabaseReference lastOnlineRef;
    private DatabaseReference connectedArduinoRef;
    private DatabaseReference temperatureRef;
    private DatabaseReference controlRef;

    private TextView tvIP;
    private String ip = "0.0.0.0";
    private ArrayList<TemperatureInfo> infoArrayList;

    private UsbService usbService;
    private TextView display;
    //private EditText editText;
    private MyHandler mHandler;

    private String buffer = "";

    private boolean isArduinoConnected;

    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    isArduinoConnected = Boolean.TRUE;
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    isArduinoConnected = Boolean.FALSE;
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    isArduinoConnected = Boolean.FALSE;
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
            }

            connectedArduinoRef = databaseReference
                    .child(DB_CHILD_STATUS)
                    .child(DB_CHILD_ONLINE_ARDUINO);
            connectedArduinoRef.setValue(isArduinoConnected);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate");

        mHandler = new MyHandler(this);

        init();
    }

    private void init() {

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child(DB_CHILD_AQUA_PI);

        tvIP = findViewById(R.id.tvIP);
        display = findViewById(R.id.tvData);

        infoArrayList = TemperatureInfo.createTempList();
        //infoArrayList = new ArrayList<>();

        setupFirebaseListener();
    }

    private void setupFirebaseListener() {

        connectedRef = databaseReference
                .child(DB_CHILD_STATUS)
                .child(DB_CHILD_ONLINE_PI);

        lastOnlineRef = databaseReference
                .child(DB_CHILD_STATUS)
                .child(DB_CHILD_LAST_ONLINE);

        Query queryTemps = databaseReference
                .child(DB_CHILD_TEMPERATURES);

        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Log.d(TAG, "onDataChanged: " + dataSnapshot.getValue());
                connectedRef.setValue(Boolean.TRUE);
                lastOnlineRef.setValue(ServerValue.TIMESTAMP);

                ip = getIPAddress(true);
                if (ip != null && (!TextUtils.isEmpty(ip) || !ip.equals(""))) {
                    tvIP.setText(ip);

                    databaseReference
                            .child(DB_CHILD_STATUS)
                            .child(DB_CHILD_IP)
                            .setValue(ip);
                }

                connectedRef.onDisconnect().setValue(Boolean.FALSE);
                lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP);

                queryTemps.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (TemperatureInfo tempInfo : infoArrayList) {
                            TemperatureInfo temperatureInfo = dataSnapshot.child(tempInfo.getName()).getValue(TemperatureInfo.class);
                            String name = dataSnapshot.child(tempInfo.getName()).getKey();

                            if (tempInfo.getName().equals(name)) {
                                if (temperatureInfo != null) {

                                    switch (name) {
                                        case "water":
                                            infoArrayList.get(0).setTemp(temperatureInfo.getTemp());
                                            infoArrayList.get(0).setTempMax(temperatureInfo.getTempMax());
                                            infoArrayList.get(0).setTempMin(temperatureInfo.getTempMin());
                                            infoArrayList.get(0).setMinTimeStamp(temperatureInfo.getMinTimeStamp());
                                            infoArrayList.get(0).setMaxTimeStamp(temperatureInfo.getMaxTimeStamp());
                                            break;

                                        case "system":
                                            infoArrayList.get(1).setTemp(temperatureInfo.getTemp());
                                            infoArrayList.get(1).setTempMax(temperatureInfo.getTempMax());
                                            infoArrayList.get(1).setTempMin(temperatureInfo.getTempMin());
                                            infoArrayList.get(1).setMinTimeStamp(temperatureInfo.getMinTimeStamp());
                                            infoArrayList.get(1).setMaxTimeStamp(temperatureInfo.getMaxTimeStamp());
                                            break;

                                        case "room":
                                            infoArrayList.get(2).setTemp(temperatureInfo.getTemp());
                                            infoArrayList.get(2).setTempMax(temperatureInfo.getTempMax());
                                            infoArrayList.get(2).setTempMin(temperatureInfo.getTempMin());
                                            infoArrayList.get(2).setMinTimeStamp(temperatureInfo.getMinTimeStamp());
                                            infoArrayList.get(2).setMaxTimeStamp(temperatureInfo.getMaxTimeStamp());
                                            break;
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        controlRef = databaseReference
                .child(DB_CHILD_CONTROL);
        controlRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Control controlData = dataSnapshot.getValue(Control.class);
                boolean bypass = controlData.isBypassOn();
                Log.d(TAG, "onDataChanged: bypass " + String.valueOf(bypass));
                if (bypass) {
                    String byteString = "bypassOn";
                    try {
                        byte[] bytes = byteString.getBytes("UTF-8");
                        usbService.write(bytes);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else {
                    String byteString = "bypassOff";
                    try {
                        byte[] bytes = byteString.getBytes("UTF-8");
                        usbService.write(bytes);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

                boolean light = controlData.isLightOn();
                if (light) {
                    String byteString = "lightOn";
                    try {
                        byte[] bytes = byteString.getBytes("UTF-8");
                        usbService.write(bytes);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else {
                    String byteString = "lightOff";
                    try {
                        byte[] bytes = byteString.getBytes("UTF-8");
                        usbService.write(bytes);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

                boolean rebootPi = controlData.isRebootPi();
                Log.d(TAG, "onDataChanged: reboot " + String.valueOf(rebootPi));
                if (rebootPi) {
                    //DeviceManager.getInstance().reboot();
                    //OR
                    /*try {
                        Runtime.getRuntime().exec("reboot");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        /*Query query = databaseReference;
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChanged: " + dataSnapshot.getValue());
                PiStatus piStatus = new PiStatus();
                piStatus.setIp(ip);
                piStatus.setOnlinePi(Boolean.TRUE);
                databaseReference.child(DB_CHILD_STATUS).setValue(piStatus);

                query.getRef().child(DB_CHILD_STATUS).child(DB_CHILD_ONLINE_PI).onDisconnect().setValue(Boolean.FALSE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/
        /*query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                PiStatus piStatus = dataSnapshot.getValue(PiStatus.class);
                Log.d(TAG, "onChildChanged: " + dataSnapshot.getValue());
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/

        //Query query = databaseReference.child(DB_CHILD_STATUS);
        /*query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChanged: " + dataSnapshot.getValue());

                query.getRef().child(DB_CHILD_ONLINE_PI).setValue(Boolean.TRUE);
                //query.getRef().child(DB_CHILD_LAST_ONLINE).setValue(ServerValue.TIMESTAMP);

                query.getRef().child(DB_CHILD_IP).setValue(ip);

                query.getRef().child(DB_CHILD_ONLINE_PI).onDisconnect().setValue(Boolean.FALSE);
                //query.getRef().child(DB_CHILD_LAST_ONLINE).onDisconnect().setValue(ServerValue.TIMESTAMP);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/
    }

    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } // for now eat exceptions
        return "";
    }

    @Override
    public void onResume() {
        super.onResume();
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
    }

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
    private class MyHandler extends Handler {
        private final WeakReference<ThingsMainActivity> mActivity;

        public MyHandler(ThingsMainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;
                    //mActivity.get().display.append(data);
                    //mActivity.get().display.setText(data);
                    //String dataUtf8 = new String(data, "UTF-8");
                    buffer += data;
                    int index;
                    while ((index = buffer.indexOf('\n')) != -1) {
                        final String dataStr = buffer.substring(0, index + 1).trim();
                        buffer = buffer.length() == index ? "" : buffer.substring(index + 1);

                        //Log.i(TAG, dataStr.substring(0, 5));
                        try {
                            if (TextUtils.equals(dataStr.substring(0, 5), "temps")) {
                                onTempDataReceived(dataStr);
                            }
                            if (TextUtils.equals(dataStr.substring(0, 6), "bypass")) {
                                //Log.i(TAG, "Serial bypass data received: " + dataStr);
                                updateBypass(dataStr);
                            }
                            if (TextUtils.equals(dataStr.substring(0, 5), "light")) {
                                //Log.i(TAG, "Serial bypass data received: " + dataStr);
                                updateLight(dataStr);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        /*runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                //Toast.makeText(mActivity.get(), dataStr,Toast.LENGTH_LONG).show();
                            }
                        });*/
                    }
                    break;
                case UsbService.CTS_CHANGE:
                    Toast.makeText(mActivity.get(), "CTS_CHANGE", Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mActivity.get(), "DSR_CHANGE", Toast.LENGTH_LONG).show();
                    break;
                /*case UsbService.SYNC_READ:
                 *//*String buffer = (String) msg.obj;
                    mActivity.get().display.append(buffer);*//*
                    break;*/
            }
        }
    }

    private void updateLight(String dataStr) {

        String bypassStr = dataStr.substring(dataStr.length() -1);
        if(TextUtils.equals(bypassStr, "1")) {
            databaseReference
                    .child(DB_CHILD_CONTROL).child(DB_CHILD_LIGHT).setValue(Boolean.TRUE);
        }else{
            databaseReference
                    .child(DB_CHILD_CONTROL).child(DB_CHILD_LIGHT).setValue(Boolean.FALSE);
        }
    }

    private void updateBypass(String dataStr) {

        String bypassStr = dataStr.substring(dataStr.length() -1);
        if(TextUtils.equals(bypassStr, "1")) {
            databaseReference
                    .child(DB_CHILD_CONTROL).child(DB_CHILD_BYPASS).setValue(Boolean.TRUE);
        }else{
            databaseReference
                    .child(DB_CHILD_CONTROL).child(DB_CHILD_BYPASS).setValue(Boolean.FALSE);
        }
    }

    private void onTempDataReceived(String data) {
        Log.i(TAG, "Serial temp data received: " + data);
        display.setText(data);

        float tempWater = 0.0f;
        float tempWaterMin = infoArrayList.get(0).getTempMin();
        float tempWaterMax = infoArrayList.get(0).getTempMax();

        float tempSystem = 0.0f;
        float tempSystemMin = infoArrayList.get(1).getTempMin();
        float tempSystemMax = infoArrayList.get(1).getTempMax();

        float tempRoom = 0.0f;
        float tempRoomMin = infoArrayList.get(2).getTempMin();
        float tempRoomMax = infoArrayList.get(2).getTempMax();

        try {
            //read info from arduino
            String[] tempsList = data.split(" ");
            tempWater = Float.parseFloat(tempsList[1]);
            tempSystem = Float.parseFloat(tempsList[2]);
            tempRoom = Float.parseFloat(tempsList[3]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (tempWater <= tempWaterMin) {
            tempWaterMin = tempWater;
            infoArrayList.get(0).setMinTimeStamp(System.currentTimeMillis());
        }
        if (tempWater >= tempWaterMax) {
            tempWaterMax = tempWater;
            infoArrayList.get(0).setMaxTimeStamp(System.currentTimeMillis());
        }
        infoArrayList.get(0).setTemp(tempWater);
        infoArrayList.get(0).setTempMin(tempWaterMin);
        infoArrayList.get(0).setTempMax(tempWaterMax);

        if (tempSystem <= tempSystemMin) {
            tempSystemMin = tempSystem;
            infoArrayList.get(1).setMinTimeStamp(System.currentTimeMillis());
        }
        if (tempSystem >= tempSystemMax) {
            tempSystemMax = tempSystem;
            infoArrayList.get(1).setMaxTimeStamp(System.currentTimeMillis());
        }
        infoArrayList.get(1).setTemp(tempSystem);
        infoArrayList.get(1).setTempMin(tempSystemMin);
        infoArrayList.get(1).setTempMax(tempSystemMax);

        if (tempRoom <= tempRoomMin) {
            tempRoomMin = tempRoom;
            infoArrayList.get(2).setMinTimeStamp(System.currentTimeMillis());
        }
        if (tempRoom >= tempRoomMax) {
            tempRoomMax = tempRoom;
            infoArrayList.get(2).setMaxTimeStamp(System.currentTimeMillis());
        }
        infoArrayList.get(2).setTemp(tempRoom);
        infoArrayList.get(2).setTempMin(tempRoomMin);
        infoArrayList.get(2).setTempMax(tempRoomMax);

        temperatureRef = databaseReference.child(DB_CHILD_TEMPERATURES);
        for (TemperatureInfo temperatureInfo : infoArrayList) {
            DatabaseReference dbReference = temperatureRef.child(temperatureInfo.getName()).getRef();
            dbReference.setValue(temperatureInfo);
        }

        /*Temperatures temperatures = new Temperatures();
        temperatures.setTempRoom(tempRoom);
        temperatures.setTempSystem(tempSystem);
        temperatures.setTempWater(tempWater);
        temperatureRef.setValue(temperatures);*/

    }
}
