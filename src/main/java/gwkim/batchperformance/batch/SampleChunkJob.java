package gwkim.batchperformance.batch;

import gwkim.batchperformance.cms.item.domain.Item;
import gwkim.batchperformance.cms.item.domain.QItem;
import gwkim.batchperformance.cms.item.repository.ItemRepository;
import gwkim.batchperformance.common.querydsl.reader.QuerydslPagingItemReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

import javax.persistence.EntityManagerFactory;

import static gwkim.batchperformance.cms.item.domain.QItem.item;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "spring.batch.job.names", havingValue = "sampleChunkJob")
@RequiredArgsConstructor
public class SampleChunkJob {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final TaskExecutor taskExecutor;
    private final EntityManagerFactory entityManagerFactory;


    private int chunkSize;
    private int pageSize;
    @Value("${chunkSize:1000}")
    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }
    @Value("${pageSize:1000}")
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }


    @Bean
    public Job chunkJob() throws Exception {
        return jobBuilderFactory.get("sampleChunkJob")
                .start(chunkStep(null))
                .build();
    }
    @Bean
    @JobScope
    public Step chunkStep(@Value("#{jobParameters[requestDate]}") String requestDate) {
        return stepBuilderFactory.get("chunkStep")
                .<Item, Item> chunk(chunkSize) // Chuck 사이즈는 한번에 처리될 트랜잭선 단위
                .reader(sampleChunkReader(null))
                .processor(sampleChunkProcessor(null))
                .writer(sampleChunkWriter(null))
//                .taskExecutor(taskExecutor)
                .build();
    }

    @Bean
    @StepScope
    public QuerydslPagingItemReader<Item> sampleChunkReader(@Value("#{jobParameters[requestDate]}") String requestDate) {
        return new QuerydslPagingItemReader<>(entityManagerFactory, pageSize,
                jpaQueryFactory -> jpaQueryFactory
                        .selectFrom(item)
        );
    };

    public ItemProcessor<Item, Item> sampleChunkProcessor(@Value("#{jobParameters[requestDate]}") String requestDate) {
        return item -> {
            item.setItemPrice(item.getItemPrice() + 50000);
            return item;
        };
    }

    @Bean
    @StepScope
    public ItemWriter<Item> sampleChunkWriter(@Value("#{jobParameters[requestDate]}") String requestDate) {
        return new JpaItemWriterBuilder<Item>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    };
}
