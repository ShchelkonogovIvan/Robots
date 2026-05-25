package gui;

import java.awt.Point;
import java.util.Observable;

public class RobotModel extends Observable
{
    private static final double MAX_VELOCITY = 0.1;
    private static final double MAX_ANGULAR_VELOCITY = 0.001;
    private static final double TARGET_EPSILON = 0.5;

    private double robotPositionX = 100;
    private double robotPositionY = 100;
    private double robotDirection = 0;

    private int targetPositionX = 150;
    private int targetPositionY = 100;

    public synchronized double getRobotPositionX()
    {
        return robotPositionX;
    }

    public synchronized double getRobotPositionY()
    {
        return robotPositionY;
    }

    public synchronized double getRobotDirection()
    {
        return robotDirection;
    }

    public synchronized int getTargetPositionX()
    {
        return targetPositionX;
    }

    public synchronized int getTargetPositionY()
    {
        return targetPositionY;
    }

    public synchronized void setTargetPosition(Point point)
    {
        targetPositionX = point.x;
        targetPositionY = point.y;
        notifyModelChanged();
    }

    public synchronized void update(double duration)
    {
        double distance = distance(targetPositionX, targetPositionY, robotPositionX, robotPositionY);
        if (distance < TARGET_EPSILON)
        {
            return;
        }

        double velocity = MAX_VELOCITY;
        double angleToTarget = angleTo(robotPositionX, robotPositionY, targetPositionX, targetPositionY);
        double angleDifference = normalizeSignedAngle(angleToTarget - robotDirection);
        double angularVelocity = 0;
        if (angleDifference > 0)
        {
            angularVelocity = MAX_ANGULAR_VELOCITY;
        }
        if (angleDifference < 0)
        {
            angularVelocity = -MAX_ANGULAR_VELOCITY;
        }

        moveRobot(velocity, angularVelocity, duration);
        notifyModelChanged();
    }

    private void moveRobot(double velocity, double angularVelocity, double duration)
    {
        velocity = applyLimits(velocity, 0, MAX_VELOCITY);
        angularVelocity = applyLimits(angularVelocity, -MAX_ANGULAR_VELOCITY, MAX_ANGULAR_VELOCITY);
        double newX = robotPositionX + velocity / angularVelocity *
            (Math.sin(robotDirection + angularVelocity * duration) - Math.sin(robotDirection));
        if (!Double.isFinite(newX))
        {
            newX = robotPositionX + velocity * duration * Math.cos(robotDirection);
        }
        double newY = robotPositionY - velocity / angularVelocity *
            (Math.cos(robotDirection + angularVelocity * duration) - Math.cos(robotDirection));
        if (!Double.isFinite(newY))
        {
            newY = robotPositionY + velocity * duration * Math.sin(robotDirection);
        }

        robotPositionX = newX;
        robotPositionY = newY;
        robotDirection = asNormalizedRadians(robotDirection + angularVelocity * duration);
    }

    private void notifyModelChanged()
    {
        setChanged();
        notifyObservers();
    }

    private static double distance(double x1, double y1, double x2, double y2)
    {
        double diffX = x1 - x2;
        double diffY = y1 - y2;
        return Math.sqrt(diffX * diffX + diffY * diffY);
    }

    private static double angleTo(double fromX, double fromY, double toX, double toY)
    {
        double diffX = toX - fromX;
        double diffY = toY - fromY;

        return asNormalizedRadians(Math.atan2(diffY, diffX));
    }

    private static double applyLimits(double value, double min, double max)
    {
        if (value < min)
            return min;
        if (value > max)
            return max;
        return value;
    }

    private static double normalizeSignedAngle(double angle)
    {
        while (angle > Math.PI)
        {
            angle -= 2 * Math.PI;
        }
        while (angle < -Math.PI)
        {
            angle += 2 * Math.PI;
        }
        return angle;
    }

    private static double asNormalizedRadians(double angle)
    {
        while (angle < 0)
        {
            angle += 2 * Math.PI;
        }
        while (angle >= 2 * Math.PI)
        {
            angle -= 2 * Math.PI;
        }
        return angle;
    }
}
