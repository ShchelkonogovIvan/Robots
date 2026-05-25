package gui;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Timer;
import java.util.TimerTask;

import log.Logger;

public class RobotController extends MouseAdapter
{
    private static final int MODEL_UPDATE_PERIOD = 10;
    private static final double MODEL_UPDATE_DURATION = 10;

    private final RobotModel model;
    private final Timer timer = new Timer("robot controller", true);

    public RobotController(RobotModel model)
    {
        this.model = model;
    }

    public void start()
    {
        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                model.update(MODEL_UPDATE_DURATION);
            }
        }, 0, MODEL_UPDATE_PERIOD);
    }

    @Override
    public void mouseClicked(MouseEvent event)
    {
        Point target = event.getPoint();
        model.setTargetPosition(target);
        Logger.debug("Новая цель: x=" + target.x + ", y=" + target.y);
    }
}
