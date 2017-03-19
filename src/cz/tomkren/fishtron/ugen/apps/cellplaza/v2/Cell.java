package cz.tomkren.fishtron.ugen.apps.cellplaza.v2;

import cz.tomkren.utils.AA;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**Created by tom on 12.03.2017.*/

class Cell {

    static final int DEAD = 0;
    static final int ALIVE = 1;

    static final Color DEAD_COLOR = Color.white;
    private static final Color ALIVE_COLOR = Color.black;
    private static final Color CHESSBOARD_COLOR = Color.gray;

    private int state;
    private int nextState;
    private Cell[] neighbours;

    Cell(int state) {
        this.state = state;
        this.nextState = -1;
        this.neighbours = null;
    }

    void setState(int state) {this.state = state;}
    void setNeighbours(Cell[] neighbours) {this.neighbours = neighbours;}


    int getNumAliveNeighbours() {
        int numAlive = 0;
        for (Cell n : neighbours) {
            if (n.isAlive()) {numAlive ++;}
        }
        return numAlive;
    }

    boolean isAlive() {
        return state == ALIVE;
    }

    void computeNextState(Rule rule, Random rand) {
        nextState = rule.nextState(this);
    }

    void setStateToNextState() {
        state = nextState;
        nextState = -1;
    }

    Color getStateColor() {
        return stateToColor(state);
    }

    Color getNumNeighbourColor() {
        int numNeighbours = neighbours.length;
        int r = (int) Math.round(255.0 * numNeighbours / 8.0);
        return new Color(r,r,r);
    }

    static int colorToState(Color seedColor, int x, int y) {
        if (seedColor.equals(ALIVE_COLOR)) {
            return ALIVE;
        } else if (seedColor.equals(DEAD_COLOR)) {
            return DEAD;
        } else if (seedColor.equals(CHESSBOARD_COLOR)) {
            return (x+y) % 2 == 0 ? ALIVE : DEAD;
        } else {
            return DEAD;
            //throw new Error("Unsupported seed color "+seedColor.toString()+" on pos [x="+x+", y="+y+"].");
        }
    }

    private static Color stateToColor(int state) {
        switch (state) {
            case DEAD: return DEAD_COLOR;
            case ALIVE: return ALIVE_COLOR;
            default: throw new Error("State without a color: "+state);
        }
    }

    static double colorToPRule(Color gradColor, int x, int y) {
        if (!isGrayScale(gradColor)) {
            throw new Error("GradImage not in gray scale: "+gradColor.toString()+" on pos [x="+x+", y="+y+"].");
        }
        int r = gradColor.getRed();
        if (r > 100) {r = 100;}
        return r / 100.0;
    }

    private static Color pRuleToColor(double pRule) {
        int r = (int) Math.round(pRule*255.0);
        if (r > 255) {
            throw new Error("r = "+r+", pRule = "+pRule);
        }
        return new Color(r,r,r);
    }


    private static boolean isGrayScale(Color color) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();

        return r == g && r == b;
    }

    static List<AA<Integer>> colorToSensorDiffs(Color sensorColor, int x, int y, int x_max, int y_max) {
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


}
