package org.example.hourglass;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.example.hourglass.HourglassConstants.MAX_LOWER_STRATEGY_STREAK;
import static org.junit.jupiter.api.Assertions.*;

class HourglassCalculatorTest
{

    private final HourglassCalculator calculator = new HourglassCalculator();

    @Test
    void winXp_returnsCorrectXpForEachStreak()
    {
        assertEquals( 4200, calculator.winXp( 0 ) );
        assertEquals( 4675, calculator.winXp( 1 ) );
        assertEquals( 5190, calculator.winXp( 2 ) );
        assertEquals( 5688, calculator.winXp( 3 ) );
        assertEquals( 6600, calculator.winXp( 4 ) );
        assertEquals( 6600, calculator.winXp( 5 ) );
        assertEquals( 6600, calculator.winXp( 100 ) );  // alles >= 4 ist gleich
    }

    @Test
    void winXp_throwsExceptionForNegativeStreak()
    {
        assertThrows( IllegalArgumentException.class, () -> calculator.winXp( -1 ) );
    }

    @Test
    void lowerXp_returnsCorrectXpForEachStreak()
    {
        assertEquals( 1100, calculator.lowerXp( 1 ) );
        assertEquals( 2640, calculator.lowerXp( 2 ) );
        assertEquals( 4680, calculator.lowerXp( 3 ) );
        assertEquals( 7800, calculator.lowerXp( 4 ) );
        assertEquals( 10800, calculator.lowerXp( 5 ) );   // 7800 + 3000*1
        assertEquals( 13800, calculator.lowerXp( 6 ) );   // 7800 + 3000*2
        assertEquals( 16800, calculator.lowerXp( 7 ) );   // 7800 + 3000*3
    }

    @Test
    void lowerXp_throwsExceptionForStreakBelowOne()
    {
        assertThrows( IllegalArgumentException.class, () -> calculator.lowerXp( 0 ) );
        assertThrows( IllegalArgumentException.class, () -> calculator.lowerXp( -5 ) );
    }

    @Test
    void simulate_with100PercentWinrate_andLowerAtStreak3_returnsExpectedXpPerMinute()
    {
        // Bei 100% Winrate ist alles berechenbar — kein Zufall im Spiel.
        // Zyklus von 4 Iterationen: Win@0, Win@1, Win@2, Lower@3
        // XP: 4200 + 4675 + 5190 + 4680 = 18745
        // Zeit: 4 * 10 min = 40 min
        // Erwartung: 18745 / 40 = 468.625 XP/min

        HourglassCalculator calc = new HourglassCalculator();
        double result = calc.simulate( 1.0, 3, 100_000 );

        assertEquals( 468.625, result, 0.001 );
    }

    @Test
    void simulate_with0PercentWinrate_alwaysReturnsLossXpPerMinute()
    {
        // Bei 0% Winrate verliert man jedes Spiel.
        // Jedes Spiel: 700 XP in 10 min = 70 XP/min

        HourglassCalculator calc = new HourglassCalculator();
        double result = calc.simulate( 0.0, 3, 100_000 );

        assertEquals( 70.0, result, 0.001 );
    }

    @Test
    void simulate_with100PercentWinrate_neverLowering_returnsExpectedXpPerMinute()
    {
        // 100% Winrate, nie lowern — Streak wächst auf, jeder Win nach Streak 3 = 6600 XP
        // Bei vielen Iterationen dominieren die 6600er Wins.
        // Erwartung: knapp unter 660 XP/min (660 = 6600/10)

        HourglassCalculator calc = new HourglassCalculator();
        double result = calc.simulate( 1.0, Integer.MAX_VALUE, 100_000 );

        assertEquals( 660.0, result, 0.5 );  // bisschen mehr Toleranz wegen der Anlauf-Phase
    }

    @Test
    void simulate_withFixedSeed_isReproducible()
    {
        // Zwei Calculators mit gleichem Seed → gleiches Ergebnis
        HourglassCalculator calc1 = new HourglassCalculator( new Random( 42 ) );
        HourglassCalculator calc2 = new HourglassCalculator( new Random( 42 ) );

        double result1 = calc1.simulate( 0.6, 3, 10_000 );
        double result2 = calc2.simulate( 0.6, 3, 10_000 );

        assertEquals( result1, result2, 0.0 );  // exakt gleich!
    }

    @Test
    void simulate_withDifferentSeeds_producesDifferentResults()
    {
        HourglassCalculator calc1 = new HourglassCalculator( new Random( 42 ) );
        HourglassCalculator calc2 = new HourglassCalculator( new Random( 123 ) );

        double result1 = calc1.simulate( 0.6, 3, 10_000 );
        double result2 = calc2.simulate( 0.6, 3, 10_000 );

        assertNotEquals( result1, result2 );  // sehr wahrscheinlich unterschiedlich
    }

    @Test
    void simulate_throwsException_whenWinrateIsInvalid()
    {
        HourglassCalculator calc = new HourglassCalculator();

        assertThrows( IllegalArgumentException.class,
                () -> calc.simulate( -0.1, 3, 1000 ) );
        assertThrows( IllegalArgumentException.class,
                () -> calc.simulate( 1.5, 3, 1000 ) );
    }

    @Test
    void simulate_throwsException_whenLowerAtStreakIsZeroOrNegative()
    {
        HourglassCalculator calc = new HourglassCalculator();

        assertThrows( IllegalArgumentException.class,
                () -> calc.simulate( 0.5, 0, 1000 ) );
        assertThrows( IllegalArgumentException.class,
                () -> calc.simulate( 0.5, -1, 1000 ) );
    }

    @Test
    void simulate_throwsException_whenNumIterationsIsZeroOrNegative()
    {
        HourglassCalculator calc = new HourglassCalculator();

        assertThrows( IllegalArgumentException.class,
                () -> calc.simulate( 0.5, 3, 0 ) );
        assertThrows( IllegalArgumentException.class,
                () -> calc.simulate( 0.5, 3, -100 ) );
    }

    @Test
    void compareAllStrategies_returnsAllStrategiesIncludingNeverLower()
    {
        HourglassCalculator calc = new HourglassCalculator();
        var results = calc.compareAllStrategies( 0.6, 1000, 1 );

        // 10 Streak-Strategien (1-10) + 1 "Never lower" = 11 insgesamt
        assertEquals( MAX_LOWER_STRATEGY_STREAK + 1, results.size() );
    }

    @Test
    void compareAllStrategies_returnsResultsSortedByXpPerMinuteDescending()
    {
        HourglassCalculator calc = new HourglassCalculator();
        var results = calc.compareAllStrategies( 0.6, 10_000, 3 );

        // Jedes Ergebnis muss >= dem nächsten sein (absteigend sortiert)
        for ( int i = 0; i < results.size() - 1; i++ )
        {
            assertTrue( results.get( i ).xpPerMinute() >= results.get( i + 1 ).xpPerMinute(),
                    "Results should be sorted descending by xpPerMinute" );
        }
    }

    @Test
    void compareAllStrategies_lowerAtStreak1_isAlwaysWorseThanLowerAtStreak2_atHighWinrate()
    {
        // Bei hoher Winrate ist "Lower at 1" Schwachsinn — der Test belegt das.
        HourglassCalculator calc = new HourglassCalculator();
        var results = calc.compareAllStrategies( 0.8, 50_000, 3 );

        StrategyResult lowerAt1 = results.stream()
                .filter( r -> r.lowerAtStreak() == 1 ).findFirst().orElseThrow();
        StrategyResult lowerAt2 = results.stream()
                .filter( r -> r.lowerAtStreak() == 2 ).findFirst().orElseThrow();

        assertTrue( lowerAt1.xpPerMinute() < lowerAt2.xpPerMinute() );
    }
}