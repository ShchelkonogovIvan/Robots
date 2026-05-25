package gui;

import java.awt.GridLayout;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class RobotCoordinatesWindow extends JInternalFrame
{
    private final JLabel xValue = new JLabel();
    private final JLabel yValue = new JLabel();
    private final JLabel directionValue = new JLabel();

    public RobotCoordinatesWindow()
    {
        super("Координаты робота", true, true, true, true);

        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.add(new JLabel("X:"));
        panel.add(xValue);
        panel.add(new JLabel("Y:"));
        panel.add(yValue);
        panel.add(new JLabel("Направление:"));
        panel.add(directionValue);
        getContentPane().add(panel);

        setCoordinates(0, 0, 0);
        pack();
    }

    public void setCoordinates(double x, double y, double direction)
    {
        xValue.setText(String.format("%.2f", x));
        yValue.setText(String.format("%.2f", y));
        directionValue.setText(String.format("%.3f", direction));
    }
}
