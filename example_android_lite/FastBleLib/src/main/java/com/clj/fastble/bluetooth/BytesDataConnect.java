package com.clj.fastble.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.clj.fastble.data.BleDevice;
import com.clj.fastble.logs.ConnectConstants;
import com.clj.fastble.logs.LogTool;
import com.clj.fastble.utils.HexUtil;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.UUID;

/**
 * Created by asus on 2018/1/17.
 */

abstract class BytesDataConnect {

    private static final int WHAT_SEND_NO_ANSWER_TIMEOUT = 1;
    private static final int WHAT_SEND_ERROR = 2;

    private static final int WHAT_SEND_Next = 3;

    /**
     * Timeout for no response after sending
     */
    private static final long TIME_SEND_TIMEOUT = 2 * 60 * 1000;

    private static final int MAX_CMD_QUEUE_SIZE = 20;
    /**
     * Queue of data commands waiting to be sent
     */
    private LinkedList<ByteDataRequest> mCmdDataQueue = new LinkedList<>();

    private final Object mLock = new Object();

    ByteDataRequest currentByteDataRequest;
    /**
     * Whether data is being sent
     */
    private boolean mIsSendingCmdData = false;

    /**
     * Whether data is being resent
     */
    private boolean mIsReSendData = false;
    /**
     * Number of consecutive write failures
     */
    private int mWriteDataFailedCount = 0;
    private int mWriteDataFailedMax = 20;

    private BluetoothGattCharacteristic mWriteNormalGattCharacteristic;
    private BluetoothGattCharacteristic mWriteHealthGattCharacteristic;

    protected String uuid_service;
    protected String uuid_write;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == WHAT_SEND_NO_ANSWER_TIMEOUT && mIsSendingCmdData) {
                mIsSendingCmdData = false;
                mWriteDataFailedCount++;
                if (mWriteDataFailedCount > mWriteDataFailedMax) {
                    LogTool.e(ConnectConstants.LOG_TGA, "[BytesDataConnect] last send out time, mCmdDataQueue.size() > 20, clear and " +
                            "disconnect");
                    clearQueueAndDisconnect();
                } else {
                    LogTool.e(ConnectConstants.LOG_TGA, "[BytesDataConnect] no respond on last cmd, send next ...");
                    sendNextCmdData();
                }
            } else if (msg.what == WHAT_SEND_ERROR) {
                LogTool.e(ConnectConstants.LOG_TGA, "[BytesDataConnect] send error  delay send ...");
                mIsSendingCmdData = false;
                sendNextCmdData();
            } else if (msg.what == WHAT_SEND_Next) {
                sendNextCmdData();
            }
        }
    };

    protected boolean isCanSendData() {
        return true;
    }

    protected void clearQueueAndDisconnect() {
        LogTool.e(ConnectConstants.LOG_TGA, "[BytesDataConnect] clearQueueAndDisconnect");
        mCmdDataQueue.clear();
    }

//    private void handleWriteFailedStatus(){
//        mIsSendingCmdData = false;
//        if (mCmdDataQueue.size() > MAX_CMD_QUEUE_SIZE){
//            LogTool.e(ConnectConstants.LOG_TGA, "[BytesDataConnect] handleWriteFailedStatus, mCmdDataQueue.size() > 10, clear and
//            disconnect");
//            clearQueueAndDisconnect();
//        }else {
//            LogTool.e(ConnectConstants.LOG_TGA, "[BytesDataConnect] handleWriteFailedStatus, send next ...");
//            sendNextCmdData();
//        }
//    }

    private synchronized void handleFileTranDataFailedStatus(byte[] failedData) {
        if (!isCanSendData()) {
            mIsReSendData = false;
            mWriteDataFailedCount = 0;
            LogTool.e(ConnectConstants.LOG_TGA, "[BytesDataConnect] handleWriteFailedStatus(), isCanSendData = false. send failed");
            return;
        }

        mWriteDataFailedCount++;
        if (mWriteDataFailedCount > mWriteDataFailedMax) {
            LogTool.e(ConnectConstants.LOG_TGA, "[BytesDataConnect] handleWriteFailedStatus, mSendFailedCount > 20, clear and disconnect");
            clearQueueAndDisconnect();
            mIsReSendData = false;
            mWriteDataFailedCount = 0;
            return;
        }

        //Sometimes writing too fast causes failures, so resend here
        //When resending, the order must not change (e.g. file-transfer commands must not get out of order, otherwise the transfer fails)
        //Only resend “file-transfer-related commands” + Alexa voice commands
        if (isFileTranCmd(failedData) || isSendAlexaVoiceCmd(failedData)) {
            synchronized (mLock) {
                mIsReSendData = true;
                mCmdDataQueue.addFirst(currentByteDataRequest);
                LogTool.e(ConnectConstants.LOG_TGA, "[BytesDataConnect] isFileTranCmd  mIsReSendData:" + mIsReSendData);
                resendData(failedData);
            }
        } else {
            //Delay sending the command when a write error occurs
            mHandler.sendEmptyMessageDelayed(WHAT_SEND_ERROR, 1000);

        }
    }

    private synchronized void sendNextCmdData() {
        if (mIsSendingCmdData) {
            LogTool.e(ConnectConstants.LOG_TGA, "[BytesDataConnect] sendNextCmdData is ing");
            return;
        }

        if (mIsReSendData) {
            LogTool.e(ConnectConstants.LOG_TGA, "[BytesDataConnect] retrySendData is ing");
            return;
        }
        if (mCmdDataQueue.size() == 0) {
            return;
        }
        mIsSendingCmdData = true;
        currentByteDataRequest = getDataFromQueue();
        if (currentByteDataRequest == null || currentByteDataRequest.getSendData() == null) {
            //Only send next when not upgrading; during an upgrade do not send it, otherwise the handler error above fires and causes an endless loop
            LogTool.e(ConnectConstants.LOG_TGA, "[BytesDataConnect] sendNextCmdData data is null|| siche update");
            mIsSendingCmdData = false;
            sendNextCmdData();
            return;
        }
        LogTool.p(ConnectConstants.LOG_TGA,
                "[BytesDataConnect] send => " + HexUtil.formatHexString(currentByteDataRequest.getSendData(), true));
        if (!isCanSendData()) {
            mIsSendingCmdData = false;
            LogTool.e(ConnectConstants.LOG_TGA, "[BytesDataConnect] send(), isCanSendData = false. send failed");
            return;
        }
        sendData();
        //    sendDataDelayTime = CommonUtils.getSendDataDelayTime();
  /*     if(sendDataDelayTime==0){
           sendData(data);
           dataAddToRecord(data);
       }else {
           mHandler.postDelayed(new Runnable() {
               @Override
               public void run() {
                   sendData(data);
                   dataAddToRecord(data);
               }
           },sendDataDelayTime);
       }*/


//        boolean result = writeBytes(data);
//        if (!result){
//            handleWriteFailedStatus(data);
//            return;
//        }
//        //Reset to zero once any write succeeds
//        mWriteDataFailedCount = 0;
//
//        if (isNoNeedWaitResponseCmd(data)){
//            mIsSendingCmdData = false;
//            sendNextCmdData();
//        }else {
//            mIsSendingCmdData = true;
//            mHandler.sendEmptyMessageDelayed(WHAT_SEND_NO_ANSWER_TIMEOUT, TIME_SEND_TIMEOUT);
//        }

    }


    //Take data from the queue
    private ByteDataRequest getDataFromQueue() {
        synchronized (mLock) {
            return mCmdDataQueue.poll();
        }
    }


    private void resendData(final byte[] data) {
        //post to the queue with a short delay
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                LogTool.e(ConnectConstants.LOG_TGA, "[BytesDataConnect] retry send => " + HexUtil.formatHexString(data, true));
                mIsReSendData = false;
                mIsSendingCmdData = false;
                sendNextCmdData();
            }
        }, 300);

    }

    private void sendData() {
        boolean result = writeBytes();
        if (!result) {
//            if (isFileTranCmd(data)) {
//                handleFileTranDataFailedStatus(data);
//            }else {
//                mIsSendingCmdData = false;
//                sendNextCmdData();
//            }

            handleFileTranDataFailedStatus(currentByteDataRequest.getSendData());
            return;
        }
        //Reset to zero once any write succeeds
        mWriteDataFailedCount = 0;
        //As long as the resend (write) has not succeeded, execution never reaches here
        mIsReSendData = false;
        LogTool.e(ConnectConstants.LOG_TGA, "[BytesDataConnect] mIsReSendData " + mIsReSendData);
/*        if (isNoNeedWaitResponseCmd(data)){
            mIsSendingCmdData = false;
            sendNextCmdData();
        }else {*/
        mHandler.sendEmptyMessageDelayed(WHAT_SEND_NO_ANSWER_TIMEOUT, TIME_SEND_TIMEOUT);
        //  }
    }

    @SuppressLint("MissingPermission")
    private boolean writeBytes() {
        final BluetoothGattCharacteristic characteristic = getGattWriteCharacteristic();
        LogTool.e(ConnectConstants.LOG_TGA, "[BytesDataConnect] send() platform:" + currentByteDataRequest.getPlatform());
        if (characteristic != null && (BluetoothGattCharacteristic.PROPERTY_WRITE & characteristic.getProperties()) > 0) {
            characteristic.setValue(currentByteDataRequest.getSendData());

            if (isNoNeedWaitResponseCmd(currentByteDataRequest.getSendData())) {
                characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            } else {
                characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            }
        } else {
            LogTool.e(ConnectConstants.LOG_TGA, "[BytesDataConnect] send(), characteristic error!");
            return false;
        }

        int properties = characteristic.getProperties();

        if ((properties & 0xC) == 0) {
            LogTool.e(ConnectConstants.LOG_TGA, "[BytesDataConnect] send(), characteristic.properties error!");
            return false;
        }

        boolean result = false;
        try {
            result = getRealGatt().writeCharacteristic(characteristic);//Previous approach; when read/write operations are frequent it easily reports [BytesDataConnect] send(),
            if (!result) {
                LogTool.e(ConnectConstants.LOG_TGA, "[BytesDataConnect] send(), writeCharacteristic() error!");
            }
        } catch (Exception e) {
            LogTool.e(ConnectConstants.LOG_TGA, "[BytesDataConnect] send(), writeCharacteristic() Exception!");
        }

        return result;
    }

    protected abstract BluetoothGatt getRealGatt();

    protected abstract BluetoothGattCharacteristic getGattWriteCharacteristic();

    private BluetoothGattCharacteristic getGattCharacteristic(BluetoothGatt gatt) {
        if (mWriteNormalGattCharacteristic == null) {
            mWriteNormalGattCharacteristic = getCharacteristic(gatt, UUID.fromString(uuid_service), UUID.fromString(uuid_write));
        }

        return mWriteNormalGattCharacteristic;
    }

    private static BluetoothGattCharacteristic getCharacteristic(BluetoothGatt gatt, UUID serviceId, UUID characteristicId) {
        if (gatt == null) {
            return null;
        }
        BluetoothGattService service = gatt.getService(serviceId);
        if (service == null) {
            return null;
        }
        BluetoothGattCharacteristic c = service.getCharacteristic(characteristicId);
        return c;
    }

    private boolean isNoNeedWaitResponseCmd(byte[] cmd) {
        //For the Agps file protocol and Alexa voice
        return (cmd[0] & 0xff) == 0xD1 || (cmd[0] & 0xff) == 0x13;
    }

    /**
     * Whether it is a file transfer command
     *
     * @param cmd
     * @return
     */
    private boolean isFileTranCmd(byte[] cmd) {
        return (cmd[0] & 0xff) == 0xD1;
    }

    /**
     * Whether it is a command that sends Alexa voice, or the device getting the login status
     *
     * @param cmd
     * @return
     */
    private boolean isSendAlexaVoiceCmd(byte[] cmd) {
        return (cmd[0] & 0xff) == 0x13 || (cmd[0] & 0xff) == 0x11;
    }

    /**
     * Whether it is the sync-time command
     *
     * @param cmd
     * @return
     */
    private boolean isSendTimeCmd(byte[] cmd) {
        return (cmd[0] & 0xff) == 0x03 && (cmd[1] & 0xff) == 0x01;
    }

    private void receiverDeviceData(byte[] values) {
        LogTool.p(ConnectConstants.LOG_TGA, "[BytesDataConnect] receive <= " + HexUtil.formatHexString(values, true));
    }

    protected void addCmdData(ByteDataRequest request, boolean isForce) {
        if (request == null || request.getSendData() == null) {
            LogTool.e(ConnectConstants.LOG_TGA, "[BytesDataConnect] onAddCmd() ignore, data is null");
            return;
        }
        //Kept for 1min to prevent duplicates; fixes the C library issue where no reply within 5s, or no send-success response within 500ms, caused duplicate commands (duplicate message reminders)
        byte[] cmd = request.getSendData();
        LogTool.p(ConnectConstants.LOG_TGA, "[BytesDataConnect] addCmdData( " + HexUtil.formatHexString(cmd, true) + ")");
        //Send the time command first, otherwise the time will lag
        if (isSendTimeCmd(cmd)) {
            isForce = true;
        }
        int size = mCmdDataQueue.size();
        synchronized (mLock) {
            if (isForce) {
                mCmdDataQueue.addFirst(request);
            } else {
                mCmdDataQueue.add(request);
            }
        }
        LogTool.p(ConnectConstants.LOG_TGA, "[BytesDataConnect] addCmdData que size = " + size);
        if (size < 2) {
            mHandler.sendEmptyMessage(WHAT_SEND_Next);
        }
    }

    protected void reset() {
        destroy();
    }

    private void destroy() {
        mCmdDataQueue.clear();
        mIsSendingCmdData = false;
        mIsReSendData = false;
        mWriteDataFailedCount = 0;
        mHandler.removeCallbacksAndMessages(null);
        mWriteHealthGattCharacteristic = null;
        mWriteNormalGattCharacteristic = null;
    }

    protected void deviceResponseOnLastSend(byte[] values, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            LogTool.p(ConnectConstants.LOG_TGA,
                    "[BytesDataConnect] onDeviceResponseOnLastSend( " + HexUtil.formatHexString(values, true) + ")");
        } else {
            LogTool.e(ConnectConstants.LOG_TGA,
                    "[BytesDataConnect] onDeviceResponseOnLastSend[failed]( " + HexUtil.formatHexString(values, true) + ")");
        }

        mIsSendingCmdData = false;
        mHandler.removeMessages(WHAT_SEND_NO_ANSWER_TIMEOUT);
//        handleCmdDataQueue();
//        CommonUtils.runOnMainThread(new Runnable() {
//            @Override
//            public void run() {
//                sendNextCmdData();
//            }
//        });
        sendNextCmdData();

    }

}
