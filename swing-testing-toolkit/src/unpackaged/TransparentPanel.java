import javax.swing.*;

import com.sun.awt.AWTUtilities;

import java.awt.*;
class TransparentPanel extends JFrame
{
/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
JPanel p1,p2;

    public TransparentPanel()
    {
        createAndShowGUI();
    }

    private void createAndShowGUI()
    {
        // Set title and default close operation
        setTitle("Transparent Panel");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Set a background for JFrame
        setContentPane(new JLabel(new ImageIcon("C:\\image.jpg")));

        // Set some layout, say FlowLayout
        setLayout(new FlowLayout());


        // Create a JPanel
        p1=new JPanel();

        // Set the background, black with 125 as alpha value
        // This is less transparent
        p1.setBackground(new Color(0,0,0,100));

        // Create another JPanel
        p2=new JPanel();

        // This is more transparent than the previous
        // one
        p2.setBackground(new Color(0,0,0,50));

        // Set some size to the panels
        p1.setPreferredSize(new Dimension(250,150));
        p2.setPreferredSize(new Dimension(250,150));

        // Add the panels to the JFrame
        add(p1);
        add(p2);

        // Set the size of the JFrame and
        // make it visible
        setSize(600,400);
    	AWTUtilities.setWindowOpacity(this, 0.5f);
        setVisible(true);
    }


    public static void main(String args[])
    {
        // Run in the EDT
        SwingUtilities.invokeLater(new Runnable(){
            public void run()
            {
                new TransparentPanel();
            }
        });
    }
}