package cz.tomkren.fishtron.ugen.apps.cellplaza.v1;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**Created by tom on 11.03.2017.*/

public class ImageWriter {

    public static void main(String[] args) {
        try {
            int w = 101;

            // TYPE_INT_ARGB specifies the image format: 8-bit RGBA packed into integer pixels
            BufferedImage bi = new BufferedImage(w, w, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g = bi.createGraphics();

            g.setColor(Color.white);
            g.fillRect(0,0,w, w);

            for (int i = 0; i<w; i++) {
                int c = w - i - 1;
                g.setColor(new Color(c,c,c));
                putPixel(g, i, i);
                putPixel(g, 0, i);
            }

            ImageIO.write(bi, "PNG", new File("cellplaza/out.png"));

        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    private static void putPixel(Graphics2D g, int x, int y) {
        g.drawLine(x, y, x, y);
    }

}
