import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { createEntity, getEntity, reset, updateEntity } from './car.reducer';

export const CarUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const carEntity = useAppSelector(state => state.car.entity);
  const loading = useAppSelector(state => state.car.loading);
  const updating = useAppSelector(state => state.car.updating);
  const updateSuccess = useAppSelector(state => state.car.updateSuccess);

  const handleClose = () => {
    navigate(`/car${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    if (values.id !== undefined && typeof values.id !== 'number') {
      values.id = Number(values.id);
    }
    if (values.cyear !== undefined && typeof values.cyear !== 'number') {
      values.cyear = Number(values.cyear);
    }

    const entity = {
      ...carEntity,
      ...values,
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {}
      : {
          ...carEntity,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="projetoIcligoApp.car.home.createOrEditLabel" data-cy="CarCreateUpdateHeading">
            <Translate contentKey="projetoIcligoApp.car.home.createOrEditLabel">Create or edit a Car</Translate>
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew ? (
                <ValidatedField
                  name="id"
                  required
                  readOnly
                  id="car-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField label={translate('projetoIcligoApp.car.model')} id="car-model" name="model" data-cy="model" type="text" />
              <ValidatedField label={translate('projetoIcligoApp.car.brand')} id="car-brand" name="brand" data-cy="brand" type="text" />
              <ValidatedField label={translate('projetoIcligoApp.car.cyear')} id="car-cyear" name="cyear" data-cy="cyear" type="text" />
              <ValidatedField
                label={translate('projetoIcligoApp.car.available')}
                id="car-available"
                name="available"
                data-cy="available"
                check
                type="checkbox"
              />
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/car" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">
                  <Translate contentKey="entity.action.back">Back</Translate>
                </span>
              </Button>
              &nbsp;
              <Button color="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp;
                <Translate contentKey="entity.action.save">Save</Translate>
              </Button>
            </ValidatedForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default CarUpdate;
