package com.snszyk.iiot.marketization.domain.position.entity;

import lombok.*;

@Value
@Builder
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class PositionName {

    public String name;
}
