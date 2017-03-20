package cz.tomkren.fishtron.ugen.apps.cellplaza.v2;

import cz.tomkren.utils.AA;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**Created by tom on 12.03.2017.*/

class Cell {

    static final int DEAD = 0;
    private static int ALIVE(int numStates) {return numStates - 1;}

    private int state;
    private int nextState;
    private Cell[] neighbours;

    Cell(int state) {
        this.state = state;
        this.nextState = -1;
        this.neighbours = null;
    }

    public int getState() {return state;}
    void setState(int state) {this.state = state;}
    void setNeighbours(Cell[] neighbours) {this.neighbours = neighbours;}


    int getSumNeighbourState() {
        int sum = 0;
        for (Cell n : neighbours) {
            sum += n.state;
        }
        return sum;
    }

    void computeNextState(Rule rule) {
        nextState = rule.nextState(this);
    }

    void setStateToNextState() {
        state = nextState;
        nextState = -1;
    }

    Color getNumNeighbourColor() {
        int numNeighbours = neighbours.length;
        int r = (int) Math.round(255.0 * numNeighbours / 8.0);
        return new Color(r,r,r);
    }

    // todo nefektivní ale vadí mín než inverz imho .. dyštak udělat tabulku barev v CellWorld
    static int colorToState(Color seedColor, int x, int y, int numStates) {
        if (isGrayScale(seedColor)) {

            double b = numStates - 1.0;
            double a = (1.0 - numStates) / 255;
            int r = seedColor.getRed();
            return (int) Math.round(a * r + b);

        } else { // anything non-gray is chessboard
            return (x+y) % 2 == 0 ? ALIVE(numStates) : DEAD;
        }
    }


    // TODO nefektivní !! udělat tabulku barev v CellWorld
    static Color stateToColor(int state, int numStates) {
        double beta  = 255.0;
        double alpha = beta / (1.0 - numStates);
        int r = (int) Math.round(alpha * state + beta);
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
