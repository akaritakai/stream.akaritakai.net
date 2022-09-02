package net.akaritakai.stream.handler.quartz;

import io.vertx.core.http.HttpServerResponse;
import net.akaritakai.stream.CheckAuth;
import net.akaritakai.stream.handler.AbstractHandler;
import net.akaritakai.stream.models.quartz.JobEntry;
import net.akaritakai.stream.models.quartz.KeyEntry;
import net.akaritakai.stream.models.quartz.request.ListJobsRequest;
import net.akaritakai.stream.models.quartz.response.ListJobsResponse;
import org.apache.commons.lang3.Validate;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.impl.matchers.GroupMatcher;

import java.util.*;

public class JobsHandler extends AbstractHandler<ListJobsRequest> {

    private final Scheduler _scheduler;

    public JobsHandler(Scheduler scheduler, CheckAuth checkAuth) {
        super(ListJobsRequest.class, checkAuth);
        _scheduler = scheduler;
    }

    @Override
    protected void validateRequest(ListJobsRequest request) {
        Validate.notNull(request, "request cannot be null");
        Validate.notEmpty(request.getKey(), "key cannot be null/empty");
    }

    @Override
    protected void handleAuthorized(ListJobsRequest listJobsRequest, HttpServerResponse response) {
        String groupPrefix = listJobsRequest.getGroupPrefix();
        try {
            List<JobEntry> jobs = new ArrayList<>();
            for (JobKey jobKey : _scheduler.getJobKeys(groupPrefix == null
                    ? GroupMatcher.anyJobGroup()
                    : GroupMatcher.jobGroupStartsWith(groupPrefix))) {
                JobDetail jobDetail = _scheduler.getJobDetail(jobKey);
                jobs.add(JobEntry.builder()
                        .key(KeyEntry.builder()
                                .name(jobDetail.getKey().getName())
                                .group(jobDetail.getKey().getGroup())
                                .build())
                        .clazz(jobDetail.getJobClass().getName())
                        .jobDataMap(QuartzUtils.mapOf(jobDetail.getJobDataMap()))
                        .description(jobDetail.getDescription())
                        .durable(jobDetail.isDurable())
                        .persist(jobDetail.isPersistJobDataAfterExecution())
                        .concurrent(!jobDetail.isConcurrentExectionDisallowed())
                        .recoverable(jobDetail.requestsRecovery())
                        .build());
            }
            ListJobsResponse jobsResponse = ListJobsResponse.builder().jobs(jobs).build();
            handleSuccess(OBJECT_MAPPER.writeValueAsString(jobsResponse), "application/json", response);
        } catch (Exception e) {
            handleFailure("Error enumerating jobs", response, e);
        }
    }

}
