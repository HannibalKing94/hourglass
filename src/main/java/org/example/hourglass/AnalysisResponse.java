package org.example.hourglass;

import java.util.List;

public record AnalysisResponse(
        double winrate,
        List<StrategyResult> allStrategies,
        StrategyResult bestStrategy
) {}