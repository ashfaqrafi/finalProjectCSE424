package com.cse424.project;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;

import android.media.MediaPlayer;
import android.os.ParcelFileDescriptor;

public class AudioReceiver  {
    private boolean isReceiving;
    private int mSourcePort;
    private MediaPlayer mPlayer;
    private ServerSocket mSocket;

    public AudioReceiver(int sourcePort)    {
        mSourcePort = sourcePort;
        isReceiving = false;
    }

    public void start() {
        try {
            mSocket = new ServerSocket(mSourcePort);
            mPlayer = new MediaPlayer();
            isReceiving = true;

            while(isReceiving)  new AudioReceiverThread(mSocket, mPlayer);
        } catch(IOException e)  {
            e.printStackTrace();
        }
    }

    public void stop()  {
        isReceiving = false;

        if(mPlayer == null) return;

        if(mPlayer.isPlaying()) mPlayer.stop();

        mPlayer.release();
        mPlayer.reset();

        if(mSocket == null) return;

        try {
            mSocket.close();
        } catch(IOException e)  {
            e.printStackTrace();
        }
    }

    private static class AudioReceiverThread extends Thread {
        private MediaPlayer mPlayer;
        private ServerSocket mSocket;

        public AudioReceiverThread(ServerSocket socket, MediaPlayer player) {
            mSocket = socket;
            mPlayer = player;

            start();
        }

        @Override
        public void run()   {
            FileInputStream fis = null;

            try {
                 fis = new FileInputStream(ParcelFileDescriptor.fromSocket(mSocket.accept()).getFileDescriptor());

                if(fis.available() != -1)   {
                    mPlayer.setDataSource(fis.getFD());
                    mPlayer.prepare();
                    mPlayer.start();
                }
            } catch(IOException e)  {
                e.printStackTrace();
            } finally {
                if(fis != null) {
                    try {
                        fis.close();
                    } catch(IOException e)  {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
