package net.fishtron.apps.cellplaza.v2;

import java.util.Arrays;

/**Created by tom on 12.03.2017.*/

class Cell {

    static final int DEAD_STATE = 0;
    static int ALIVE_STATE(int numStates) {return numStates - 1;}

    private int state;
    private int nextState;
    private Cell[] neighbours;

    Cell(int state) {
        this.state = state;
        this.neighbours = null;
    }

    void setNeighbours(Cell[] neighbours) {
        this.neighbours = neighbours;
    }

    public int getState() {return state;}
    void setState(int state) {this.state = state;}

    int getNeighbourStateSum() {
        return Arrays.stream(neighbours).mapToInt(n -> n.state).sum();
    }

    void computeNextState(Rule rule) {
        nextState = rule.nextState(this);
    }

    void setStateToNextState() {
        state = nextState;
    }

    int getNumNeighbours() {
        return neighbours.length;
    }


}
