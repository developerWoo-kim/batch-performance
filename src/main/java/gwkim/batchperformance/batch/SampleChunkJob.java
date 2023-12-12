package gwkim.batchperformance.batch;

import gwkim.batchperformance.cms.item.domain.Item;
import gwkim.batchperformance.cms.item.domain.QItem;
import gwkim.batchperformance.cms.item.repository.ItemRepository;
import gwkim.batchperformance.common.querydsl.reader.QuerydslNoOffsetPagingItemReader;
import gwkim.batchperformance.common.querydsl.reader.QuerydslPagingItemReader;
import gwkim.batchperformance.common.querydsl.reader.expression.Expression;
import gwkim.batchperformance.common.querydsl.reader.options.QuerydslNoOffsetNumberOptions;
import gwkim.batchperformance.common.querydsl.reader.options.QuerydslNoOffsetOptions;
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
    private final JobBuilderFactory jbf;
    private final StepBuilderFactory sbf;
    private final EntityManagerFactory emf;


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
        return jbf.get("sampleChunkJob")
                .start(offsetStep(null))
                .build();
    }
    @Bean
    @JobScope
    public Step noOffsetStep(@Value("#{jobParameters[requestDate]}") String requestDate) {
        return sbf.get("noOffsetStep")
                .<Item, Item> chunk(chunkSize)
                .reader(noOffsetPagingItemReader(null))
                .processor(sampleChunkProcessor(null))
                .writer(sampleChunkWriter(null))
                .build();
    }

    @Bean
    @StepScope
    public QuerydslNoOffsetPagingItemReader<Item> noOffsetPagingItemReader(@Value("#{jobParameters[requestDate]}") String requestDate) {
        QuerydslNoOffsetNumberOptions<Item, Long> options =
                new QuerydslNoOffsetNumberOptions<>(item.id, Expression.ASC);
        return new QuerydslNoOffsetPagingItemReader<>(emf, chunkSize, options, queryFactory -> queryFactory
                .selectFrom(item)
        );
    };

    @Bean
    @JobScope
    public Step offsetStep(@Value("#{jobParameters[requestDate]}") String requestDate) {
        return sbf.get("offsetStep")
                .<Item, Item> chunk(chunkSize)
                .reader(offsetPagingItemReader(null))
                .processor(sampleChunkProcessor(null))
                .writer(sampleChunkWriter(null))
                .build();
    }

    @Bean
    @JobScope
    public Step chunkStep3(@Value("#{jobParameters[requestDate]}") String requestDate) {
        return sbf.get("chunkStep")
                .<Item, Item> chunk(chunkSize)
                .reader(offsetPagingItemReader(null))
                .processor(sampleChunkProcessor(null))
                .writer(sampleChunkWriter(null))
                .build();
    }

    @Bean
    @StepScope
    public QuerydslPagingItemReader<Item> offsetPagingItemReader(@Value("#{jobParameters[requestDate]}") String requestDate) {
        return new QuerydslPagingItemReader<>(emf, chunkSize, queryFactory -> queryFactory
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
                .entityManagerFactory(emf)
                .build();
    };
}
