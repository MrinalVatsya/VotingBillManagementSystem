package assignment.altir.voting.service;

import assignment.altir.voting.dto.VotingResult;
import assignment.altir.voting.model.Bill;
import assignment.altir.voting.model.BillStatus;
import assignment.altir.voting.model.PresentFlag;
import assignment.altir.voting.model.Speaker;
import assignment.altir.voting.model.TieVote;
import assignment.altir.voting.model.VoteStatus;
import assignment.altir.voting.repository.BillRepository;
import assignment.altir.voting.repository.PersonRepository;
import assignment.altir.voting.repository.SpeakerRepository;
import assignment.altir.voting.service.VotingService;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Test class for Voting Service..
 */
@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class VotingServiceTest {
        @Mock
        private BillRepository billRepository;

        @Mock
        private PersonRepository personRepository;

    @Mock
    private SpeakerRepository speakerRepository;

    @InjectMocks
    private VotingService votingService;

    private static Long speakerId = 1L;
    private static Long billId = 1L;
    private static LocalDateTime startTime = LocalDateTime.now();
    private static LocalDateTime endTime = LocalDateTime.now().plusMinutes(15L);

    private static Optional<Bill> optionalBillRecord = null;
    private static Optional<Speaker> optionalSpeakerRecord = null;

    /**
     * Checks whether Bill is reopened in special cases by speaker..
     */
    @Test
    public void testRestartVotingForSpecialCases()
    {
        Bill billRecord = Bill.builder().billId(billId).status(BillStatus.CLOSED).endTime(endTime).startTime(startTime).build();
        optionalBillRecord = Optional.of(billRecord);
        Mockito.when(billRepository.findById(billId)).thenReturn(optionalBillRecord);
        Mockito.when(billRepository.save(Mockito.isA(Bill.class))).thenReturn(billRecord);
        ResponseEntity<Bill> response = votingService.restartVotingForSpecialCases(speakerId,billId);
        Mockito.verify(billRepository, Mockito.times(1)).save(Mockito.any());
        assertEquals(response.getBody().getStatus(), BillStatus.OPEN);
    }

    /**
     * Checks whether Bill is passed in case of VoteFor > VotedAgainst..
     */
    @Test
    public void testCloseVotingForBillPass()
    {
        Bill billRecord = Bill.builder().billId(billId).status(BillStatus.OPEN).endTime(endTime).startTime(startTime).build();
        optionalBillRecord = Optional.of(billRecord);
        Mockito.when(billRepository.findById(billId)).thenReturn(optionalBillRecord);
        Mockito.when(personRepository.findCountVotedFor(Mockito.isA(Long.class), Mockito.isA(LocalDateTime.class), Mockito.isA(LocalDateTime.class), Mockito.isA(VoteStatus.class))).thenReturn(2);
        Mockito.when(personRepository.findTotalVotes(Mockito.isA(Long.class), Mockito.isA(LocalDateTime.class), Mockito.isA(LocalDateTime.class))).thenReturn(3);
        ResponseEntity<VotingResult> response = votingService.closeVoting(speakerId,billId);
        Mockito.verify(billRepository, Mockito.times(1)).save(Mockito.isA(Bill.class));
        Assertions.assertEquals(response.getBody().getBillStatus(), BillStatus.PASSED);
    }

    /**
     * Checks whether Bill is failed in case of VoteFor < VotedAgainst..
     */
    @Test
    public void testCloseVotingForBillFail()
    {
        Bill billRecord = Bill.builder().billId(billId).status(BillStatus.OPEN).endTime(endTime).startTime(startTime).build();
        optionalBillRecord = Optional.of(billRecord);
        Mockito.when(billRepository.findById(billId)).thenReturn(optionalBillRecord);
        Mockito.when(personRepository.findCountVotedFor(Mockito.isA(Long.class), Mockito.isA(LocalDateTime.class), Mockito.isA(LocalDateTime.class), Mockito.isA(VoteStatus.class))).thenReturn(1);
        Mockito.when(personRepository.findTotalVotes(Mockito.isA(Long.class), Mockito.isA(LocalDateTime.class), Mockito.isA(LocalDateTime.class))).thenReturn(3);
        ResponseEntity<VotingResult> response = votingService.closeVoting(speakerId,billId);
        Mockito.verify(billRepository, Mockito.times(1)).save(Mockito.isA(Bill.class));
        Assertions.assertEquals(response.getBody().getBillStatus(), BillStatus.FAILED);
    }

    /**
     * Checks whether Bill is passed in case of tie if speaker votes in favour of motion..
     */
    @Test
    public void testCloseVotingForBillPassInCaseOfTie()
    {
        Bill billRecord = Bill.builder().billId(billId).status(BillStatus.OPEN).endTime(endTime).startTime(startTime).build();
        optionalBillRecord = Optional.of(billRecord);
        Speaker speaker = Speaker.builder().speakerId(speakerId).presentFlag(PresentFlag.Y).tieVote(TieVote.Y).bill(billRecord).build();
        optionalSpeakerRecord = Optional.of(speaker);
        Mockito.when(billRepository.findById(billId)).thenReturn(optionalBillRecord);
        Mockito.when(personRepository.findCountVotedFor(Mockito.isA(Long.class), Mockito.isA(LocalDateTime.class), Mockito.isA(LocalDateTime.class), Mockito.isA(VoteStatus.class))).thenReturn(1);
        Mockito.when(personRepository.findTotalVotes(Mockito.isA(Long.class), Mockito.isA(LocalDateTime.class), Mockito.isA(LocalDateTime.class))).thenReturn(2);
        Mockito.when(speakerRepository.findById(speakerId)).thenReturn(optionalSpeakerRecord);
        ResponseEntity<VotingResult> response = votingService.closeVoting(speakerId,billId);
        Mockito.verify(billRepository, Mockito.times(1)).save(Mockito.isA(Bill.class));
        Assertions.assertEquals(response.getBody().getBillStatus(), BillStatus.PASSED);
    }

    /**
     * Checks whether Bill failed in case of tie if speaker votes in against of motion..
     */
    @Test
    public void testCloseVotingForBillFailInCaseOfTie()
    {
        Bill billRecord = Bill.builder().billId(billId).status(BillStatus.OPEN).endTime(endTime).startTime(startTime).build();
        optionalBillRecord = Optional.of(billRecord);
        Speaker speaker = Speaker.builder().speakerId(speakerId).presentFlag(PresentFlag.Y).tieVote(TieVote.N).bill(billRecord).build();
        optionalSpeakerRecord = Optional.of(speaker);
        Mockito.when(billRepository.findById(billId)).thenReturn(optionalBillRecord);
        Mockito.when(personRepository.findCountVotedFor(Mockito.isA(Long.class), Mockito.isA(LocalDateTime.class), Mockito.isA(LocalDateTime.class), Mockito.isA(VoteStatus.class))).thenReturn(1);
        Mockito.when(personRepository.findTotalVotes(Mockito.isA(Long.class), Mockito.isA(LocalDateTime.class), Mockito.isA(LocalDateTime.class))).thenReturn(2);
        Mockito.when(speakerRepository.findById(speakerId)).thenReturn(optionalSpeakerRecord);
        ResponseEntity<VotingResult> response = votingService.closeVoting(speakerId,billId);
        Mockito.verify(billRepository, Mockito.times(1)).save(Mockito.isA(Bill.class));
        Assertions.assertEquals(response.getBody().getBillStatus(), BillStatus.FAILED);
    }

    /**
     * Check whether the bill is forced to failed state in case of speaker is absent from voting..
     */
    @Test
    public void testCloseVotingForSpeakerAbsent()
    {
        Bill billRecord = Bill.builder().billId(billId).status(BillStatus.OPEN).endTime(endTime).startTime(startTime).build();
        optionalBillRecord = Optional.of(billRecord);
        Speaker speaker = Speaker.builder().speakerId(speakerId).presentFlag(PresentFlag.N).tieVote(TieVote.N).bill(billRecord).build();
        optionalSpeakerRecord = Optional.of(speaker);
        Mockito.when(billRepository.findById(billId)).thenReturn(optionalBillRecord);
        Mockito.when(personRepository.findCountVotedFor(Mockito.isA(Long.class), Mockito.isA(LocalDateTime.class), Mockito.isA(LocalDateTime.class), Mockito.isA(VoteStatus.class))).thenReturn(1);
        Mockito.when(personRepository.findTotalVotes(Mockito.isA(Long.class), Mockito.isA(LocalDateTime.class), Mockito.isA(LocalDateTime.class))).thenReturn(2);
        Mockito.when(speakerRepository.findById(speakerId)).thenReturn(optionalSpeakerRecord);
        ResponseEntity<VotingResult> response = votingService.closeVoting(speakerId,billId);
        Mockito.verify(billRepository, Mockito.times(1)).save(Mockito.isA(Bill.class));
        Assertions.assertEquals(response.getBody().getBillStatus(), BillStatus.FAILED);
    }


}
