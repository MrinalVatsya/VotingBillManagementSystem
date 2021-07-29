package assignment.altir.voting.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 * Entity class for Person of Parliament..
 */
@Entity
@Getter
@ToString
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "personId", callSuper = false)
@Table(name = "PERSON")
public class Person {
    @Id
    @SequenceGenerator(name = "MY_PERSON_SEQ", sequenceName = "MY_PERSON_SEQ", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MY_PERSON_SEQ")
    @Column(name="PERSON_ID", nullable = false)
    @JsonProperty(value = "USER_ID")
    private Long personId;

    @Enumerated(EnumType.STRING)
    @JsonProperty(value = "VOTE_STATUS")
    @Column(name="VOTE_STATUS", nullable = false)
    private VoteStatus voteStatus;

    @ManyToOne
    @JsonProperty(value = "BILL_DETAILS")
    @JoinColumn(name = "BILL_ID", nullable = false)
    private Bill bill;

    @Column(name = "VOTE_TIME")
    @JsonProperty(value = "VOTING_TIME")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime voteTime;
}
