package assignment.altir.voting.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 * Entity class for Bill
 */
@Entity
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "billId", callSuper = false)
@Table(name = "VOTING_BILL")
public class Bill {
    @Id
    @SequenceGenerator(name = "MY_BILL_SEQ", sequenceName = "MY_BILL_SEQ", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MY_BILL_SEQ")
    @JsonProperty(value = "BillId")
    @Column(name="BILL_ID", nullable = false)
    private Long billId;

    @Enumerated(EnumType.STRING)
    @JsonProperty(value = "BILL_STATUS")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Column(name = "STATUS", length = 100)
    private BillStatus status;

    @Column(name = "START_TIME")
    @JsonProperty(value = "BILL_OPEN_TIME")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Column(name = "END_TIME")
    @JsonProperty(value = "BILL_CLOSING_TIME")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * Reset Bill Status in special cases..
     * @param status
     * @param startTime
     */
    public void resetBillToOpenStatus(BillStatus status, LocalDateTime startTime) {
        this.status = BillStatus.OPEN;
        this.startTime = startTime;
        this.endTime = null;
    }
}
