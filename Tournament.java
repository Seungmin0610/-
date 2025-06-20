package IdealTypeWorldCup;

import java.util.*;

public class Tournament {
    private List<Person> candidates;
    private Queue<Person> currentRound;
    private List<Person> nextRound;
    private int currentRoundSize;
    private int matchCounter = 1;

    public Tournament(List<Person> candidates) {
        this.candidates = new ArrayList<>(candidates);
        Collections.shuffle(this.candidates);
        currentRound = new LinkedList<>(this.candidates);
        nextRound = new ArrayList<>();
        currentRoundSize = this.candidates.size();
    }

    public boolean hasNextMatch() {
        return currentRound.size() >= 2;
    }

    public Person[] getNextMatch() {
        return new Person[] { currentRound.poll(), currentRound.poll() };
    }

    public void selectWinner(Person winner) {
        nextRound.add(winner);
    }

    public boolean advanceRound() {
        if (!currentRound.isEmpty()) return true;

        if (nextRound.size() <= 1) return false;
        
        currentRound = new LinkedList<>(nextRound);
        nextRound = new ArrayList<>();
        currentRoundSize = currentRound.size();
        matchCounter = 1;
        return true;
    }

    public boolean isFinalRoundComplete() {
        return currentRound.isEmpty() && nextRound.size() == 1;
    }

    public Person getFinalWinner() {
        return isFinalRoundComplete() ? nextRound.get(0) : null;
    }

    public int getCurrentRoundSize() {
        return currentRound.size() + nextRound.size();
    }  

    public int getCurrentMatchNumber() {
        int totalPairs = (currentRound.size() + 1) / 2 + nextRound.size() / 2;
        return totalPairs - currentRound.size() / 2;
    }

    public String getRoundLabel() {
        String roundName = switch (currentRoundSize) {
            case 2 -> "결승";
            case 4 -> "4강";
            case 8 -> "8강";
            case 16 -> "16강";
            default -> currentRoundSize + "강";
        };

        if ("결승".equals(roundName)) {
            return roundName;
        }

        else {
            return roundName + " " + (matchCounter++) + "경기";
        }
    }
}