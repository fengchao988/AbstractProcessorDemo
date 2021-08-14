package com.snszyk.iiot.marketization.domain.position.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode(exclude = {"positionId"})
public class Position {

    /**
     * 岗位标识
     */
    private PositionId positionId = new PositionId("");
    /**
     * 岗位名称
     */
    private PositionName positionName;

}
