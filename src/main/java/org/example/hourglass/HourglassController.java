package org.example.hourglass;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin( origins = "*" )  // erlaubt Anfragen von jeder Quelle (für Entwicklung okay)
public class HourglassController
{

    private final HourglassCalculator calculator = new HourglassCalculator();

    @GetMapping( "/api/analyze" )
    public AnalysisResponse analyze(
            @RequestParam int gamesWon,
            @RequestParam int gamesPlayed )
    {

        // Validierung
        if ( gamesPlayed <= 0 )
        {
            throw new IllegalArgumentException( "Games played must be greater than 0" );
        }
        if ( gamesWon < 0 || gamesWon > gamesPlayed )
        {
            throw new IllegalArgumentException( "Games won must be between 0 and games played" );
        }

        double winrate = ( double ) gamesWon / gamesPlayed;
        List<StrategyResult> results = calculator.compareAllStrategies( winrate, 100000, 10 );
        StrategyResult best = results.getFirst();

        return new AnalysisResponse( winrate, results, best );
    }

    // Fall 1: Pflicht-Parameter fehlt — sauberere Meldung
    @ExceptionHandler( MissingServletRequestParameterException.class )
    public ResponseEntity<Map<String, String>> handleMissingParam( MissingServletRequestParameterException e )
    {
        return ResponseEntity
                .status( HttpStatus.BAD_REQUEST )
                .body( Map.of( "message", "Missing parameter: " + e.getParameterName() ) );
    }

    // Fall 2: Falscher Datentyp — sauberere Meldung
    @ExceptionHandler( MethodArgumentTypeMismatchException.class )
    public ResponseEntity<Map<String, String>> handleTypeMismatch( MethodArgumentTypeMismatchException e )
    {
        return ResponseEntity
                .status( HttpStatus.BAD_REQUEST )
                .body( Map.of( "message", "Invalid value for parameter: " + e.getName() ) );
    }

    // Fall 3: Unsere eigene Validierung
    @ExceptionHandler( IllegalArgumentException.class )
    public ResponseEntity<Map<String, String>> handleBadInput( IllegalArgumentException e )
    {
        return ResponseEntity
                .status( HttpStatus.BAD_REQUEST )
                .body( Map.of( "message", e.getMessage() ) );
    }

}