package code.sample.ed.chtvoice;

import android.content.pm.ActivityInfo;
import android.media.AudioRecord;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.media.AudioManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothA2dp;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import android.bluetooth.BluetoothProfile.ServiceListener;
import android.bluetooth.BluetoothProfile;
import android.content.IntentFilter;
import android.view.View;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.os.HandlerThread;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothA2dp;
import android.telephony.TelephonyManager;
import android.os.IBinder;


public class MainActivity extends AppCompatActivity {

    MediaRecorder mediaRecorder ;

    //variables
    private int audioSource = MediaRecorder.AudioSource.MIC;
    private static int samplingRate = 11025; /* in Hz*/
    private static int channelConfig = AudioFormat.CHANNEL_IN_MONO;//Use CHANNEL_OUT_MONO or CHANNEL_IN_MONO instead
    private static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private static int bufferSize = AudioRecord.getMinBufferSize(samplingRate, channelConfig, audioFormat);
    static boolean isRecording=true;

    int readBytes=0;
    int writtenBytes=0;

    BluetoothAdapter adapter;
    BluetoothA2dp mA2dpService;
    AudioManager mAudioManager;
    MediaPlayer mPlayer;
    boolean mIsA2dpReady = false;
    //private static BluetoothSocket mmSocket;

    Button btn;
    static AudioRecord recorder;
    static AudioTrack audioPlayer;
    static byte[] buffer;
    private static Handler myHandler;
    String macAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        //macAddress = android.provider.Settings.Secure.getString(MainActivity.this.getContentResolver(), "bluetooth_address");
 //       TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        //UUID MY_UUID = tManager.getDeviceId();

        //Log.e("ed", "macAddress ="+ macAddress.length() +" uuid="+uuid);



/*
        BluetoothDevice device = adapter.getRemoteDevice("1C:52:16:94:11:43");
        BluetoothSocket mmSocket = null;
        //BluetoothSocket mmSocket = null;
        // Get a BluetoothSocket for a connection with the
        // given BluetoothDevice
        UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        try {
            mmSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            //Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
            //tmp = (BluetoothSocket) m.invoke(device, 1);
            //mmSocket=tmp;
            mmSocket.connect();
        } catch (IOException e) {
            Log.e("", "create() failed", e);
        }*/
        //mmSocket = tmp;

        btn = (Button)findViewById(R.id.button);
        bufferSize += 2048;
        buffer = new byte[bufferSize];

        //start record
        recorder = new AudioRecord(audioSource, samplingRate, channelConfig, audioFormat, bufferSize);
        recorder.startRecording();

        //start play voice
        audioPlayer = new AudioTrack(AudioManager.STREAM_VOICE_CALL, 11025, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);

        if(audioPlayer.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
            audioPlayer.play();
        }


        HandlerThread readThread = new HandlerThread("");
        readThread.start();
        myHandler = new Handler(readThread.getLooper());
        myHandler.post(sRunnable);


        //btn
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isRecording=!isRecording;
                if(isRecording){
                    myHandler.post(sRunnable);
                    //mHandler.post(sRunnable);

                    btn.setText("voice off");
                    Log.e("ed", "voice off");
                } else{

                    btn.setText("voice on");
                    //mHandler.removeCallbacks(sRunnable);

                }
            }
        });

        //mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
/*
        BroadcastReceiver mReceiver = new BroadcastReceiver() {
 
            @Override
            public void onReceive(Context ctx, Intent intent) {
                String action = intent.getAction();
                Log.e("ed", "receive intent for action : " + action);
                if (action.equals(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)) {
                    int state = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, BluetoothA2dp.STATE_DISCONNECTED);
                    if (state == BluetoothA2dp.STATE_CONNECTED) {
                        //setIsA2dpReady(true);
                        //mAudioManager.setBluetoothScoOn(true);
                        //mAudioManager.startBluetoothSco();
                        //playMusic();
                    } else if (state == BluetoothA2dp.STATE_DISCONNECTED) {
                        setIsA2dpReady(false);
                    }
                } else if (action.equals(BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED)) {
                    int state = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, BluetoothA2dp.STATE_NOT_PLAYING);
                    if (state == BluetoothA2dp.STATE_PLAYING) {
                        Log.e("ed", "A2DP start playing");
                        //mAudioManager.setBluetoothScoOn(true);
                        //mAudioManager.startBluetoothSco();
 
                        Toast.makeText(MainActivity.this, "A2dp is playing", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("ed", "A2DP stop playing");
                        Toast.makeText(MainActivity.this, "A2dp is stopped", Toast.LENGTH_SHORT).show();
                    }
                }
            }
 
        };
 
        registerReceiver(mReceiver, new IntentFilter(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED));
        registerReceiver(mReceiver, new IntentFilter(BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED));
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        mBtAdapter.getProfileProxy(this, mA2dpListener , BluetoothProfile.A2DP);*/

    }
 
    /*
    public void setIsA2dpReady(boolean ready) {
        mIsA2dpReady = ready;
        Toast.makeText(this, "A2DP ready ? " + (ready ? "true" : "false"), Toast.LENGTH_SHORT).show();
    }*/
 
    /*
    private ServiceListener mA2dpListener = new ServiceListener() {
 
        @Override
        public void onServiceConnected(int profile, BluetoothProfile a2dp) {
            Log.d("ed", "a2dp service connected. profile = " + profile);
            if (profile == BluetoothProfile.A2DP) {
                mA2dpService = (BluetoothA2dp) a2dp;
                if (mAudioManager.isBluetoothA2dpOn()) {
                    setIsA2dpReady(true);
                    Log.e("ed", "bluetooth a2dp is on while service connected");
                    //playMusic();
                } else {
                    Log.e("ed", "bluetooth a2dp is not on while service connected");
                }
            }
        }
 
        @Override
        public void onServiceDisconnected(int profile) {
            setIsA2dpReady(false);
        }
 
    };
*/
    /*
    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;
 
        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }
 
        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if (activity != null) {
                //activity.handleMessage(msg);
 
            }
        }
    }
 
    private final MyHandler mHandler = new MyHandler(this);*/

    /**
     * Instances of anonymous classes do not hold an implicit
     * reference to their outer class when they are "static".
     */
    private final Runnable sRunnable = new Runnable() {
        @Override
        public void run() {

            adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter != null) {
                Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
                if (pairedDevices != null) {
                    for (BluetoothDevice device : pairedDevices) {
                        connectUsingBluetoothA2dp(MainActivity.this,device);
                        Log.e("pairedDevices", "DeviceName=" + device.getName() + " macaddress=" + device.getAddress());
                        /*
                        String sDeviceName = device.getName().trim();
                        String macaddress = device.getAddress().trim();
                        Log.e("pairedDevices","DeviceName="+sDeviceName+" macaddress="+ macaddress);
                        ConnectThread connectBtThread = new ConnectThread(device);
                        Log.e("pairedDevices","connectBtThread start");
                        connectBtThread.start();
                        Log.e("pairedDevices", "connectBtThread end");*/
                    }
                }
            }

            Log.e("ed","@@@@@@@@@@@@@@@");
            while(isRecording){
                //readBytes = recorder.read(data, 0, bufferSize);
                //writtenBytes += audioPlayer.write(data, 0, readBytes);
                recorder.read(buffer, 0, bufferSize);
                audioPlayer.write(buffer, 0, buffer.length);
            }
        }

    };

    public void connectUsingBluetoothA2dp(Context context,
                                          final BluetoothDevice deviceToConnect) {
        try {
            Class<?> c2 = Class.forName("android.os.ServiceManager");
            Method m2 = c2.getDeclaredMethod("getService", String.class);
            IBinder b = (IBinder) m2.invoke(c2.newInstance(), "bluetooth_a2dp");
            if (b == null) {
                // For Android 4.2 Above Devices
                BluetoothAdapter.getDefaultAdapter().getProfileProxy(context,
                        new ServiceListener() {

                            @Override
                            public void onServiceDisconnected(int profile) {

                            }
                            @Override
                            public void onServiceConnected(int profile,
                                                           BluetoothProfile proxy) {
                                BluetoothA2dp a2dp = (BluetoothA2dp) proxy;
                                try {
                                    a2dp.getClass()
                                            .getMethod("connect",BluetoothDevice.class)
                                            .invoke(a2dp, deviceToConnect);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }, BluetoothProfile.A2DP);
            }else{
                // For Android below 4.2 devices
                //Class<?> c3 = Class.forName("android.bluetooth.IBluetoothA2dp");
                //Class<?>[] s2 = c3.getDeclaredClasses();
                //Class<?> c = s2[0];
                //Method m = c.getDeclaredMethod("asInterface", IBinder.class);
                //m.setAccessible(true);
                //IBluetoothA2dp a2dp = (IBluetoothA2dp) m.invoke(null, b);
                //a2dp.connect(deviceToConnect);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ConnectThread extends Thread {

        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");//00000000-0000-1000-8000-00805F9B34FB
        //0000110E-0000-1000-8000-00805F9B34FB
        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;
            Log.e("ed","mmDevice uuid xxxxx="+mmDevice.getName());

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);//00001101-0000-1000-8000-00805F9B34FB
                Log.e("ed","device.createRfcommSocketToServiceRecord");
                //Method m = mmDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
                //mmSocket = (BluetoothSocket) m.invoke(mmDevice, 1);
            } catch (IOException e) {
                Log.e("ed","Error creating socket");
            }
            mmSocket = tmp;
            //Log.e("ed","mmSocket "+mmSocket.);

        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            Log.e("exp", "adapter.cancelDiscovery start");
            adapter.cancelDiscovery();
            Log.e("exp", "adapter.cancelDiscovery end");
            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                //Log.e("ed","run sDeviceName="+ mmDevice.getName() +" macaddress" + mmDevice.getAddress() + "method" +mmDevice.getUuids());
                Log.e("exp", "mmSocket.connect() start");
                mmSocket.connect();
                Log.e("exp", "mmSocket.connect() end");
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    Log.e("exp", "mmSocket connectException" +connectException);
                    mmSocket.close();
                    Log.e("exp", "mmSocket.close() end");
                } catch (IOException closeException) {
                    Log.e("exp", "Could not close the client socket" + closeException);
                }

                // Unable to connect; close the socket and get out
                /*
                try {
                    Log.e("ed","connectException="+connectException);
                    //mmSocket =(BluetoothSocket) mmDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(mmDevice,1);
                    mmSocket.connect();
                    Log.e("ed", "BluetoothSocket=#########");
                   // mmSocket.close();
                } catch (IOException closeException) { } catch (InvocationTargetException e) {
                    e.printStackTrace();
                    Log.e("ed", "InvocationTargetException=" + e);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                    Log.e("ed", "printStackTrace=" + e);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    Log.e("ed", "IllegalAccessException=" + e);
                }*/
                return;
            }

            // Do work to manage the connection (in a separate thread)
            //manageConnectedSocket(mmSocket);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e("exp", "Could not close the client socket" + e);
            }
        }
    }


}