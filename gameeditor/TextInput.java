
package gameeditor;

/* TextDemo.java requires no other files. */
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import gameconcepts.Item;
import knowledgerep.KnowledgeBase;
 
public class TextInput extends JPanel implements ActionListener {
    protected JTextField textField;
    protected static JFrame frame;
 
    public TextInput(Item item, KnowledgeBase kb) {
        super(new GridBagLayout());
        
        Action action = new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
            	String text = textField.getText();
            	String[] props = text.replaceAll(" ", "").split(",");
            	java.util.List<String> propsList = Arrays.asList(props);
            	kb.removeProperties(item.properties, item.name);
            	item.setProperties(propsList);
            	kb.addProperties(item.properties, item.name);
            	frame.dispose();
            	item.printDetails();
            }
        };

        String props = String.join(",", item.properties);
        textField = new JTextField(props, 20);
        textField.addActionListener(action);
    }
 
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI(Item item, KnowledgeBase kb) {
        //Create and set up the window.
        frame = new JFrame("Properties");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        //Add contents to the window.
        frame.add(new TextInput(item, kb).textField);
 
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
    
    public static void launchPropertyWindow(Item item, KnowledgeBase kb) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(item, kb);
            }
        });
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		frame.dispose();
	}
}
