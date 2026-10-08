package org.hospitalqueing.ui;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Renders queue ticket tokens to real, scannable QR codes.
 *
 * <p>Tokens were previously shown as a plain UUID string (not scannable). A waiting-room display or
 * a staff counter can now scan the QR to look the ticket up by its {@code qr_token}.
 *
 * <p>Uses zxing (already on the classpath). If encoding ever fails (it shouldn't for a UUID),
 * {@link #toImage(String, int)} returns a non-null fallback image so the UI never breaks.
 */
public final class QrUtils {

  private QrUtils() {}

  /**
   * Renders {@code content} as a {@code size x size} QR image with quiet zone and high error
   * correction (the display/scan environment is out of our control).
   *
   * @param content the data to encode (a queue {@code qr_token})
   * @param size    square edge length in pixels
   */
  public static BufferedImage toImage(String content, int size) {
    if (content == null) {
      content = "";
    }
    if (content.isEmpty()) {
      return blank(size);
    }
    try {
      Map<EncodeHintType, Object> hints = new HashMap<>();
      hints.put(EncodeHintType.MARGIN, 2);
      hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
      hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
      BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints);
      return MatrixToImageWriter.toBufferedImage(matrix);
    } catch (Exception ex) {
      // Degenerate fallback: a blank image the size requested so a JLabel still has something.
      ex.printStackTrace();
      return blank(size);
    }
  }

  private static BufferedImage blank(int size) {
    BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
    java.awt.Graphics2D g = img.createGraphics();
    try {
      g.setColor(java.awt.Color.WHITE);
      g.fillRect(0, 0, size, size);
    } finally {
      g.dispose();
    }
    return img;
  }
}
