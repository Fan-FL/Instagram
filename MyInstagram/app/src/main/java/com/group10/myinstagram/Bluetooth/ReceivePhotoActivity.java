package com.group10.myinstagram.Bluetooth;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.group10.myinstagram.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ReceivePhotoActivity extends AppCompatActivity {
    private static final String TAG = "ReceivePhotoActivity";
    /**
     * Member object for the chat services
     */
    private BluetoothService mChatService = null;

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
            Log.d(TAG, "handleMessage: I have received messages!!!"+msg);
            switch(msg.what){
                case Constants.MESSAGE_STATE_CHANGE:
                    Log.d(TAG, "state change");
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            setStatus(R.string.title_connected_to + mConnectedDeviceName);
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
                    Log.d(TAG, "handleMessage: "+writeBuf);
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    Log.d(TAG, "write message:" + writeMessage);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != ReceivePhotoActivity.this) {
                        Toast.makeText(ReceivePhotoActivity.this, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Log.d(TAG, "read message: "+readMessage);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: receive photo");
        setContentView(R.layout.activity_in_range);

        if (mChatService == null) {
            // Initialize the BluetoothService to perform bluetooth connections
            mChatService = new BluetoothService(ReceivePhotoActivity.this, mHandler);

            // Initialize the buffer for outgoing messages
//            mOutStringBuffer = new StringBuffer("");
//            mChatService.start();
        }

    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
        }
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

    /**
     * Updates the status on the action bar.
     *
     * @param resId a string resource ID
     */
    private void setStatus(int resId) {
        TextView status = (TextView)findViewById(R.id.in_range_status);
        status.setText(resId);
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
        TextView status = (TextView)findViewById(R.id.in_range_status);
        status.setText(subTitle);
    }
}
