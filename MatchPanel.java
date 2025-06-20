package IdealTypeWorldCup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.text.*;

public class MatchPanel extends JPanel {
    private JLabel roundLabel;
    private JButton leftButton, rightButton;
    private JLabel leftImageLabel, rightImageLabel;

    public MatchPanel(Person p1, Person p2, ActionListener listener, String roundTitle) {
        setLayout(new BorderLayout());

        roundLabel = new JLabel(roundTitle, SwingConstants.CENTER);
        roundLabel.setFont(new Font("SansSerif", Font.BOLD, 30));
        roundLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        roundLabel.setOpaque(true);
        roundLabel.setBackground(Color.LIGHT_GRAY);
        roundLabel.setForeground(Color.BLACK);
        add(roundLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2));

        leftButton = new JButton(p1.getName());
        leftImageLabel = new JLabel(resizeImage(p1.getImagePath(), 650, 650), SwingConstants.CENTER);
        leftButton.addActionListener(_ -> listener.actionPerformed(new ActionEvent(p1, ActionEvent.ACTION_PERFORMED, "selected")));

        rightButton = new JButton(p2.getName());
        rightImageLabel = new JLabel(resizeImage(p2.getImagePath(), 650, 650), SwingConstants.CENTER);
        rightButton.addActionListener(_ -> listener.actionPerformed(new ActionEvent(p2, ActionEvent.ACTION_PERFORMED, "selected")));

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(leftImageLabel, BorderLayout.CENTER);
        leftPanel.add(leftButton, BorderLayout.SOUTH);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(rightImageLabel, BorderLayout.CENTER);
        rightPanel.add(rightButton, BorderLayout.SOUTH);

        centerPanel.add(leftPanel);
        centerPanel.add(rightPanel);
        add(centerPanel, BorderLayout.CENTER);

        JTextPane leftDesc = new JTextPane();
        leftDesc.setText(p1.getDescription());
        leftDesc.setEditable(false);
        leftDesc.setOpaque(false);

        StyledDocument leftDoc = leftDesc.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        leftDoc.setParagraphAttributes(0, leftDoc.getLength(), center, false);

        JTextPane rightDesc = new JTextPane();
        rightDesc.setText(p2.getDescription());
        rightDesc.setEditable(false);
        rightDesc.setOpaque(false);

        StyledDocument rightDoc = rightDesc.getStyledDocument();
        rightDoc.setParagraphAttributes(0, rightDoc.getLength(), center, false);

        JScrollPane leftScroll = new JScrollPane(leftDesc);
        JScrollPane rightScroll = new JScrollPane(rightDesc);

        JPanel descriptionPanel = new JPanel(new GridLayout(1, 2));
        descriptionPanel.add(leftScroll);
        descriptionPanel.add(rightScroll);
        add(descriptionPanel, BorderLayout.SOUTH);
    }

    private ImageIcon resizeImage(String path, int width, int height) {
        ImageIcon icon = new ImageIcon(path);
        Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }
}