import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './car.reducer';

export const CarDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const carEntity = useAppSelector(state => state.car.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="carDetailsHeading">
          <Translate contentKey="projetoIcligoApp.car.detail.title">Car</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{carEntity.id}</dd>
          <dt>
            <span id="model">
              <Translate contentKey="projetoIcligoApp.car.model">Model</Translate>
            </span>
          </dt>
          <dd>{carEntity.model}</dd>
          <dt>
            <span id="brand">
              <Translate contentKey="projetoIcligoApp.car.brand">Brand</Translate>
            </span>
          </dt>
          <dd>{carEntity.brand}</dd>
          <dt>
            <span id="cyear">
              <Translate contentKey="projetoIcligoApp.car.cyear">Cyear</Translate>
            </span>
          </dt>
          <dd>{carEntity.cyear}</dd>
          <dt>
            <span id="available">
              <Translate contentKey="projetoIcligoApp.car.available">Available</Translate>
            </span>
          </dt>
          <dd>{carEntity.available ? 'true' : 'false'}</dd>
        </dl>
        <Button tag={Link} to="/car" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/car/${carEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default CarDetail;
