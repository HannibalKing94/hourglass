package org.example.hourglass;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")  // erlaubt Anfragen von jeder Quelle (für Entwicklung okay)
public class HourglassController {

    private final HourglassCalculator calculator = new HourglassCalculator();

    @GetMapping("/api/analyze")
    public AnalysisResponse analyze(
            @RequestParam int gamesWon,
            @RequestParam int gamesPlayed) {

        // Validierung
        if (gamesPlayed <= 0) {
            throw new IllegalArgumentException("Games played must be greater than 0");
        }
        if (gamesWon < 0 || gamesWon > gamesPlayed) {
            throw new IllegalArgumentException("Games won must be between 0 and games played");
        }

        double winrate = (double) gamesWon / gamesPlayed;
        List<StrategyResult> results = calculator.compareAllStrategies(winrate, 100000, 10);
        StrategyResult best = results.getFirst();

        return new AnalysisResponse(winrate, results, best);
    }
}