package tech.wetech.admin3.sys.event;

import tech.wetech.admin3.common.DomainEvent;

public record ActivityApproved(String activityTitle, String operatorName, String ip) implements DomainEvent {}
