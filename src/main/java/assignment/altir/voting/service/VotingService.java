package assignment.altir.voting.service;

import assignment.altir.voting.model.Bill;
import assignment.altir.voting.model.BillStatus;
import assignment.altir.voting.repository.BillRepository;
import assignment.altir.voting.repository.PersonRepository;
import lombok.extern.slf4j.Slf4j;
import assignment.altir.voting.dto.VotingResult;
import assignment.altir.voting.model.PresentFlag;
import assignment.altir.voting.model.Speaker;
import assignment.altir.voting.model.TieVote;
import assignment.altir.voting.model.VoteStatus;
import assignment.altir.voting.repository.SpeakerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
@Slf4j
public class VotingService {
    @Autowired
    private BillRepository billRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private SpeakerRepository speakerRepository;

    /**
     * This method is used for closing the bill and aftermath returning result..
     * @param speakerId Speaker ID
     * @param billId Bill Id
     * @return VotingResult
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity<VotingResult> closeVoting(Long speakerId, Long billId) {
        VotingResult result = null;
        Optional<Bill> billRecordOpt = billRepository.findById(billId);
        if(billRecordOpt.isPresent())
        {
            Bill billRecord = billRecordOpt.get();
            LocalDateTime billStartTime = billRecord.getStartTime();
            LocalDateTime billEndTime = LocalDateTime.now(ZoneOffset.UTC);
            //Check if Billing is closed prior to 15 minutes..
            Duration duration = Duration.between(billStartTime, billEndTime);
            Long minutes = duration.toMinutes();
            if(minutes < 15L)
            {
                log.error("Voting cannot be closed prior to 15 minutes");
                return new ResponseEntity("Voting cannot be closed prior to 15 minutes", HttpStatus.OK);
            }
            Integer voteFor = personRepository.findCountVotedFor(billId, billStartTime, billEndTime, VoteStatus.YES);
            Integer totalVotes = personRepository.findTotalVotes(billId, billStartTime, billEndTime);
            Integer voteAgainst = totalVotes - voteFor;
             if(totalVotes > 543)
             {
                 log.error("A Bill cannot have more than 543 votes");
                 return new ResponseEntity("A Bill cannot have more than 543 votes", HttpStatus.OK);
             }
             else if(voteFor > voteAgainst){
                 //Bill is passed..
                 log.info("=========Bill is passed with majority=============");
                 result = constructVotingResultRecord(billId, billStartTime, billEndTime, voteFor, voteAgainst, BillStatus.PASSED);
             }
             else if(voteFor < voteAgainst)
             {
                 log.info("------------------Bill is not passed------------");
                 result = constructVotingResultRecord(billId, billStartTime, billEndTime, voteFor, voteAgainst, BillStatus.FAILED);
             }
             else
             {
                 //tie in this case..speaker can vote..assuming he will vote in favour of bill
                 log.info("------------------Tie happened------------");
                 //check if speaker is available to vote or not..
                 Speaker speaker = speakerRepository.findById(speakerId).get();
                 if(speaker.getPresentFlag().equals(PresentFlag.Y)) {
                     log.info("------------------Speaker is Present for Voting------------");
                     if(speaker.getTieVote().equals(TieVote.Y)) {
                         voteFor += 1;
                         result = constructVotingResultRecord(billId, billStartTime, billEndTime, voteFor, voteAgainst, BillStatus.PASSED);
                     }
                     else
                     {
                         voteAgainst += 1;
                         result = constructVotingResultRecord(billId, billStartTime, billEndTime, voteFor, voteAgainst, BillStatus.FAILED);
                     }
                 }
                 else
                 {
                     log.info("------------------Speaker is Not Present for Voting------------");
                     result = constructVotingResultRecord(billId, billStartTime, billEndTime, voteFor, voteAgainst, BillStatus.FAILED);
                 }
             }
             //close the bill..
            billRecord.setStatus(BillStatus.CLOSED);
             billRecord.setEndTime(billEndTime);
            billRepository.save(billRecord);
        }
        else
        {
            log.error("No Bill Found For Given Id {}", billId);
            return new ResponseEntity("No Bill Found For Given Id", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity(result,HttpStatus.OK);
    }

    /*
    Method to Construct Voting Result Record..
     */
    private VotingResult constructVotingResultRecord(Long billId, LocalDateTime billStartTime, LocalDateTime billEndTime, Integer voteFor, Integer voteAgainst, BillStatus billStatus) {
        return VotingResult.builder().votingAgainst(voteAgainst).votingCloseTime(billEndTime)
                .votingOpenTime(billStartTime).votedFor(voteFor).billStatus(billStatus).billId(billId).build();
    }

    /**
     * Method to open bill in special cases..
     * @param speakerId
     * @param billId
     */
    public ResponseEntity<Bill> restartVotingForSpecialCases(Long speakerId, Long billId)
    {
        LocalDateTime startTime = LocalDateTime.now(ZoneOffset.UTC);
        Bill billRecord = billRepository.findById(billId).get();
        billRecord.resetBillToOpenStatus(BillStatus.REOPEN, startTime);
        billRecord = billRepository.save(billRecord);

        return new ResponseEntity(billRecord,HttpStatus.OK);
    }

}
