package com.snszyk.iiot.marketization.domain.position.repository;

import com.snszyk.iiot.marketization.domain.position.entity.Position;
import com.snszyk.iiot.marketization.domain.position.entity.PositionId;
import io.tools.BaseResourceLibrary;

@BaseResourceLibrary(bean = Position.class)
public interface PositionRepository {

    Position fromId(PositionId positionId);

    void remove(Position position);

    void save(Position position);
}
