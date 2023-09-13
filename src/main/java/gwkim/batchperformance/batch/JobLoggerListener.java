package gwkim.batchperformance.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JobLoggerListener extends JobExecutionListenerSupport {

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("JOB 수행완료 {}", jobExecution);

        if (jobExecution.getStatus().equals(BatchStatus.COMPLETED)) {
            log.info("성공 슬랙 API");
        } else {
            log.info("실패 슬랙 API");
        }
    }
}
