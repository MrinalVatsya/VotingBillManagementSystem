package assignment.altir.voting.repository;


import assignment.altir.voting.model.Speaker;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository class for Speaker Table..
 */
@Repository
public interface SpeakerRepository extends CrudRepository<Speaker,Long> {
}
