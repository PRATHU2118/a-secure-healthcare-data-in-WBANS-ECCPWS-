import javax.swing.*;
import java.awt.*;
public class UIUtils {
    public static JButton createBackButton(JFrame currentFrame) {
        JButton btn = new JButton("← Back");
        btn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        btn.addActionListener(e -> {
            currentFrame.dispose();
            new MainLauncher().setVisible(true);
        });
        return btn;
    }
}
