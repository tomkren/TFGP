package cz.tomkren.fishtron.ugen.apps.cellplaza.shared;

import cz.tomkren.fishtron.ugen.apps.cellplaza.v2.CellPlaza;
import cz.tomkren.utils.F;
import cz.tomkren.utils.Log;
import org.json.JSONArray;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**Created by tom on 12.03.2017.*/

public class PlazaImg {

    private final Color[][] pixels;
    private List<Integer> pixelSizes;
    private final boolean silent;

    private void log(Object x) {if (!silent) {Log.it(x);}}
    private void log_noln(Object x) {if (!silent) {Log.it_noln(x);}}

    private static void log(Object x, boolean silent) {if (!silent) {Log.it(x);}}
    private static void log_noln(Object x, boolean silent) {if (!silent) {Log.it_noln(x);}}


    public static PlazaImg mk(String filename, boolean silent) {
        if (filename == null) {return null;}

        log_noln("Trying to load "+filename+" ... ",silent);
        File file = new File(CellPlaza.BASE_DIR +"/"+ filename);
        if (file.exists()) {
            PlazaImg ret = new PlazaImg(file, silent);
            log("done.", silent);
            return ret;
        } else {
            log("does not exist.", silent);
            return null;
        }
    }

    public PlazaImg(Color[][] pixels, JSONArray pixelSizes, boolean silent) {
        this.pixels = pixels;
        this.pixelSizes = F.map(pixelSizes,x->(int)x);
        this.silent = silent;
    }

    public PlazaImg(File file) {
        this(file, true);
    }

    private PlazaImg(File file, boolean silent) {
        this.silent = silent;

        BufferedImage img;

        try {
            img = ImageIO.read(file);
        } catch (IOException e) {
            throw new Error(e);
        }

        int height = img.getHeight();
        int width = img.getWidth();

        pixels = new Color[height][width];
        pixelSizes = Collections.singletonList(1);

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

    public void checkSize(PlazaImg img2) {
        int w = img2.getWidth();
        int h = img2.getHeight();
        if (getWidth() != w) {throw new Error("Width "+getWidth()+" is not matching desired w = "+w);}
        if (getHeight() != h) {throw new Error("Height "+getHeight()+" is not matching desired h = "+h);}
    }

    public Color getColor(int x, int y) {
        return pixels[y][x];
    }

    public int sumColor() {
        int sum = 0;
        int w = getWidth();
        int h = getHeight();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                sum += getColor(x, y).getRed();
            }
        }
        return sum;
    }

    public void writeImage(String filename) {

        if (pixelSizes == null || pixelSizes.isEmpty()) {
            pixelSizes = Collections.singletonList(1);
        }

        writeImage("", filename, pixelSizes.get(0));
    }

    public void writeImage(String dir, String filename) {

        if (pixelSizes == null || pixelSizes.isEmpty()) {
            pixelSizes = Collections.singletonList(1);
        }

        writeImage(dir, filename, pixelSizes.get(0));

        for (int i = 1; i < pixelSizes.size(); i++) {
            int pixelSize = pixelSizes.get(i);

            String dirPath = dir+"/px"+pixelSize;

            String fullDirPath = alreadyContainsBaseDir(dirPath) ? dirPath : CellPlaza.BASE_DIR+"/"+dirPath;

            if (!(new File(fullDirPath).exists())) {
                if (!(new File(fullDirPath).mkdirs())) {throw new Error("Unable to create "+dir+" directory!");}
            }

            writeImage(dirPath, filename, pixelSize);
        }


    }

    private void writeImage(String dir, String filename, int pixelSize) {
        try {

            log_noln("Writing "+filename+" ... ");

            int w = getWidth();
            int h = getHeight();

            // TYPE_INT_ARGB specifies the image format: 8-bit RGBA packed into integer pixels
            BufferedImage bi = new BufferedImage(w*pixelSize, h*pixelSize, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g = bi.createGraphics();

            g.setColor(Color.white);
            g.fillRect(0, 0, w*pixelSize, h*pixelSize);

            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    g.setColor(getColor(x, y));
                    putPixel(g, x, y,pixelSize);
                }
            }

            String prefix = alreadyContainsBaseDir(dir) ? "" : CellPlaza.BASE_DIR +"/";

            ImageIO.write(bi, "PNG", new File(prefix + (dir.equals("") ? "" : dir+"/")+ filename));

            log("done");

        } catch (IOException e) {
            throw new Error(e);
        }
    }

    private static boolean alreadyContainsBaseDir(String pathStr) {
        return CellPlaza.BASE_DIR.equals(pathStr.substring(0,CellPlaza.BASE_DIR.length()));
    }

    private void putPixel(Graphics2D g, int x, int y, int pixelSize) {
        g.fillRect(x * pixelSize, y * pixelSize, pixelSize, pixelSize);
    }


}
