package assignment.altir.voting.model;

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
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Getter
@ToString
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "personId", callSuper = false)
@Table(name = "SPEAKER")
public class Speaker {

    @Id
    @SequenceGenerator(name = "MY_SPEAKER_SEQ", sequenceName = "MY_SPEAKER_SEQ", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MY_SPEAKER_SEQ")
    @Column(name="SPEAKER_ID", nullable = false)
    @JsonProperty(value = "SPEAKER_ID")
    private Long speakerId;

    @OneToOne
    @JsonProperty(value = "BILL_DETAILS")
    @JoinColumn(name = "BILL_ID", nullable = false)
    private Bill bill;

    @Column(name = "PRESENT", length = 1)
    @JsonProperty(value = "AVAILABLE")
    @Enumerated(EnumType.STRING)
    private PresentFlag presentFlag;

    @Column(name = "TIE_VOTE", length = 1)
    @JsonProperty(value = "TIE_VOTE")
    @Enumerated(EnumType.STRING)
    private TieVote tieVote;

}
