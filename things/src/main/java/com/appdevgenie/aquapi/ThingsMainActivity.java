package com.appdevgenie.aquapi;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.appdevgenie.aquapi.models.PiStatus;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import static com.appdevgenie.aquapi.utils.Constants.DB_CHILD_AQUA_PI;
import static com.appdevgenie.aquapi.utils.Constants.DB_CHILD_IP;
import static com.appdevgenie.aquapi.utils.Constants.DB_CHILD_LAST_ONLINE;
import static com.appdevgenie.aquapi.utils.Constants.DB_CHILD_ONLINE_PI;
import static com.appdevgenie.aquapi.utils.Constants.DB_CHILD_STATUS;

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

    private TextView tvIP;
    String ip = "0.0.0.0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child(DB_CHILD_AQUA_PI);

        tvIP = findViewById(R.id.tvIP);

        ip = getIPAddress(true);
        if (ip != null && (!TextUtils.isEmpty(ip) || !ip.equals(""))) {
            tvIP.setText(ip);
        }

        setupFirebaseListener();
    }

    private void setupFirebaseListener() {

        connectedRef = databaseReference
                .child(DB_CHILD_STATUS)
                .child(DB_CHILD_ONLINE_PI);
        lastOnlineRef = databaseReference
                .child(DB_CHILD_STATUS)
                .child(DB_CHILD_LAST_ONLINE);

        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChanged: " + dataSnapshot.getValue());
                connectedRef.setValue(Boolean.TRUE);
                lastOnlineRef.setValue(ServerValue.TIMESTAMP);

                databaseReference
                        .child(DB_CHILD_STATUS)
                        .child(DB_CHILD_IP)
                        .setValue(ip);

                connectedRef.onDisconnect().setValue(Boolean.FALSE);
                lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP);

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
}
