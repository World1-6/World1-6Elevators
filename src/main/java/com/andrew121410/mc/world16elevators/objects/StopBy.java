package com.andrew121410.mc.world16elevators.objects;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Collections;
import java.util.PriorityQueue;
import java.util.Queue;

@EqualsAndHashCode
@ToString
public class StopBy {

    private PriorityQueue<Integer> stopByQueue;
    private boolean goUp;

    public StopBy() {
        this.stopByQueue = new PriorityQueue<>();
    }

    public void setGoUp(boolean goUp) {
        if (goUp) this.stopByQueue = new PriorityQueue<>();
        else this.stopByQueue = new PriorityQueue<>(Collections.reverseOrder());
        this.goUp = goUp;
    }

    public boolean isGoUp() {
        return goUp;
    }

    public ElevatorStatus toElevatorStatus() {
        return ElevatorStatus.upOrDown(goUp);
    }

    public Queue<Integer> getStopByQueue() {
        return stopByQueue;
    }
}