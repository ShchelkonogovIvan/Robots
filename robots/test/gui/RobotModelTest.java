package gui;

import java.awt.Point;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class RobotModelTest
{
    public static void main(String[] args) throws Exception
    {
        testSignedAngleNormalization();
        testRobotTurnsThroughZeroForward();
        testRobotTurnsThroughZeroBackward();
        testTwoObserversReceiveUpdates();
        testCoordinatesPresenterUpdatesWindow();
        System.out.println("RobotModelTest: OK");
    }

    private static void testSignedAngleNormalization() throws Exception
    {
        assertClose(0.08, normalizeSignedAngle(-6.20), 0.01,
                "Угол около границы 2pi/0 должен стать маленьким положительным углом");
        assertClose(-0.08, normalizeSignedAngle(6.20), 0.01,
                "Угол около границы 0/2pi должен стать маленьким отрицательным углом");
    }

    private static void testRobotTurnsThroughZeroForward() throws Exception
    {
        RobotModel model = new RobotModel();
        setDoubleField(model, "robotDirection", 6.25);
        model.setTargetPosition(new Point(200, 105));
        model.update(10);

        assertTrue(model.getRobotDirection() > 6.25,
                "Робот должен повернуть вперед через границу 2pi/0, а не назад");
    }

    private static void testRobotTurnsThroughZeroBackward() throws Exception
    {
        RobotModel model = new RobotModel();
        setDoubleField(model, "robotDirection", 0.05);
        model.setTargetPosition(new Point(200, 97));
        model.update(10);

        assertTrue(model.getRobotDirection() < 0.05,
                "Робот должен повернуть назад через границу 0/2pi, а не вперед");
    }

    private static void testTwoObserversReceiveUpdates()
    {
        RobotModel model = new RobotModel();
        AtomicInteger firstObserverUpdates = new AtomicInteger();
        AtomicInteger secondObserverUpdates = new AtomicInteger();

        model.addObserver((observable, argument) -> firstObserverUpdates.incrementAndGet());
        model.addObserver((observable, argument) -> secondObserverUpdates.incrementAndGet());

        model.setTargetPosition(new Point(170, 140));

        assertEquals(1, firstObserverUpdates.get(), "Первый наблюдатель должен получить уведомление");
        assertEquals(1, secondObserverUpdates.get(), "Второй наблюдатель должен получить уведомление");
    }

    private static void testCoordinatesPresenterUpdatesWindow() throws Exception
    {
        RobotModel model = new RobotModel();
        RobotCoordinatesWindow window = new RobotCoordinatesWindow();
        new RobotCoordinatesPresenter(model, window);

        setDoubleField(model, "robotPositionX", 123.456);
        setDoubleField(model, "robotPositionY", 78.9);
        setDoubleField(model, "robotDirection", 1.2345);
        model.setTargetPosition(new Point(160, 160));
        SwingUtilities.invokeAndWait(() -> {
        });

        JLabel xValue = getValueLabel(window, 1);
        JLabel yValue = getValueLabel(window, 3);
        JLabel directionValue = getValueLabel(window, 5);

        assertEquals(String.format("%.2f", 123.456), xValue.getText(), "Окно координат должно показать X");
        assertEquals(String.format("%.2f", 78.9), yValue.getText(), "Окно координат должно показать Y");
        assertEquals(String.format("%.3f", 1.2345), directionValue.getText(),
                "Окно координат должно показать направление");
    }

    private static double normalizeSignedAngle(double angle) throws Exception
    {
        Method method = RobotModel.class.getDeclaredMethod("normalizeSignedAngle", double.class);
        method.setAccessible(true);
        return (Double)method.invoke(null, angle);
    }

    private static void setDoubleField(RobotModel model, String name, double value) throws Exception
    {
        Field field = RobotModel.class.getDeclaredField(name);
        field.setAccessible(true);
        field.setDouble(model, value);
    }

    private static JLabel getValueLabel(RobotCoordinatesWindow window, int index)
    {
        JPanel panel = (JPanel)window.getContentPane().getComponent(0);
        return (JLabel)panel.getComponent(index);
    }

    private static void assertClose(double expected, double actual, double maxDifference, String message)
    {
        if (Math.abs(expected - actual) > maxDifference)
        {
            throw new AssertionError(message + ": expected " + expected + ", actual " + actual);
        }
    }

    private static void assertEquals(int expected, int actual, String message)
    {
        if (expected != actual)
        {
            throw new AssertionError(message + ": expected " + expected + ", actual " + actual);
        }
    }

    private static void assertEquals(String expected, String actual, String message)
    {
        if (!expected.equals(actual))
        {
            throw new AssertionError(message + ": expected " + expected + ", actual " + actual);
        }
    }

    private static void assertTrue(boolean condition, String message)
    {
        if (!condition)
        {
            throw new AssertionError(message);
        }
    }
}
