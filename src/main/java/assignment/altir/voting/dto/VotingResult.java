package assignment.altir.voting.dto;

import assignment.altir.voting.model.BillStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Dto Class for Result after Voting Closes..
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class VotingResult {
    @JsonProperty(value = "BillId")
    Long billId;

    @JsonProperty(value = "VotedFor")
    Integer votedFor;

    @JsonProperty(value = "VotingAgainst")
    Integer votingAgainst;

    @JsonProperty(value = "VotingOpenTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime votingOpenTime;

    @JsonProperty(value = "VotingCloseTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime votingCloseTime;

    @JsonProperty(value = "BillStatus")
    BillStatus billStatus;
}
