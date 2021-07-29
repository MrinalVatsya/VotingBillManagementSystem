package assignment.altir.voting.repository;

import assignment.altir.voting.model.Bill;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository class for Bill Table..
 */
@Repository
public interface BillRepository extends CrudRepository<Bill, Long> {
}
