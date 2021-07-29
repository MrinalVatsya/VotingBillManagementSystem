package assignment.altir.voting.controller;


import assignment.altir.voting.dto.VotingResult;
import assignment.altir.voting.model.Bill;
import assignment.altir.voting.model.BillStatus;
import assignment.altir.voting.model.Person;
import assignment.altir.voting.repository.BillRepository;
import assignment.altir.voting.repository.PersonRepository;
import assignment.altir.voting.service.VotingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.extern.slf4j.Slf4j;
import assignment.altir.voting.model.Speaker;
import assignment.altir.voting.repository.SpeakerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

/**
 * REST CONTROLLER CLASS for Voting Process IN Parliament..
 */
@RestController
@Slf4j
@RequestMapping("/altir")
public class VotingController {

    private static Long allowedVotingTime = 15L;

    private PersonRepository personRepository;
    private SpeakerRepository speakerRepository;
    private BillRepository billRepository;
    private VotingService votingService;

    /**
     * Constructor..
     * @param personRepository
     * @param speakerRepository
     * @param billRepository
     * @param votingService
     */
    public VotingController(PersonRepository personRepository, SpeakerRepository speakerRepository, BillRepository billRepository, VotingService votingService) {
        this.personRepository = personRepository;
        this.speakerRepository = speakerRepository;
        this.billRepository = billRepository;
        this.votingService = votingService;
    }

    /**
     * This API is used for Adding a new Bill Entry in Bill Table..
     * @param bill Instance of Bill To be created..
     * @return ResponseEntity
     */
    @PostMapping("/addBillEntry")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private ResponseEntity addBillEntry(Bill bill)
    {
        //StartTime and EndTime will not be set here, it will be set by Speaker..
        return new ResponseEntity(billRepository.save(bill), HttpStatus.CREATED);
    }

    /**
     * This API is authorized to be used by SPEAKER. This will add entry for Speaker and Initialize the Bill for Voting..
     * @param roles Roles associated with Speaker..
     * @param speaker Instance of Speaker..
     * @param billId BillID for which Speaker wants to be associated with..
     * @return ResponseEntity response
     */
    @PostMapping("/startVoting/{id}")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private ResponseEntity startBillingProcess(@RequestHeader("ROLES_KEY") String roles, @RequestBody Speaker speaker, @PathVariable("id")Long billId)
    {
            if(roles.equals("SPEAKER")) {
                //make entry in bill table for starting voting..
                LocalDateTime startTime = LocalDateTime.now(ZoneOffset.UTC);
                Optional<Bill> billEntryOpt = billRepository.findById(billId);
                if (billEntryOpt.isPresent()) {
                    Bill billEntry = billEntryOpt.get();
                    billEntry.setStartTime(startTime);
                    billEntry.setStatus(BillStatus.OPEN);
                    billRepository.save(billEntry);

                    Speaker newSpeaker = Speaker.builder().bill(billEntry).presentFlag(speaker.getPresentFlag()).tieVote(speaker.getTieVote()).build();
                    newSpeaker = speakerRepository.save(newSpeaker);

                    return new ResponseEntity(newSpeaker, HttpStatus.CREATED);
                } else {
                    log.error("No Bill Found in Table for ID {}", billId);
                    return new ResponseEntity("NO BILL FOUND", HttpStatus.NOT_FOUND);
                }
            }
            else
            {
                return new ResponseEntity<String>("USER IS NOT AUTHORIZED TO HIT THIS API", HttpStatus.UNAUTHORIZED);
            }
    }

    /**
     * This API is used for creating Person and voting For/Against a Bill Id by Person
     * @param person Member of Parliament
     * @return ResponseEntity response
     */
    @PostMapping("/voteForBill")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private ResponseEntity addPersonVote(@RequestBody Person person)
    {
        //check whether time of voting has exceeded or not..
        Long billId = person.getBill().getBillId();
        Optional<Bill> billEntryOpt = billRepository.findById(billId);
        Person newPerson = null;
        if(billEntryOpt.isPresent())
        {
            Bill entity = billEntryOpt.get();
            if(entity.getStatus()!=null && entity.getStatus().equals(BillStatus.OPEN)) {
                newPerson = Person.builder().bill(entity).voteTime(LocalDateTime.now(ZoneOffset.UTC))
                        .voteStatus(person.getVoteStatus()).build();

                newPerson = personRepository.save(newPerson);
                log.info("Vote Added For Person with ID - {}", newPerson.getPersonId());
                return new ResponseEntity(newPerson, HttpStatus.CREATED);
            }
            else if(entity.getStatus()!=null && entity.getStatus().equals(BillStatus.REOPEN)) {
                //update existing record only..
                return new ResponseEntity(newPerson, HttpStatus.CREATED);
            }
            else
            {
                log.info("========VOTING CANNOT BE INITIATED FOR USER FOR THIS BILL========");
                return  new ResponseEntity("VOTING CANNOT BE INITIATED FOR USER FOR THIS BILL", HttpStatus.OK);
            }
        }
        else {
            log.error("NO BILL FOUND For Person");
            return new ResponseEntity(newPerson, HttpStatus.NOT_FOUND);
        }
    }


    /**
     * API to get current status of Bill..
     * @param id BillId
     * @return ResponseEntity
     */
    @GetMapping("/currentState/{id}")
    private ResponseEntity<Bill> getBillStatus(@PathVariable("id") Long id)
    {
        Bill billRecord = billRepository.findById(id).get();
        return new ResponseEntity<>(billRecord, HttpStatus.OK);
    }

    /**
     * API to close Voting by Speaker..
     * @param roles
     * @param speakerId
     * @param billId
     * @return
     */
    @GetMapping("/closeVoting")
    private ResponseEntity<VotingResult> closeVotingAndGetResult(@RequestHeader("ROLES_KEY") String roles, @RequestParam Long speakerId, @RequestParam Long billId)
    {
        ResponseEntity<VotingResult> response = null;
        if(roles.equals("SPEAKER")) {
            response = votingService.closeVoting(speakerId, billId);
        }
        else
        {
            response = new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return response;
    }

    /**
     * API To Reopen Bill by Speaker..
     * @param roles
     * @param speakerId
     * @param billId
     * @return
     */
    @GetMapping("/reopenBill")
    private ResponseEntity<Bill> reopenBillForSpecialCases(@RequestHeader("ROLES_KEY") String roles, @RequestParam Long speakerId, @RequestParam Long billId)
    {
        if(roles.equals("SPEAKER")) {
            return votingService.restartVotingForSpecialCases(speakerId, billId);
        }
        else
        {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
}
