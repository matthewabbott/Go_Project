package src;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class GoOptionMenu extends JFrame implements ActionListener {

	private JPanel optionMenu;
	
	private boolean startSelected = false;
	
	private boolean allOptionsSelected = false;
	
	private boolean koOptionSelected = false;
	private boolean disadvantageBonusSelected = false;

	private boolean usingKo;
	private int whiteDisadvantageBonus;
	
	private JTextField whiteDisadvantage;
	private JTextField numLines;

	public GoOptionMenu() {

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(50, 50, 800, 800);
		optionMenu = new JPanel();
		optionMenu.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(optionMenu);
		optionMenu.setLayout(null);

		addKoOptions();
		
		addDisadvantageOptions();
		
		JButton start = new JButton("Start Game");
		start.setActionCommand("start");
		start.addActionListener(this);
		start.setBounds(270, 200, 200, 60);
		optionMenu.add(start);
	}
	
	private void addKoOptions() {

		JLabel koLabel = new JLabel("Are you using Ko or SuperKo?:");
		koLabel.setBounds(30, 20, 172, 25);
		optionMenu.add(koLabel);
		
		JButton ko = new JButton("Use Ko");
		ko.setActionCommand("ko");
		ko.addActionListener(this);
		ko.setBounds(212, 20, 110, 25);
		optionMenu.add(ko);

		JButton superKo = new JButton("Use SuperKo");
		superKo.setActionCommand("superKo");
		superKo.addActionListener(this);
		superKo.setBounds(332, 20, 110, 25);
		optionMenu.add(superKo);

		JButton koInquiry = new JButton("What are Ko and SuperKo?");
		koInquiry.setActionCommand("koInquiry");
		koInquiry.addActionListener(this);
		koInquiry.setBounds(452, 20, 200, 25);
		optionMenu.add(koInquiry);
		
	}
	
	private void addDisadvantageOptions() {
		
		JLabel whiteDisadvantagePrompt = new JLabel("How many extra points does the white player get?:");
		whiteDisadvantagePrompt.setBounds(30, 65, 300, 25);
		optionMenu.add(whiteDisadvantagePrompt);
		
		whiteDisadvantage = new JTextField(2);
		whiteDisadvantage.setActionCommand("wDisadvantage");
		whiteDisadvantage.addActionListener(this);
		whiteDisadvantage.setBounds(320, 65, 20, 25);
		whiteDisadvantage.setText("0");
		optionMenu.add(whiteDisadvantage);
		
		JButton disadvantageBonusInquiry = new JButton("Why would White get extra points?");
		disadvantageBonusInquiry.setActionCommand("disadvantageInquiry");
		disadvantageBonusInquiry.addActionListener(this);
		disadvantageBonusInquiry.setBounds(350, 65, 250, 25);
		optionMenu.add(disadvantageBonusInquiry);
		
	}

	public boolean isCompleted() {
		if (startSelected) {
			if (!koOptionSelected) {
				JOptionPane
				.showMessageDialog(
						this,
						"Make sure you choose between Ko or SuperKo",
						"Make All Selections", JOptionPane.PLAIN_MESSAGE);
				startSelected = false;
				return false;
			}
			
			
			return true;
		}
		return false;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("ko")) {
			koOptionSelected = true;
			usingKo = true;
			
		} else if (e.getActionCommand().equals("superKo")) {
			koOptionSelected = true;
			usingKo = false;
			
		} else if (e.getActionCommand().equals("koInquiry")) {
			explainKo();
			
		} else if (e.getActionCommand().equals("wDisadvantage")){
			disadvantageBonusSelected = true;
			try {
				whiteDisadvantageBonus = Integer.parseInt(whiteDisadvantage.getText());
				
			} catch (NullPointerException playerInputInvalid) {
				whiteDisadvantageBonus = 0;
				nonIntegerInputMessage();
				
			} catch (NumberFormatException playerInputInvalid) {
				whiteDisadvantageBonus = 0;
				nonIntegerInputMessage();
			}

		} else if (e.getActionCommand().equals("disadvantageInquiry")){
			explainDisadvantage();
			
		} else if (e.getActionCommand().equals("start")) {
			startSelected = true;
		}

	}

	private void explainKo() {
		JOptionPane
				.showMessageDialog(
						this,
						"Under Ko, you cannot make a move that would cause the board "
								+ "\nto be layed out the same way it was after your previous move."
								+ " \nUnder Superko, no previous board layout can ever be repeated.",
						"Rules", JOptionPane.PLAIN_MESSAGE);
	}
	
	private void explainDisadvantage() {
		JOptionPane
				.showMessageDialog(
						this,
						"Because White goes second, they are inherently at a disadvantage "
								+ "\nA good handicap for white is a number from 5 to 7"
								+ " \nBe sure to press enter after choosing a value",
						"Rules", JOptionPane.PLAIN_MESSAGE);
	}
	
	private void nonIntegerInputMessage() {
		JOptionPane
		.showMessageDialog(
				this,
				"Please input integer values for this field",
				"Error", JOptionPane.PLAIN_MESSAGE);
	}

	public boolean getUsingKo() {
		return usingKo;
	}
	
	public int getWhiteDisadvantageBonus() {
		return whiteDisadvantageBonus;
	}

}
