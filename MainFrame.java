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
            new Person("홍은채", "images\\홍은채.jpeg", "리더십 강함"),
            new Person("가을", "images\\가을.jpeg", "명랑하고 긍정적인 성격"),
            new Person("규진", "images\\규진.jpeg", "에너지 넘치고 사교적"),
            new Person("레이", "images\\레이.jpeg", "유쾌하고 친근한 성격"),
            new Person("리즈", "images\\리즈.jpeg", "부드럽고 상냥한 성격"),
            new Person("민니", "images\\민니.jpeg", "다정하고 이해심 많은 성격"),
            new Person("민지", "images\\민지.jpeg", "배려심 깊고 친절한 성격"),
            new Person("슈화", "images\\슈화.jpeg", "공감 잘 해주고 센스 있는 성격"),
            new Person("배이", "images\\배이.jpeg", "똑부러지고 자신감 넘치는 성격"),
            new Person("우기", "images\\우기.jpeg", "지적이고 분석적인 성격"),
            new Person("이서", "images\\이서.jpeg", "책임감이 강하고 성실한 성격"),
            new Person("하니", "images\\하니.jpeg", "논리적이고 창의적인 성격"),
            new Person("해린", "images\\해린.jpeg", "장난기 넘치고 재치 있는 성격"),
            new Person("닝닝", "images\\닝닝.jpeg", "감성적이고 예술적인 성격"),
            new Person("카즈하", "images\\카즈하.jpeg", "영화 보는 것을 좋아함"),
            new Person("김채원", "images\\김채원.jpeg", "드라마 정주행을 즐겨함"),
            new Person("허윤진", "images\\허윤진.jpeg", "베이킹 취미"),
            new Person("채령", "images\\채령.jpeg", "악기 연주를 잘함"),
            new Person("유나", "images\\유나.jpeg", "그림 그리기를 좋아함"),
            new Person("예지", "images\\예지.jpeg", "캠핑을 즐겨함"),
            new Person("사쿠라", "images\\사쿠라.jpeg", "필라테스를 좋아함"),
            new Person("류진", "images\\류진.jpeg", "게임을 즐겨함"),
            new Person("송하영", "images\\송하영.jpeg", "노래 부르기를 좋아함"),
            new Person("백지헌", "images\\백지헌.jpeg", "카페 탐방을 즐겨함"),
            new Person("아이사", "images\\아이사.jpeg", "강아지랑 산책하는 것을 좋아함")
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
            new Person("홍은채", "images\\홍은채.jpeg", "리더십 강함"),
            new Person("가을", "images\\가을.jpeg", "명랑하고 긍정적인 성격"),
            new Person("규진", "images\\규진.jpeg", "에너지 넘치고 사교적"),
            new Person("레이", "images\\레이.jpeg", "유쾌하고 친근한 성격"),
            new Person("리즈", "images\\리즈.jpeg", "부드럽고 상냥한 성격"),
            new Person("민니", "images\\민니.jpeg", "다정하고 이해심 많은 성격"),
            new Person("민지", "images\\민지.jpeg", "배려심 깊고 친절한 성격"),
            new Person("슈화", "images\\슈화.jpeg", "공감 잘 해주고 센스 있는 성격"),
            new Person("배이", "images\\배이.jpeg", "똑부러지고 자신감 넘치는 성격"),
            new Person("우기", "images\\우기.jpeg", "지적이고 분석적인 성격"),
            new Person("이서", "images\\이서.jpeg", "책임감이 강하고 성실한 성격"),
            new Person("하니", "images\\하니.jpeg", "논리적이고 창의적인 성격"),
            new Person("해린", "images\\해린.jpeg", "장난기 넘치고 재치 있는 성격"),
            new Person("닝닝", "images\\닝닝.jpeg", "감성적이고 예술적인 성격"),
            new Person("카즈하", "images\\카즈하.jpeg", "영화 보는 것을 좋아함"),
            new Person("김채원", "images\\김채원.jpeg", "드라마 정주행을 즐겨함"),
            new Person("허윤진", "images\\허윤진.jpeg", "베이킹 취미"),
            new Person("채령", "images\\채령.jpeg", "악기 연주를 잘함"),
            new Person("유나", "images\\유나.jpeg", "그림 그리기를 좋아함"),
            new Person("예지", "images\\예지.jpeg", "캠핑을 즐겨함"),
            new Person("사쿠라", "images\\사쿠라.jpeg", "필라테스를 좋아함"),
            new Person("류진", "images\\류진.jpeg", "게임을 즐겨함"),
            new Person("송하영", "images\\송하영.jpeg", "노래 부르기를 좋아함"),
            new Person("백지헌", "images\\백지헌.jpeg", "카페 탐방을 즐겨함"),
            new Person("아이사", "images\\아이사.jpeg", "강아지랑 산책하는 것을 좋아함")
        );

        this.tournament = new Tournament(people);
        showNextMatch();
    }
}