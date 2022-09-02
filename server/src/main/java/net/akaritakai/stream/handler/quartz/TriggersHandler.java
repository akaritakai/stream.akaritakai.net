package net.akaritakai.stream.handler.quartz;

import io.vertx.core.http.HttpServerResponse;
import net.akaritakai.stream.CheckAuth;
import net.akaritakai.stream.handler.AbstractHandler;
import net.akaritakai.stream.models.quartz.KeyEntry;
import net.akaritakai.stream.models.quartz.TriggerEntry;
import net.akaritakai.stream.models.quartz.request.ListTriggersRequest;
import net.akaritakai.stream.models.quartz.response.ListTriggersResponse;
import org.apache.commons.lang3.Validate;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;

import java.util.ArrayList;
import java.util.List;

public class TriggersHandler extends AbstractHandler<ListTriggersRequest> {
    private final Scheduler _scheduler;
    public TriggersHandler(Scheduler scheduler, CheckAuth checkAuth) {
        super(ListTriggersRequest.class, checkAuth);
        _scheduler = scheduler;
    }

    @Override
    protected void validateRequest(ListTriggersRequest request) {
        Validate.notNull(request, "request cannot be null");
        Validate.notEmpty(request.getKey(), "key cannot be null/empty");
    }

    @Override
    protected void handleAuthorized(ListTriggersRequest listTriggersRequest, HttpServerResponse response) {
        String groupPrefix = listTriggersRequest.getGroupPrefix();
        try {
            List<TriggerEntry> triggers = new ArrayList<>();
            for (TriggerKey triggerKey : _scheduler.getTriggerKeys(groupPrefix == null
                    ? GroupMatcher.anyTriggerGroup()
                    : GroupMatcher.triggerGroupStartsWith(groupPrefix))) {
                Trigger trigger = _scheduler.getTrigger(triggerKey);
                triggers.add(TriggerEntry.builder()
                        .key(KeyEntry.builder()
                                .name(trigger.getKey().getName())
                                .group(trigger.getKey().getGroup())
                                .build())
                        .job(KeyEntry.builder()
                                .name(trigger.getJobKey().getName())
                                .group(trigger.getJobKey().getName())
                                .build())
                        .jobDataMap(QuartzUtils.mapOf(trigger.getJobDataMap()))
                        .description(trigger.getDescription())
                        .calendar(trigger.getCalendarName())
                        .priority(trigger.getPriority())
                        .mayFireAgain(trigger.mayFireAgain())
                        .startTime(QuartzUtils.instantOf(trigger.getStartTime()))
                        .endTime(QuartzUtils.instantOf(trigger.getEndTime()))
                        .nextFireTime(QuartzUtils.instantOf(trigger.getNextFireTime()))
                        .previousFireTime(QuartzUtils.instantOf(trigger.getPreviousFireTime()))
                        .finalFireTime(QuartzUtils.instantOf(trigger.getFinalFireTime()))
                        .misfireInstruction(trigger.getMisfireInstruction())
                        .build());
            }
            ListTriggersResponse jobsResponse = ListTriggersResponse.builder().triggers(triggers).build();
            handleSuccess(OBJECT_MAPPER.writeValueAsString(jobsResponse), "application/json", response);
        } catch (Exception e) {
            handleFailure("Error enumerating jobs", response, e);
        }
    }
}
