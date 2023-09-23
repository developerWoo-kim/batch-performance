package gwkim.batchperformance.batch;

import gwkim.batchperformance.cms.item.domain.Item;
import gwkim.batchperformance.cms.item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "spring.batch.job.names", havingValue = "sampleTaskletJob")
@RequiredArgsConstructor
public class SampleTaskletJob {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final ItemRepository itemRepository;

    @Bean
    public Job sampleJob() {
        return jobBuilderFactory.get("sampleTaskletJob")
                .listener(new JobLoggerListener())
                .start(startStep(null))
                .build();
    }
    @Bean
    @JobScope
    public Step startStep(@Value("#{jobParameters[requestDate]}") String requestDate) {
        List<Item> allItems = itemRepository.findAll();
        return stepBuilderFactory.get("startStep")
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("Dsadas");
                    for (Item item : allItems) {
//                        log.info("tasklet.startStep item : {}", item.getItemName());
                        item.setItemPrice(item.getItemPrice() + 50000);
                    }

                    itemRepository.saveAll(allItems);

                    return RepeatStatus.FINISHED;
                })
                .build();
    }
}
