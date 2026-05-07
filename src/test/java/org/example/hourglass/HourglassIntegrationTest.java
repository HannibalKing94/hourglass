package org.example.hourglass;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.example.hourglass.HourglassConstants.MAX_LOWER_STRATEGY_STREAK;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class HourglassIntegrationTest
{

    @Autowired
    private MockMvc mockMvc;

    @Test
    void analyze_endToEnd_returnsValidResultsForRealCalculation() throws Exception
    {
        mockMvc.perform( get( "/api/analyze" )
                        .param( "gamesWon", "60" )
                        .param( "gamesPlayed", "100" ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.winrate" ).value( 0.6 ) )
                .andExpect( jsonPath( "$.bestStrategy.xpPerMinute" ).value( org.hamcrest.Matchers.greaterThan( 0.0 ) ) )
                .andExpect( jsonPath( "$.allStrategies.length()" ).value( MAX_LOWER_STRATEGY_STREAK + 1 ) )  // 15 + "never"
                .andExpect( jsonPath( "$.bestStrategy.lowerAtStreak" ).exists() );
    }

    @Test
    void rootUrl_returnsSuccess() throws Exception
    {
        // Stellt sicher, dass die Hauptseite erreichbar ist (Forward auf index.html)
        mockMvc.perform( get( "/" ) )
                .andExpect( status().isOk() );
    }
}