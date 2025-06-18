package org.eris.utils.render.animation;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class Animation {
    private final AnimTimeUtil timerUtil = new AnimTimeUtil();
    private int duration;
    private double endPoint;
    private Direction direction;

    public Animation(int ms, double endPoint) {
        this(ms, endPoint, Direction.FORWARDS);
    }

    public Animation(int ms, double endPoint, Direction direction) {
        this.duration = ms;
        this.endPoint = endPoint;
        this.direction = direction;
    }

    public boolean finished(Direction direction) {
        return this.isDone() && this.direction.equals(direction);
    }

    public double getLinearOutput() {
        return 1.0 - (double) this.timerUtil.getTime() / (double) this.duration * this.endPoint;
    }

    public void reset() {
        this.timerUtil.reset();
    }

    public boolean isDone() {
        return this.timerUtil.hasTimeElapsed(this.duration);
    }

    public void changeDirection() {
        this.setDirection(this.direction.opposite());
    }

    public boolean isForward() {
        return this.direction.forwards();
    }

    public Animation setDirection(Direction direction) {
        if (this.direction != direction) {
            this.direction = direction;
            this.timerUtil.setTime(System.currentTimeMillis() - ((long) this.duration - Math.min(this.duration, this.timerUtil.getTime())));
        }
        return this;
    }

    protected boolean correctOutput() {
        return false;
    }

    public double getOutput() {
        if (this.direction.forwards()) {
            if (this.isDone()) {
                return this.endPoint;
            }
            return this.getEquation((double) this.timerUtil.getTime() / (double) this.duration) * this.endPoint;
        }
        if (this.isDone()) {
            return 0.0;
        }
        if (this.correctOutput()) {
            double revTime = Math.min(this.duration, Math.max(0L, (long) this.duration - this.timerUtil.getTime()));
            return this.getEquation(revTime / (double) this.duration) * this.endPoint;
        }
        return (1.0 - this.getEquation((double) this.timerUtil.getTime() / (double) this.duration)) * this.endPoint;
    }

    protected abstract double getEquation(double var1);
}
