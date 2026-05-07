package org.example.hourglass;

import java.util.*;

import static org.example.hourglass.HourglassConstants.*;

/**
 * Kalkuliert den optimalen Streak zum lowern, aufgrund der Winrate in Sea of Thieves Hourglass.
 */
public class HourglassCalculator
{
    private final Random random;

    public HourglassCalculator()
    {
        this.random = new Random();
    }

    // Konstruktor für Tests — hier kann ein Random mit fixem Seed reingegeben werden
    public HourglassCalculator( Random random )
    {
        this.random = random;
    }

    /**
     * Gibt die XP zurück, die man für einen Win bei dem aktuellen Streak bekommt.
     *
     * @param streak der aktuelle Streak VOR dem Win (0, 1, 2, 3, 4+)
     */
    protected int winXp( int streak )
    {
        if ( streak < 0 )
        {
            throw new IllegalArgumentException( "Streak must be >= 0" );
        }
        return switch ( streak )
        {
            case 0 -> WIN_XP_STREAK_0;
            case 1 -> WIN_XP_STREAK_1;
            case 2 -> WIN_XP_STREAK_2;
            case 3 -> WIN_XP_STREAK_3;
            default -> WIN_XP_STREAK_4_PLUS; // 4 oder mehr
        };
    }

    /**
     * Gibt die XP zurück, die man bekommt, wenn man von einem bestimmten Streak auf 0 lowert.
     *
     * @param streak der aktuelle Streak (>= 1, sonst ergibt lowern keinen Sinn)
     */
    protected int lowerXp( int streak )
    {
        if ( streak < 1 )
        {
            throw new IllegalArgumentException( "Can only lower from streak >= 1" );
        }
        return switch ( streak )
        {
            case 1 -> LOWER_XP_STREAK_1;
            case 2 -> LOWER_XP_STREAK_2;
            case 3 -> LOWER_XP_STREAK_3;
            default -> LOWER_XP_STREAK_4_BASE + LOWER_XP_STREAK_4_PLUS_INCREMENT * ( streak - 4 );
            // streak 4: 7800 + 3000*0 = 7800
            // streak 5: 7800 + 3000*1 = 10800
            // streak 6: 7800 + 3000*2 = 13800
        };
    }

    /**
     * Simuliert eine bestimmte Strategie und gibt die durchschnittliche XP/Minute zurück.
     *
     * @param winrate       Winrate des Spielers (0.0 bis 1.0, z.B. 0.6 für 60%)
     * @param lowerAtStreak bei welchem Streak gelowert wird (z.B. 3 = "lower sobald Streak 3 erreicht ist")
     *                      Spezialwert Integer.MAX_VALUE = "nie lowern"
     * @param numIterations Anzahl der zu simulierenden Aktionen (Spiele + Lowers)
     * @return durchschnittliche XP pro Minute
     */
    public double simulate( double winrate, int lowerAtStreak, int numIterations )
    {
        if ( winrate < 0.0 || winrate > 1.0 )
        {
            throw new IllegalArgumentException( "Winrate must be between 0.0 and 1.0" );
        }
        if ( lowerAtStreak < 1 )
        {
            throw new IllegalArgumentException( "lowerAtStreak must be >= 1 (or Integer.MAX_VALUE for 'never')" );
        }
        if ( numIterations <= 0 )
        {
            throw new IllegalArgumentException( "numIterations must be > 0" );
        }

        int streak = 0;
        long totalXp = 0;
        long totalMinutes = 0;

        for ( int i = 0; i < numIterations; i++ )
        {
            if ( streak >= lowerAtStreak )
            {
                // Lower-Aktion
                totalXp += lowerXp( streak );
                totalMinutes += MINUTES_PER_LOWER;
                streak = 0;
            } else
            {
                // Spiel-Aktion
                totalMinutes += MINUTES_PER_GAME;
                if ( random.nextDouble() < winrate )
                {
                    // Win
                    totalXp += winXp( streak );
                    streak++;
                } else
                {
                    // Loss
                    totalXp += LOSS_XP;
                    streak = 0;
                }
            }
        }

        return ( double ) totalXp / totalMinutes;//NOSONAR
    }

    /**
     * Simuliert eine Strategie mehrfach und gibt das gemittelte Ergebnis zurück.
     * Reduziert die statistische Schwankung gegenüber einem einzelnen Durchlauf.
     *
     * @param winrate       Winrate des Spielers
     * @param lowerAtStreak Lower-Schwelle
     * @param numIterations Iterationen pro Durchlauf
     * @param numRuns       Anzahl der Durchläufe (z.B. 10)
     * @return durchschnittliche XP/min über alle Durchläufe
     */
    public double simulateAveraged( double winrate, int lowerAtStreak,
                                    int numIterations, int numRuns )
    {
        if ( numRuns <= 0 )
        {
            throw new IllegalArgumentException( "numRuns must be > 0" );
        }

        double sum = 0;
        for ( int i = 0; i < numRuns; i++ )
        {
            sum += simulate( winrate, lowerAtStreak, numIterations );
        }
        return sum / numRuns;
    }

    /**
     * Simuliert alle Strategien und gibt die Ergebnisse sortiert zurück (beste zuerst).
     */
    public List<StrategyResult> compareAllStrategies( double winrate, int numIterations, int numRuns )
    {
        List<StrategyResult> results = new ArrayList<>();

        for ( int lowerAt = 1; lowerAt <= 10; lowerAt++ )
        {
            double xpPerMinute = simulateAveraged( winrate, lowerAt, numIterations, numRuns );
            results.add( new StrategyResult( lowerAt, xpPerMinute ) );
        }

        double neverLowerXp = simulateAveraged( winrate, Integer.MAX_VALUE, numIterations, numRuns );
        results.add( new StrategyResult( Integer.MAX_VALUE, neverLowerXp ) );

        // sortieren: beste (höchste XP/min) zuerst
        results.sort( ( a, b ) -> Double.compare( b.xpPerMinute(), a.xpPerMinute() ) );

        return results;
    }

    public void runHourglassAnalysis( Scanner sc )
    {
        try
        {
            System.out.println( "=== Sea of Thieves Hourglass Strategy Analyzer ===" );
            System.out.println( "Please enter played games:" );
            int gamesPlayed = sc.nextInt();
            System.out.println( "Please enter won games:" );
            int gamesWon = sc.nextInt();

            // Winrate berechnen
            if ( gamesPlayed <= 0 )
            {
                throw new IllegalArgumentException( "Games played must be greater than 0" );
            }
            if ( gamesWon < 0 || gamesWon > gamesPlayed )
            {
                throw new IllegalArgumentException( "Games won must be between 0 and games played" );
            }
            double winrate = ( double ) gamesWon / gamesPlayed;

            System.out.printf( "%nYour winrate: %.2f%% (%d/%d)%n%n",
                    winrate * 100, gamesWon, gamesPlayed );
            System.out.println( "Running simulation (1 million games per strategy)..." );
            System.out.println();

            // Strategien vergleichen
            int numIterations = 100000;
            int numRuns = 10;
            List<StrategyResult> results = compareAllStrategies( winrate, numIterations, numRuns );

            // Tabelle ausgeben
            System.out.println( "=== Strategy Comparison ===" );
            System.out.printf( "%-25s %12s%n", "Strategy", "XP/min" );
            System.out.println( "-".repeat( 40 ) );
            for ( StrategyResult r : results )
            {
                System.out.printf( "%-25s %12.2f%n", r.describe(), r.xpPerMinute() );
            }

            // Empfehlung
            StrategyResult best = results.get( 0 );
            System.out.println();
            System.out.println( "=== Recommendation ===" );
            System.out.printf( "Best strategy: %s (%.2f XP/min)%n",
                    best.describe(), best.xpPerMinute() );

            // Bonus-Info: wie lange für 1 Level?
            double minutesPerLevel = XP_PER_LEVEL / best.xpPerMinute();
            System.out.printf( "Estimated time per level: %.1f minutes (%.1f hours)%n",
                    minutesPerLevel, minutesPerLevel / 60 );

        } catch ( InputMismatchException e )
        {
            System.out.println( "Please enter a number." );
            sc.nextLine();
        } catch ( IllegalArgumentException e )
        {
            System.out.println( "Error: " + e.getMessage() );
        }
    }
}
