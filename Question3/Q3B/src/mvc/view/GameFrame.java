package mvc.view;

import java.awt.*;
import java.awt.event.WindowEvent;
import javax.swing.*;

public class GameFrame extends JFrame {

  private JPanel contentPane;
  private BorderLayout borderLayout1 = new BorderLayout();

  public GameFrame() {
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    try {
      initialize();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void initialize() throws Exception {
    contentPane = (JPanel) this.getContentPane();
    contentPane.setLayout(borderLayout1);
  }

  @Override
  protected void processWindowEvent(WindowEvent e) {
    super.processWindowEvent(e);
    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
      System.exit(0);
    }
  }
}
