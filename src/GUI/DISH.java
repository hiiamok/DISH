package GUI;

import is.ImageSearcher;

import java.awt.Image;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JButton;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.ToolRunner;

import common.ImageFeatures;
import common.Utilities;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

import java.awt.Font;

import javax.swing.SwingConstants;

import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.GridLayout;

/**
 * GUI for DISH
 * 
 * @author ok
 */
public class DISH {

	private JFrame frame;
	private File queryFile;
	private JLabel lblQueryImage;
	private JLabel lblNumberOfResults;
	private JComboBox<String> cBnumberOfResults;
	private JButton btnSearch;
	private JPanel panelImages;
	
	private Configuration config;	
	private int numberOfResults = 12;
	private String[] arguments;

	/**
	 * Constructor
	 */
	public DISH() {
		initialize();
		frame.setVisible(true);
	}
	
	/**
	 * Constructor
	 * @wbp.parser.constructor
	 */
	public DISH(Configuration conf, String[] args) {
		config = conf;
		arguments = args;
		initialize();
		try {
			showQueryImage(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		frame.setVisible(true);
	}

	/**
	 * Initialize GUI components
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 590, 500);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setResizable(false);
		
		lblQueryImage = new JLabel();
		lblQueryImage.setBounds(13, 225, 102, 108);
		frame.getContentPane().add(lblQueryImage);
		
		panelImages = new JPanel();
		panelImages.setBackground(Color.WHITE);
		panelImages.setBounds(139, 0, 450, 478);
		
		panelImages.setLayout(new GridLayout(0, 3, 0, 0));
		panelImages.setBorder(new EmptyBorder(0, 20, 0, 0));
		frame.getContentPane().add(panelImages);

		btnSearch = new JButton("Search");
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				clearImagePanel();

				try {
					ImageFeatures queryImage = Utilities.getImageFeatures(queryFile);
					
					config.set("QueryImage", queryImage.toString());
					config.setInt("NumberOfResults", numberOfResults);
					
					ToolRunner.run(config, new ImageSearcher(), arguments);
					
					drawImages();
					
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		btnSearch.setBounds(6, 425, 117, 29);
		frame.getContentPane().add(btnSearch);
		btnSearch.setVisible(false);
		
		cBnumberOfResults = new JComboBox<String>();
		cBnumberOfResults.setModel(new DefaultComboBoxModel<String>(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"}));
		cBnumberOfResults.setSelectedIndex(11);
		cBnumberOfResults.addActionListener (new ActionListener () {
		    public void actionPerformed(ActionEvent e) {
		    	numberOfResults = Integer.parseInt(cBnumberOfResults.getSelectedItem().toString());
		    }
		});
		cBnumberOfResults.setBounds(23, 370, 78, 27);
		frame.getContentPane().add(cBnumberOfResults);
		cBnumberOfResults.setVisible(false);
		
		lblNumberOfResults = new JLabel("Number of Results");
		lblNumberOfResults.setForeground(new Color(51, 51, 51));
		lblNumberOfResults.setHorizontalAlignment(SwingConstants.CENTER);
		lblNumberOfResults.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
		lblNumberOfResults.setBounds(6, 350, 111, 16);
		frame.getContentPane().add(lblNumberOfResults);
		lblNumberOfResults.setVisible(false);
		
		JButton btnChooseFile = new JButton("Choose File");
		btnChooseFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				JFileChooser fileChooser = new JFileChooser();
				ImagePreviewPanel preview = new ImagePreviewPanel();
				fileChooser.setAccessory(preview);
				fileChooser.addPropertyChangeListener(preview);
				fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
				int result = fileChooser.showOpenDialog(frame);
				if (result == JFileChooser.APPROVE_OPTION) {
					
					clearImagePanel();
					queryFile = fileChooser.getSelectedFile();
					
					try {
						showQueryImage(false);				
					}
					catch(Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		});
		btnChooseFile.setBounds(6, 188, 117, 29);
		frame.getContentPane().add(btnChooseFile);
		
		JLabel lblTitle1 = new JLabel("DI");
		lblTitle1.setForeground(new Color(51, 102, 153));
		lblTitle1.setFont(new Font("Verdana", Font.BOLD, 62));
		lblTitle1.setBounds(16, 6, 111, 63);
		frame.getContentPane().add(lblTitle1);
		
		JLabel lblTitle2 = new JLabel("SH");
		lblTitle2.setForeground(new Color(51, 51, 51));
		lblTitle2.setFont(new Font("Verdana", Font.BOLD, 55));
		lblTitle2.setBounds(17, 43, 92, 74);
		frame.getContentPane().add(lblTitle2);
		
		JLabel lblDesc = new JLabel("<html>Distributed<br>Image Search<br>with Hadoop</html>");
		lblDesc.setForeground(new Color(51, 51, 51));
		lblDesc.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
		lblDesc.setBounds(20, 115, 92, 63);
		frame.getContentPane().add(lblDesc);
	}
	
	/**
	 * Show query image in GUI
	 */
	private void showQueryImage(boolean getFromConfig) throws IOException {
		
		if(getFromConfig) {
			File f = new File(config.get("QueryImage"));
			if(f.exists())
				queryFile = f;
		}

		if(queryFile != null && queryFile.exists()) {
			BufferedImage bimg = ImageIO.read(queryFile);
	    	Image simg = bimg.getScaledInstance(lblQueryImage.getWidth(), lblQueryImage.getHeight(), Image.SCALE_SMOOTH);

	    	ImageIcon ii = new ImageIcon(simg);
	    	lblQueryImage.setIcon(ii);
	    	
	    	lblNumberOfResults.setVisible(true);
	    	cBnumberOfResults.setVisible(true);
	    	btnSearch.setVisible(true);		
		}	
	}
	
	/**
	 * Empty the image panel
	 */
	private void clearImagePanel() {
		panelImages.removeAll();
		panelImages.revalidate();
		panelImages.repaint();
		
		frame.revalidate();
		frame.repaint();
	}
	
	/**
	 * Draw Images found by MapReduce
	 */
	private synchronized void drawImages() throws Exception {
		LinkedHashMap<String, Double> map = Utilities.getResults(config);
		FileSystem dfs = FileSystem.get(config);
		
		for (Entry<String, Double> result : map.entrySet()) {
			
			FSDataInputStream in = dfs.open(new Path(result.getKey()));
			BufferedImage bimg = ImageIO.read(in);
			Image simg = bimg.getScaledInstance(130, 110, Image.SCALE_SMOOTH);
			ImageIcon ii = new ImageIcon(simg);
		
			JLabel lbl = new JLabel();
			lbl.setIcon(ii);

			panelImages.add(lbl);
			
			panelImages.revalidate();
		}
	    
		panelImages.revalidate();
		panelImages.repaint();
	}

}