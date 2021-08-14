package com.snszyk.iiot.marketization.domain.position.entity;

import lombok.*;

import java.io.Serializable;

@Value
@Builder
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class PositionId implements Serializable {


    private String id;

}
