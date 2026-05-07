package org.example.hourglass;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest( HourglassController.class )
@org.springframework.context.annotation.Import( HourglassControllerTest.MockCalculatorConfig.class )
class HourglassControllerTest
{

    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    static class MockCalculatorConfig
    {
        @Bean
        @Primary
        public HourglassCalculator hourglassCalculator()
        {
            return new HourglassCalculator()
            {
                @Override
                public List<StrategyResult> compareAllStrategies( double winrate, int numIterations, int numRuns )
                {
                    return List.of(
                            new StrategyResult( 5, 350.0 ),
                            new StrategyResult( 4, 320.0 ),
                            new StrategyResult( Integer.MAX_VALUE, 300.0 )
                    );
                }
            };
        }
    }

    @Test
    void analyze_withValidInput_returns200AndCorrectStructure() throws Exception
    {
        mockMvc.perform( get( "/api/analyze" )
                        .param( "gamesWon", "60" )
                        .param( "gamesPlayed", "100" ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.winrate" ).value( 0.6 ) )
                .andExpect( jsonPath( "$.bestStrategy.lowerAtStreak" ).value( 5 ) )
                .andExpect( jsonPath( "$.bestStrategy.xpPerMinute" ).value( 350.0 ) )
                .andExpect( jsonPath( "$.allStrategies" ).isArray() )
                .andExpect( jsonPath( "$.allStrategies.length()" ).value( 3 ) );
    }

    @Test
    void analyze_withGamesPlayedZero_returns400() throws Exception
    {
        mockMvc.perform( get( "/api/analyze" )
                        .param( "gamesWon", "5" )
                        .param( "gamesPlayed", "0" ) )
                .andExpect( status().isBadRequest() )
                .andExpect( jsonPath( "$.message" ).value( "Games played must be greater than 0" ) );
    }

    @Test
    void analyze_withGamesWonGreaterThanPlayed_returns400() throws Exception
    {
        mockMvc.perform( get( "/api/analyze" )
                        .param( "gamesWon", "200" )
                        .param( "gamesPlayed", "100" ) )
                .andExpect( status().isBadRequest() )
                .andExpect( jsonPath( "$.message" ).value( "Games won must be between 0 and games played" ) );
    }

    @Test
    void analyze_withMissingParameter_returns400() throws Exception
    {
        mockMvc.perform( get( "/api/analyze" )
                        .param( "gamesWon", "60" ) )
                .andExpect( status().isBadRequest() )
                .andExpect( jsonPath( "$.message" ).value( "Missing parameter: gamesPlayed" ) );
    }

    @Test
    void analyze_withNonNumericParameter_returns400() throws Exception
    {
        mockMvc.perform( get( "/api/analyze" )
                        .param( "gamesWon", "abc" )
                        .param( "gamesPlayed", "100" ) )
                .andExpect( status().isBadRequest() )
                .andExpect( jsonPath( "$.message" ).value( "Invalid value for parameter: gamesWon" ) );
    }

    @Test
    void analyze_withNegativeGamesWon_returns400() throws Exception
    {
        mockMvc.perform( get( "/api/analyze" )
                        .param( "gamesWon", "-1" )
                        .param( "gamesPlayed", "100" ) )
                .andExpect( status().isBadRequest() )
                .andExpect( jsonPath( "$.message" ).value( "Games won must be between 0 and games played" ) );
    }
}