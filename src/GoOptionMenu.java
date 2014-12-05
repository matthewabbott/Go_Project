package src;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class GoOptionMenu extends JFrame implements ActionListener {

	private JPanel optionMenu;

	private boolean startSelected = false;

	private boolean koOptionSelected = false;
	private boolean disadvantageBonusSelected = false;
	private boolean boardSizeSelected = false;

	private boolean usingKo;
	private int whiteDisadvantageBonus;
	private int numLines;

	private JTextField whiteDisadvantage;
	private JTextField boardSize;

	public GoOptionMenu() {

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(50, 50, 800, 800);
		optionMenu = new JPanel();
		optionMenu.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(optionMenu);
		optionMenu.setLayout(null);

		addKoOptions();

		addDisadvantageOptions();

		addBoardSizeOptions();

		addDefaultOptions();

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

		JLabel whiteDisadvantagePrompt = new JLabel(
				"How many extra points does the white player get?:");
		whiteDisadvantagePrompt.setBounds(30, 65, 300, 25);
		optionMenu.add(whiteDisadvantagePrompt);

		whiteDisadvantage = new JTextField(2);
		whiteDisadvantage.setActionCommand("wDisadvantage");
		whiteDisadvantage.addActionListener(this);
		whiteDisadvantage.setBounds(320, 65, 20, 25);
		whiteDisadvantage.setText("5");
		optionMenu.add(whiteDisadvantage);

		JButton disadvantageBonusInquiry = new JButton(
				"Why would White get extra points?");
		disadvantageBonusInquiry.setActionCommand("disadvantageInquiry");
		disadvantageBonusInquiry.addActionListener(this);
		disadvantageBonusInquiry.setBounds(350, 65, 250, 25);
		optionMenu.add(disadvantageBonusInquiry);

	}

	private void addBoardSizeOptions() {

		JLabel boardSizePrompt = new JLabel("How large is the board?:");
		boardSizePrompt.setBounds(30, 100, 140, 25);
		optionMenu.add(boardSizePrompt);

		boardSize = new JTextField(2);
		boardSize.setActionCommand("boardSize");
		boardSize.addActionListener(this);
		boardSize.setBounds(180, 100, 20, 25);
		boardSize.setText("19");
		optionMenu.add(boardSize);

	}

	private void addDefaultOptions() {

		JButton useDefaults = new JButton("Use Default Options");
		useDefaults.setActionCommand("default");
		useDefaults.addActionListener(this);
		useDefaults.setBounds(30, 135, 180, 30);
		optionMenu.add(useDefaults);

		JButton defaultInquiry = new JButton("What are the Default Options?");
		defaultInquiry.setActionCommand("defaultInquiry");
		defaultInquiry.addActionListener(this);
		defaultInquiry.setBounds(220, 135, 220, 30);
		optionMenu.add(defaultInquiry);
	}

	public boolean isCompleted() {
		if (startSelected) {
			if (!koOptionSelected) {

				JOptionPane.showMessageDialog(this,
						"Make sure you choose between Ko or SuperKo",
						"Make All Selections", JOptionPane.PLAIN_MESSAGE);
				startSelected = false;
				return false;

			} else if (!disadvantageBonusSelected) {

				JOptionPane
						.showMessageDialog(
								this,
								"Press Enter in the white bonus points field to choose the value typed there",
								"Make All Selections",
								JOptionPane.PLAIN_MESSAGE);
				startSelected = false;
				return false;
				

			} else if (!boardSizeSelected) {

				JOptionPane
						.showMessageDialog(
								this,
								"Press Enter in the board size field to choose the value typed there",
								"Make All Selections",
								JOptionPane.PLAIN_MESSAGE);
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

		} else if (e.getActionCommand().equals("wDisadvantage")) {
			disadvantageBonusSelected = true;
			try {
				whiteDisadvantageBonus = Integer.parseInt(whiteDisadvantage
						.getText());

			} catch (NullPointerException playerInputInvalid) {
				whiteDisadvantageBonus = 0;
				nonIntegerInputMessage();

			} catch (NumberFormatException playerInputInvalid) {
				whiteDisadvantageBonus = 0;
				nonIntegerInputMessage();
			}

		} else if (e.getActionCommand().equals("disadvantageInquiry")) {
			explainDisadvantage();

		} else if (e.getActionCommand().equals("boardSize")) {
			boardSizeSelected = true;
			try {
				numLines = Integer.parseInt(boardSize.getText());

			} catch (NullPointerException playerInputInvalid) {
				numLines = 19;
				nonIntegerInputMessage();

			} catch (NumberFormatException playerInputInvalid) {
				numLines = 19;
				nonIntegerInputMessage();
			}

		} else if (e.getActionCommand().equals("default")) {
			koOptionSelected = true;
			disadvantageBonusSelected = true;
			boardSizeSelected = true;

			usingKo = true;
			whiteDisadvantageBonus = 5;
			numLines = 19;

		} else if (e.getActionCommand().equals("defaultInquiry")) {
			explainDefaults();

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
		JOptionPane.showMessageDialog(this,
				"Because White goes second, they are inherently at a disadvantage "
						+ "\nA good handicap for white is a number from 5 to 7"
						+ " \nBe sure to press enter after choosing a value",
				"Rules", JOptionPane.PLAIN_MESSAGE);
	}

	private void explainDefaults() {
		JOptionPane.showMessageDialog(this, "Ko is used"
				+ "\nWhite gets 5 extra points" + " \nThe board is 19 by 19",
				"Defaults", JOptionPane.PLAIN_MESSAGE);
	}

	private void nonIntegerInputMessage() {
		JOptionPane.showMessageDialog(this,
				"Please input integer values for this field", "Error",
				JOptionPane.PLAIN_MESSAGE);
	}

	public boolean getUsingKo() {
		return usingKo;
	}

	public int getWhiteDisadvantageBonus() {
		return whiteDisadvantageBonus;
	}

	public int getBoardSize() {
		return numLines;
	}

}
