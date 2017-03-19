package cz.tomkren.fishtron.ugen.apps.cellplaza.v1;

import cz.tomkren.utils.Log;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**Created by tom on 12.03.2017.*/

public class PlazaImg {

    private static final String BASE_DIR = "cellplaza/";

    private final Color[][] pixels;

    static PlazaImg mk(String filename) {
        if (filename == null) {return null;}

        Log.it_noln("Trying to load "+filename+" ... ");
        File file = new File(BASE_DIR + filename);
        if (file.exists()) {
            PlazaImg ret = new PlazaImg(file);
            Log.it("done.");
            return ret;
        } else {
            Log.it("does not exist.");
            return null;
        }
    }

    PlazaImg(Color[][] pixels) {
        this.pixels = pixels;
    }

    private PlazaImg(File file) {

        BufferedImage img;

        try {
            img = ImageIO.read(file);
        } catch (IOException e) {
            throw new Error(e);
        }

        int height = img.getHeight();
        int width = img.getWidth();

        pixels = new Color[height][width];

        for (int y = 0; y<height; y++) {

            Color[] pixelRow = new Color[width];

            for (int x = 0; x<width; x++) {

                int rgb = img.getRGB(x, y);
                int red = (rgb >> 16 ) & 0x000000FF;
                int green = (rgb >> 8 ) & 0x000000FF;
                int blue = (rgb) & 0x000000FF;

                pixelRow[x] = new Color(red, green, blue);
            }
            pixels[y] = pixelRow;
        }
    }

    public int getWidth() {
        return pixels[0].length;
    }

    public int getHeight() {
        return pixels.length;
    }

    void checkSize(PlazaImg img2) {
        int w = img2.getWidth();
        int h = img2.getHeight();
        if (getWidth() != w) {throw new Error("Width "+getWidth()+" is not matching desired w = "+w);}
        if (getHeight() != h) {throw new Error("Height "+getHeight()+" is not matching desired h = "+h);}
    }

    Color getColor(int x, int y) {
        return pixels[y][x];
    }

    void writeImage(String filename) {
        try {

            Log.it_noln("Writing "+filename+" ... ");

            int w = getWidth();
            int h = getHeight();

            // TYPE_INT_ARGB specifies the image format: 8-bit RGBA packed into integer pixels
            BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g = bi.createGraphics();

            g.setColor(Color.white);
            g.fillRect(0, 0, w, h);

            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    g.setColor(getColor(x, y));
                    putPixel(g, x, y);
                }
            }

            ImageIO.write(bi, "PNG", new File(BASE_DIR + filename));

            Log.it("done");

        } catch (IOException e) {
            throw new Error(e);
        }
    }

    private static void putPixel(Graphics2D g, int x, int y) {
        g.drawLine(x, y, x, y);
    }


}
