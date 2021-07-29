package assignment.altir.voting.repository;

import assignment.altir.voting.model.Person;
import assignment.altir.voting.model.VoteStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Repository class for Person Table..
 */
@Repository
public interface PersonRepository extends CrudRepository<Person,Long> {

    /**
     * This query returns all persons count who have voted between Open and Closing time of Bill Either Against/For based on VoteStatus
     * @param billId Instance of BillId
     * @param startTime Opening Time of Bill
     * @param endTime Closing Time of Bill
     * @param voteStatus VoteStatus
     * @return Integer count
     */
    @Query(value = "SELECT count(e) from Person e where e.bill.billId= :billId " +
            "and e.voteTime >= :startTime and e.voteTime <= :endTime and e.voteStatus = :voteStatus")
    Integer findCountVotedFor(@Param("billId") Long billId, @Param("startTime")LocalDateTime startTime,
                                         @Param("endTime")LocalDateTime endTime, @Param("voteStatus")VoteStatus voteStatus);

    /**
     * This query returns count of all persons who voted during Open and Close time of Bill..
     * @param billId Instance of BillId
     * @param startTime Opening Time of Bill
     * @param endTime Closing Time of Bill
     * @return Integer count
     */
    @Query(value = "SELECT count(e) from Person e where e.bill.billId= :billId " +
            "and e.voteTime >= :startTime and e.voteTime <= :endTime")
    Integer findTotalVotes(@Param("billId") Long billId, @Param("startTime")LocalDateTime startTime,
                              @Param("endTime")LocalDateTime endTime);

}
