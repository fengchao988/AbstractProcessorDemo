//  This codes are generated automatically. Do not modify!
package com.snszyk.iiot.marketization.domain.position.repository;

import com.snszyk.iiot.marketization.domain.position.entity.Position;
import com.snszyk.iiot.marketization.domain.position.entity.PositionId;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.Override;

public class PositionRepositoryImpl implements PositionRepository {

  @Autowired
  PersistenceJpa persistenceJpa;

  @Override
  public Position fromId(PositionId positionId) {
    return new Position();
  }

  @Override
  public void remove(Position position) {
    ;
  }

  @Override
  public void save(Position position) {
    ;
  }
}
