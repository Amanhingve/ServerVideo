/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cacoalpano.GUI;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameGrabber;

public class sendData implements Runnable {

    private volatile boolean sema;
    private OpenCVFrameGrabber grabber;
    private FFmpegFrameRecorder recoder;
    public static int captureWidth = 1280;
    public static int captureHeight = 720;
    public final static int FRAME_RATE = 30;
    public final static int GOP_LENGTH_IN_FRAMES = 60;

    public sendData(OpenCVFrameGrabber grabber) {
        this.sema = false;
        this.grabber = grabber;
    }

    public boolean isSema() {
        return sema;
    }

    public void setSema(boolean sema) {
        this.sema = sema;
    }

    public OpenCVFrameGrabber getGrabber() {
        return grabber;
    }

    public void setGrabber(OpenCVFrameGrabber grabber) {
        this.grabber = grabber;
    }

    @Override
    public void run() {
        try {
            ServerSocket server = new ServerSocket(8000);
            Socket s;
            while (sema) {
                try {
                    s = server.accept();
                    DataOutputStream output = (DataOutputStream) s.getOutputStream();
                    recoder = new FFmpegFrameRecorder(output, captureWidth, captureHeight);
                    recoder.setInterleaved(true);
                    recoder.setVideoOption("tune", "zerolatency");
                    recoder.setVideoOption("preset", "ultrafast");
                    recoder.setVideoOption("crf", "28");
                    recoder.setVideoBitrate(2000000);
                    recoder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                    recoder.setFormat("avi");
                    recoder.setFrameRate(FRAME_RATE);
                    recoder.setGopSize(GOP_LENGTH_IN_FRAMES);
                    recoder.start();
                    Frame frame = null;
                    CanvasFrame cFrame = new CanvasFrame("Image");
                    while ((frame = grabber.grab()) != null) {
                        cFrame.showImage(frame);
                        recoder.record(frame);
                        if (!sema) {
                            cFrame.dispose();
                            wait();
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(sendData.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(sendData.class.getName()).log(Level.SEVERE, null, ex);
                }

                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                
                
            }
        } catch (IOException ex) {
            Logger.getLogger(sendData.class.getName()).log(Level.SEVERE, null, ex);
        }
        } 
}
