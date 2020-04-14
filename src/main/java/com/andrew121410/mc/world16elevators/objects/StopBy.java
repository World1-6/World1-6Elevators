package com.andrew121410.mc.world16elevators.objects;

import java.util.Collections;
import java.util.PriorityQueue;
import java.util.Queue;

public class StopBy {

    private PriorityQueue<Integer> stopByQueue;
    private boolean goUp;

    public StopBy() {
        this.stopByQueue = new PriorityQueue<>();
    }

    //GETTERS AND SETTERS
    public void setGoUp(boolean goUp) {
        if (goUp) this.stopByQueue = new PriorityQueue<>();
        else this.stopByQueue = new PriorityQueue<>(Collections.reverseOrder());
        this.goUp = goUp;
    }

    public boolean isGoUp() {
        return goUp;
    }

    public ElevatorStatus toElevatorStatus() {
        ElevatorStatus elevatorStatus = ElevatorStatus.DONT_KNOW;
        return elevatorStatus.upOrDown(goUp);
    }

    public Queue<Integer> getStopByQueue() {
        return stopByQueue;
    }
}