package com.buessow.dex.g4;

import java.io.OutputStream;

/**
 * Created by buessow on 6/6/16.
 */
public class G4Device {
  private OutputStream out;

  public G4Device(OutputStream out) {
    this.out = out;
  }

  public String getFirmwareHeader() throws Exception {
    out.write(PackageFormatter.format(Command.READ_FIRMWARE_HEADER));
    return "";
  }
}
