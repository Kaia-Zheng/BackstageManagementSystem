package tech.wetech.admin3.sys.event;

import tech.wetech.admin3.common.DomainEvent;

public record ActivityRegistrationCancelled(String activityTitle, String username, String ip) implements DomainEvent {}
