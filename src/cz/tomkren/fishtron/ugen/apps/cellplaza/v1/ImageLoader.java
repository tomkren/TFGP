package cz.tomkren.fishtron.ugen.apps.cellplaza.v1;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**Created by tom on 11.03.2017.*/

public class ImageLoader {

    public static PlazaImg loadPlazaMap(String filename) {

        BufferedImage img;

        try {
            img = ImageIO.read(new File(filename));
        } catch (IOException e) {
            throw new Error(e);
        }

        int height = img.getHeight();
        int width = img.getWidth();

        int numPixels = 0;
        int numRedPixels = 0;

        int rgb;
        int red;
        int green;
        int blue;

        double percentPixel;

        System.out.println(height  + "  " +  width /*+ " " + img.getRGB(30, 30)*/);

        Color[][] pixels = new Color[height][width];

        for (int y = 0; y<height; y++) {

            Color[] pixelRow = new Color[width];

            for (int x = 0; x<width; x++) {

                numPixels++;

                rgb = img.getRGB(x, y);
                red = (rgb >> 16 ) & 0x000000FF;
                green = (rgb >> 8 ) & 0x000000FF;
                blue = (rgb) & 0x000000FF;

                pixelRow[x] = new Color(red, green, blue);

                if (red > 200 && green < 50 && blue < 50) {
                    numRedPixels ++;
                }
            }

            pixels[y] = pixelRow;
        }



        percentPixel = 100.0 * (double)numRedPixels / (double)numPixels;

        System.out.println("amount pixel: "+numPixels);
        System.out.println("amount red pixel: "+numRedPixels);
        System.out.println("amount pixel red percent: "+percentPixel+" %");

        return new PlazaImg(pixels);
    }
}
