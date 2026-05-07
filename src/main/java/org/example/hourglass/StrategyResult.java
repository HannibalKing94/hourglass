package org.example.hourglass;

/**
 * Ergebnis einer Strategie-Simulation.
 *
 * @param lowerAtStreak bei welchem Streak gelowert wird (Integer.MAX_VALUE = nie)
 * @param xpPerMinute   die durchschnittlich erreichte XP pro Minute
 */
public record StrategyResult(int lowerAtStreak, double xpPerMinute) {

    public String describe() {
        if (lowerAtStreak == Integer.MAX_VALUE) {
            return "Never lower";
        }
        return "Lower at streak " + lowerAtStreak;
    }
}