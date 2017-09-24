package cz.tomkren.fishtron.ugen.apps.cellplaza;

import cz.tomkren.fishtron.ugen.apps.cellplaza.shared.PlazaImg;
import net.fishtron.utils.Checker;
import net.fishtron.utils.F;
import org.json.JSONArray;

import java.io.File;

/**Created by tom on 25.03.2017.*/

public class GradientMaster {

    public static void main(String[] args) {

        String plazaPath = "cellplaza/kostky/winner.png";
        JSONArray tilePaths  = F.arr("cellplaza/kostky/3.png", "cellplaza/kostky/2.png", "cellplaza/kostky/1.png");

        String dir = "cellplaza/kostky";

        File plazaFile = new File(plazaPath);

        PlazaImg plazaImg = new PlazaImg(plazaFile);

        String newFilename = plazaFile.getName() +"_with_"+ String.join("_", F.map(tilePaths,x-> new File((String)x).getName() ))+".png";
        PlazaImg zoomedImg = plazaImg.zoom(tilePaths);

        zoomedImg.writeImage(dir, newFilename);



    }


    public static void main_grad(String[] args) {

        Checker ch = new Checker();

        PlazaImg original = new PlazaImg(new File("cellplaza/gradient/fontany_2x3.png"));
        PlazaImg gradient = new PlazaImg(new File("cellplaza/gradient/grad4.png"));

        PlazaImg result = original.chessboardGradient(gradient, F.arr(1), ch.getRandom());

        result.writeImage("cellplaza/gradient", "result"+ch.getSeed()+".png");

        ch.results();
    }

}
