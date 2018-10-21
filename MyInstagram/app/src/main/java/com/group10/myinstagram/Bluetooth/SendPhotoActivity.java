package com.group10.myinstagram.Bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.group10.myinstagram.Main.MainActivity;
import com.group10.myinstagram.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SendPhotoActivity extends AppCompatActivity implements AdapterView
        .OnItemClickListener {
    // Intent request codes
//    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    private static final String[] BLUE_PERMISSIONS = {Manifest.permission.BLUETOOTH, Manifest
            .permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_COARSE_LOCATION};
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter;
    ListView newDevices;
    String imageString = "";
    int count = 0;
    private String TAG = "Send Photo Activity";
    /**
     * Broadcast Receiver that detects bond state changes (Pairing status changes)
     */
    private final BroadcastReceiver mBondDetecter = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                }
                //case2: creating a bone
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                }
                //case3: breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
                }
            }
        }
    };
    private Context mContext = SendPhotoActivity.this;

    //bluetooth
//    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;
//    private static final int REQUEST_ENABLE_BT = 2;
    private BluetoothAdapter mBluetoothAdapter;
    private Intent intent;
    private String imgUrl;
    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;
    /**
     * The Handler that gets information back from the BluetoothService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage: receive message" + msg);
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    Log.d(TAG, "state change");
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            String status = "connected to " + mConnectedDeviceName;
                            setStatus(status);
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    Log.d(TAG, "handleMessage: " + writeBuf);
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    Log.d(TAG, "write message:" + writeMessage);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != SendPhotoActivity.this) {
                        Toast.makeText(SendPhotoActivity.this, "Connected to " +
                                mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    int bytes = msg.arg1;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);

                    if (readMessage.equals("SendingFinished")) {
                        Log.d(TAG, "handleMessage: finish sending ");
                        ImageView imageView = (ImageView) findViewById(R.id.receivedImage);
                        byte[] decodedString = Base64.decode(imageString.getBytes(),
                                Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0,
                                decodedString.length);
                        imageView.setImageBitmap(decodedByte);
                        imageString = "";
                    } else {
                        imageString = imageString + readMessage;
                        Log.d(TAG, "read message: " + readMessage);
                        Log.d(TAG, "allMessage: " + imageString);
                        Log.d(TAG, "message size:" + imageString.length());
                    }
                    break;
                default:
                    break;
            }

        }
    };
    private BluetoothAdapter bluetoothAdapter;
    /**
     * Member object for the chat services
     */
    private BluetoothService mChatService = null;
    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                boolean added = false;
                for (BluetoothDevice bluetoothDevice : mBTDevices) {
                    if (device.equals(bluetoothDevice)) {
                        added = true;
                    }
                }
                if (!added) mBTDevices.add(device);
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view,
                        mBTDevices);
                newDevices.setAdapter(mDeviceListAdapter);
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: looking for unpaired devices");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        startBluetoothSensor();

//        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        newDevices = (ListView) findViewById(R.id.newDevices);
//        newDevices.setOnItemClickListener(SendPhotoActivity.this );
////        mBTDevices = new ArrayList<>();
//        scanDevices();

        Button btnScanDevices = (Button) findViewById(R.id.btn_scan_devices);

        btnScanDevices.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Log.d(TAG, "onClick: scan devices");
                scanDevices();

            }
        });

        Button btnSendMessage = (Button) findViewById(R.id.btn_send_message);

        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Log.d(TAG, "onClick: send messages");
//                sendMessage(getPhoto());
                try {
                    sendPhoto();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Toast.makeText(SendPhotoActivity.this, "Attempting to share photo via bluetooth" +
                        ".", Toast.LENGTH_SHORT).show();

            }
        });

        Button btnFinish = (Button) findViewById(R.id.btn_finish);

        btnFinish.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Log.d(TAG, "onClick: finish");
                unregisterReceiver(mBroadcastReceiver);
                unregisterReceiver(mBondDetecter);
                if (mChatService != null) {
                    mChatService.stop();
                }
                Intent intent = new Intent(SendPhotoActivity.this, MainActivity.class);
                startActivity(intent);

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        newDevices = (ListView) findViewById(R.id.newDevices);
        newDevices.setOnItemClickListener(SendPhotoActivity.this);
//        mBTDevices = new ArrayList<>();
        scanDevices();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
        unregisterReceiver(mBondDetecter);
        if (mChatService != null) {
            mChatService.stop();
        }
        //mBluetoothAdapter.cancelDiscovery();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    private void scanDevices() {
        mBTDevices = new ArrayList<>();
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                Log.d(TAG, "onReceive: ACTION FOUND.");

                if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice
                            .EXTRA_DEVICE);
                    boolean added = false;
                    for (BluetoothDevice bluetoothDevice : mBTDevices) {
                        if (device.equals(bluetoothDevice)) {
                            added = true;
                        }
                    }
                    if (!added) mBTDevices.add(device);
                    Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                    mDeviceListAdapter = new DeviceListAdapter(context, R.layout
                            .device_adapter_view, mBTDevices);
                    newDevices.setAdapter(mDeviceListAdapter);
                }
            }
        };

        //Broadcasts when bond state changes (ie:pairing)
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBondDetecter, filter);

        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Canceling discovery.");

            //check BT permissions in manifest
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver, discoverDevicesIntent);
        }
        if (!mBluetoothAdapter.isDiscovering()) {

            //check BT permissions in manifest
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver, discoverDevicesIntent);
        }
    }

    /**
     * This method is required for all devices running API23+
     * Android must programmatically check the permissions for bluetooth. Putting the proper
     * permissions
     * in the manifest is not enough.
     * <p>
     * NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
     */
    private void checkBTPermissions() {
        Log.d(TAG, "checkBTPermissions");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = this.checkSelfPermission("Manifest.permission" +
                    ".ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission" +
                    ".ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {
                Log.d(TAG, "request BT permissions");
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        } else {
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //first cancel discovery because its very memory intensive.
        mBluetoothAdapter.cancelDiscovery();

        Log.d(TAG, "onItemClick: You Clicked on a device.");
        String deviceName = mBTDevices.get(position).getName();
        String deviceAddress = mBTDevices.get(position).getAddress();

        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

        //create the bond.
        //NOTE: Requires API 17+? I think this is JellyBean
//        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
//            Log.d(TAG, "Trying to pair with " + deviceName);
//            mBTDevices.get(position).createBond();
//        }

        if (mChatService == null) {
            // Initialize the BluetoothService to perform bluetooth connections
            mChatService = new BluetoothService(SendPhotoActivity.this, mHandler);

            // Initialize the buffer for outgoing messages
            mOutStringBuffer = new StringBuffer("");
        }

        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceAddress);
        // Attempt to connect to the device
        mChatService.connect(device, false);
    }

    //Bluetooth

    public void startBluetoothSensor() {

        // This only targets API 23+
        // check permission using a thousand lines (Google is naive!)

        Log.d(TAG, "Start bluetooth sensors");
        if (!hasPermissionsGranted(BLUE_PERMISSIONS)) {
            requestBluePermissions(BLUE_PERMISSIONS);
            return;
        }

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Log.d(TAG, "Device has no bluetooth");
            return;
        }

        // ask users to open bluetooth
        if (bluetoothAdapter.isEnabled() == false) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }


        // make this device visible to others for 3000 seconds
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3000);
        startActivity(discoverableIntent);
    }

    // check if app has a list of permissions, then request not-granted ones

    public void requestBluePermissions(String[] permissions) {
        Log.d(TAG, "Request bluetooth permissions");
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager
                    .PERMISSION_GRANTED) {
                listPermissionsNeeded.add(permission);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new
                    String[listPermissionsNeeded.size()]), REQUEST_BLUETOOTH_PERMISSIONS);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_BLUETOOTH_PERMISSIONS:
                Log.d(TAG, "Request permission");

                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            Log.d(TAG, "one or more permission denied");
                            return;
                        }
                    }
                    Log.d(TAG, "all permissions granted");

                }

        }
    }


    private boolean hasPermissionsGranted(String[] permissions) {
        Log.d(TAG, "Check permission granted");
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager
                    .PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    /**
     * Updates the status on the action bar.
     *
     * @param resId a string resource ID
     */
    private void setStatus(int resId) {
        TextView status = (TextView) findViewById(R.id.bluetooth_status);
        status.setText(resId);
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
        TextView status = (TextView) findViewById(R.id.bluetooth_status);
        status.setText(subTitle);
    }

    private String getPhoto() {
        Intent intent = getIntent();
        if (intent.hasExtra(getString(R.string.selected_image))) {
            imgUrl = intent.getStringExtra(getString(R.string.selected_image));
            Log.d(TAG, "send image:" + imgUrl);

        } else Log.d(TAG, "no selected image");
        File imagefile = new File(imgUrl);
        String imageString = imageFileToByte(imagefile);
        Log.d(TAG, "getPhoto size: " + imageString.length());
        return imageString;
    }

    public String imageFileToByte(File file) {
        Log.d(TAG, "imageFileToByte");
        Bitmap bm = BitmapFactory.decodeFile(file.getAbsolutePath());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 70, baos); //bm is the bitmap object
        byte[] bytes = baos.toByteArray();
        String string = Base64.encodeToString(bytes, Base64.NO_WRAP);

        ImageView imageView = (ImageView) findViewById(R.id.receivedImage);
        byte[] decodedString = Base64.decode(string.getBytes(), Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        imageView.setImageBitmap(decodedByte);

        return string;
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        Log.d(TAG, "sendMessage: " + message);
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothService.STATE_CONNECTED) {
            Log.d(TAG, "not connected");
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
//            OutputStream mmOutStream;
            // Get the message bytes and tell the BluetoothService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

        }
    }

    private void sendPhoto() throws InterruptedException {
        sendMessage(getPhoto());
        Thread.sleep(1000);
        sendMessage("SendingFinished");

    }

}
