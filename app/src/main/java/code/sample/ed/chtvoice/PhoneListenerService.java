package code.sample.ed.chtvoice;

import java.io.File;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Created by CTBC_CCIT3_04 on 17/2/13.
 */
public class PhoneListenerService extends Service {

    Context context = PhoneListenerService.this;
    private final String TAG = "PhoneListenerService";
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(new TeleListine(), PhoneStateListener.LISTEN_CALL_STATE);
        Log.i(TAG,"onCreate");
        super.onCreate();
    }

    private class TeleListine extends PhoneStateListener {

        private String mobile; //来电电话
        private MediaRecorder recorder; //多媒体刻录文件
        private File autoFile; //保存文件
        private boolean recoder; //是否刻录

        @Override
        public void onCallStateChanged(int state,String incomingNumber) {
            try{
                switch(state) {
                    case TelephonyManager.CALL_STATE_IDLE :

                        if(recoder) {
                            recorder.stop();
                            recorder.release();
                            recoder = false;
                        }
                        break;

                    case TelephonyManager.CALL_STATE_OFFHOOK :
                        recorder = new MediaRecorder();
                        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);                 //这里只是录制自己的声音，如果想录制双方的通话声音，可改用MediaRecorder.AudioSource.VOICE_CALL
                        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                        String root_directory = context.getCacheDir().getPath() + "/recorded_calls";
                        Log.e("ed","root_directory="+root_directory);
                        File root_file = new File(root_directory);
                        if(!root_file.exists()) {
                            root_file.mkdir();
                        }
                        String record_call = root_directory + "/" + mobile + "_" + System.currentTimeMillis() + ".3gp";
                        File autoFile = new File(record_call);
                        if(!autoFile.exists()) {
                            autoFile.createNewFile();
                        }
                        //autoFile = new File(getCacheDir(),mobile+ "_" + System.currentTimeMillis() + ".3gp");
                        recorder.setOutputFile(autoFile.getAbsolutePath());
                        recorder.prepare();
                        recorder.start();
                        recoder = true;
                        Log.i(TAG,"接起电话");
                        break;

                    case TelephonyManager.CALL_STATE_RINGING :
                        mobile = incomingNumber;
                        Log.i(TAG,"mobile=" + mobile);
                        break;
                    default :
                        break;
                }
            }catch(Exception e) {
                Log.i(TAG,e.toString());
                e.printStackTrace();
            }
            super.onCallStateChanged(state, incomingNumber);
        }


    }

}