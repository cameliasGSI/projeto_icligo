import MenuItem from 'app/shared/layout/menus/menu-item';
import { Translate } from 'react-jhipster';
import React from 'react';

const EntitiesMenu = () => {
  return (
    <>
      {/* prettier-ignore */}
      <MenuItem icon="asterisk" to="/car">
        <Translate contentKey="global.menu.entities.car" />
      </MenuItem>
      {/* jhipster-needle-add-entity-to-menu - JHipster will add entities to the menu here */}
    </>
  );
};

export default EntitiesMenu;
