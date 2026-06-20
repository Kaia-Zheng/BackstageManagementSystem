package tech.wetech.admin3.sys.event;

import tech.wetech.admin3.common.DomainEvent;

public record ClubMemberAdded(String clubName, String username, String operatorName, String ip) implements DomainEvent {}
