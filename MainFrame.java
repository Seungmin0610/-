package IdealTypeWorldCup;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import java.awt.*;
import java.io.File;
import java.util.List;

public class MainFrame extends JFrame {
    private Tournament tournament;

    public MainFrame(List<Person> candidates) {
        setTitle("이상형 월드컵");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        tournament = new Tournament(candidates);
        showNextMatch();
    }

    private void showNextMatch() {
        if (tournament.isFinalRoundComplete()) {
            System.out.println("최종 우승자 선택 완료");
            showResult(tournament.getFinalWinner());
            return;
        }
        
        if (!tournament.hasNextMatch()) {
            tournament.advanceRound();
        }

        if (tournament.hasNextMatch()) {
            Person[] match = tournament.getNextMatch();
            String roundText = "이상형 월드컵 - " + tournament.getRoundLabel();

            getContentPane().removeAll();
            getContentPane().add(new MatchPanel(match[0], match[1], e -> {
                Person selected = (Person) e.getSource();
                System.out.println("선택됨: " + selected.getName());
                tournament.selectWinner((Person) e.getSource());
                showNextMatch();
            }, roundText));

            revalidate();
            repaint();
        }
    }

    private void showResult(Person winner) {
        getContentPane().removeAll();

        JPanel mainPanel = new JPanel(new BorderLayout());

        JLabel nameLabel = new JLabel("당신의 이상형: " + winner.getName() + " !", SwingConstants.CENTER);
        nameLabel.setFont(new Font("Serif", Font.BOLD, 30));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLabel.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
        
        JTextPane descPane = new JTextPane();
        descPane.setText(winner.getDescription());
        descPane.setEditable(false);
        descPane.setOpaque(false);
        descPane.setFont(new Font("SansSerif", Font.PLAIN, 20));
        descPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        StyledDocument doc = descPane.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);
        
        JButton restartButton = new JButton("다시 시작");
        restartButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        restartButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        restartButton.addActionListener(_ -> restartTournament());

        JLabel imageLabel = new JLabel(resizeImage(winner.getImagePath(), 550, 550));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 20, 10));
        centerPanel.add(nameLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        centerPanel.add(descPane);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        centerPanel.add(restartButton);

        mainPanel.add(imageLabel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        getContentPane().add(mainPanel);
        revalidate();
        repaint();

        System.out.println("결과 화면 표시 완료");
    }

    private ImageIcon resizeImage(String path, int width, int height) {
        ImageIcon original = new ImageIcon(path);
        Image scaled = original.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }
    public static void main(String[] args) {
        List<Person> people = List.of(
            new Person("미연", "images\\미연.jpeg", "활발한 성격, 여행을 좋아함"),
            new Person("설윤", "images\\설윤.jpeg", "차분하고 책을 좋아함"),
            new Person("안유진", "images\\안유진.jpeg", "동물을 사랑함, 요리 취미"),
            new Person("윈터", "images\\윈터.jpeg", "운동 매니아"),
            new Person("장원영", "images\\장원영.jpeg", "음악과 춤을 즐김"),
            new Person("카리나", "images\\카리나.jpeg", "유머감각 풍부"),
            new Person("해원", "images\\해원.jpeg", "섬세하고 따뜻한 성격"),
            new Person("홍은채", "images\\홍은채.jpeg", "리더십 강함")
        );

        new MainFrame(people).setVisible(true);
    }

    private void restartTournament() {
        System.out.println("다시 시작 버튼 클릭됨");

        List<Person> people = List.of(
            new Person("미연", "images\\미연.jpeg", "활발한 성격, 여행을 좋아함"),
            new Person("설윤", "images\\설윤.jpeg", "차분하고 책을 좋아함"),
            new Person("안유진", "images\\안유진.jpeg", "동물을 사랑함, 요리 취미"),
            new Person("윈터", "images\\윈터.jpeg", "운동 매니아"),
            new Person("장원영", "images\\장원영.jpeg", "음악과 춤을 즐김"),
            new Person("카리나", "images\\카리나.jpeg", "유머감각 풍부"),
            new Person("해원", "images\\해원.jpeg", "섬세하고 따뜻한 성격"),
            new Person("홍은채", "images\\홍은채.jpeg", "리더십 강함")
        );

        this.tournament = new Tournament(people);
        showNextMatch();
    }
}