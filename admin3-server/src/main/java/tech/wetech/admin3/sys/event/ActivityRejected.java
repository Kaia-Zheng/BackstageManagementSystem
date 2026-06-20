package tech.wetech.admin3.sys.event;

import tech.wetech.admin3.common.DomainEvent;

public record ActivityRejected(String activityTitle, String reason, String operatorName, String ip) implements DomainEvent {}
