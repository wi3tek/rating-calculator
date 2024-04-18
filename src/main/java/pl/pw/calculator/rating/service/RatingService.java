package pl.pw.calculator.rating.service;

import jakarta.validation.Valid;
import pl.pw.calculator.rating.model.request.GamePlayerData;
import pl.pw.calculator.rating.model.request.GameTeamData;
import pl.pw.calculator.rating.model.request.RatingRequest;
import pl.pw.calculator.rating.model.response.RatingResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;


public class RatingService {

    private final ExpectedScoreService expectedScoreService = new ExpectedScoreService();
    private final ActualScoreService actualScoreService = new ActualScoreService();
    private final GoalsService goalsService = new GoalsService();

    private static final BigDecimal WEIGHT_INDEX = BigDecimal.valueOf( 20 );

//    @LogEvent dodać logowanie
    public RatingResponse calculateRating(@Valid RatingRequest request) {
        if (request.getTeamA() == null || request.getTeamB() == null)
            throw new RatingException( "Requested teams cannot be null" );

        GameTeamData teamA = request.getTeamA();
        GameTeamData teamB = request.getTeamB();

        Map<String, BigDecimal> actualScores = actualScoreService.calculateActualScores( teamA, teamB );
        Map<String, BigDecimal> expectedScores = expectedScoreService.calculateExpectedScores( teamA, teamB, request.getMode());
        BigDecimal goalsDifferenceIndex = goalsService.calculateGoalsDifferenceIndex( teamA, teamB );
        List<GamePlayerData> players = Stream.concat(
                request.getTeamA().getPlayers().stream(),
                request.getTeamB().getPlayers().stream()
        ).toList();

        return RatingResponse.builder()
                .players( prepareRatingMap( players, actualScores, expectedScores, goalsDifferenceIndex ) )
                .build();
    }

    private List<GamePlayerData> prepareRatingMap(
            List<GamePlayerData> players,
            Map<String, BigDecimal> actualScores,
            Map<String, BigDecimal> expectedScores,
            BigDecimal goalsDifferenceIndex
    ) {
        return players.stream()
                .map( player -> {
                    BigDecimal actualScore = actualScores.get( player.getId() );
                    BigDecimal expectedScore = expectedScores.get( player.getId() );

                    BigDecimal ratingDifference = calculateRatingDifference( actualScore, expectedScore,
                            goalsDifferenceIndex );

                    return GamePlayerData.builder()
                            .id( player.getId() )
                            .rating( prepareRating( player.getRating(), ratingDifference ) )
                            .ratingDifference( ratingDifference )
                            .build();
                } )
                .toList();
    }

    private BigDecimal prepareRating(
            BigDecimal playerRating,
            BigDecimal ratingDifference

    ) {
        return playerRating.add( ratingDifference );
    }

    private static BigDecimal calculateRatingDifference(BigDecimal actualScore, BigDecimal expectedScore, BigDecimal goalsDifferenceIndex) {
        return WEIGHT_INDEX
                .multiply( goalsDifferenceIndex )
                .multiply( actualScore.subtract( expectedScore ) )
                .divide( new BigDecimal( 1 ), 0,
                        RoundingMode.HALF_UP );
    }
}
