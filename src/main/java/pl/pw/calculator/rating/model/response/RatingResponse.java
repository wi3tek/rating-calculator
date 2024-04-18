package pl.pw.calculator.rating.model.response;

import lombok.Builder;
import lombok.Data;
import pl.pw.calculator.rating.model.request.GamePlayerData;

import java.util.List;

@Data
@Builder
public class RatingResponse {
    private List<GamePlayerData> players;
}
