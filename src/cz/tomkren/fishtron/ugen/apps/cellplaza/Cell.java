package cz.tomkren.fishtron.ugen.apps.cellplaza;

import cz.tomkren.utils.AA;
import cz.tomkren.utils.Log;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**Created by tom on 12.03.2017.*/

class Cell {

    public enum State {DEAD, ALIVE}

    static final Color DEAD_COLOR = Color.white;
    private static final Color ALIVE_COLOR = Color.black;
    private static final Color CHESSBOARD_COLOR = Color.gray;

    private State state;
    private State nextState;
    private double pRule;
    private Cell[] neighbours;

    Cell(State state, double pRule) {
        this.state = state;
        this.pRule = /*pRule*/ Math.pow(pRule, 16.0);
        this.nextState = null;
        this.neighbours = null;
    }

    void setState(State state) {
        this.state = state;
    }

    void setPRule(double pRule) {
        this.pRule = pRule;
    }

    void setNeighbours(Cell[] neighbours) {
        this.neighbours = neighbours;
    }

    int getNumAliveNeighbours() {
        int numAlive = 0;
        for (Cell n : neighbours) {
            if (n.isAlive()) {numAlive ++;}
        }
        return numAlive;
    }

    boolean isAlive() {
        return state == State.ALIVE;
    }

    void computeNextState(Rule rule, Random rand) {

        if (pRule == 1.0 || rand.nextDouble() < pRule) {
            nextState = rule.nextState(this);
        } else {
            nextState = state;
        }
    }

    void setStateToNextState() {
        state = nextState;
        nextState = null;
    }

    Color getStateColor() {
        return stateToColor(state);
    }

    Color getPRuleColor() {
        return pRuleToColor(pRule);
    }

    Color getNumNeighbourColor() {
        int numNeighbours = neighbours.length;
        int r = (int) Math.round(255.0 * numNeighbours / 8.0);
        return new Color(r,r,r);
    }

    static State colorToState(Color seedColor, int x, int y) {
        if (seedColor.equals(ALIVE_COLOR)) {
            return Cell.State.ALIVE;
        } else if (seedColor.equals(DEAD_COLOR)) {
            return Cell.State.DEAD;
        } else if (seedColor.equals(CHESSBOARD_COLOR)) {
            return (x+y) % 2 == 0 ? Cell.State.ALIVE : Cell.State.DEAD;
        } else {
            return Cell.State.DEAD;
            //throw new Error("Unsupported seed color "+seedColor.toString()+" on pos [x="+x+", y="+y+"].");
        }
    }

    private static Color stateToColor(Cell.State state) {
        switch (state) {
            case ALIVE: return ALIVE_COLOR;
            case DEAD: return DEAD_COLOR;
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

        boolean isTop    = y == 0;
        boolean isBottom = y == y_max;
        boolean isLeft   = x == 0;
        boolean isRight  = x == x_max;

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
                throw new Error("Unsupported sensor color: " + sensorColor.toString() + " on pos [x=" + x + ", y=" + y + "].");
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
