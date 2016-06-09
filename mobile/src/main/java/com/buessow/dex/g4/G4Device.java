package com.buessow.dex.g4;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by buessow on 6/6/16.
 */
public class G4Device {

  private static final int DEXCOM_G4_USB_VENDOR = 0x22a3;
  private static final int DEXCOM_G4_USB_PRODUCT = 0x0047;

  private OutputStream out;
  private InputStream in;

  public G4Device(InputStream in, OutputStream out) {
    this.in = in;
    this.out = out;
  }

  public static class UsbInputStream extends InputStream {
    private final UsbDeviceConnection connection;
    private final UsbEndpoint endpoint;

    public UsbInputStream(UsbDeviceConnection connection, UsbEndpoint endpoint) {
      this.connection = connection;
      this.endpoint = endpoint;
    }

    @Override public int read() throws IOException {
      byte[] buffer = new byte[1];
      read(buffer);
      return buffer[0];
    }

    @Override public int read(byte[] buffer) throws IOException {
      return read(buffer, 0, buffer.length);
    }

    @Override public int read(byte[] buffer, int off, int len) throws IOException {
      Log.v("G4", "reading: " + len + " bytes");
      return connection.bulkTransfer(endpoint, buffer, off, len, 10000);
    }
  }

  public static class OpenDeviceActivity extends Activity {

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grant_results) {
      Log.v("G4", "result");
      UsbDevice dev = (UsbDevice)getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE);
      for (int i = 0; i < permissions.length; i++) {
        Log.v("G4", "perm: "+ permissions[i]);
      }
      UsbManager manager = (UsbManager)getSystemService(Context.USB_SERVICE);
      openDevice(manager, dev);
    }

  }

  public static List<String> connect(Context ctx) throws Exception {
    List<String> result = new ArrayList<String>();
    getDevice(ctx);
    return result;
  }

  private static void openDevice(UsbManager manager, UsbDevice dev) {
    UsbDeviceConnection con = manager.openDevice(dev);
    for (int i = 0; i < dev.getInterfaceCount(); i++) {
      UsbInterface itf = dev.getInterface(i);
      Log.v("G4", i + ": "+ itf.getName());
      for (int j = 0; j < itf.getEndpointCount(); j++) {
        UsbEndpoint endpoint = itf.getEndpoint(j);
        Log.v("G4", "ep: " + endpoint.getDirection());
      }
    }

    UsbEndpoint in = null;
    UsbEndpoint out = null;
    UsbInterface itf = dev.getInterface(1);
    con.claimInterface(itf, true);
    for (int j = 0; j < itf.getEndpointCount(); j++) {
      UsbEndpoint endpoint = itf.getEndpoint(j);
      switch (endpoint.getDirection()) {
        case UsbConstants.USB_DIR_IN:
          in = endpoint;
          break;
        case UsbConstants.USB_DIR_OUT:
          out = endpoint;
          break;
        default:
          break;
      }
    }

    Log.v("G4", "sending");
    byte[] data = new Packet(Command.READ_FIRMWARE_HEADER).format();
    int len = con.bulkTransfer(out, data, data.length, 60000);
    Log.v("G4", "send: " + len + " bytes");

    Log.v("G4", "recv");
    try {
      InputStream ins = new UsbInputStream(con, in);
      Packet p = new Packet(ins);
      Log.v("G4", "recv: " + p.getDataAsString());
    } catch (Exception e) {
      Log.e("G4", "read failed", e);
    }
    con.close();
  }

  private static void getDevice(Context ctx) throws Exception {
    UsbManager manager = (UsbManager) ctx.getSystemService(Context.USB_SERVICE);
    HashMap<String, UsbDevice> devices = manager.getDeviceList();
    Log.v("G4", "found "+ devices.size());
    for (UsbDevice dev : devices.values()) {
      Log.v("G4", "device: " + dev.getDeviceName() + " " + dev.getManufacturerName()
       + " vendor:" + dev.getVendorId() + " prod: " + dev.getProductId());

      if (dev.getVendorId() == DEXCOM_G4_USB_VENDOR
              && dev.getProductId() == DEXCOM_G4_USB_PRODUCT) {
        if (!manager.hasPermission(dev)) {
          manager.requestPermission(
                  dev,
                  PendingIntent.getActivity(
                          ctx,
                          0,
                          new Intent(ctx, OpenDeviceActivity.class),
                          PendingIntent.FLAG_ONE_SHOT));

        } else {
          openDevice(manager, dev);
        }
      }
    }
  }

  public String getFirmwareHeader() throws Exception {
    new Packet(Command.READ_FIRMWARE_HEADER).write(out);
    return new Packet(in).getDataAsString();
  }
}
