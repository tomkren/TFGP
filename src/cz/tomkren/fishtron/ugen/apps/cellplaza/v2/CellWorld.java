package cz.tomkren.fishtron.ugen.apps.cellplaza.v2;

import cz.tomkren.fishtron.ugen.apps.cellplaza.shared.PlazaImg;
import cz.tomkren.utils.AA;
import net.fishtron.utils.F;
import cz.tomkren.utils.Log;
import org.json.JSONArray;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**Created by tom on 11.03.2017.*/

public class CellWorld {

    private final int numStates;

    private Cell[][] cells;
    private int step;
    private Rule rule;
    private String plazaDir;

    private final JSONArray pixelSizes;

    private final Color[] state2color;
    private final double c2s_beta;
    private final double c2s_alpha;

    private final boolean silent;

    CellWorld(CellOpts opts, String coreName, Rule rule, boolean writeTestImages) {

        this.numStates = opts.getNumStates();
        this.pixelSizes = opts.getPixelSizes();
        this.silent = opts.isSilent();

        this.rule = rule;
        this.step = 0;
        this.plazaDir = opts.getPlazaDir();

        state2color = new Color[numStates];
        initStateToColor();
        c2s_alpha = (1.0 - numStates) / 255;
        c2s_beta = numStates - 1.0;


        String filename_seed = plazaDir + "/seed.png";
        //String filename_grad = plazaDir + "/grad.png";
        String filename_sens = plazaDir + "/sens.png";
        String filename_core = coreName == null ? null : plazaDir +"/cores/"+ coreName;

        PlazaImg seed = PlazaImg.mk(filename_seed, silent);
        PlazaImg core = PlazaImg.mk(filename_core, silent);
        //PlazaImg grad = PlazaImg.mk(filename_grad, silent);
        PlazaImg sens = PlazaImg.mk(filename_sens, silent);

        checkSizes(seed, /*grad,*/ sens);

        mkCells(seed, core, /*grad,*/ sens);

        if (writeTestImages) {
            writeCellsImgs();
            writeTests(seed, /*grad,*/ sens);
        }

        log("CellWorld created!\n");
    }

    private void log(Object x) {if (!silent) {Log.it(x);}}
    private void log_noln(Object x) {if (!silent) {Log.it_noln(x);}}


    private void mkCells(PlazaImg seed, PlazaImg core, /*PlazaImg grad,*/ PlazaImg sens) {

        int width = seed.getWidth();
        int height = seed.getHeight();

        cells = new Cell[height][width];

        AA<Integer> coreMarker1 = null;
        AA<Integer> coreMarker2 = null;

        // make cells
        for (int y = 0; y < height; y++) {
            Cell[] cellRow = new Cell[width];
            for (int x = 0; x < width; x++) {

                Color seedColor = seed.getColor(x, y);

                if (seedColor.equals(Color.red)) {
                    seedColor = stateToColor(Cell.DEAD_STATE);  //Cell.DEAD_COLOR;
                    AA<Integer> coreMarker = AA.mk(x,y);
                    if (coreMarker1 == null) {
                        coreMarker1 = coreMarker;
                    } else if (coreMarker2 == null) {
                        coreMarker2 = coreMarker;
                    } else {
                        throw new Error("More then two core markers, third : "+coreMarker);
                    }
                }

                int state = colorToState(seedColor, x, y);
                cellRow[x] = new Cell(state);
            }
            cells[y] = cellRow;
        }

        /*if (grad != null) {
            // set gradients
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    Color gradColor = grad.getColor(x, y);
                    double pRule = Cell.colorToPRule(gradColor, x, y);
                    getCell(x, y).setPRule(pRule);
                }
            }
        }*/

        if (core != null) {

            if (coreMarker1 == null || coreMarker2 == null) {
                throw new Error("Less then two core markers.");
            }

            int coreDx = coreMarker1._1();
            int coreDy = coreMarker1._2();
            int coreWidth = coreMarker2._1() - coreDx + 1;
            int coreHeight = coreMarker2._2() - coreDy + 1;
            log("Core markers: " + coreMarker1 + " & " + coreMarker2 + " ... " + coreWidth + " × " + coreHeight);
            if (coreWidth != core.getWidth() || coreHeight != core.getHeight()) {
                throw new Error("Core size does not match core markers: " +
                        core.getWidth() + " × " + core.getHeight() + " -vs- " + coreWidth + " × " + coreHeight);
            }

            // load core
            for (int y = 0; y < coreHeight; y++) {
                for (int x = 0; x < coreWidth; x++) {
                    int xCell = x + coreDx;
                    int yCell = y + coreDy;
                    Color seedColor = core.getColor(x, y);
                    if (!seedColor.equals(Color.white)) {
                        int state = colorToState(seedColor, xCell, yCell);
                        getCell(xCell, yCell).setState(state);
                    }
                }
            }

        }

        // assign neighbours
        int x_max = width - 1;
        int y_max = height - 1;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Cell cell = getCell(x, y);

                Color sensorColor = sens == null ? Color.white : sens.getColor(x,y);

                List<AA<Integer>> diffs = colorToSensorDiffs(sensorColor, x, y, x_max, y_max);
                Cell[] neighbours = new Cell[diffs.size()];

                int n = 0;
                for (AA<Integer> diff : diffs) {
                    neighbours[n] = getCell(x + diff._1(), y + diff._2());
                    n++;
                }

                cell.setNeighbours(neighbours);
            }
        }
    }

    void step() {
        log_noln("Computing next step ("+(step+1)+") ... ");
        eachCell(this::computeNextState);
        eachCell(Cell::setStateToNextState);
        log("done");
        step++;
    }

    void step(int numSteps) {
        for (int s = 0; s < numSteps; s++) {
            step();
        }
    }

    private void computeNextState(Cell cell) {
        cell.computeNextState(rule);
    }

    private void initStateToColor() {

        double beta = 255.0;
        double alpha = beta / (1.0 - numStates);

        for (int s = 0; s < numStates; s++) {
            int r = (int) Math.round(alpha * s + beta);
            state2color[s] = new Color(r, r, r);
        }
    }

    private Color stateToColor(int state) {
        return state2color[state];
    }

    private int colorToState(Color seedColor, int x, int y) {
        if (isGrayScale(seedColor)) {
            return (int) Math.round(c2s_alpha * seedColor.getRed() + c2s_beta);
        } else { // anything non-gray is chessboard
            return (x+y) % 2 == 0 ? Cell.ALIVE_STATE(numStates) : Cell.DEAD_STATE;
        }
    }

    public static int colorToState(Color color, int numStates) {
        double a = (1.0 - numStates) / 255;
        double b = numStates - 1.0;
        return (int) Math.round(a * color.getRed() + b);
    }



    public static boolean isGrayScale(Color color) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        return r == g && r == b;
    }

    private static List<AA<Integer>> colorToSensorDiffs(Color sensorColor, int x, int y, int x_max, int y_max) {
        List<AA<Integer>> diffs = new ArrayList<>(8);

        boolean isLeft   = x == 0;
        boolean isTop    = y == 0;
        boolean isRight  = x == x_max;
        boolean isBottom = y == y_max;

        if (!sensorColor.equals(Color.white)) {
            if (sensorColor.equals(Color.red)) {
                isLeft = true;
            } else if (sensorColor.equals(Color.green)) {
                isRight = true;
            } else if (sensorColor.equals(Color.blue)) {
                isBottom = true;
            } else if (sensorColor.equals(Color.magenta)) {
                isLeft = true;
                isBottom = true;
            } else if (sensorColor.equals(Color.cyan)) {
                isRight = true;
                isBottom = true;
            } else if (!isGrayScale(sensorColor)) {
                throw new Error("Unsupported sensor color : " + sensorColor.toString() + " on pos [x=" + x + ", y=" + y + "].");
            }
        }

        if (!isTop)                {diffs.add(AA.mk( 0,-1));}
        if (!isBottom)             {diffs.add(AA.mk( 0, 1));}
        if (!isLeft)               {diffs.add(AA.mk(-1, 0));}
        if (!isRight)              {diffs.add(AA.mk( 1, 0));}
        if (!isTop    && !isLeft)  {diffs.add(AA.mk(-1,-1));}
        if (!isTop    && !isRight) {diffs.add(AA.mk( 1,-1));}
        if (!isBottom && !isLeft)  {diffs.add(AA.mk(-1, 1));}
        if (!isBottom && !isRight) {diffs.add(AA.mk( 1, 1));}

        return diffs;
    }



    private void eachCell(Consumer<Cell> processCell) {
        int width  = getWidth();
        int height = getHeight();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                processCell.accept(getCell(x,y));
            }
        }
    }


    public int getWidth() {
        return cells[0].length;
    }

    public int getHeight() {
        return cells.length;
    }

    private Cell getCell(int x, int y) {
        return cells[y][x];
    }

    private PlazaImg toImg(Function<Cell,Color> showFun) {

        log_noln("Converting cells to image ... ");

        int width  = getWidth();
        int height = getHeight();

        Color[][] pixels = new Color[height][width];

        for (int y = 0; y<height; y++) {
            Color[] pixelRow = new Color[width];
            for (int x = 0; x<width; x++) {
                pixelRow[x] = showFun.apply(getCell(x,y));
            }
            pixels[y] = pixelRow;
        }

        log("done");
        return new PlazaImg(pixels, pixelSizes, silent);
    }

    private PlazaImg toStateImg() {
        return toImg(cell -> stateToColor(cell.getState()));
    }

    //private PlazaImg toGradImg() {return toImg(Cell::getPRuleColor);}

    private PlazaImg toNumNeighbourImg() {
        return toImg(this::getNumNeighbourColor);
    }

    private Color getNumNeighbourColor(Cell cell) {
        int r = (int) Math.round(255.0 * cell.getNumNeighbours() / 8.0);
        return new Color(r,r,r);
    }


    String writeState_eva(String runDirName, int indivId, int frameIndex) {

        String dir = runDirName+"/frames";
        String filename = F.fillZeros(indivId, 5)+"_"+F.fillZeros(frameIndex,2)+".png";

        PlazaImg stateImg = toStateImg();
        stateImg.writeImage(dir, filename);

        return dir+"/"+filename;
    }


    void writeState() {
        PlazaImg stateImg = toStateImg();
        stateImg.writeImage(plazaDir+"/run", "state"+getStepXXX()+".png");
    }

    String writeState(int indivId) {
        PlazaImg stateImg = toStateImg();
        String locDir = "indivs";
        String filename = "indiv"+indivId+".png";
        stateImg.writeImage(plazaDir+"/"+locDir, filename);
        return locDir+"/"+filename;
    }


    private String getStepXXX() {
        return F.fillZeros(step, 3);
    }



    private void writeCellsImgs() {
        //toGradImg().writeImage(plazaDir+"/out/cells_grad.png");
        toNumNeighbourImg().writeImage(plazaDir+"/out","cells_sens.png");
    }

    private void checkSizes(PlazaImg seed, /*PlazaImg grad,*/ PlazaImg sens) {
        log_noln("Checking sizes ... ");
        //if (grad != null) {grad.checkSize(seed);}
        if (sens != null) {sens.checkSize(seed);}
        log("OK");
    }

    private void writeTests(PlazaImg seed, /*PlazaImg grad,*/ PlazaImg sens) {
        if (seed != null) {seed.writeImage(plazaDir+"/out","test_seed.png");}
        //if (grad != null) {grad.writeImage(plazaDir+"/out","test_grad.png");}
        if (sens != null) {sens.writeImage(plazaDir+"/out","test_sens.png");}
    }
}
