package gwkim.batchperformance.cms.item.repository;

import gwkim.batchperformance.cms.item.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
