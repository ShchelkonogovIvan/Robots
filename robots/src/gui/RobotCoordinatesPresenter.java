package gui;

import java.util.Observable;
import java.util.Observer;

import javax.swing.SwingUtilities;

import log.Logger;

public class RobotCoordinatesPresenter implements Observer
{
    private static final int LOG_EVERY_UPDATE = 50;

    private final RobotModel model;
    private final RobotCoordinatesWindow view;
    private int updateCount;

    public RobotCoordinatesPresenter(RobotModel model, RobotCoordinatesWindow view)
    {
        this.model = model;
        this.view = view;
        model.addObserver(this);
        updateView();
    }

    @Override
    public void update(Observable observable, Object argument)
    {
        SwingUtilities.invokeLater(this::updateView);
        updateCount++;
        if (updateCount >= LOG_EVERY_UPDATE)
        {
            updateCount = 0;
            Logger.debug(String.format("Координаты робота: x=%.2f, y=%.2f, direction=%.3f",
                    model.getRobotPositionX(), model.getRobotPositionY(), model.getRobotDirection()));
        }
    }

    private void updateView()
    {
        view.setCoordinates(model.getRobotPositionX(), model.getRobotPositionY(), model.getRobotDirection());
    }
}
